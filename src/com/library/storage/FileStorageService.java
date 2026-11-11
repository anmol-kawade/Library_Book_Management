package com.library.storage;

import com.library.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * Handles persistent file storage using standard CSV format.
 * Loads and saves Books, Members, and IssueRecords from and to the data directory.
 */
public class FileStorageService {
    private static final String DATA_DIR = "data";
    private static final String BOOKS_FILE = DATA_DIR + File.separator + "books.csv";
    private static final String MEMBERS_FILE = DATA_DIR + File.separator + "members.csv";
    private static final String ISSUES_FILE = DATA_DIR + File.separator + "issues.csv";

    public FileStorageService() {
        ensureDataDirectory();
    }

    private void ensureDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ==========================================
    // Book Storage
    // ==========================================

    public Map<String, Book> loadBooks() {
        Map<String, Book> books = new HashMap<>();
        File file = new File(BOOKS_FILE);
        if (!file.exists()) {
            return books;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> tokens = parseCsvLine(line);
                if (tokens.size() >= 5) {
                    String id = tokens.get(0).trim();
                    String title = tokens.get(1).trim();
                    String author = tokens.get(2).trim();
                    String category = tokens.get(3).trim();
                    boolean available = Boolean.parseBoolean(tokens.get(4).trim());
                    Book book = new Book(id, title, author, category, available);
                    books.put(id, book);
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Error loading books file: " + e.getMessage());
        }
        return books;
    }

    public void saveBooks(Collection<Book> books) {
        ensureDataDirectory();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(BOOKS_FILE), StandardCharsets.UTF_8))) {
            writer.println("id,title,author,category,available");
            for (Book book : books) {
                writer.println(book.toCsv());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to save books: " + e.getMessage());
        }
    }

    // ==========================================
    // Member Storage
    // ==========================================

    public Map<String, Member> loadMembers() {
        Map<String, Member> members = new HashMap<>();
        File file = new File(MEMBERS_FILE);
        if (!file.exists()) {
            return members;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> tokens = parseCsvLine(line);
                if (tokens.size() >= 7) {
                    String id = tokens.get(0).trim();
                    String name = tokens.get(1).trim();
                    String contact = tokens.get(2).trim();
                    String email = tokens.get(3).trim();
                    String type = tokens.get(4).trim();
                    LocalDate regDate = LocalDate.parse(tokens.get(5).trim());
                    int borrowedCount = Integer.parseInt(tokens.get(6).trim());

                    Member member;
                    if ("FACULTY".equalsIgnoreCase(type)) {
                        member = new FacultyMember(id, name, contact, email, regDate, borrowedCount);
                    } else {
                        member = new StudentMember(id, name, contact, email, regDate, borrowedCount);
                    }
                    members.put(id, member);
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Error loading members file: " + e.getMessage());
        }
        return members;
    }

    public void saveMembers(Collection<Member> members) {
        ensureDataDirectory();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(MEMBERS_FILE), StandardCharsets.UTF_8))) {
            writer.println("id,name,contact,email,type,registrationDate,borrowedCount");
            for (Member member : members) {
                writer.println(member.toCsv());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to save members: " + e.getMessage());
        }
    }

    // ==========================================
    // Issue Record Storage
    // ==========================================

    public List<IssueRecord> loadIssues() {
        List<IssueRecord> records = new ArrayList<>();
        File file = new File(ISSUES_FILE);
        if (!file.exists()) {
            return records;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> tokens = parseCsvLine(line);
                if (tokens.size() >= 10) {
                    String recordId = tokens.get(0).trim();
                    String bookId = tokens.get(1).trim();
                    String bookTitle = tokens.get(2).trim();
                    String memberId = tokens.get(3).trim();
                    String memberName = tokens.get(4).trim();
                    LocalDate issueDate = LocalDate.parse(tokens.get(5).trim());
                    LocalDate dueDate = LocalDate.parse(tokens.get(6).trim());
                    String retStr = tokens.get(7).trim();
                    LocalDate returnDate = retStr.isEmpty() ? null : LocalDate.parse(retStr);
                    double fine = Double.parseDouble(tokens.get(8).trim());
                    String status = tokens.get(9).trim();

                    IssueRecord record = new IssueRecord(recordId, bookId, bookTitle, memberId, memberName,
                            issueDate, dueDate, returnDate, fine, status);
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Error loading issue records: " + e.getMessage());
        }
        return records;
    }

    public void saveIssues(Collection<IssueRecord> records) {
        ensureDataDirectory();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ISSUES_FILE), StandardCharsets.UTF_8))) {
            writer.println("recordId,bookId,bookTitle,memberId,memberName,issueDate,dueDate,returnDate,fineAmount,status");
            for (IssueRecord record : records) {
                writer.println(record.toCsv());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to save issue records: " + e.getMessage());
        }
    }

    /**
     * Parses a CSV line taking quotes into account.
     */
    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder curVal = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    curVal.append('\"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(curVal.toString());
                curVal.setLength(0);
            } else {
                curVal.append(c);
            }
        }
        result.add(curVal.toString());
        return result;
    }
}
