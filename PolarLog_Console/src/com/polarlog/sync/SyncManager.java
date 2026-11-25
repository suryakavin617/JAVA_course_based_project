package com.polarlog.sync;

import com.polarlog.dao.FileStorage;
import com.polarlog.model.SyncRecord;
import java.io.IOException;

/**
 * Background thread created by extending the Thread class.
 * It "sends" records to the command centre (a text file, for now).
 */
public class SyncManager extends Thread {
    private final SyncQueue queue;
    private final String outputFile;
    private volatile int sentCount = 0;

    public SyncManager(SyncQueue queue, String outputFile) {
        super("SyncManager");
        this.queue = queue;
        this.outputFile = outputFile;
        setPriority(Thread.MAX_PRIORITY);   // thread priority
        setDaemon(true);
    }

    public int getSentCount() {
        return sentCount;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                SyncRecord record = queue.take();
                deliver(record);
            }
        } catch (InterruptedException e) {
            // interrupt is the normal way to stop this thread
        }
    }

    private void deliver(SyncRecord record) throws InterruptedException {
        try {
            FileStorage.appendLine(outputFile, record.toLine());
            record.markSynced();
            sentCount++;
            System.out.println("   [SYNC] sent to command centre: " + record);
        } catch (IOException e) {
            System.out.println("   [SYNC] could not send, will retry: " + e.getMessage());
            queue.requeue(record);
            Thread.sleep(1000);
        }
    }
}
