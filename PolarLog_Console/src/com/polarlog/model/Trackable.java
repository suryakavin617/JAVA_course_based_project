package com.polarlog.model;

/** Extended interface: an Identifiable thing that also has a location. */
public interface Trackable extends Identifiable {
    String getLocation();

    void moveTo(String newLocation);
}
