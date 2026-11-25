package com.polarlog.service;

import com.polarlog.util.DateUtil;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Keeps the 10 most recent actions in an ArrayDeque (newest first). */
public class ActivityLog {
    private static final int LIMIT = 10;
    private final Deque<String> entries = new ArrayDeque<>();

    public synchronized void record(String message) {
        entries.addFirst(DateUtil.format(LocalDateTime.now()) + "  " + message);
        if (entries.size() > LIMIT) {
            entries.removeLast();
        }
    }

    public synchronized List<String> latest() {
        return new ArrayList<>(entries);
    }
}
