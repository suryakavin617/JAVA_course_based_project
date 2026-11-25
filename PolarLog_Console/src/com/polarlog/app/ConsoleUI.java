package com.polarlog.app;

import com.polarlog.dao.DataStore;
import com.polarlog.dao.FileStorage;
import com.polarlog.exception.InsufficientStockException;
import com.polarlog.exception.InvalidInputException;
import com.polarlog.exception.InvalidStatusChangeException;
import com.polarlog.exception.ItemNotFoundException;
import com.polarlog.model.Alert;
import com.polarlog.model.Asset;
import com.polarlog.model.AssetStatus;
import com.polarlog.model.InventoryItem;
import com.polarlog.model.MovementLog;
import com.polarlog.model.Person;
import com.polarlog.model.SyncRecord;
import com.polarlog.service.AlertService;
import com.polarlog.service.AssetService;
import com.polarlog.service.DashboardService;
import com.polarlog.service.ExpeditionService;
import com.polarlog.service.InventoryService;
import com.polarlog.service.PersonnelService;
import com.polarlog.sim.DeviceSimulator;
import com.polarlog.sync.SyncManager;
import com.polarlog.sync.SyncQueue;
import com.polarlog.util.CollectionUtil;
import com.polarlog.util.Validator;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/** The console menus. Every menu action runs through execute(), which handles all exceptions. */
public class ConsoleUI {

    /** Functional interface (nested): one menu action, written as a lambda or method reference. */
    @FunctionalInterface
    private interface MenuAction {
        void run() throws Exception;
    }

    /** Nested class: thrown when the keyboard input ends (for example Ctrl+D). */
    private static class InputClosedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final Scanner scanner = new Scanner(System.in);
    private final DataStore store;
    private final SyncQueue syncQueue;
    private final SyncManager syncThread;
    private final AssetService assetService;
    private final InventoryService inventoryService;
    private final PersonnelService personnelService;
    private final ExpeditionService expeditionService;
    private final AlertService alertService;
    private final DashboardService dashboardService;
    private final String dataFile;
    private final String commandCentreFile;
    private final String reportFile;

    private DeviceSimulator simulator;
    private Thread simulatorThread;

    public ConsoleUI(DataStore store, SyncQueue syncQueue, SyncManager syncThread,
                     AssetService assetService, InventoryService inventoryService,
                     PersonnelService personnelService, ExpeditionService expeditionService,
                     AlertService alertService, DashboardService dashboardService,
                     String dataFile, String commandCentreFile, String reportFile) {
        this.store = store;
        this.syncQueue = syncQueue;
        this.syncThread = syncThread;
        this.assetService = assetService;
        this.inventoryService = inventoryService;
        this.personnelService = personnelService;
        this.expeditionService = expeditionService;
        this.alertService = alertService;
        this.dashboardService = dashboardService;
        this.dataFile = dataFile;
        this.commandCentreFile = commandCentreFile;
        this.reportFile = reportFile;
    }

    // ------------------------------------------------------------------ input helpers

    private String ask(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    private int askInt(String prompt) {
        return Validator.parseInt(ask(prompt));
    }

    public boolean askYesNo(String prompt) {
        try {
            String answer = ask(prompt);
            return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
        } catch (InputClosedException e) {
            return false;
        }
    }

    /** Runs one menu action and turns every kind of exception into a friendly message. */
    private void execute(MenuAction action) {
        try {
            action.run();
        } catch (InputClosedException e) {
            throw e;
        } catch (ItemNotFoundException e) {
            System.out.println("  [Not found] " + e.getMessage());
        } catch (InvalidStatusChangeException | InsufficientStockException e) {
            System.out.println("  [Rule] " + e.getMessage());
        } catch (InvalidInputException e) {
            System.out.println("  [Input] " + e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("  [Not allowed] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [Error] " + e);
        } finally {
            System.out.println();
        }
    }

    // ------------------------------------------------------------------ main menu

    public void run() {
        try {
            boolean running = true;
            while (running) {
                printMainMenu();
                String choice = ask("Choose an option: ");
                switch (choice) {
                    case "1": assetMenu(); break;
                    case "2": inventoryMenu(); break;
                    case "3": personnelMenu(); break;
                    case "4": expeditionMenu(); break;
                    case "5": alertMenu(); break;
                    case "6": syncMenu(); break;
                    case "7": execute(() -> System.out.println(dashboardService.getSummary())); break;
                    case "8": reportMenu(); break;
                    case "0": running = false; break;
                    default: System.out.println("  Invalid option, please try again.\n");
                }
            }
        } catch (InputClosedException e) {
            System.out.println();
            System.out.println("Input ended, closing the program...");
        }
    }

    private void printMainMenu() {
        System.out.println("==============================================");
        System.out.println("  PolarLog - Polar Expedition Logistics");
        System.out.println("  Link: " + (syncQueue.isOnline() ? "ONLINE" : "OFFLINE")
            + "  |  Pending sync: " + syncQueue.size());
        System.out.println("==============================================");
        System.out.println("  1. Asset management");
        System.out.println("  2. Inventory");
        System.out.println("  3. Personnel and movement");
        System.out.println("  4. Expeditions");
        System.out.println("  5. Alerts and SOS");
        System.out.println("  6. Sync, link and device simulator");
        System.out.println("  7. Command dashboard");
        System.out.println("  8. Reports");
        System.out.println("  0. Save and exit");
    }

    // ------------------------------------------------------------------ assets

    private void assetMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- ASSET MANAGEMENT ---");
            System.out.println("  1. Register new asset");
            System.out.println("  2. Change asset status");
            System.out.println("  3. Move asset to a new location");
            System.out.println("  4. View asset location history");
            System.out.println("  5. List all assets (sorted by name)");
            System.out.println("  6. Search asset by ID");
            System.out.println("  7. Search asset by name");
            System.out.println("  8. Filter assets by status");
            System.out.println("  0. Back");
            switch (ask("Choose: ")) {
                case "1": execute(this::registerAsset); break;
                case "2": execute(this::changeAssetStatus); break;
                case "3": execute(this::moveAsset); break;
                case "4": execute(this::viewAssetHistory); break;
                case "5": execute(this::listAssets); break;
                case "6": execute(this::searchAssetById); break;
                case "7": execute(this::searchAssetByName); break;
                case "8": execute(this::filterAssets); break;
                case "0": back = true; break;
                default: System.out.println("  Invalid option.\n");
            }
        }
    }

    private void registerAsset() {
        String name = ask("Asset name: ");
        String category = ask("Category (for example Equipment, Medical): ");
        String location = ask("Current location: ");
        Asset asset = assetService.registerAsset(name, category, location);
        System.out.println("  Registered: " + asset);
    }

    private AssetStatus chooseStatus() {
        AssetStatus[] all = AssetStatus.values();
        for (int i = 0; i < all.length; i++) {
            System.out.println("    " + (i + 1) + ". " + all[i]);
        }
        int number = askInt("Status number: ");
        if (number < 1 || number > all.length) {
            throw new InvalidInputException("Choose a number between 1 and " + all.length);
        }
        return all[number - 1];
    }

    private void changeAssetStatus() throws ItemNotFoundException, InvalidStatusChangeException {
        int id = askInt("Asset ID: ");
        AssetStatus next = chooseStatus();
        assetService.updateStatus(id, next);
        System.out.println("  Asset #" + id + " is now " + next);
    }

    private void moveAsset() throws ItemNotFoundException {
        int id = askInt("Asset ID: ");
        String location = ask("New location: ");
        assetService.moveAsset(id, location);
        System.out.println("  Asset #" + id + " moved to " + location.trim());
    }

    private void viewAssetHistory() throws ItemNotFoundException {
        Asset asset = assetService.getAsset(askInt("Asset ID: "));
        System.out.println("  " + asset);
        for (Asset.LocationRecord record : asset.getHistory()) {
            System.out.println("     " + record);
        }
    }

    private void listAssets() {
        List<Asset> assets = assetService.listSorted();
        if (assets.isEmpty()) {
            System.out.println("  No assets yet.");
            return;
        }
        CollectionUtil.printNumbered(assets);
    }

    private void searchAssetById() throws ItemNotFoundException {
        Asset asset = assetService.searchAsset(askInt("Asset ID: "));
        System.out.println(asset.report());
    }

    private void searchAssetByName() throws ItemNotFoundException {
        Asset asset = assetService.searchAsset(ask("Asset name (exact name): "));
        System.out.println(asset.report());
    }

    private void filterAssets() {
        final AssetStatus wanted = chooseStatus();
        List<Asset> result = assetService.filter(a -> a.getStatus() == wanted);   // lambda
        System.out.println("  " + result.size() + " asset(s) with status " + wanted);
        CollectionUtil.printNumbered(result);
    }

    // ------------------------------------------------------------------ inventory

    private void inventoryMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- INVENTORY ---");
            System.out.println("  1. Add new item");
            System.out.println("  2. Quick add (name,quantity,reorderLevel)");
            System.out.println("  3. Add stock");
            System.out.println("  4. Remove stock");
            System.out.println("  5. View inventory (sorted by name)");
            System.out.println("  6. View low stock items");
            System.out.println("  0. Back");
            switch (ask("Choose: ")) {
                case "1": execute(this::addItem); break;
                case "2": execute(this::quickAddItem); break;
                case "3": execute(this::addStock); break;
                case "4": execute(this::removeStock); break;
                case "5": execute(this::viewInventory); break;
                case "6": execute(this::viewLowStock); break;
                case "0": back = true; break;
                default: System.out.println("  Invalid option.\n");
            }
        }
    }

    private void addItem() {
        String name = ask("Item name: ");
        int quantity = askInt("Quantity: ");
        int reorder = askInt("Reorder level: ");
        System.out.println("  Added: " + inventoryService.addItem(name, quantity, reorder));
    }

    private void quickAddItem() {
        String line = ask("Enter name,quantity,reorderLevel : ");
        System.out.println("  Added: " + inventoryService.quickAdd(line));
    }

    private void addStock() throws ItemNotFoundException {
        int id = askInt("Item ID: ");
        int amount = askInt("Amount to add: ");
        inventoryService.addStock(id, amount);
        System.out.println("  Now: " + inventoryService.getItem(id));
    }

    private void removeStock() throws ItemNotFoundException, InsufficientStockException {
        int id = askInt("Item ID: ");
        int amount = askInt("Amount to remove: ");
        inventoryService.removeStock(id, amount);
        System.out.println("  Now: " + inventoryService.getItem(id));
    }

    private void viewInventory() {
        TreeMap<String, InventoryItem> map = inventoryService.getInventoryByName();
        if (map.isEmpty()) {
            System.out.println("  No inventory items yet.");
            return;
        }
        Iterator<Map.Entry<String, InventoryItem>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            System.out.println("  " + iterator.next().getValue());
        }
    }

    private void viewLowStock() {
        List<InventoryItem> low = inventoryService.getLowStockItems();
        if (low.isEmpty()) {
            System.out.println("  No item is low on stock.");
            return;
        }
        CollectionUtil.printNumbered(low);
    }

    // ------------------------------------------------------------------ personnel

    private void personnelMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- PERSONNEL AND MOVEMENT ---");
            System.out.println("  1. Add station member");
            System.out.println("  2. Add command centre operator");
            System.out.println("  3. List people");
            System.out.println("  4. Check in");
            System.out.println("  5. Check out");
            System.out.println("  6. View movement log");
            System.out.println("  7. Who is checked in now");
            System.out.println("  0. Back");
            switch (ask("Choose: ")) {
                case "1": execute(this::addMember); break;
                case "2": execute(this::addOperator); break;
                case "3": execute(this::listPeople); break;
                case "4": execute(this::checkIn); break;
                case "5": execute(this::checkOut); break;
                case "6": execute(this::viewMovementLog); break;
                case "7": execute(this::whoIsCheckedIn); break;
                case "0": back = true; break;
                default: System.out.println("  Invalid option.\n");
            }
        }
    }

    private void addMember() {
        String name = ask("Member name: ");
        String station = ask("Station (for example Maitri): ");
        System.out.println("  Added: " + personnelService.addMember(name, station));
    }

    private void addOperator() {
        String name = ask("Operator name: ");
        String station = ask("Station / office: ");
        System.out.println("  Added: " + personnelService.addOperator(name, station));
    }

    private void listPeople() {
        List<Person> people = personnelService.getAllPeople();
        if (people.isEmpty()) {
            System.out.println("  No people added yet.");
            return;
        }
        for (Person person : people) {
            System.out.println("  " + person);        // polymorphism: each subclass prints its own role
        }
    }

    private void checkIn() throws ItemNotFoundException {
        int id = askInt("Member ID: ");
        String location = ask("Location: ");
        personnelService.checkIn(id, location);
        System.out.println("  Checked in.");
    }

    private void checkOut() throws ItemNotFoundException {
        personnelService.checkOut(askInt("Member ID: "));
        System.out.println("  Checked out.");
    }

    private void viewMovementLog() {
        List<MovementLog> entries = personnelService.getMovementLog();
        if (entries.isEmpty()) {
            System.out.println("  Movement log is empty.");
            return;
        }
        CollectionUtil.printNumbered(entries);
    }

    private void whoIsCheckedIn() {
        Set<String> names = personnelService.getCheckedInNames();
        System.out.println("  Checked in (" + names.size() + "): " + names);
    }

    // ------------------------------------------------------------------ expeditions

    private void expeditionMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- EXPEDITIONS ---");
            System.out.println("  1. Create expedition");
            System.out.println("  2. Add crew member to expedition");
            System.out.println("  3. Add cargo (asset) to expedition");
            System.out.println("  4. View expeditions");
            System.out.println("  0. Back");
            switch (ask("Choose: ")) {
                case "1": execute(this::createExpedition); break;
                case "2": execute(this::addCrew); break;
                case "3": execute(this::addCargo); break;
                case "4": execute(this::viewExpeditions); break;
                case "0": back = true; break;
                default: System.out.println("  Invalid option.\n");
            }
        }
    }

    private void createExpedition() {
        String name = ask("Expedition name: ");
        String vessel = ask("Vessel name: ");
        String date = ask("Departure date (dd-MM-yyyy): ");
        System.out.println("  Created: " + expeditionService.createExpedition(name, vessel, date));
    }

    private void addCrew() throws ItemNotFoundException {
        int expeditionId = askInt("Expedition ID: ");
        int memberId = askInt("Member ID: ");
        expeditionService.assignCrew(expeditionId, memberId);
        System.out.println("  Crew member added.");
    }

    private void addCargo() throws ItemNotFoundException {
        int expeditionId = askInt("Expedition ID: ");
        int assetId = askInt("Asset ID: ");
        expeditionService.assignCargo(expeditionId, assetId);
        System.out.println("  Cargo added.");
    }

    private void viewExpeditions() {
        if (expeditionService.getAll().isEmpty()) {
            System.out.println("  No expeditions yet.");
            return;
        }
        CollectionUtil.printReports(expeditionService.getAll());
    }

    // ------------------------------------------------------------------ alerts

    private void alertMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- ALERTS AND SOS ---");
            System.out.println("  1. Raise SOS");
            System.out.println("  2. Acknowledge alert (operator)");
            System.out.println("  3. Dispatch help (operator)");
            System.out.println("  4. Close alert (operator)");
            System.out.println("  5. View open alerts (highest priority first)");
            System.out.println("  6. View all alerts");
            System.out.println("  0. Back");
            switch (ask("Choose: ")) {
                case "1": execute(this::raiseSos); break;
                case "2": execute(this::acknowledgeAlert); break;
                case "3": execute(this::dispatchAlert); break;
                case "4": execute(this::closeAlert); break;
                case "5": execute(this::viewOpenAlerts); break;
                case "6": execute(this::viewAllAlerts); break;
                case "0": back = true; break;
                default: System.out.println("  Invalid option.\n");
            }
        }
    }

    private void raiseSos() throws ItemNotFoundException {
        int memberId = askInt("Your member ID: ");
        String location = ask("Where are you: ");
        String description = ask("Describe the emergency: ");
        System.out.println("  SOS raised: " + alertService.raiseSOS(memberId, location, description));
    }

    private void acknowledgeAlert() throws ItemNotFoundException, InvalidStatusChangeException {
        int alertId = askInt("Alert ID: ");
        int operatorId = askInt("Operator ID: ");
        alertService.acknowledge(alertId, operatorId);
        System.out.println("  Alert #" + alertId + " acknowledged.");
    }

    private void dispatchAlert() throws ItemNotFoundException, InvalidStatusChangeException {
        int alertId = askInt("Alert ID: ");
        int operatorId = askInt("Operator ID: ");
        alertService.dispatch(alertId, operatorId);
        System.out.println("  Help dispatched for alert #" + alertId + ".");
    }

    private void closeAlert() throws ItemNotFoundException, InvalidStatusChangeException {
        int alertId = askInt("Alert ID: ");
        int operatorId = askInt("Operator ID: ");
        alertService.close(alertId, operatorId);
        System.out.println("  Alert #" + alertId + " closed.");
    }

    private void viewOpenAlerts() {
        List<Alert> open = alertService.getOpenAlertsByPriority();
        if (open.isEmpty()) {
            System.out.println("  No open alerts.");
            return;
        }
        CollectionUtil.printNumbered(open);
    }

    private void viewAllAlerts() {
        List<Alert> all = alertService.getAllAlerts();
        if (all.isEmpty()) {
            System.out.println("  No alerts yet.");
            return;
        }
        CollectionUtil.printNumbered(all);
    }

    // ------------------------------------------------------------------ sync and simulator

    private void syncMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- SYNC, LINK AND DEVICE SIMULATOR ---");
            System.out.println("  Link is " + (syncQueue.isOnline() ? "ONLINE" : "OFFLINE")
                + ", pending records: " + syncQueue.size());
            System.out.println("  1. Switch link ONLINE / OFFLINE");
            System.out.println("  2. View pending sync queue");
            System.out.println("  3. Start device simulator");
            System.out.println("  4. Stop device simulator");
            System.out.println("  5. Show thread states");
            System.out.println("  0. Back");
            switch (ask("Choose: ")) {
                case "1": execute(this::toggleLink); break;
                case "2": execute(this::viewPending); break;
                case "3": execute(this::startSimulator); break;
                case "4": execute(this::stopSimulator); break;
                case "5": execute(this::showThreads); break;
                case "0": back = true; break;
                default: System.out.println("  Invalid option.\n");
            }
        }
    }

    private void toggleLink() {
        boolean goOnline = !syncQueue.isOnline();
        syncQueue.setOnline(goOnline);
        System.out.println("  Link is now " + (goOnline ? "ONLINE (pending records will be sent)"
            : "OFFLINE (updates are saved and queued; SOS still goes out)"));
    }

    private void viewPending() {
        List<SyncRecord> pending = syncQueue.snapshot();
        if (pending.isEmpty()) {
            System.out.println("  Nothing is waiting to be synced.");
            return;
        }
        System.out.println("  Waiting to be sent (highest priority first):");
        CollectionUtil.printNumbered(pending);
    }

    private void startSimulator() {
        if (simulatorThread != null && simulatorThread.isAlive()) {
            System.out.println("  The simulator is already running.");
            return;
        }
        simulator = new DeviceSimulator(assetService, 4000);
        simulatorThread = new Thread(simulator, "DeviceSimulator");
        simulatorThread.setPriority(Thread.MIN_PRIORITY);
        simulatorThread.setDaemon(true);
        simulatorThread.start();
        System.out.println("  Simulator started: one mock event every 4 seconds (lines start with [SIM]).");
    }

    private void stopSimulator() {
        if (simulatorThread == null || !simulatorThread.isAlive()) {
            System.out.println("  The simulator is not running.");
            return;
        }
        simulator.stop();
        simulatorThread.interrupt();
        try {
            simulatorThread.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("  Simulator stopped.");
    }

    private void showThreads() {
        Thread main = Thread.currentThread();
        System.out.println("  Menu thread      : " + main.getName() + "  state=" + main.getState()
            + "  priority=" + main.getPriority());
        System.out.println("  Sync thread      : " + syncThread.getName() + "  state=" + syncThread.getState()
            + "  priority=" + syncThread.getPriority() + "  sent so far=" + syncThread.getSentCount());
        if (simulatorThread == null) {
            System.out.println("  Simulator thread : not created yet (NEW)");
        } else {
            System.out.println("  Simulator thread : " + simulatorThread.getName() + "  state="
                + simulatorThread.getState() + "  priority=" + simulatorThread.getPriority());
        }
        System.out.println("  (RUNNABLE = running, WAITING = waiting for work, TIMED_WAITING = sleeping,");
        System.out.println("   TERMINATED = finished)");
    }

    // ------------------------------------------------------------------ reports

    private void reportMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- REPORTS ---");
            System.out.println("  1. Generate and save full report");
            System.out.println("  2. View saved report");
            System.out.println("  3. View log received by the command centre");
            System.out.println("  0. Back");
            switch (ask("Choose: ")) {
                case "1": execute(this::generateReport); break;
                case "2": execute(this::viewReport); break;
                case "3": execute(this::viewCommandCentreLog); break;
                case "0": back = true; break;
                default: System.out.println("  Invalid option.\n");
            }
        }
    }

    private void generateReport() throws IOException {
        FileStorage.writeText(reportFile, dashboardService.buildReport());
        System.out.println("  Report saved to " + new File(reportFile).getAbsolutePath());
    }

    private void viewReport() throws IOException {
        if (!FileStorage.exists(reportFile)) {
            System.out.println("  No report yet. Choose option 1 first.");
            return;
        }
        for (String line : FileStorage.readLines(reportFile)) {
            System.out.println("  " + line);
        }
    }

    private void viewCommandCentreLog() throws IOException {
        if (!FileStorage.exists(commandCentreFile)) {
            System.out.println("  The command centre has not received anything yet.");
            return;
        }
        List<String> lines = FileStorage.readLines(commandCentreFile);
        int from = Math.max(0, lines.size() - 20);
        System.out.println("  Last " + (lines.size() - from) + " of " + lines.size() + " received record(s):");
        for (String line : lines.subList(from, lines.size())) {
            System.out.println("  " + line);
        }
    }

    // ------------------------------------------------------------------ shutdown

    /** Stops the threads and saves all data (byte stream + serialization). */
    public void shutdown() {
        if (simulatorThread != null && simulatorThread.isAlive()) {
            stopSimulator();
        }
        // give the sync thread a moment to send what is pending, if the link is up
        long start = System.currentTimeMillis();
        while (syncQueue.isOnline() && syncQueue.size() > 0 && System.currentTimeMillis() - start < 2000) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        syncThread.interrupt();
        try {
            syncThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        store.replacePendingSync(syncQueue.snapshot());
        try {
            FileStorage.save(store, dataFile);
            System.out.println("Data saved to " + new File(dataFile).getAbsolutePath());
            if (syncQueue.size() > 0) {
                System.out.println(syncQueue.size() + " update(s) are still waiting to be synced; they will be sent next time.");
            }
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
        System.out.println("Goodbye!");
    }
}
