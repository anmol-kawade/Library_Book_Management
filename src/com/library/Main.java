package com.library;

import com.library.exception.*;
import com.library.model.*;
import com.library.service.*;
import com.library.storage.FileStorageService;
import com.library.util.DateUtils;
import com.library.util.InputValidator;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * Main application entry point for the College Library Book Lending System.
 * Integrates all modules with an interactive, menu-driven console interface.
 */
public class Main {
    private static Scanner scanner;
    private static FileStorageService storageService;
    private static BookService bookService;
    private static MemberService memberService;
    private static LendingService lendingService;
    private static ReportService reportService;

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        } catch (Exception ignored) {}

        scanner = new Scanner(System.in);
        initializeApplication();

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = InputValidator.readInt(scanner, "Enter your choice (1-6): ", 1, 6);
            System.out.println();

            switch (choice) {
                case 1:
                    handleBookManagementMenu();
                    break;
                case 2:
                    handleMemberManagementMenu();
                    break;
                case 3:
                    handleLendingOperationsMenu();
                    break;
                case 4:
                    handleReportsMenu();
                    break;
                case 5:
                    handleDataStorageMenu();
                    break;
                case 6:
                    exitApplication();
                    running = false;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please select from the menu.");
            }
        }
    }

    private static void initializeApplication() {
        System.out.println("+==============================================================+");
        System.out.println("|           COLLEGE LIBRARY BOOK LENDING SYSTEM v1.0           |");
        System.out.println("+==============================================================+");
        System.out.println("Initializing data stores and loading catalog...");

        storageService = new FileStorageService();
        Map<String, Book> books = storageService.loadBooks();
        Map<String, Member> members = storageService.loadMembers();
        List<IssueRecord> issues = storageService.loadIssues();

        if (books.isEmpty() && members.isEmpty()) {
            System.out.println("[*] No existing data files found. Seeding initial library catalog...");
            seedDefaultData(books, members, issues);
            storageService.saveBooks(books.values());
            storageService.saveMembers(members.values());
            storageService.saveIssues(issues);
        }

        bookService = new BookService(books);
        memberService = new MemberService(members);
        lendingService = new LendingService(issues, bookService, memberService);
        reportService = new ReportService(bookService, memberService, lendingService);

        System.out.printf("[+] System ready: %d Books, %d Members, %d Transaction Records loaded.\n\n",
                bookService.getAllBooks().size(),
                memberService.getAllMembers().size(),
                lendingService.getAllIssues().size());
    }

    public static void seedDefaultData(Map<String, Book> books, Map<String, Member> members, List<IssueRecord> issues) {
        // Sample Books
        books.put("B101", new Book("B101", "Clean Code", "Robert C. Martin", "Software Eng", true));
        books.put("B102", new Book("B102", "Introduction to Algorithms", "Thomas H. Cormen", "Computer Science", true));
        books.put("B103", new Book("B103", "Effective Java", "Joshua Bloch", "Programming", false));
        books.put("B104", new Book("B104", "Design Patterns", "Erich Gamma", "Software Eng", true));
        books.put("B105", new Book("B105", "The Pragmatic Programmer", "Andrew Hunt", "Software Eng", false));
        books.put("B106", new Book("B106", "Database System Concepts", "Abraham Silberschatz", "Databases", true));
        books.put("B107", new Book("B107", "Computer Networks", "Andrew S. Tanenbaum", "Networking", true));
        books.put("B108", new Book("B108", "Operating System Concepts", "Peter B. Galvin", "Operating Systems", true));

        // Sample Members
        LocalDate pastDate = LocalDate.now().minusMonths(2);
        members.put("S101", new StudentMember("S101", "Aarav Sharma", "9876543210", "aarav@college.edu", pastDate, 1));
        members.put("S102", new StudentMember("S102", "Priya Patel", "9812345678", "priya@college.edu", pastDate, 1));
        members.put("S103", new StudentMember("S103", "Rohan Verma", "9900112233", "rohan@college.edu", pastDate, 0));
        members.put("F101", new FacultyMember("F101", "Dr. Sneha Kulkarni", "9765432100", "sneha.k@college.edu", pastDate, 0));
        members.put("F102", new FacultyMember("F102", "Prof. Rajesh Mehta", "9845098450", "rajesh.m@college.edu", pastDate, 0));

        // Sample Issue Records (one normal active loan, one overdue active loan for demonstration)
        LocalDate today = LocalDate.now();
        // B103 borrowed by S101 (issued 5 days ago, due in 9 days -> not overdue)
        issues.add(new IssueRecord("REC-1001", "B103", "Effective Java", "S101", "Aarav Sharma",
                today.minusDays(5), today.plusDays(9)));

        // B105 borrowed by S102 (issued 20 days ago, was due 6 days ago -> overdue by 6 days!)
        issues.add(new IssueRecord("REC-1002", "B105", "The Pragmatic Programmer", "S102", "Priya Patel",
                today.minusDays(20), today.minusDays(6)));

        // Returned record
        issues.add(new IssueRecord("REC-1000", "B101", "Clean Code", "S103", "Rohan Verma",
                today.minusDays(30), today.minusDays(16), today.minusDays(15), 5.0, IssueRecord.STATUS_RETURNED));
    }

    private static void displayMainMenu() {
        System.out.println("+--------------------------------------------------------+");
        System.out.println("|                       MAIN MENU                        |");
        System.out.println("+--------------------------------------------------------+");
        System.out.println("|  1. [Book] Catalog Management                          |");
        System.out.println("|  2. [Member] Registration & Directory                  |");
        System.out.println("|  3. [Lending] Operations (Issue & Return)              |");
        System.out.println("|  4. [Reports] Overdue Fines & Lambda Analytics         |");
        System.out.println("|  5. [Storage] File Persistence & Reset                 |");
        System.out.println("|  6. [Exit] Save & Exit Application                     |");
        System.out.println("+--------------------------------------------------------+");
    }

    // =========================================================================
    // Module 4: Book Management
    // =========================================================================

    private static void handleBookManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [Book Catalog Management] ---");
            System.out.println("1. List All Books");
            System.out.println("2. Search Books (Title / Author / Category)");
            System.out.println("3. Add New Book");
            System.out.println("4. Edit Book Details");
            System.out.println("5. Remove Book");
            System.out.println("6. Filter by Availability (Available / Issued)");
            System.out.println("7. Back to Main Menu");

            int opt = InputValidator.readInt(scanner, "Choose an option (1-7): ", 1, 7);
            System.out.println();
            switch (opt) {
                case 1:
                    listAllBooks();
                    break;
                case 2:
                    searchBooksMenu();
                    break;
                case 3:
                    addNewBook();
                    break;
                case 4:
                    editBook();
                    break;
                case 5:
                    removeBook();
                    break;
                case 6:
                    filterBooksByAvailability();
                    break;
                case 7:
                    back = true;
                    break;
            }
        }
    }

    private static void listAllBooks() {
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("[*] No books found in the library catalog.");
            return;
        }
        System.out.println("============================== BOOK CATALOG ==============================");
        for (Book b : books) {
            System.out.println(b);
        }
        System.out.println("==========================================================================");
        System.out.printf("Total: %d book(s) in catalog.\n", books.size());
    }

    private static void searchBooksMenu() {
        System.out.println("Search by: 1. Title  2. Author  3. Category");
        int sub = InputValidator.readInt(scanner, "Choose (1-3): ", 1, 3);
        String term = InputValidator.readString(scanner, "Enter search keyword: ");

        List<Book> results;
        if (sub == 1) {
            results = bookService.searchByTitle(term);
        } else if (sub == 2) {
            results = bookService.searchByAuthor(term);
        } else {
            results = bookService.searchByCategory(term);
        }

        if (results.isEmpty()) {
            System.out.println("[-] No matching books found for '" + term + "'.");
        } else {
            System.out.println("Search Results (" + results.size() + " found):");
            results.forEach(System.out::println);
        }
    }

    private static void addNewBook() {
        System.out.println("--- Add New Book ---");
        String suggestedId = bookService.generateNextBookId();
        String id = InputValidator.readOptionalString(scanner, "Enter Book ID", suggestedId).toUpperCase();
        if (bookService.exists(id)) {
            System.out.println("[-] Error: A book with ID '" + id + "' already exists!");
            return;
        }
        String title = InputValidator.readString(scanner, "Enter Title: ");
        String author = InputValidator.readString(scanner, "Enter Author: ");
        String category = InputValidator.readString(scanner, "Enter Category/Genre: ");

        Book book = new Book(id, title, author, category);
        try {
            bookService.addBook(book);
            autoSave();
            System.out.println("[+] Book added successfully: " + book);
        } catch (LibraryException e) {
            System.out.println("[-] Error: " + e.getMessage());
        }
    }

    private static void editBook() {
        System.out.println("--- Edit Book Details ---");
        String id = InputValidator.readString(scanner, "Enter Book ID to edit: ").toUpperCase();
        try {
            Book book = bookService.getBookById(id);
            System.out.println("Current details: " + book);
            System.out.println("Leave empty and press Enter to keep current value.");

            String newTitle = InputValidator.readOptionalString(scanner, "New Title", book.getTitle());
            String newAuthor = InputValidator.readOptionalString(scanner, "New Author", book.getAuthor());
            String newCategory = InputValidator.readOptionalString(scanner, "New Category", book.getCategory());

            bookService.editBook(id, newTitle, newAuthor, newCategory);
            autoSave();
            System.out.println("[+] Book details updated successfully!");
        } catch (BookNotFoundException e) {
            System.out.println("[-] " + e.getMessage());
        }
    }

    private static void removeBook() {
        System.out.println("--- Remove Book ---");
        String id = InputValidator.readString(scanner, "Enter Book ID to remove: ").toUpperCase();
        try {
            Book book = bookService.getBookById(id);
            System.out.println("Book to remove: " + book);
            if (InputValidator.readConfirmation(scanner, "Are you sure you want to remove this book?")) {
                bookService.removeBook(id);
                autoSave();
                System.out.println("[+] Book '" + book.getTitle() + "' was successfully removed.");
            } else {
                System.out.println("[*] Deletion canceled.");
            }
        } catch (LibraryException e) {
            System.out.println("[-] " + e.getMessage());
        }
    }

    private static void filterBooksByAvailability() {
        System.out.println("Filter: 1. Available Books  2. Currently Issued Books");
        int ch = InputValidator.readInt(scanner, "Choose (1-2): ", 1, 2);
        boolean avail = (ch == 1);
        List<Book> list = bookService.filterByAvailability(avail);
        System.out.printf("Found %d %s book(s):\n", list.size(), avail ? "available" : "issued");
        list.forEach(System.out::println);
    }

    // =========================================================================
    // Module 4: Member Management
    // =========================================================================

    private static void handleMemberManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [Member Registration & Directory] ---");
            System.out.println("1. List All Members");
            System.out.println("2. Register New Member (Student / Faculty)");
            System.out.println("3. Search Member by Name");
            System.out.println("4. View Member Details & Loan History");
            System.out.println("5. Remove Member");
            System.out.println("6. Back to Main Menu");

            int opt = InputValidator.readInt(scanner, "Choose an option (1-6): ", 1, 6);
            System.out.println();
            switch (opt) {
                case 1:
                    listAllMembers();
                    break;
                case 2:
                    registerNewMember();
                    break;
                case 3:
                    searchMembers();
                    break;
                case 4:
                    viewMemberHistory();
                    break;
                case 5:
                    removeMember();
                    break;
                case 6:
                    back = true;
                    break;
            }
        }
    }

    private static void listAllMembers() {
        List<Member> members = memberService.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("[*] No registered members found.");
            return;
        }
        System.out.println("============================= REGISTERED MEMBERS =============================");
        for (Member m : members) {
            System.out.println(m);
        }
        System.out.println("==============================================================================");
        System.out.printf("Total: %d registered member(s).\n", members.size());
    }

    private static void registerNewMember() {
        System.out.println("--- Register New Member ---");
        System.out.println("Member Type: 1. Student (Limit: 3 books, 14 days, ₹5/day fine)");
        System.out.println("             2. Faculty (Limit: 6 books, 30 days, ₹2/day fine)");
        int typeChoice = InputValidator.readInt(scanner, "Select Type (1-2): ", 1, 2);
        String type = typeChoice == 1 ? "STUDENT" : "FACULTY";

        String suggestedId = memberService.generateNextMemberId(type);
        String id = InputValidator.readOptionalString(scanner, "Enter Member ID", suggestedId).toUpperCase();
        if (memberService.exists(id)) {
            System.out.println("[-] Member ID '" + id + "' is already registered!");
            return;
        }

        String name = InputValidator.readString(scanner, "Enter Full Name: ");
        String phone = InputValidator.readPhoneNumber(scanner, "Enter Contact Phone (10-13 digits): ");
        String email = InputValidator.readEmail(scanner, "Enter Email Address: ");

        Member member;
        if (typeChoice == 1) {
            member = new StudentMember(id, name, phone, email);
        } else {
            member = new FacultyMember(id, name, phone, email);
        }

        try {
            memberService.registerMember(member);
            autoSave();
            System.out.println("[+] Member successfully registered: " + member);
        } catch (LibraryException e) {
            System.out.println("[-] " + e.getMessage());
        }
    }

    private static void searchMembers() {
        String nameQuery = InputValidator.readString(scanner, "Enter member name to search: ");
        List<Member> list = memberService.searchByName(nameQuery);
        if (list.isEmpty()) {
            System.out.println("[-] No members found matching '" + nameQuery + "'.");
        } else {
            System.out.println("Search Results (" + list.size() + " found):");
            list.forEach(System.out::println);
        }
    }

    private static void viewMemberHistory() {
        String id = InputValidator.readString(scanner, "Enter Member ID: ").toUpperCase();
        try {
            Member member = memberService.getMemberById(id);
            System.out.println("\nMember Profile: " + member);
            System.out.printf("Registered: %s | Max Allowed: %d | Loan Period: %d days | Daily Fine: ₹%.2f\n",
                    member.getRegistrationDate(), member.getMaxBooksAllowed(), member.getLoanPeriodDays(), member.getDailyFineRate());

            List<IssueRecord> records = lendingService.getIssuesForMember(id);
            System.out.println("\nLoan History (" + records.size() + " total transactions):");
            if (records.isEmpty()) {
                System.out.println("  (No loans recorded for this member)");
            } else {
                for (IssueRecord r : records) {
                    System.out.println("  " + r);
                }
            }
        } catch (MemberNotFoundException e) {
            System.out.println("[-] " + e.getMessage());
        }
    }

    private static void removeMember() {
        String id = InputValidator.readString(scanner, "Enter Member ID to remove: ").toUpperCase();
        try {
            Member member = memberService.getMemberById(id);
            if (InputValidator.readConfirmation(scanner, "Remove member '" + member.getName() + "'?")) {
                memberService.removeMember(id);
                autoSave();
                System.out.println("[+] Member removed successfully.");
            }
        } catch (LibraryException e) {
            System.out.println("[-] " + e.getMessage());
        }
    }

    // =========================================================================
    // Module 4 & 5: Lending Operations (Issue & Return)
    // =========================================================================

    private static void handleLendingOperationsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [Lending & Return Operations] ---");
            System.out.println("1. Issue a Book");
            System.out.println("2. Return a Book (Calculate Fine)");
            System.out.println("3. View All Currently Issued Books");
            System.out.println("4. Back to Main Menu");

            int opt = InputValidator.readInt(scanner, "Choose an option (1-4): ", 1, 4);
            System.out.println();
            switch (opt) {
                case 1:
                    issueBook();
                    break;
                case 2:
                    returnBook();
                    break;
                case 3:
                    viewActiveIssuedBooks();
                    break;
                case 4:
                    back = true;
                    break;
            }
        }
    }

    private static void issueBook() {
        System.out.println("--- Issue Book ---");
        String bookId = InputValidator.readString(scanner, "Enter Book ID: ").toUpperCase();
        String memberId = InputValidator.readString(scanner, "Enter Member ID: ").toUpperCase();
        LocalDate issueDate = InputValidator.readDate(scanner, "Enter Issue Date (YYYY-MM-DD)", true);

        try {
            IssueRecord record = lendingService.issueBook(bookId, memberId, issueDate);
            autoSave();
            System.out.println("\n[+] BOOK ISSUED SUCCESSFULLY!");
            System.out.println("Transaction Record Details:");
            System.out.println("  Record ID : " + record.getRecordId());
            System.out.println("  Book      : [" + record.getBookId() + "] " + record.getBookTitle());
            System.out.println("  Member    : [" + record.getMemberId() + "] " + record.getMemberName());
            System.out.println("  Issue Date: " + record.getIssueDate());
            System.out.println("  Due Date  : " + record.getDueDate());
        } catch (BookNotFoundException e) {
            System.out.println("[-] Error: Book not found! " + e.getMessage());
        } catch (MemberNotFoundException e) {
            System.out.println("[-] Error: Member not registered! " + e.getMessage());
        } catch (BookNotAvailableException e) {
            System.out.println("[-] Availability Error: " + e.getMessage());
        } catch (MaxBooksLimitExceededException e) {
            System.out.println("[-] Quota Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[-] Unexpected error issuing book: " + e.getMessage());
        }
    }

    private static void returnBook() {
        System.out.println("--- Return Book ---");
        String bookId = InputValidator.readString(scanner, "Enter Book ID to return: ").toUpperCase();
        LocalDate returnDate = InputValidator.readDate(scanner, "Enter Return Date (YYYY-MM-DD)", true);

        try {
            IssueRecord record = lendingService.returnBook(bookId, returnDate);
            autoSave();
            System.out.println("\n[+] BOOK RETURNED SUCCESSFULLY!");
            System.out.println("Transaction Summary:");
            System.out.println("  Record ID   : " + record.getRecordId());
            System.out.println("  Book        : [" + record.getBookId() + "] " + record.getBookTitle());
            System.out.println("  Member      : [" + record.getMemberId() + "] " + record.getMemberName());
            System.out.println("  Issue Date  : " + record.getIssueDate());
            System.out.println("  Due Date    : " + record.getDueDate());
            System.out.println("  Return Date : " + record.getReturnDate());

            if (record.getFineAmount() > 0) {
                long overdueDays = DateUtils.calculateOverdueDays(record.getDueDate(), record.getReturnDate());
                System.out.printf("  [!] Overdue Days: %d day(s)\n", overdueDays);
                System.out.printf("  [$] Fine Imposed: ₹%.2f (Collected)\n", record.getFineAmount());
            } else {
                System.out.println("  [*] Returned on time! No overdue fine.");
            }
        } catch (BookNotFoundException e) {
            System.out.println("[-] Error: " + e.getMessage());
        } catch (InvalidReturnException e) {
            System.out.println("[-] Return Error: " + e.getMessage());
        } catch (MemberNotFoundException e) {
            System.out.println("[-] Error: Member reference invalid: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[-] Unexpected error during return: " + e.getMessage());
        }
    }

    private static void viewActiveIssuedBooks() {
        List<IssueRecord> active = lendingService.getActiveIssues();
        if (active.isEmpty()) {
            System.out.println("[*] There are no books currently issued.");
            return;
        }
        System.out.println("========================= ACTIVE ISSUED BOOKS =========================");
        for (IssueRecord r : active) {
            boolean overdue = r.isOverdue(LocalDate.now());
            System.out.println(r + (overdue ? " [OVERDUE!]" : ""));
        }
        System.out.println("=======================================================================");
        System.out.printf("Total Active Loans: %d\n", active.size());
    }

    // =========================================================================
    // Module 6: Reports & Lambda Processing
    // =========================================================================

    private static void handleReportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [Reports & Lambda Analytics] ---");
            System.out.println("1. Overdue Books & Estimated Fines Report");
            System.out.println("2. Complete Transaction History (Sorted by Date)");
            System.out.println("3. Members with Active Loans");
            System.out.println("4. Library Executive Summary");
            System.out.println("5. Back to Main Menu");

            int opt = InputValidator.readInt(scanner, "Choose an option (1-5): ", 1, 5);
            System.out.println();
            switch (opt) {
                case 1:
                    generateOverdueReport();
                    break;
                case 2:
                    generateTransactionHistoryReport();
                    break;
                case 3:
                    generateActiveBorrowersReport();
                    break;
                case 4:
                    reportService.printLibrarySummary();
                    break;
                case 5:
                    back = true;
                    break;
            }
        }
    }

    private static void generateOverdueReport() {
        LocalDate asOfDate = InputValidator.readDate(scanner, "Calculate overdue as of date", true);
        List<ReportService.OverdueItem> overdueList = reportService.getOverdueReport(asOfDate);

        if (overdueList.isEmpty()) {
            System.out.println("[*] Excellent! No books are overdue as of " + asOfDate + ".");
            return;
        }

        System.out.println("\n======================== OVERDUE BOOKS REPORT ========================");
        System.out.printf("As of Date: %s | Total Overdue Items: %d\n", asOfDate, overdueList.size());
        System.out.println("----------------------------------------------------------------------");

        double totalFine = 0.0;
        for (ReportService.OverdueItem item : overdueList) {
            IssueRecord r = item.getRecord();
            Member m = item.getMember();
            String memberType = m != null ? m.getMembershipType() : "UNKNOWN";
            double dailyRate = m != null ? m.getDailyFineRate() : 5.0;

            System.out.printf("Record: %-8s | Book: [%s] %-20s\n", r.getRecordId(), r.getBookId(), r.getBookTitle());
            System.out.printf("  Member : [%s] %s (%s, Rate: ₹%.1f/day)\n", r.getMemberId(), r.getMemberName(), memberType, dailyRate);
            System.out.printf("  Issued : %s | Due: %s | Overdue: %d days | Est Fine: ₹%.2f\n",
                    r.getIssueDate(), r.getDueDate(), item.getOverdueDays(), item.getEstimatedFine());
            System.out.println("----------------------------------------------------------------------");
            totalFine += item.getEstimatedFine();
        }
        System.out.printf("Total Outstanding Overdue Fines: ₹%.2f\n", totalFine);
        System.out.println("======================================================================\n");
    }

    private static void generateTransactionHistoryReport() {
        List<IssueRecord> history = reportService.getIssueHistorySortedByDate();
        if (history.isEmpty()) {
            System.out.println("[*] No transactions recorded yet.");
            return;
        }
        System.out.println("========================= TRANSACTION HISTORY =========================");
        history.forEach(System.out::println);
        System.out.println("=======================================================================");
        System.out.printf("Total Records: %d | Total Fines Collected: ₹%.2f\n", history.size(), reportService.getTotalFinesCollected());
    }

    private static void generateActiveBorrowersReport() {
        List<Member> borrowers = reportService.getMembersWithActiveLoans();
        if (borrowers.isEmpty()) {
            System.out.println("[*] No members currently hold active borrowed books.");
            return;
        }
        System.out.println("===================== MEMBERS WITH ACTIVE LOANS =====================");
        for (Member m : borrowers) {
            System.out.printf("[%s] %-20s (ID: %s) -> Currently Borrowed: %d / %d books\n",
                    m.getMembershipType(), m.getName(), m.getId(), m.getCurrentBorrowedCount(), m.getMaxBooksAllowed());
        }
        System.out.println("=====================================================================");
    }

    // =========================================================================
    // Module 6: File Persistence Management
    // =========================================================================

    private static void handleDataStorageMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [File Persistence & Data Management] ---");
            System.out.println("1. Save All Data to Files Now");
            System.out.println("2. Reload Data from Files");
            System.out.println("3. Reset and Re-seed Default Sample Data");
            System.out.println("4. Back to Main Menu");

            int opt = InputValidator.readInt(scanner, "Choose an option (1-4): ", 1, 4);
            System.out.println();
            switch (opt) {
                case 1:
                    saveAllData();
                    break;
                case 2:
                    reloadAllData();
                    break;
                case 3:
                    resetAndSeedSampleData();
                    break;
                case 4:
                    back = true;
                    break;
            }
        }
    }

    private static void autoSave() {
        storageService.saveBooks(bookService.getAllBooks());
        storageService.saveMembers(memberService.getAllMembers());
        storageService.saveIssues(lendingService.getAllIssues());
    }

    private static void saveAllData() {
        try {
            storageService.saveBooks(bookService.getAllBooks());
            storageService.saveMembers(memberService.getAllMembers());
            storageService.saveIssues(lendingService.getAllIssues());
            System.out.println("[+] All data successfully saved to 'data/' CSV files.");
        } catch (Exception e) {
            System.out.println("[-] Error saving data: " + e.getMessage());
        }
    }

    private static void reloadAllData() {
        if (InputValidator.readConfirmation(scanner, "Reload data from files? Unsaved in-memory changes will be overwritten.")) {
            Map<String, Book> books = storageService.loadBooks();
            Map<String, Member> members = storageService.loadMembers();
            List<IssueRecord> issues = storageService.loadIssues();

            bookService = new BookService(books);
            memberService = new MemberService(members);
            lendingService = new LendingService(issues, bookService, memberService);
            reportService = new ReportService(bookService, memberService, lendingService);

            System.out.println("[+] Data reloaded successfully from files.");
        }
    }

    private static void resetAndSeedSampleData() {
        if (InputValidator.readConfirmation(scanner, "Reset entire library database and reload demo sample data?")) {
            Map<String, Book> books = new HashMap<>();
            Map<String, Member> members = new HashMap<>();
            List<IssueRecord> issues = new ArrayList<>();

            seedDefaultData(books, members, issues);
            storageService.saveBooks(books.values());
            storageService.saveMembers(members.values());
            storageService.saveIssues(issues);

            bookService = new BookService(books);
            memberService = new MemberService(members);
            lendingService = new LendingService(issues, bookService, memberService);
            reportService = new ReportService(bookService, memberService, lendingService);

            System.out.println("[+] Library database reset and preloaded with sample data.");
        }
    }

    private static void exitApplication() {
        System.out.println("Saving catalog data before exit...");
        autoSave();
        System.out.println("[+] Data successfully saved.");
        System.out.println("Thank you for using College Library Book Lending System. Goodbye!");
    }
}
