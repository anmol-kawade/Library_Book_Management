package com.library.model;

import java.time.LocalDate;

/**
 * Base Member class extending Person.
 * Implements polymorphism for specialized borrowing rules and fine rates.
 */
public abstract class Member extends Person {
    private LocalDate registrationDate;
    private int currentBorrowedCount;

    public Member(String id, String name, String contact, String email, LocalDate registrationDate, int currentBorrowedCount) {
        super(id, name, contact, email);
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
        this.currentBorrowedCount = currentBorrowedCount;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public int getCurrentBorrowedCount() {
        return currentBorrowedCount;
    }

    public void setCurrentBorrowedCount(int currentBorrowedCount) {
        this.currentBorrowedCount = currentBorrowedCount;
    }

    public void incrementBorrowedCount() {
        this.currentBorrowedCount++;
    }

    public void decrementBorrowedCount() {
        if (this.currentBorrowedCount > 0) {
            this.currentBorrowedCount--;
        }
    }

    public boolean canBorrow() {
        return currentBorrowedCount < getMaxBooksAllowed();
    }

    // Abstract methods demonstrating polymorphism in subclasses
    public abstract String getMembershipType();
    public abstract int getMaxBooksAllowed();
    public abstract int getLoanPeriodDays();
    public abstract double getDailyFineRate();

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | Name: %-18s | Phone: %-12s | Email: %-22s | Borrowed: %d/%d",
                getMembershipType(), getId(), getName(), getContact(), getEmail(), currentBorrowedCount, getMaxBooksAllowed());
    }

    /**
     * Converts Member details to CSV format line.
     */
    public String toCsv() {
        return String.join(",",
                getId(),
                escapeCsv(getName()),
                escapeCsv(getContact()),
                escapeCsv(getEmail()),
                getMembershipType(),
                getRegistrationDate().toString(),
                String.valueOf(currentBorrowedCount)
        );
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
