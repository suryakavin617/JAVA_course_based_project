package com.polarlog.service;

import com.polarlog.dao.DataStore;
import com.polarlog.model.Alert;
import com.polarlog.model.Asset;
import com.polarlog.model.AssetStatus;
import com.polarlog.model.Expedition;
import com.polarlog.model.InventoryItem;
import com.polarlog.model.MovementLog;
import com.polarlog.model.Person;
import com.polarlog.sync.SyncQueue;
import com.polarlog.util.CollectionUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Builds the dashboard summary and the full text report (using StringBuffer). */
public class DashboardService {
    private final DataStore store;
    private final AssetService assetService;
    private final InventoryService inventoryService;
    private final PersonnelService personnelService;
    private final AlertService alertService;
    private final SyncQueue syncQueue;
    private final ActivityLog log;
    private final long startTime = System.currentTimeMillis();

    public DashboardService(DataStore store, AssetService assetService, InventoryService inventoryService,
                            PersonnelService personnelService, AlertService alertService,
                            SyncQueue syncQueue, ActivityLog log) {
        this.store = store;
        this.assetService = assetService;
        this.inventoryService = inventoryService;
        this.personnelService = personnelService;
        this.alertService = alertService;
        this.syncQueue = syncQueue;
        this.log = log;
    }

    public String getSummary() {
        StringBuffer sb = new StringBuffer();
        String nl = System.lineSeparator();
        long seconds = (System.currentTimeMillis() - startTime) / 1000;

        sb.append("=========== COMMAND DASHBOARD ===========").append(nl);
        sb.append("Session time     : ").append(seconds).append(" seconds (Java ")
          .append(System.getProperty("java.version")).append(")").append(nl);
        sb.append("Link status      : ").append(syncQueue.isOnline() ? "ONLINE" : "OFFLINE").append(nl);
        sb.append("Pending sync     : ").append(syncQueue.size()).append(" record(s)").append(nl);
        sb.append(nl).append("ASSETS (").append(store.getAssets().size()).append(")").append(nl);
        for (Map.Entry<AssetStatus, Integer> entry : assetService.countByStatus().entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" : ").append(entry.getValue()).append(nl);
        }
        sb.append("  Categories : ").append(assetService.categories()).append(nl);

        List<InventoryItem> items = inventoryService.getAllItems();
        double total = 0;
        for (InventoryItem item : items) {
            total += item.getQuantity();
        }
        Double average = items.isEmpty() ? Double.valueOf(0) : Double.valueOf(total / items.size());
        sb.append(nl).append("INVENTORY (").append(items.size()).append(" items, average stock ")
          .append(String.format("%.1f", average)).append(")").append(nl);
        List<InventoryItem> low = inventoryService.getLowStockItems();
        sb.append("  Low stock items : ").append(low.size()).append(nl);
        for (InventoryItem item : low) {
            sb.append("    - ").append(item.getName()).append(" (").append(item.getQuantity()).append(")").append(nl);
        }

        Set<String> stations = new HashSet<>();
        for (Person person : personnelService.getAllPeople()) {
            stations.add(person.getStation());
        }
        sb.append(nl).append("PEOPLE (").append(personnelService.getAllPeople().size())
          .append(" at ").append(stations.size()).append(" station(s))").append(nl);
        sb.append("  Checked in : ").append(personnelService.getCheckedInNames()).append(nl);

        List<Alert> open = alertService.getOpenAlertsByPriority();
        sb.append(nl).append("OPEN ALERTS (").append(open.size()).append(")").append(nl);
        for (Alert alert : open) {
            sb.append("  ").append(alert).append(nl);
        }

        sb.append(nl).append("RECENT ACTIVITY (newest first)").append(nl);
        List<String> recent = log.latest();
        if (recent.isEmpty()) {
            sb.append("  nothing yet").append(nl);
        }
        for (String entry : recent) {
            sb.append("  ").append(entry).append(nl);
        }
        sb.append("=========================================");
        return sb.toString();
    }

    public String buildReport() {
        StringBuffer sb = new StringBuffer();
        String nl = System.lineSeparator();
        sb.append("POLARLOG REPORT").append(nl).append(nl);
        sb.append(getSummary()).append(nl).append(nl);

        sb.append("--- ALL ASSETS (sorted by name) ---").append(nl);
        for (Asset asset : assetService.listSorted()) {
            sb.append(asset).append(nl);
        }
        Asset last = CollectionUtil.findMax(store.getAssets().getAll());
        if (last != null) {
            sb.append("(Last name alphabetically: ").append(last.getName()).append(")").append(nl);
        }

        sb.append(nl).append("--- INVENTORY ---").append(nl);
        for (InventoryItem item : inventoryService.getInventoryByName().values()) {
            sb.append(item).append(nl);
        }

        sb.append(nl).append("--- PEOPLE ---").append(nl);
        for (Person person : personnelService.getAllPeople()) {
            sb.append(person).append(nl);
        }

        sb.append(nl).append("--- EXPEDITIONS ---").append(nl);
        for (Expedition expedition : store.getExpeditions().getAll()) {
            sb.append(expedition.report()).append(nl).append(nl);
        }

        sb.append("--- ALL ALERTS ---").append(nl);
        for (Alert alert : alertService.getAllAlerts()) {
            sb.append(alert).append(nl);
        }

        sb.append(nl).append("--- MOVEMENT LOG ---").append(nl);
        for (MovementLog entry : personnelService.getMovementLog()) {
            sb.append(entry).append(nl);
        }
        return sb.toString();
    }
}
