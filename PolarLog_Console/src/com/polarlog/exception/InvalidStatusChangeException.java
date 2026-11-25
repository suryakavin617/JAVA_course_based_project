package com.polarlog.exception;

/** User-defined checked exception: thrown when an asset or alert status change is not allowed. */
public class InvalidStatusChangeException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidStatusChangeException(String message) {
        super(message);
    }
}
