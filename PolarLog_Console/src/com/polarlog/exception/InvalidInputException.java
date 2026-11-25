package com.polarlog.exception;

/** User-defined unchecked exception: thrown when the user types something invalid. */
public class InvalidInputException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
