package com.polarlog.dao;

import com.polarlog.exception.ItemNotFoundException;
import com.polarlog.model.Identifiable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Generic class (Unit IV): an in-memory store for any type that has an ID.
 * Methods are synchronized because the simulator thread and the menu thread share it.
 */
public class Repository<T extends Identifiable> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Integer, T> items = new HashMap<>();
    private int lastId = 0;

    public synchronized int nextId() {
        lastId++;
        return lastId;
    }

    public synchronized void add(T item) {
        items.put(item.getId(), item);
        if (item.getId() > lastId) {
            lastId = item.getId();
        }
    }

    public synchronized T get(int id) throws ItemNotFoundException {
        T item = items.get(id);
        if (item == null) {
            throw new ItemNotFoundException("No record found with ID " + id);
        }
        return item;
    }

    public synchronized boolean contains(int id) {
        return items.containsKey(id);
    }

    public synchronized List<T> getAll() {
        return new ArrayList<>(items.values());
    }

    public synchronized List<T> filter(Predicate<T> condition) {
        List<T> result = new ArrayList<>();
        for (T item : items.values()) {
            if (condition.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public synchronized int size() {
        return items.size();
    }
}
