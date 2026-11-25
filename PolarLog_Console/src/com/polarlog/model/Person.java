package com.polarlog.model;

import java.io.Serializable;

/** Abstract parent class for everyone who uses the system. */
public abstract class Person implements Identifiable, Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String station;

    public Person(int id, String name, String station) {
        this.id = id;
        this.name = name;
        this.station = station;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStation() {
        return station;
    }

    /** Each subclass gives its own role (runtime polymorphism). */
    public abstract String getRole();

    public String getDetails() {
        return "#" + id + " " + name + " - " + getRole() + " (" + station + ")";
    }

    @Override
    public String toString() {
        return getDetails();
    }
}
