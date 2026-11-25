package com.polarlog.service;

import com.polarlog.dao.DataStore;
import com.polarlog.exception.ItemNotFoundException;
import com.polarlog.model.Asset;
import com.polarlog.model.Expedition;
import com.polarlog.model.StationMember;
import com.polarlog.model.SyncRecord;
import com.polarlog.sync.SyncQueue;
import com.polarlog.util.DateUtil;
import com.polarlog.util.Validator;
import java.time.LocalDate;
import java.util.List;

/** Expedition planning: vessel, departure date, crew and cargo. */
public class ExpeditionService {
    private final DataStore store;
    private final PersonnelService personnelService;
    private final AssetService assetService;
    private final SyncQueue syncQueue;
    private final ActivityLog log;

    public ExpeditionService(DataStore store, PersonnelService personnelService, AssetService assetService,
                             SyncQueue syncQueue, ActivityLog log) {
        this.store = store;
        this.personnelService = personnelService;
        this.assetService = assetService;
        this.syncQueue = syncQueue;
        this.log = log;
    }

    public Expedition createExpedition(String name, String vessel, String departureText) {
        String cleanName = Validator.requireName(name);
        String cleanVessel = Validator.requireName(vessel);
        LocalDate date = DateUtil.parseDate(departureText);
        Expedition expedition = new Expedition(store.getExpeditions().nextId(), cleanName, cleanVessel, date);
        store.getExpeditions().add(expedition);
        syncQueue.add("EXPEDITION_CREATED", expedition.toString(), SyncRecord.NORMAL);
        log.record("Created expedition " + cleanName);
        return expedition;
    }

    public void assignCrew(int expeditionId, int memberId) throws ItemNotFoundException {
        Expedition expedition = store.getExpeditions().get(expeditionId);
        StationMember member = personnelService.getMember(memberId);
        expedition.addCrew(member);
        syncQueue.add("EXPEDITION_CREW", member.getName() + " -> " + expedition.getName(), SyncRecord.NORMAL);
        log.record(member.getName() + " added to " + expedition.getName());
    }

    public void assignCargo(int expeditionId, int assetId) throws ItemNotFoundException {
        Expedition expedition = store.getExpeditions().get(expeditionId);
        Asset asset = assetService.getAsset(assetId);
        expedition.addCargo(asset);
        syncQueue.add("EXPEDITION_CARGO", asset.getName() + " -> " + expedition.getName(), SyncRecord.NORMAL);
        log.record(asset.getName() + " loaded for " + expedition.getName());
    }

    public List<Expedition> getAll() {
        return store.getExpeditions().getAll();
    }
}
