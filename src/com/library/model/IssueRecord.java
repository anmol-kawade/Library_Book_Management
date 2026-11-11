package com.library.model;

import java.time.LocalDate;

/**
 * Represents a transaction record of a book issued to a member.
 * Tracks loan duration, due date, return date, and calculated fine.
 */
public class IssueRecord {
    public static final String STATUS_ISSUED = "ISSUED";
    public static final String STATUS_RETURNED = "RETURNED";

    private String recordId;
    private String bookId;
    private String bookTitle;
    private String memberId;
    private String memberName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fineAmount;
    private String status;

    public IssueRecord(String recordId, String bookId, String bookTitle, String memberId, String memberName,
                       LocalDate issueDate, LocalDate dueDate) {
        this.recordId = recordId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.memberId = memberId;
        this.memberName = memberName;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.fineAmount = 0.0;
        this.status = STATUS_ISSUED;
    }

    public IssueRecord(String recordId, String bookId, String bookTitle, String memberId, String memberName,
                       LocalDate issueDate, LocalDate dueDate, LocalDate returnDate, double fineAmount, String status) {
        this.recordId = recordId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.memberId = memberId;
        this.memberName = memberName;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.status = status;
    }

    // Getters and Setters
    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOverdue(LocalDate referenceDate) {
        if (STATUS_ISSUED.equals(status)) {
            return referenceDate.isAfter(dueDate);
        }
        return false;
    }

    @Override
    public String toString() {
        String retDateStr = returnDate != null ? returnDate.toString() : "Not Returned";
        return String.format("Rec: %-8s | Book: [%s] %-20s | Member: [%s] %-15s | Issued: %s | Due: %s | Ret: %-12s | Status: %-8s | Fine: ₹%.2f",
                recordId,
                bookId,
                bookTitle.length() > 20 ? bookTitle.substring(0, 17) + "..." : bookTitle,
                memberId,
                memberName.length() > 15 ? memberName.substring(0, 12) + "..." : memberName,
                issueDate,
                dueDate,
                retDateStr,
                status,
                fineAmount);
    }

    /**
     * Converts IssueRecord to CSV format.
     */
    public String toCsv() {
        return String.join(",",
                recordId,
                bookId,
                escapeCsv(bookTitle),
                memberId,
                escapeCsv(memberName),
                issueDate.toString(),
                dueDate.toString(),
                returnDate != null ? returnDate.toString() : "",
                String.format("%.2f", fineAmount),
                status
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
