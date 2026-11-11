package com.library.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility methods for date manipulation, parsing, and fine calculations.
 */
public class DateUtils {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtils() {}

    /**
     * Parses a string in yyyy-MM-dd format into a LocalDate.
     * @param dateStr date string
     * @return LocalDate or null if invalid
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Formats a LocalDate into yyyy-MM-dd string.
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(FORMATTER) : "";
    }

    /**
     * Calculates overdue days between due date and actual return date.
     * Returns 0 if returned on or before due date.
     */
    public static long calculateOverdueDays(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || returnDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(dueDate, returnDate);
        return Math.max(0, days);
    }

    /**
     * Calculates overdue fine based on overdue days and daily fine rate.
     */
    public static double calculateFine(LocalDate dueDate, LocalDate returnDate, double dailyRate) {
        long overdueDays = calculateOverdueDays(dueDate, returnDate);
        return overdueDays * dailyRate;
    }
}
