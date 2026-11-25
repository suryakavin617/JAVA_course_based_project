package com.polarlog.model;

/** Emergency alert: always the highest priority. */
public class SOSAlert extends Alert {
    private static final long serialVersionUID = 1L;

    public SOSAlert(int id, String raisedBy, String location, String description) {
        super(id, raisedBy, location, description);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public String getType() {
        return "SOS";
    }
}
