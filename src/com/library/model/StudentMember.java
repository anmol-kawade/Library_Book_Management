package com.library.model;

import java.time.LocalDate;

/**
 * Represents a Student member in the college library.
 * Specialized subclass with 14-day borrowing duration, max 3 books, and standard fine.
 */
public class StudentMember extends Member {
    public static final int MAX_BOOKS = 3;
    public static final int LOAN_PERIOD_DAYS = 14;
    public static final double DAILY_FINE_RATE = 5.0; // ₹5.00 per day

    public StudentMember(String id, String name, String contact, String email) {
        super(id, name, contact, email, LocalDate.now(), 0);
    }

    public StudentMember(String id, String name, String contact, String email, LocalDate regDate, int borrowedCount) {
        super(id, name, contact, email, regDate, borrowedCount);
    }

    @Override
    public String getMembershipType() {
        return "STUDENT";
    }

    @Override
    public int getMaxBooksAllowed() {
        return MAX_BOOKS;
    }

    @Override
    public int getLoanPeriodDays() {
        return LOAN_PERIOD_DAYS;
    }

    @Override
    public double getDailyFineRate() {
        return DAILY_FINE_RATE;
    }
}
