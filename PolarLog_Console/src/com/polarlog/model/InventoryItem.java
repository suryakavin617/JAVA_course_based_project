package com.polarlog.model;

import com.polarlog.exception.InsufficientStockException;
import java.io.Serializable;

/** A stock item such as spare parts, fuel or rations. */
public class InventoryItem implements Identifiable, Comparable<InventoryItem>, Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private int quantity;
    private final int reorderLevel;

    public InventoryItem(int id, String name, int quantity, int reorderLevel) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void addStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        quantity += amount;
    }

    public void removeStock(int amount) throws InsufficientStockException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (amount > quantity) {
            throw new InsufficientStockException(
                "Only " + quantity + " of " + name + " left, cannot remove " + amount);
        }
        quantity -= amount;
    }

    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    @Override
    public int compareTo(InventoryItem other) {
        return name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return "#" + id + " " + name + " | quantity=" + quantity + " | reorder level=" + reorderLevel
            + (isLowStock() ? "  ** LOW STOCK **" : "");
    }
}
