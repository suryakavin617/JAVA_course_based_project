package com.polarlog.model;

/** A crew member working at a research station. */
public class StationMember extends Person {
    private static final long serialVersionUID = 1L;

    private boolean checkedIn;
    private String currentLocation = "-";

    public StationMember(int id, String name, String station) {
        super(id, name, station);
    }

    @Override
    public String getRole() {
        return "Station Member";
    }

    public void checkIn(String location) {
        if (checkedIn) {
            throw new IllegalStateException(getName() + " is already checked in at " + currentLocation);
        }
        checkedIn = true;
        currentLocation = location;
    }

    public void checkOut() {
        if (!checkedIn) {
            throw new IllegalStateException(getName() + " is not checked in");
        }
        checkedIn = false;
        currentLocation = "-";
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public String getDetails() {
        return super.getDetails() + (checkedIn ? " | checked in at " + currentLocation : " | checked out");
    }
}
