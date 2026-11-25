package com.polarlog.app;

import com.polarlog.dao.DataStore;
import com.polarlog.dao.FileStorage;
import com.polarlog.service.ActivityLog;
import com.polarlog.service.AlertService;
import com.polarlog.service.AssetService;
import com.polarlog.service.DashboardService;
import com.polarlog.service.ExpeditionService;
import com.polarlog.service.InventoryService;
import com.polarlog.service.PersonnelService;
import com.polarlog.sync.SyncManager;
import com.polarlog.sync.SyncQueue;
import java.io.File;

/**
 * PolarLog - Integrated Polar Expedition Logistics and Asset Management System (console edition).
 *
 * Command line arguments:
 *   --demo   load sample data (only when there is no saved data)
 *   --reset  delete saved data and start fresh
 *   --help   show this help
 */
public class Main {
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + File.separator + "polarlog.dat";
    private static final String COMMAND_CENTRE_FILE = DATA_DIR + File.separator + "command_centre_log.txt";
    private static final String REPORT_FILE = DATA_DIR + File.separator + "report.txt";

    private static void printHelp() {
        System.out.println("PolarLog console edition");
        System.out.println("Usage: java com.polarlog.app.Main [--demo] [--reset] [--help]");
        System.out.println("  --demo   load sample data (only when there is no saved data)");
        System.out.println("  --reset  delete saved data and start fresh");
        System.out.println("  --help   show this help");
    }

    public static void main(String[] args) {
        boolean demo = false;
        boolean reset = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--demo")) {
                demo = true;
            } else if (arg.equalsIgnoreCase("--reset")) {
                reset = true;
            } else if (arg.equalsIgnoreCase("--help")) {
                printHelp();
                return;
            } else {
                System.out.println("Ignoring unknown option: " + arg);
            }
        }

        System.out.println();
        System.out.println("PolarLog - Integrated Polar Expedition Logistics and Asset Management System");
        System.out.println("Console edition (physical devices are simulated)");
        System.out.println();

        if (reset) {
            FileStorage.delete(DATA_FILE);
            FileStorage.delete(COMMAND_CENTRE_FILE);
            FileStorage.delete(REPORT_FILE);
            System.out.println("Saved data cleared.");
        }

        // ---- load saved data and create the objects ----
        DataStore store = FileStorage.load(DATA_FILE);
        SyncQueue syncQueue = new SyncQueue();
        syncQueue.restore(store.getPendingSync());
        ActivityLog log = new ActivityLog();

        PersonnelService personnelService = new PersonnelService(store, syncQueue, log);
        AlertService alertService = new AlertService(store, personnelService, syncQueue, log);
        AssetService assetService = new AssetService(store, syncQueue, log);
        InventoryService inventoryService = new InventoryService(store, alertService, syncQueue, log);
        ExpeditionService expeditionService =
            new ExpeditionService(store, personnelService, assetService, syncQueue, log);
        DashboardService dashboardService = new DashboardService(store, assetService, inventoryService,
            personnelService, alertService, syncQueue, log);

        SyncManager syncThread = new SyncManager(syncQueue, COMMAND_CENTRE_FILE);
        ConsoleUI ui = new ConsoleUI(store, syncQueue, syncThread, assetService, inventoryService,
            personnelService, expeditionService, alertService, dashboardService,
            DATA_FILE, COMMAND_CENTRE_FILE, REPORT_FILE);

        // ---- optional demo data ----
        boolean loadDemo = demo;
        if (demo && !store.isEmpty()) {
            System.out.println("Saved data already exists, so demo data was not loaded (use --reset --demo).");
            loadDemo = false;
        } else if (!demo && store.isEmpty()) {
            loadDemo = ui.askYesNo("No saved data found. Load sample demo data? (y/n): ");
        }
        if (loadDemo) {
            try {
                DemoData.load(personnelService, assetService, inventoryService, expeditionService);
                syncQueue.setOnline(false);     // start offline so the queue can be demonstrated
                System.out.println("Demo data loaded: operator ID 1, members 2-4, assets 1-5, items 1-4.");
                System.out.println("The link starts OFFLINE: updates are queued. Use menu 6 to go online.");
            } catch (Exception e) {
                System.out.println("Could not load demo data: " + e.getMessage());
            }
        }

        // ---- run ----
        syncThread.start();
        try {
            ui.run();
        } finally {
            ui.shutdown();
        }
    }
}
