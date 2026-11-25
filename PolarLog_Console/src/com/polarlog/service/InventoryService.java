package com.polarlog.service;

import com.polarlog.dao.DataStore;
import com.polarlog.exception.InsufficientStockException;
import com.polarlog.exception.InvalidInputException;
import com.polarlog.exception.ItemNotFoundException;
import com.polarlog.model.InventoryItem;
import com.polarlog.model.SyncRecord;
import com.polarlog.sync.SyncQueue;
import com.polarlog.util.Validator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

/** Stock handling with low-stock detection. */
public class InventoryService {
    private final DataStore store;
    private final AlertService alertService;
    private final SyncQueue syncQueue;
    private final ActivityLog log;

    public InventoryService(DataStore store, AlertService alertService,
                            SyncQueue syncQueue, ActivityLog log) {
        this.store = store;
        this.alertService = alertService;
        this.syncQueue = syncQueue;
        this.log = log;
    }

    public InventoryItem addItem(String name, int quantity, int reorderLevel) {
        String cleanName = Validator.requireName(name);
        if (quantity < 0 || reorderLevel < 0) {
            throw new InvalidInputException("Quantity and reorder level cannot be negative");
        }
        InventoryItem item = new InventoryItem(store.getInventory().nextId(), cleanName, quantity, reorderLevel);
        store.getInventory().add(item);
        syncQueue.add("INVENTORY_ADDED", item.toString(), SyncRecord.NORMAL);
        log.record("Added inventory item " + cleanName);
        if (item.isLowStock()) {
            alertService.raiseLowStock(item);
        }
        return item;
    }

    /** Reads a line like "Bolt Pack,50,10" using StringTokenizer. */
    public InventoryItem quickAdd(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        if (tokenizer.countTokens() != 3) {
            throw new InvalidInputException("Format is: name,quantity,reorderLevel   (example: Bolt Pack,50,10)");
        }
        String name = tokenizer.nextToken();
        int quantity = Validator.parseInt(tokenizer.nextToken());
        int reorderLevel = Validator.parseInt(tokenizer.nextToken());
        return addItem(name, quantity, reorderLevel);
    }

    public InventoryItem getItem(int id) throws ItemNotFoundException {
        return store.getInventory().get(id);
    }

    public void addStock(int id, int amount) throws ItemNotFoundException {
        InventoryItem item = getItem(id);
        item.addStock(amount);
        syncQueue.add("STOCK_ADDED", item.getName() + " +" + amount + " (now " + item.getQuantity() + ")",
                      SyncRecord.NORMAL);
        log.record("Added " + amount + " to " + item.getName());
    }

    public void removeStock(int id, int amount) throws ItemNotFoundException, InsufficientStockException {
        InventoryItem item = getItem(id);
        item.removeStock(amount);
        syncQueue.add("STOCK_REMOVED", item.getName() + " -" + amount + " (now " + item.getQuantity() + ")",
                      SyncRecord.NORMAL);
        log.record("Removed " + amount + " from " + item.getName());
        if (item.isLowStock()) {
            alertService.raiseLowStock(item);
        }
    }

    public List<InventoryItem> getLowStockItems() {
        return store.getInventory().filter(InventoryItem::isLowStock);   // method reference
    }

    public List<InventoryItem> getAllItems() {
        return store.getInventory().getAll();
    }

    /** TreeMap keeps the items sorted by name. */
    public TreeMap<String, InventoryItem> getInventoryByName() {
        TreeMap<String, InventoryItem> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (InventoryItem item : store.getInventory().getAll()) {
            map.put(item.getName(), item);
        }
        return map;
    }
}
