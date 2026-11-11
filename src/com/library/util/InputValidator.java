package com.library.util;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Robust console input reader and validator.
 * Prevents input mismatch crashes, handles EOF safely, and ensures all fields satisfy business rules.
 */
public class InputValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{10,13}$");

    private InputValidator() {}

    private static String safeReadLine(Scanner scanner) {
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException ignored) {}
        System.out.println("\n[Console input closed. Exiting...]");
        System.exit(0);
        return "";
    }

    /**
     * Reads a non-empty string from console.
     */
    public static String readString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = safeReadLine(scanner).trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("[!] Input cannot be empty. Please try again.");
        }
    }

    /**
     * Reads a string from console, allowing empty input if a default is provided.
     */
    public static String readOptionalString(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt + (defaultValue != null ? " [" + defaultValue + "]: " : ": "));
        String input = safeReadLine(scanner).trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        return input;
    }

    /**
     * Reads an integer within [min, max] range.
     */
    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = safeReadLine(scanner).trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("[!] Value must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input! Please enter a valid number.");
            }
        }
    }

    /**
     * Reads a valid phone number.
     */
    public static String readPhoneNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String phone = safeReadLine(scanner).trim();
            if (phone.isEmpty()) {
                System.out.println("[!] Phone number cannot be empty.");
                continue;
            }
            String cleanPhone = phone.replaceAll("[\\s-]", "");
            if (PHONE_PATTERN.matcher(cleanPhone).matches()) {
                return cleanPhone;
            }
            System.out.println("[!] Invalid phone number format! (Must be 10-13 digits, optional + prefix).");
        }
    }

    /**
     * Reads a valid email address.
     */
    public static String readEmail(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String email = safeReadLine(scanner).trim();
            if (email.isEmpty()) {
                System.out.println("[!] Email cannot be empty.");
                continue;
            }
            if (EMAIL_PATTERN.matcher(email).matches()) {
                return email;
            }
            System.out.println("[!] Invalid email format! (Example: user@college.edu).");
        }
    }

    /**
     * Reads a date in yyyy-MM-dd format or accepts empty string for today's date.
     */
    public static LocalDate readDate(Scanner scanner, String prompt, boolean defaultToday) {
        while (true) {
            String fullPrompt = prompt + (defaultToday ? " (Enter for Today: " + LocalDate.now() + "): " : ": ");
            System.out.print(fullPrompt);
            String input = safeReadLine(scanner).trim();
            if (input.isEmpty() && defaultToday) {
                return LocalDate.now();
            }
            LocalDate date = DateUtils.parseDate(input);
            if (date != null) {
                return date;
            }
            System.out.println("[!] Invalid date format! Please use YYYY-MM-DD (e.g., 2026-11-02).");
        }
    }

    /**
     * Reads a Yes/No confirmation from user.
     */
    public static boolean readConfirmation(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = safeReadLine(scanner).trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("[!] Please enter 'y' for yes or 'n' for no.");
        }
    }
}
