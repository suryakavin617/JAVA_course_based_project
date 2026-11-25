package com.polarlog.model;

import com.polarlog.util.DateUtil;
import java.io.Serializable;
import java.time.LocalDateTime;

/** One update waiting to be sent to the command centre. */
public class SyncRecord implements Comparable<SyncRecord>, Serializable {
    private static final long serialVersionUID = 1L;

    public static final int URGENT = 1;   // SOS
    public static final int NORMAL = 2;
    public static final int LOW = 3;

    private final long sequence;
    private final String type;
    private final String data;
    private final int priority;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private boolean synced;

    public SyncRecord(long sequence, String type, String data, int priority) {
        this.sequence = sequence;
        this.type = type;
        this.data = data;
        this.priority = priority;
    }

    public long getSequence() {
        return sequence;
    }

    public String getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    public void markSynced() {
        synced = true;
    }

    public boolean isSynced() {
        return synced;
    }

    public String toLine() {
        return DateUtil.format(createdAt) + " | P" + priority + " | " + type + " | " + data;
    }

    @Override
    public int compareTo(SyncRecord other) {
        int byPriority = Integer.compare(priority, other.priority);
        return (byPriority != 0) ? byPriority : Long.compare(sequence, other.sequence);
    }

    @Override
    public String toString() {
        return "P" + priority + " " + type + " - " + data;
    }
}
