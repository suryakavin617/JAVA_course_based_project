package com.polarlog.sim;

import com.polarlog.exception.ItemNotFoundException;
import com.polarlog.model.Asset;
import com.polarlog.model.AssetStatus;
import com.polarlog.service.AssetService;
import java.util.List;
import java.util.Random;

/**
 * Stands in for the physical devices. Runs in its own thread (implements Runnable) and
 * creates mock scan and location events at regular intervals.
 */
public class DeviceSimulator implements Runnable {
    private static final String[] PLACES = {"Warehouse", "Camp A", "Camp B", "Ship Deck", "Lab", "Runway"};

    private final AssetService assetService;
    private final long intervalMillis;
    private final Random random = new Random();
    private volatile boolean running = true;

    public DeviceSimulator(AssetService assetService, long intervalMillis) {
        this.assetService = assetService;
        this.intervalMillis = intervalMillis;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        System.out.println("   [SIM] device simulator started");
        while (running) {
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                break;
            }
            if (!running) {
                break;
            }
            if (random.nextBoolean()) {
                generateLocationEvent();
            } else {
                generateScanEvent();
            }
        }
        System.out.println("   [SIM] device simulator stopped");
    }

    private Asset pickAsset() {
        // lambda expression passed to our functional interface AssetFilter
        List<Asset> usable = assetService.filter(a -> a.getStatus() != AssetStatus.RETIRED);
        if (usable.isEmpty()) {
            return null;
        }
        return usable.get(random.nextInt(usable.size()));
    }

    public void generateLocationEvent() {
        Asset asset = pickAsset();
        if (asset == null) {
            System.out.println("   [SIM] no assets available to simulate");
            return;
        }
        String place = PLACES[random.nextInt(PLACES.length)];
        try {
            assetService.moveAsset(asset.getId(), place);
            System.out.println("   [SIM] location event: " + asset.getName() + " moved to " + place);
        } catch (ItemNotFoundException | IllegalStateException e) {
            System.out.println("   [SIM] event skipped: " + e.getMessage());
        }
    }

    public void generateScanEvent() {
        Asset asset = pickAsset();
        if (asset == null) {
            System.out.println("   [SIM] no assets available to simulate");
            return;
        }
        try {
            assetService.recordScan(asset.getId());
            System.out.println("   [SIM] scan event: " + asset.getName() + " scanned");
        } catch (ItemNotFoundException e) {
            System.out.println("   [SIM] event skipped: " + e.getMessage());
        }
    }
}
