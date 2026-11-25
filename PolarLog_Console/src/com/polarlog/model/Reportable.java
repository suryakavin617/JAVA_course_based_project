package com.polarlog.model;

/** Interface with a default method. */
public interface Reportable {
    String report();

    default String reportHeader() {
        return "=== " + getClass().getSimpleName() + " ===";
    }
}
