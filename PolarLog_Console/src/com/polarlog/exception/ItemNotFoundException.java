package com.polarlog.exception;

/** User-defined checked exception: thrown when an ID or name does not exist. */
public class ItemNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public ItemNotFoundException(String message) {
        super(message);
    }
}
