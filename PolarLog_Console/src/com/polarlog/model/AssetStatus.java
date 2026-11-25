package com.polarlog.model;

/** Asset lifecycle: Registered, Deployed, In Use, Inspected, Retired. */
public enum AssetStatus {
    REGISTERED, DEPLOYED, IN_USE, INSPECTED, RETIRED;

    public boolean canChangeTo(AssetStatus next) {
        switch (this) {
            case REGISTERED:
                return next == DEPLOYED || next == RETIRED;
            case DEPLOYED:
                return next == IN_USE || next == INSPECTED || next == RETIRED;
            case IN_USE:
                return next == INSPECTED || next == RETIRED;
            case INSPECTED:
                return next == DEPLOYED || next == IN_USE || next == RETIRED;
            default:
                return false;   // RETIRED is the final state
        }
    }
}
