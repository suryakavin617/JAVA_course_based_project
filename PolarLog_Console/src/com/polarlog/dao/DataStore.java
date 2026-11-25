package com.polarlog.dao;

import com.polarlog.model.Alert;
import com.polarlog.model.Asset;
import com.polarlog.model.Expedition;
import com.polarlog.model.InventoryItem;
import com.polarlog.model.MovementLog;
import com.polarlog.model.Person;
import com.polarlog.model.SyncRecord;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** Holds all application data. The whole object is saved to a file using serialization. */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Repository<Asset> assets = new Repository<>();
    private final Repository<InventoryItem> inventory = new Repository<>();
    private final Repository<Person> people = new Repository<>();
    private final Repository<Expedition> expeditions = new Repository<>();
    private final Repository<Alert> alerts = new Repository<>();
    private final List<MovementLog> movementLog = new LinkedList<>();
    private final List<SyncRecord> pendingSync = new ArrayList<>();

    public Repository<Asset> getAssets() {
        return assets;
    }

    public Repository<InventoryItem> getInventory() {
        return inventory;
    }

    public Repository<Person> getPeople() {
        return people;
    }

    public Repository<Expedition> getExpeditions() {
        return expeditions;
    }

    public Repository<Alert> getAlerts() {
        return alerts;
    }

    public List<MovementLog> getMovementLog() {
        return movementLog;
    }

    public List<SyncRecord> getPendingSync() {
        return pendingSync;
    }

    public void replacePendingSync(List<SyncRecord> records) {
        pendingSync.clear();
        pendingSync.addAll(records);
    }

    public boolean isEmpty() {
        return assets.size() == 0 && inventory.size() == 0 && people.size() == 0;
    }
}
