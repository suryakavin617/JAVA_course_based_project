package com.polarlog.app;

import com.polarlog.exception.InvalidStatusChangeException;
import com.polarlog.exception.ItemNotFoundException;
import com.polarlog.model.Asset;
import com.polarlog.model.AssetStatus;
import com.polarlog.model.Expedition;
import com.polarlog.model.StationMember;
import com.polarlog.service.AssetService;
import com.polarlog.service.ExpeditionService;
import com.polarlog.service.InventoryService;
import com.polarlog.service.PersonnelService;
import com.polarlog.util.DateUtil;
import java.time.LocalDate;

/** Sample data so that the whole project can be demonstrated quickly. */
public class DemoData {
    private DemoData() { }

    public static void load(PersonnelService personnel, AssetService assets,
                            InventoryService inventory, ExpeditionService expeditions)
            throws ItemNotFoundException, InvalidStatusChangeException {

        personnel.addOperator("Arjun Mehta", "Command Centre");
        StationMember ravi = personnel.addMember("Ravi Kumar", "Maitri");
        StationMember sneha = personnel.addMember("Sneha Iyer", "Bharati");
        personnel.addMember("Imran Khan", "Himadri");
        personnel.checkIn(ravi.getId(), "Camp A");

        Asset drill = assets.registerAsset("Ice Drill Rig", "Equipment", "Warehouse");
        Asset medical = assets.registerAsset("Medical Crate A", "Medical", "Warehouse");
        Asset generator = assets.registerAsset("Generator Unit", "Power", "Camp A");
        Asset drone = assets.registerAsset("Survey Drone", "Research", "Lab");
        Asset antenna = assets.registerAsset("Spare Antenna", "Communication", "Warehouse");

        assets.updateStatus(generator.getId(), AssetStatus.DEPLOYED);
        assets.updateStatus(generator.getId(), AssetStatus.IN_USE);
        assets.updateStatus(drone.getId(), AssetStatus.DEPLOYED);
        assets.moveAsset(drone.getId(), "Camp B");

        inventory.quickAdd("Bolt Pack,50,10");
        inventory.quickAdd("First Aid Kit,6,4");
        inventory.quickAdd("Ration Box,80,30");
        inventory.quickAdd("Fuel Drum,5,5");          // already at reorder level: raises a low stock alert

        Expedition trip = expeditions.createExpedition("Winter Resupply", "MV Polar Star",
                DateUtil.format(LocalDate.now().plusDays(20)));
        expeditions.assignCrew(trip.getId(), ravi.getId());
        expeditions.assignCrew(trip.getId(), sneha.getId());
        expeditions.assignCargo(trip.getId(), drill.getId());
        expeditions.assignCargo(trip.getId(), medical.getId());
        expeditions.assignCargo(trip.getId(), antenna.getId());
    }
}
