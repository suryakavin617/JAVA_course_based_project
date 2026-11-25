package com.polarlog.model;

import com.polarlog.exception.InvalidStatusChangeException;

/** An operator at the command centre who handles alerts. */
public class CommandOperator extends Person {
    private static final long serialVersionUID = 1L;

    public CommandOperator(int id, String name, String station) {
        super(id, name, station);
    }

    @Override
    public String getRole() {
        return "Command Centre Operator";
    }

    public void handleAlert(Alert alert) throws InvalidStatusChangeException {
        alert.advanceStatus();
    }
}
