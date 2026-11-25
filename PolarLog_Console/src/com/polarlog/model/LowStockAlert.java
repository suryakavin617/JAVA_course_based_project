package com.polarlog.model;

/** Automatic alert raised when an item reaches its reorder level. */
public class LowStockAlert extends Alert {
    private static final long serialVersionUID = 1L;

    private final String itemName;

    public LowStockAlert(int id, String itemName, int quantityLeft) {
        super(id, "SYSTEM", "Store", "Low stock: " + itemName + " (" + quantityLeft + " left)");
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public String getType() {
        return "LOW STOCK";
    }
}
