package com.polarlog.model;

import com.polarlog.exception.InvalidStatusChangeException;
import com.polarlog.util.DateUtil;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** A crate, tool or spare part tracked through its whole lifecycle. */
public class Asset implements Trackable, Reportable, Comparable<Asset>, Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String category;
    private AssetStatus status;
    private String location;
    private final List<LocationRecord> history = new ArrayList<>();

    /** Static nested class: one entry of the location history. */
    public static class LocationRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String location;
        private final LocalDateTime timestamp;

        public LocationRecord(String location) {
            this.location = location;
            this.timestamp = LocalDateTime.now();
        }

        public String getLocation() {
            return location;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return DateUtil.format(timestamp) + "  ->  " + location;
        }
    }

    public Asset(int id, String name, String category, String location) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.location = location;
        this.status = AssetStatus.REGISTERED;
        this.history.add(new LocationRecord(location));
    }

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public synchronized AssetStatus getStatus() {
        return status;
    }

    @Override
    public synchronized String getLocation() {
        return location;
    }

    public synchronized void changeStatus(AssetStatus next) throws InvalidStatusChangeException {
        if (!status.canChangeTo(next)) {
            throw new InvalidStatusChangeException(
                "Asset #" + id + " cannot change from " + status + " to " + next);
        }
        status = next;
    }

    @Override
    public synchronized void moveTo(String newLocation) {
        if (status == AssetStatus.RETIRED) {
            throw new IllegalStateException("Asset #" + id + " is retired and cannot be moved");
        }
        location = newLocation;
        history.add(new LocationRecord(newLocation));
    }

    public synchronized List<LocationRecord> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public synchronized String report() {
        StringBuffer sb = new StringBuffer();
        String nl = System.lineSeparator();
        sb.append(reportHeader()).append(nl);
        sb.append("ID        : ").append(id).append(nl);
        sb.append("Name      : ").append(name).append(nl);
        sb.append("Category  : ").append(category).append(nl);
        sb.append("Status    : ").append(status).append(nl);
        sb.append("Location  : ").append(location).append(nl);
        sb.append("Moves     : ").append(history.size() - 1);
        return sb.toString();
    }

    /** Sorted by name, ignoring case (used by Collections.sort and Collections.binarySearch). */
    @Override
    public int compareTo(Asset other) {
        return name.compareToIgnoreCase(other.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Asset)) {
            return false;
        }
        return id == ((Asset) obj).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public synchronized String toString() {
        return "#" + id + " " + name + " [" + category + "] status=" + status + " location=" + location;
    }
}
