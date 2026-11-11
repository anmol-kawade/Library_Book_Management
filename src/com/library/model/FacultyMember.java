package com.library.model;

import java.time.LocalDate;

/**
 * Represents a Faculty member in the college library.
 * Specialized subclass with 30-day borrowing duration, max 6 books, and reduced fine.
 */
public class FacultyMember extends Member {
    public static final int MAX_BOOKS = 6;
    public static final int LOAN_PERIOD_DAYS = 30;
    public static final double DAILY_FINE_RATE = 2.0; // ₹2.00 per day

    public FacultyMember(String id, String name, String contact, String email) {
        super(id, name, contact, email, LocalDate.now(), 0);
    }

    public FacultyMember(String id, String name, String contact, String email, LocalDate regDate, int borrowedCount) {
        super(id, name, contact, email, regDate, borrowedCount);
    }

    @Override
    public String getMembershipType() {
        return "FACULTY";
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
