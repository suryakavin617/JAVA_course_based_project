package com.polarlog.sync;

import com.polarlog.model.SyncRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Shared queue between the threads that create updates (menu thread, simulator thread)
 * and the SyncManager thread that sends them.
 * Uses synchronized methods and wait()/notifyAll() for inter-thread communication.
 */
public class SyncQueue {
    private final PriorityQueue<SyncRecord> queue = new PriorityQueue<>();
    private boolean online = true;
    private long counter = 0;

    public synchronized void add(String type, String data, int priority) {
        counter++;
        queue.add(new SyncRecord(counter, type, data, priority));
        notifyAll();                       // wake up the sync thread
    }

    /**
     * Called by the sync thread. Waits while there is nothing to send, or while the link is down.
     * An urgent (SOS) record is sent even when the normal link is down (emergency channel).
     */
    public synchronized SyncRecord take() throws InterruptedException {
        while (queue.isEmpty() || (!online && queue.peek().getPriority() != SyncRecord.URGENT)) {
            wait();
        }
        return queue.poll();
    }

    public synchronized void requeue(SyncRecord record) {
        queue.add(record);
        notifyAll();
    }

    public synchronized void setOnline(boolean value) {
        online = value;
        notifyAll();                       // wake up the sync thread
    }

    public synchronized boolean isOnline() {
        return online;
    }

    public synchronized int size() {
        return queue.size();
    }

    /** Copy of the pending records in the order they will be sent. */
    public synchronized List<SyncRecord> snapshot() {
        PriorityQueue<SyncRecord> copy = new PriorityQueue<>(queue);
        List<SyncRecord> result = new ArrayList<>();
        while (!copy.isEmpty()) {
            result.add(copy.poll());
        }
        return result;
    }

    public synchronized void restore(List<SyncRecord> records) {
        for (SyncRecord record : records) {
            queue.add(record);
            if (record.getSequence() > counter) {
                counter = record.getSequence();
            }
        }
        notifyAll();
    }
}
