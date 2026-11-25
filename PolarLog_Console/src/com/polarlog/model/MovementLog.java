package com.polarlog.model;

import com.polarlog.util.DateUtil;
import java.io.Serializable;
import java.time.LocalDateTime;

/** One check-in or check-out entry. */
public class MovementLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String memberName;
    private final String action;
    private final String location;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public MovementLog(String memberName, String action, String location) {
        this.memberName = memberName;
        this.action = action;
        this.location = location;
    }

    @Override
    public String toString() {
        return DateUtil.format(timestamp) + "  " + memberName + "  " + action + "  " + location;
    }
}
