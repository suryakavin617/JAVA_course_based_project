package com.polarlog.service;

import com.polarlog.dao.DataStore;
import com.polarlog.exception.InvalidStatusChangeException;
import com.polarlog.exception.ItemNotFoundException;
import com.polarlog.model.Asset;
import com.polarlog.model.AssetStatus;
import com.polarlog.model.SyncRecord;
import com.polarlog.sync.SyncQueue;
import com.polarlog.util.CollectionUtil;
import com.polarlog.util.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/** Business logic for the asset lifecycle and location history. */
public class AssetService {
    private final DataStore store;
    private final SyncQueue syncQueue;
    private final ActivityLog log;

    public AssetService(DataStore store, SyncQueue syncQueue, ActivityLog log) {
        this.store = store;
        this.syncQueue = syncQueue;
        this.log = log;
    }

    public synchronized Asset registerAsset(String name, String category, String location) {
        String cleanName = Validator.requireName(name);
        String cleanCategory = Validator.requireName(category);
        String cleanLocation = Validator.requireLocation(location);
        Asset asset = new Asset(store.getAssets().nextId(), cleanName, cleanCategory, cleanLocation);
        store.getAssets().add(asset);
        syncQueue.add("ASSET_REGISTERED", asset.toString(), SyncRecord.NORMAL);
        log.record("Registered asset " + asset.getName() + " (#" + asset.getId() + ")");
        return asset;
    }

    public Asset getAsset(int id) throws ItemNotFoundException {
        return store.getAssets().get(id);
    }

    public synchronized void updateStatus(int id, AssetStatus next)
            throws ItemNotFoundException, InvalidStatusChangeException {
        Asset asset = getAsset(id);
        asset.changeStatus(next);
        syncQueue.add("ASSET_STATUS", "Asset #" + id + " is now " + next, SyncRecord.NORMAL);
        log.record("Asset #" + id + " status changed to " + next);
    }

    public synchronized void moveAsset(int id, String location) throws ItemNotFoundException {
        String cleanLocation = Validator.requireLocation(location);
        Asset asset = getAsset(id);
        asset.moveTo(cleanLocation);
        syncQueue.add("ASSET_MOVED", "Asset #" + id + " moved to " + cleanLocation, SyncRecord.LOW);
        log.record("Asset #" + id + " moved to " + cleanLocation);
    }

    /** Used by the device simulator: a scan changes nothing but is recorded and synced. */
    public synchronized void recordScan(int id) throws ItemNotFoundException {
        Asset asset = getAsset(id);
        syncQueue.add("ASSET_SCANNED", "Asset #" + id + " scanned at " + asset.getLocation(), SyncRecord.LOW);
        log.record("Asset #" + id + " scanned at " + asset.getLocation());
    }

    /** Method overloading: same name, different parameter types. */
    public Asset searchAsset(int id) throws ItemNotFoundException {
        return getAsset(id);
    }

    public Asset searchAsset(String name) throws ItemNotFoundException {
        List<Asset> sorted = listSorted();
        Asset probe = new Asset(0, name.trim(), "-", "-");
        int index = Collections.binarySearch(sorted, probe);     // binary search on the sorted list
        if (index < 0) {
            throw new ItemNotFoundException("No asset is named '" + name.trim() + "'");
        }
        return sorted.get(index);
    }

    public List<Asset> listSorted() {
        return CollectionUtil.sortedCopy(store.getAssets().getAll());
    }

    /** Accepts a lambda expression such as a -> a.getStatus() == AssetStatus.IN_USE. */
    public List<Asset> filter(AssetFilter filter) {
        return store.getAssets().filter(a -> filter.accept(a));
    }

    public Map<AssetStatus, Integer> countByStatus() {
        Map<AssetStatus, Integer> counts = new TreeMap<>();
        for (AssetStatus status : AssetStatus.values()) {
            counts.put(status, 0);
        }
        for (Asset asset : store.getAssets().getAll()) {
            counts.put(asset.getStatus(), counts.get(asset.getStatus()) + 1);
        }
        return counts;
    }

    public SortedSet<String> categories() {
        SortedSet<String> set = new TreeSet<>();
        for (Asset asset : store.getAssets().getAll()) {
            set.add(asset.getCategory());
        }
        return set;
    }
}
