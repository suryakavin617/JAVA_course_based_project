package com.polarlog.service;

import com.polarlog.dao.DataStore;
import com.polarlog.exception.InvalidInputException;
import com.polarlog.exception.InvalidStatusChangeException;
import com.polarlog.exception.ItemNotFoundException;
import com.polarlog.model.Alert;
import com.polarlog.model.AlertStatus;
import com.polarlog.model.CommandOperator;
import com.polarlog.model.InventoryItem;
import com.polarlog.model.LowStockAlert;
import com.polarlog.model.SOSAlert;
import com.polarlog.model.StationMember;
import com.polarlog.model.SyncRecord;
import com.polarlog.sync.SyncQueue;
import com.polarlog.util.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/** SOS and low-stock alerts. The status can only move forward. */
public class AlertService {
    private final DataStore store;
    private final PersonnelService personnelService;
    private final SyncQueue syncQueue;
    private final ActivityLog log;

    public AlertService(DataStore store, PersonnelService personnelService,
                        SyncQueue syncQueue, ActivityLog log) {
        this.store = store;
        this.personnelService = personnelService;
        this.syncQueue = syncQueue;
        this.log = log;
    }

    public synchronized SOSAlert raiseSOS(int memberId, String location, String description)
            throws ItemNotFoundException {
        StationMember member = personnelService.getMember(memberId);
        String cleanLocation = Validator.requireLocation(location);
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidInputException("Please describe the emergency");
        }
        SOSAlert alert = new SOSAlert(store.getAlerts().nextId(), member.getName(),
                                      cleanLocation, description.trim());
        store.getAlerts().add(alert);
        syncQueue.add("SOS_RAISED", alert.toString(), SyncRecord.URGENT);   // urgent: goes first
        log.record("SOS raised by " + member.getName() + " at " + cleanLocation);
        return alert;
    }

    /** Called automatically when stock reaches the reorder level. Only one open alert per item. */
    public synchronized LowStockAlert raiseLowStock(InventoryItem item) {
        for (Alert existing : store.getAlerts().getAll()) {
            if (existing.isOpen() && existing instanceof LowStockAlert
                    && ((LowStockAlert) existing).getItemName().equals(item.getName())) {
                return (LowStockAlert) existing;
            }
        }
        LowStockAlert alert = new LowStockAlert(store.getAlerts().nextId(), item.getName(), item.getQuantity());
        store.getAlerts().add(alert);
        syncQueue.add("LOW_STOCK", alert.getDescription(), SyncRecord.NORMAL);
        log.record("Low stock alert for " + item.getName());
        return alert;
    }

    public void acknowledge(int alertId, int operatorId)
            throws ItemNotFoundException, InvalidStatusChangeException {
        advance(alertId, operatorId, AlertStatus.ACKNOWLEDGED);
    }

    public void dispatch(int alertId, int operatorId)
            throws ItemNotFoundException, InvalidStatusChangeException {
        advance(alertId, operatorId, AlertStatus.DISPATCHED);
    }

    public void close(int alertId, int operatorId)
            throws ItemNotFoundException, InvalidStatusChangeException {
        advance(alertId, operatorId, AlertStatus.CLOSED);
    }

    private synchronized void advance(int alertId, int operatorId, AlertStatus expected)
            throws ItemNotFoundException, InvalidStatusChangeException {
        CommandOperator operator = personnelService.getOperator(operatorId);
        Alert alert = store.getAlerts().get(alertId);
        if (alert.getStatus().next() != expected) {
            throw new InvalidStatusChangeException("Alert #" + alertId + " is " + alert.getStatus()
                + ", so it cannot be set to " + expected + " now (status only moves forward)");
        }
        operator.handleAlert(alert);
        syncQueue.add("ALERT_" + expected, "Alert #" + alertId + " " + expected + " by " + operator.getName(),
                      alert.getPriority());
        log.record("Alert #" + alertId + " " + expected + " by " + operator.getName());
    }

    /** Open alerts, highest priority first, using a PriorityQueue. */
    public List<Alert> getOpenAlertsByPriority() {
        PriorityQueue<Alert> queue = new PriorityQueue<>();
        for (Alert alert : store.getAlerts().getAll()) {
            if (alert.isOpen()) {
                queue.add(alert);
            }
        }
        List<Alert> ordered = new ArrayList<>();
        while (!queue.isEmpty()) {
            ordered.add(queue.poll());
        }
        return ordered;
    }

    public List<Alert> getAllAlerts() {
        return store.getAlerts().getAll();
    }
}
