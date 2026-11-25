package com.polarlog.util;

import com.polarlog.exception.InvalidInputException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Input checks using regular expressions (java.util.regex) and wrapper classes (Integer). */
public class Validator {
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9 \\-]{2,29}$");
    private static final Pattern LOCATION = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 \\-]{1,24}$");

    private Validator() { }

    public static String requireName(String text) {
        String value = (text == null) ? "" : text.trim();
        Matcher matcher = NAME.matcher(value);
        if (!matcher.matches()) {
            throw new InvalidInputException(
                "Name must be 3-30 characters (letters, digits, space or '-') and start with a letter");
        }
        return value;
    }

    public static String requireLocation(String text) {
        String value = (text == null) ? "" : text.trim();
        Matcher matcher = LOCATION.matcher(value);
        if (!matcher.matches()) {
            throw new InvalidInputException(
                "Location must be 2-25 characters (letters, digits, space or '-')");
        }
        return value;
    }

    public static int parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("'" + text + "' is not a valid whole number");
        }
    }
}
