package com.polarlog.model;

import com.polarlog.exception.InvalidStatusChangeException;

/** Alert flow moves only forward: Raised, Acknowledged, Dispatched, Closed. */
public enum AlertStatus {
    RAISED, ACKNOWLEDGED, DISPATCHED, CLOSED;

    public AlertStatus next() throws InvalidStatusChangeException {
        if (this == CLOSED) {
            throw new InvalidStatusChangeException("The alert is already closed");
        }
        return values()[ordinal() + 1];
    }
}
