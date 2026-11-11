package com.library.service;

import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.model.Member;
import com.library.util.DateUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating reports and statistics using Java Streams and Lambda expressions.
 * Implements Module 6 requirements.
 */
public class ReportService {
    private final BookService bookService;
    private final MemberService memberService;
    private final LendingService lendingService;

    public ReportService(BookService bookService, MemberService memberService, LendingService lendingService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.lendingService = lendingService;
    }

    /**
     * DTO for representing an overdue loan item.
     */
    public static class OverdueItem {
        private final IssueRecord record;
        private final Member member;
        private final long overdueDays;
        private final double estimatedFine;

        public OverdueItem(IssueRecord record, Member member, long overdueDays, double estimatedFine) {
            this.record = record;
            this.member = member;
            this.overdueDays = overdueDays;
            this.estimatedFine = estimatedFine;
        }

        public IssueRecord getRecord() {
            return record;
        }

        public Member getMember() {
            return member;
        }

        public long getOverdueDays() {
            return overdueDays;
        }

        public double getEstimatedFine() {
            return estimatedFine;
        }
    }

    /**
     * Generates a list of all currently overdue active loans.
     * Uses Lambda expressions with filter, map, and sorted operations.
     */
    public List<OverdueItem> getOverdueReport(LocalDate asOfDate) {
        LocalDate referenceDate = asOfDate != null ? asOfDate : LocalDate.now();

        return lendingService.getActiveIssues().stream()
                .filter(record -> record.getDueDate().isBefore(referenceDate))
                .map(record -> {
                    Member member = null;
                    try {
                        member = memberService.getMemberById(record.getMemberId());
                    } catch (Exception ignored) {}
                    double rate = member != null ? member.getDailyFineRate() : 5.0;
                    long overdueDays = DateUtils.calculateOverdueDays(record.getDueDate(), referenceDate);
                    double fine = DateUtils.calculateFine(record.getDueDate(), referenceDate, rate);
                    return new OverdueItem(record, member, overdueDays, fine);
                })
                .sorted(Comparator.comparingLong(OverdueItem::getOverdueDays).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns full transaction issue history sorted in reverse chronological order using lambdas.
     */
    public List<IssueRecord> getIssueHistorySortedByDate() {
        return lendingService.getAllIssues().stream()
                .sorted(Comparator.comparing(IssueRecord::getIssueDate).reversed()
                        .thenComparing(IssueRecord::getRecordId).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Computes total fine collected from completed returns using mapToDouble lambda reduction.
     */
    public double getTotalFinesCollected() {
        return lendingService.getAllIssues().stream()
                .filter(r -> IssueRecord.STATUS_RETURNED.equals(r.getStatus()))
                .mapToDouble(IssueRecord::getFineAmount)
                .sum();
    }

    /**
     * Retrieves members who currently hold active loans using lambdas.
     */
    public List<Member> getMembersWithActiveLoans() {
        return memberService.getAllMembers().stream()
                .filter(m -> m.getCurrentBorrowedCount() > 0)
                .sorted(Comparator.comparingInt(Member::getCurrentBorrowedCount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Prints an executive library summary snapshot.
     */
    public void printLibrarySummary() {
        long totalBooks = bookService.getAllBooks().size();
        long availableBooks = bookService.filterByAvailability(true).size();
        long issuedBooks = totalBooks - availableBooks;

        long totalMembers = memberService.getAllMembers().size();
        long studentCount = memberService.filterMembers(m -> "STUDENT".equalsIgnoreCase(m.getMembershipType())).size();
        long facultyCount = memberService.filterMembers(m -> "FACULTY".equalsIgnoreCase(m.getMembershipType())).size();

        long activeLoans = lendingService.getActiveIssues().size();
        long totalTransactions = lendingService.getAllIssues().size();
        double totalFines = getTotalFinesCollected();

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                LIBRARY EXECUTIVE SUMMARY                     ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  📚 Total Books Cataloged:       %-28d ║\n", totalBooks);
        System.out.printf("║     - Available for Loan:        %-28d ║\n", availableBooks);
        System.out.printf("║     - Currently Issued:          %-28d ║\n", issuedBooks);
        System.out.println("║                                                              ║");
        System.out.printf("║  👥 Total Registered Members:    %-28d ║\n", totalMembers);
        System.out.printf("║     - Student Members:           %-28d ║\n", studentCount);
        System.out.printf("║     - Faculty Members:           %-28d ║\n", facultyCount);
        System.out.println("║                                                              ║");
        System.out.printf("║  🔄 Active Loans:                %-28d ║\n", activeLoans);
        System.out.printf("║  📜 Total Transaction Records:   %-28d ║\n", totalTransactions);
        System.out.printf("║  💰 Total Overdue Fines Paid:    ₹%-27.2f ║\n", totalFines);
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
}
