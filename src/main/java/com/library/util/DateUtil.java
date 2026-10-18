package com.library.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility functions for date calculations, conversions, and human-friendly formatting.
 */
public class DateUtil {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HEADER_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy EEEE");

    public static Date today() {
        return Date.valueOf(LocalDate.now());
    }

    public static Date addDays(Date date, int days) {
        LocalDate local = date.toLocalDate().plusDays(days);
        return Date.valueOf(local);
    }

    public static String formatDisplay(Date date) {
        if (date == null) return "-";
        return date.toLocalDate().format(DISPLAY_FORMATTER);
    }

    public static String formatIso(Date date) {
        if (date == null) return "";
        return date.toLocalDate().format(ISO_FORMATTER);
    }

    public static String formatHeaderDate(LocalDate date) {
        if (date == null) return "";
        return date.format(HEADER_DATE_FORMATTER);
    }

    public static Date parseDisplay(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Date.valueOf(LocalDate.parse(str.trim(), DISPLAY_FORMATTER));
        } catch (Exception e) {
            try {
                return Date.valueOf(LocalDate.parse(str.trim(), ISO_FORMATTER));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static int calculateOverdueDays(Date dueDate, Date returnDate) {
        if (dueDate == null) return 0;
        LocalDate due = dueDate.toLocalDate();
        LocalDate ret = (returnDate != null) ? returnDate.toLocalDate() : LocalDate.now();

        long diff = ChronoUnit.DAYS.between(due, ret);
        return diff > 0 ? (int) diff : 0;
    }

    public static String getRelativeTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        long now = System.currentTimeMillis();
        long diffMs = now - timestamp.getTime();

        long seconds = diffMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " min" + (minutes > 1 ? "s" : "") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            return new SimpleDateFormat("dd MMM yyyy").format(timestamp);
        }
    }
}
