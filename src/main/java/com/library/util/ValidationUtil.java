package com.library.util;

import java.util.regex.Pattern;

/**
 * Validates inputs across the application.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9\\s-]{7,15}$"
    );

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidIsbn(String isbn) {
        if (!isNotEmpty(isbn)) {
            return true; // ISBN can be optional in some contexts
        }
        String clean = isbn.replaceAll("[-\\s]", "");
        return clean.length() == 10 || clean.length() == 13;
    }

    public static boolean isPositiveInt(String val) {
        if (!isNotEmpty(val)) {
            return false;
        }
        try {
            int num = Integer.parseInt(val.trim());
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeInt(String val) {
        if (!isNotEmpty(val)) {
            return false;
        }
        try {
            int num = Integer.parseInt(val.trim());
            return num >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
