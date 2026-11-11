package com.library.service;

import com.library.exception.*;
import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.model.Member;
import com.library.util.DateUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing book issue and return transactions.
 * Enforces business rules: availability check, borrowing limits, due date calculation, and overdue fines.
 */
public class LendingService {
    private final List<IssueRecord> issueRecords;
    private final BookService bookService;
    private final MemberService memberService;

    public LendingService(List<IssueRecord> initialRecords, BookService bookService, MemberService memberService) {
        this.issueRecords = new ArrayList<>(initialRecords);
        this.bookService = bookService;
        this.memberService = memberService;
    }

    /**
     * Issues a book to a member with validation checks.
     */
    public IssueRecord issueBook(String bookId, String memberId, LocalDate issueDate)
            throws BookNotFoundException, MemberNotFoundException, BookNotAvailableException, MaxBooksLimitExceededException {

        Book book = bookService.getBookById(bookId);
        Member member = memberService.getMemberById(memberId);

        // Check availability
        if (!book.isAvailable()) {
            throw new BookNotAvailableException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") is currently checked out.");
        }

        // Check member borrow limit
        if (!member.canBorrow()) {
            throw new MaxBooksLimitExceededException("Member '" + member.getName() + "' (ID: " + member.getId() +
                    ") has reached the maximum borrowing limit of " + member.getMaxBooksAllowed() + " book(s).");
        }

        LocalDate safeIssueDate = issueDate != null ? issueDate : LocalDate.now();
        LocalDate dueDate = safeIssueDate.plusDays(member.getLoanPeriodDays());
        String recordId = generateNextRecordId();

        IssueRecord record = new IssueRecord(
                recordId,
                book.getId(),
                book.getTitle(),
                member.getId(),
                member.getName(),
                safeIssueDate,
                dueDate
        );

        // Update domain state
        book.setAvailable(false);
        member.incrementBorrowedCount();
        issueRecords.add(record);

        return record;
    }

    /**
     * Returns a currently issued book, updates status, and calculates overdue fines if applicable.
     */
    public IssueRecord returnBook(String bookId, LocalDate returnDate)
            throws BookNotFoundException, InvalidReturnException, MemberNotFoundException {

        Book book = bookService.getBookById(bookId);

        // Locate active issue record for this book
        Optional<IssueRecord> activeRecordOpt = issueRecords.stream()
                .filter(r -> r.getBookId().equalsIgnoreCase(book.getId()) && IssueRecord.STATUS_ISSUED.equals(r.getStatus()))
                .findFirst();

        if (!activeRecordOpt.isPresent()) {
            if (book.isAvailable()) {
                throw new InvalidReturnException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") is already in library inventory and was not issued.");
            } else {
                throw new InvalidReturnException("No active issue record found for book ID: " + book.getId());
            }
        }

        IssueRecord record = activeRecordOpt.get();
        Member member = memberService.getMemberById(record.getMemberId());

        LocalDate safeReturnDate = returnDate != null ? returnDate : LocalDate.now();
        if (safeReturnDate.isBefore(record.getIssueDate())) {
            throw new InvalidReturnException("Return date cannot be earlier than the issue date (" + record.getIssueDate() + ").");
        }

        // Calculate fine if overdue
        double fine = DateUtils.calculateFine(record.getDueDate(), safeReturnDate, member.getDailyFineRate());

        record.setReturnDate(safeReturnDate);
        record.setFineAmount(fine);
        record.setStatus(IssueRecord.STATUS_RETURNED);

        // Update domain state
        book.setAvailable(true);
        member.decrementBorrowedCount();

        return record;
    }

    /**
     * Returns all active (unreturned) issue records.
     */
    public List<IssueRecord> getActiveIssues() {
        return issueRecords.stream()
                .filter(r -> IssueRecord.STATUS_ISSUED.equals(r.getStatus()))
                .sorted(Comparator.comparing(IssueRecord::getDueDate))
                .collect(Collectors.toList());
    }

    /**
     * Returns all issue records for a specific member.
     */
    public List<IssueRecord> getIssuesForMember(String memberId) {
        String query = memberId.trim().toUpperCase();
        return issueRecords.stream()
                .filter(r -> r.getMemberId().equalsIgnoreCase(query))
                .sorted(Comparator.comparing(IssueRecord::getIssueDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns all issue records (active and history).
     */
    public List<IssueRecord> getAllIssues() {
        return Collections.unmodifiableList(issueRecords);
    }

    /**
     * Automatically generates a sequential record ID (e.g. REC-1001, REC-1002).
     */
    public String generateNextRecordId() {
        int maxId = 1000;
        for (IssueRecord record : issueRecords) {
            String rid = record.getRecordId();
            if (rid != null && rid.startsWith("REC-")) {
                try {
                    int num = Integer.parseInt(rid.substring(4));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "REC-" + (maxId + 1);
    }
}
