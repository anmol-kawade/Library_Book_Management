package com.library.test;

import com.library.exception.*;
import com.library.model.*;
import com.library.service.*;
import com.library.storage.FileStorageService;
import com.library.util.DateUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

/**
 * Automated Test Runner verifying all functional requirements, edge cases,
 * polymorphism, exception handling, and file persistence.
 */
public class LibraryTestRunner {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("       RUNNING AUTOMATED TEST SUITE FOR LIBRARY SYSTEM          ");
        System.out.println("================================================================\n");

        testBookOperations();
        testMemberPolymorphismAndOperations();
        testBookLendingFlow();
        testEdgeCaseUnavailableBook();
        testEdgeCaseQuotaExceeded();
        testEdgeCaseInvalidReturn();
        testOverdueFineCalculation();
        testLambdaExpressionsAndReports();
        testFilePersistenceReload();

        System.out.println("\n================================================================");
        System.out.printf("TEST RESULTS: %d PASSED, %d FAILED\n", testsPassed, testsFailed);
        System.out.println("================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void testBookOperations() {
        System.out.println("Test Group 1: Book CRUD Operations");
        BookService bookService = new BookService(new HashMap<>());
        try {
            Book b1 = new Book("B201", "Core Java Advanced", "Cay Horstmann", "Programming", true);
            bookService.addBook(b1);
            assertTrue("Book added and exists", bookService.exists("B201"));

            Book retrieved = bookService.getBookById("B201");
            assertTrue("Retrieved book title matches", "Core Java Advanced".equals(retrieved.getTitle()));

            bookService.editBook("B201", "Core Java Volume 1", "Cay S. Horstmann", "Programming");
            assertTrue("Book title updated", "Core Java Volume 1".equals(bookService.getBookById("B201").getTitle()));

            bookService.removeBook("B201");
            assertTrue("Book removed successfully", !bookService.exists("B201"));
        } catch (Exception e) {
            assertTrue("Book operations threw unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void testMemberPolymorphismAndOperations() {
        System.out.println("\nTest Group 2: Member Polymorphism & Registration");
        Member student = new StudentMember("S901", "Aakash Gupta", "9812345678", "aakash@test.com");
        Member faculty = new FacultyMember("F901", "Dr. Sharma", "9876543210", "sharma@test.com");

        assertTrue("Student max books allowed is 3", student.getMaxBooksAllowed() == 3);
        assertTrue("Student loan period is 14 days", student.getLoanPeriodDays() == 14);
        assertTrue("Student daily fine rate is ₹5.0", student.getDailyFineRate() == 5.0);

        assertTrue("Faculty max books allowed is 6", faculty.getMaxBooksAllowed() == 6);
        assertTrue("Faculty loan period is 30 days", faculty.getLoanPeriodDays() == 30);
        assertTrue("Faculty daily fine rate is ₹2.0", faculty.getDailyFineRate() == 2.0);

        MemberService memberService = new MemberService(new HashMap<>());
        try {
            memberService.registerMember(student);
            memberService.registerMember(faculty);
            assertTrue("Members registered in service", memberService.getAllMembers().size() == 2);
        } catch (Exception e) {
            assertTrue("Member registration failed: " + e.getMessage(), false);
        }
    }

    private static void testBookLendingFlow() {
        System.out.println("\nTest Group 3: Normal Issue & Return Lending Flow");
        Map<String, Book> books = new HashMap<>();
        Book book = new Book("B301", "Algorithms in Java", "Robert Sedgewick", "Computer Science", true);
        books.put(book.getId(), book);

        Map<String, Member> members = new HashMap<>();
        Member student = new StudentMember("S301", "Neha Rao", "9898989898", "neha@test.com");
        members.put(student.getId(), student);

        BookService bookService = new BookService(books);
        MemberService memberService = new MemberService(members);
        LendingService lendingService = new LendingService(new ArrayList<>(), bookService, memberService);

        try {
            LocalDate issueDate = LocalDate.now();
            IssueRecord issue = lendingService.issueBook("B301", "S301", issueDate);

            assertTrue("Book availability updated to false after issue", !book.isAvailable());
            assertTrue("Member borrowed count incremented", student.getCurrentBorrowedCount() == 1);
            assertTrue("Issue record status is ISSUED", IssueRecord.STATUS_ISSUED.equals(issue.getStatus()));
            assertTrue("Due date correctly set to issueDate + 14 days", issue.getDueDate().equals(issueDate.plusDays(14)));

            IssueRecord returnRecord = lendingService.returnBook("B301", issueDate.plusDays(7));
            assertTrue("Book availability updated to true after return", book.isAvailable());
            assertTrue("Member borrowed count decremented to 0", student.getCurrentBorrowedCount() == 0);
            assertTrue("Issue record status is RETURNED", IssueRecord.STATUS_RETURNED.equals(returnRecord.getStatus()));
            assertTrue("No fine on timely return", returnRecord.getFineAmount() == 0.0);
        } catch (Exception e) {
            assertTrue("Lending flow threw unexpected error: " + e.getMessage(), false);
        }
    }

    private static void testEdgeCaseUnavailableBook() {
        System.out.println("\nTest Group 4: Edge Case - Issuing Unavailable Book");
        Map<String, Book> books = new HashMap<>();
        Book book = new Book("B401", "System Design", "Alex Xu", "Architecture", false); // already checked out
        books.put(book.getId(), book);

        Map<String, Member> members = new HashMap<>();
        Member student = new StudentMember("S401", "Karan Johar", "9123456789", "karan@test.com");
        members.put(student.getId(), student);

        LendingService lendingService = new LendingService(new ArrayList<>(), new BookService(books), new MemberService(members));

        try {
            lendingService.issueBook("B401", "S401", LocalDate.now());
            assertTrue("Expected BookNotAvailableException not thrown!", false);
        } catch (BookNotAvailableException e) {
            assertTrue("Caught BookNotAvailableException as expected: " + e.getMessage(), true);
        } catch (Exception e) {
            assertTrue("Unexpected exception caught: " + e.getClass().getName(), false);
        }
    }

    private static void testEdgeCaseQuotaExceeded() {
        System.out.println("\nTest Group 5: Edge Case - Borrowing Quota Limit Exceeded");
        Map<String, Book> books = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            books.put("B50" + i, new Book("B50" + i, "Book " + i, "Author " + i, "Tech", true));
        }

        Map<String, Member> members = new HashMap<>();
        StudentMember student = new StudentMember("S501", "Limit Tester", "9000000000", "limit@test.com");
        members.put(student.getId(), student);

        BookService bookService = new BookService(books);
        MemberService memberService = new MemberService(members);
        LendingService lendingService = new LendingService(new ArrayList<>(), bookService, memberService);

        try {
            // Student limit is 3 books
            lendingService.issueBook("B501", "S501", LocalDate.now());
            lendingService.issueBook("B502", "S501", LocalDate.now());
            lendingService.issueBook("B503", "S501", LocalDate.now());
            assertTrue("Issued 3 books successfully", student.getCurrentBorrowedCount() == 3);

            // 4th issue should throw MaxBooksLimitExceededException
            lendingService.issueBook("B504", "S501", LocalDate.now());
            assertTrue("Expected MaxBooksLimitExceededException was not thrown!", false);
        } catch (MaxBooksLimitExceededException e) {
            assertTrue("Caught MaxBooksLimitExceededException as expected: " + e.getMessage(), true);
        } catch (Exception e) {
            assertTrue("Unexpected exception caught: " + e.getClass().getName(), false);
        }
    }

    private static void testEdgeCaseInvalidReturn() {
        System.out.println("\nTest Group 6: Edge Case - Returning Book That Was Never Issued");
        Map<String, Book> books = new HashMap<>();
        Book book = new Book("B601", "Unissued Book", "Author X", "Tech", true);
        books.put(book.getId(), book);

        BookService bookService = new BookService(books);
        MemberService memberService = new MemberService(new HashMap<>());
        LendingService lendingService = new LendingService(new ArrayList<>(), bookService, memberService);

        try {
            lendingService.returnBook("B601", LocalDate.now());
            assertTrue("Expected InvalidReturnException was not thrown!", false);
        } catch (InvalidReturnException e) {
            assertTrue("Caught InvalidReturnException as expected: " + e.getMessage(), true);
        } catch (Exception e) {
            assertTrue("Unexpected exception caught: " + e.getClass().getName(), false);
        }
    }

    private static void testOverdueFineCalculation() {
        System.out.println("\nTest Group 7: Overdue Fine Calculation (Student ₹5/day, Faculty ₹2/day)");
        Map<String, Book> books = new HashMap<>();
        Book b1 = new Book("B701", "Student Overdue Book", "Author A", "Tech", true);
        Book b2 = new Book("B702", "Faculty Overdue Book", "Author B", "Tech", true);
        books.put(b1.getId(), b1);
        books.put(b2.getId(), b2);

        Map<String, Member> members = new HashMap<>();
        Member s = new StudentMember("S701", "Late Student", "9111111111", "student@test.com");
        Member f = new FacultyMember("F701", "Late Faculty", "9222222222", "faculty@test.com");
        members.put(s.getId(), s);
        members.put(f.getId(), f);

        BookService bookService = new BookService(books);
        MemberService memberService = new MemberService(members);
        LendingService lendingService = new LendingService(new ArrayList<>(), bookService, memberService);

        try {
            LocalDate baseDate = LocalDate.of(2026, 1, 1);
            // Student issue: due after 14 days = Jan 15
            lendingService.issueBook("B701", "S701", baseDate);

            // Faculty issue: due after 30 days = Jan 31
            lendingService.issueBook("B702", "F701", baseDate);

            // Return Student book on Jan 20 (5 days overdue -> 5 * 5 = ₹25.0)
            LocalDate studentReturnDate = LocalDate.of(2026, 1, 20);
            IssueRecord r1 = lendingService.returnBook("B701", studentReturnDate);
            long studentOverdueDays = DateUtils.calculateOverdueDays(r1.getDueDate(), studentReturnDate);
            assertTrue("Student overdue days is 5", studentOverdueDays == 5);
            assertTrue("Student fine is ₹25.0", Math.abs(r1.getFineAmount() - 25.0) < 0.001);

            // Return Faculty book on Feb 10 (10 days overdue -> 10 * 2 = ₹20.0)
            LocalDate facultyReturnDate = LocalDate.of(2026, 2, 10);
            IssueRecord r2 = lendingService.returnBook("B702", facultyReturnDate);
            long facultyOverdueDays = DateUtils.calculateOverdueDays(r2.getDueDate(), facultyReturnDate);
            assertTrue("Faculty overdue days is 10", facultyOverdueDays == 10);
            assertTrue("Faculty fine is ₹20.0", Math.abs(r2.getFineAmount() - 20.0) < 0.001);

        } catch (Exception e) {
            assertTrue("Fine calculation test failed: " + e.getMessage(), false);
        }
    }

    private static void testLambdaExpressionsAndReports() {
        System.out.println("\nTest Group 8: Lambda Expressions, Filtering & Reporting");
        Map<String, Book> books = new HashMap<>();
        books.put("B1", new Book("B1", "Java in Action", "Raoul-Gabriel Urma", "Java", true));
        books.put("B2", new Book("B2", "Modern Java", "Joshua Bloch", "Java", false));
        books.put("B3", new Book("B3", "Python Crash Course", "Eric Matthes", "Python", true));

        BookService bookService = new BookService(books);

        // Filter using lambda
        List<Book> javaBooks = bookService.filterBooks(b -> b.getCategory().equalsIgnoreCase("Java"));
        assertTrue("Lambda filtered 2 Java books", javaBooks.size() == 2);

        List<Book> availableBooks = bookService.filterByAvailability(true);
        assertTrue("Lambda filtered 2 available books", availableBooks.size() == 2);

        // Streams sorting
        List<Book> sortedByTitle = bookService.getAllBooks();
        assertTrue("Books list non-empty", !sortedByTitle.isEmpty());
    }

    private static void testFilePersistenceReload() {
        System.out.println("\nTest Group 9: File Persistence and Cross-run Reload Verification");
        FileStorageService storage = new FileStorageService();

        Map<String, Book> books = new HashMap<>();
        books.put("BP1", new Book("BP1", "Persistence Book 1", "Persistent Author", "Storage", true));
        books.put("BP2", new Book("BP2", "Persistence Book 2", "Persistent Author", "Storage", false));

        Map<String, Member> members = new HashMap<>();
        members.put("SP1", new StudentMember("SP1", "Persistent Student", "9998887776", "pers@test.com"));
        members.put("FP1", new FacultyMember("FP1", "Persistent Faculty", "8887776665", "prof.pers@test.com"));

        List<IssueRecord> issues = new ArrayList<>();
        issues.add(new IssueRecord("REC-P1", "BP2", "Persistence Book 2", "SP1", "Persistent Student",
                LocalDate.now().minusDays(3), LocalDate.now().plusDays(11)));

        // Save
        storage.saveBooks(books.values());
        storage.saveMembers(members.values());
        storage.saveIssues(issues);

        // Reload fresh
        Map<String, Book> reloadedBooks = storage.loadBooks();
        Map<String, Member> reloadedMembers = storage.loadMembers();
        List<IssueRecord> reloadedIssues = storage.loadIssues();

        assertTrue("Reloaded books contain BP1", reloadedBooks.containsKey("BP1"));
        assertTrue("Reloaded book BP2 availability preserved as false", !reloadedBooks.get("BP2").isAvailable());
        assertTrue("Reloaded member SP1 is instance of StudentMember", reloadedMembers.get("SP1") instanceof StudentMember);
        assertTrue("Reloaded member FP1 is instance of FacultyMember", reloadedMembers.get("FP1") instanceof FacultyMember);
        assertTrue("Reloaded issues contains REC-P1", reloadedIssues.stream().anyMatch(i -> "REC-P1".equals(i.getRecordId())));
    }
}
