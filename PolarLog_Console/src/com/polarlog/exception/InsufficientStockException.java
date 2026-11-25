package com.polarlog.exception;

/** User-defined checked exception: thrown when more stock is removed than is available. */
public class InsufficientStockException extends Exception {
    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String message) {
        super(message);
    }
}
