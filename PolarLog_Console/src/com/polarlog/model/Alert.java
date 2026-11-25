package com.polarlog.model;

import com.polarlog.exception.InvalidStatusChangeException;
import com.polarlog.util.DateUtil;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Abstract alert. SOSAlert and LowStockAlert extend it and give their own priority. */
public abstract class Alert implements Identifiable, Comparable<Alert>, Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String raisedBy;
    private final String location;
    private final String description;
    private AlertStatus status = AlertStatus.RAISED;
    private final LocalDateTime raisedTime = LocalDateTime.now();

    public Alert(int id, String raisedBy, String location, String description) {
        this.id = id;
        this.raisedBy = raisedBy;
        this.location = location;
        this.description = description;
    }

    @Override
    public int getId() {
        return id;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public boolean isOpen() {
        return status != AlertStatus.CLOSED;
    }

    public String getDescription() {
        return description;
    }

    /** 1 is the highest priority. */
    public abstract int getPriority();

    public abstract String getType();

    public void advanceStatus() throws InvalidStatusChangeException {
        status = status.next();
    }

    /** Lower priority number first, then the older alert first. Used by PriorityQueue. */
    @Override
    public int compareTo(Alert other) {
        int byPriority = Integer.compare(getPriority(), other.getPriority());
        if (byPriority != 0) {
            return byPriority;
        }
        int byTime = raisedTime.compareTo(other.raisedTime);
        return (byTime != 0) ? byTime : Integer.compare(id, other.id);
    }

    @Override
    public String toString() {
        return "#" + id + " [" + getType() + "] " + status + " - " + description + " @ " + location
            + " (by " + raisedBy + " at " + DateUtil.format(raisedTime) + ")";
    }
}
