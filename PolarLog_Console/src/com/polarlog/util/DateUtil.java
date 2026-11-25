package com.polarlog.util;

import com.polarlog.exception.InvalidInputException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Helper for the java.time date and time API. */
public class DateUtil {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private DateUtil() { }

    public static String format(LocalDateTime time) {
        return time.format(DATE_TIME);
    }

    public static String format(LocalDate date) {
        return date.format(DATE);
    }

    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Date must be in dd-MM-yyyy format, for example 15-12-2026");
        }
    }
}
