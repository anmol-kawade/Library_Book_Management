package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.exception.LibraryException;
import com.library.model.Book;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service class for managing library books.
 * Utilizes Java Collections (HashMap, ArrayList) and Lambda Expressions with functional interfaces.
 */
public class BookService {
    private final Map<String, Book> bookMap; // Fast lookup by Book ID

    public BookService(Map<String, Book> initialBooks) {
        this.bookMap = new HashMap<>(initialBooks);
    }

    /**
     * Adds a new book to the catalog.
     */
    public void addBook(Book book) throws LibraryException {
        if (book == null || book.getId() == null || book.getId().trim().isEmpty()) {
            throw new LibraryException("Book details and ID cannot be empty.");
        }
        String id = book.getId().trim().toUpperCase();
        book.setId(id);
        if (bookMap.containsKey(id)) {
            throw new LibraryException("A book with ID " + id + " already exists.");
        }
        bookMap.put(id, book);
    }

    /**
     * Updates an existing book's details.
     */
    public void editBook(String id, String newTitle, String newAuthor, String newCategory) throws BookNotFoundException {
        Book book = getBookById(id);
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            book.setTitle(newTitle.trim());
        }
        if (newAuthor != null && !newAuthor.trim().isEmpty()) {
            book.setAuthor(newAuthor.trim());
        }
        if (newCategory != null && !newCategory.trim().isEmpty()) {
            book.setCategory(newCategory.trim());
        }
    }

    /**
     * Removes a book from the catalog if it is not currently issued.
     */
    public void removeBook(String id) throws LibraryException {
        Book book = getBookById(id);
        if (!book.isAvailable()) {
            throw new LibraryException("Cannot remove book '" + book.getTitle() + "' because it is currently issued to a member.");
        }
        bookMap.remove(book.getId());
    }

    /**
     * Retrieves a book by ID or throws BookNotFoundException.
     */
    public Book getBookById(String id) throws BookNotFoundException {
        if (id == null) {
            throw new BookNotFoundException("Book ID cannot be null.");
        }
        Book book = bookMap.get(id.trim().toUpperCase());
        if (book == null) {
            throw new BookNotFoundException("Book with ID '" + id + "' was not found in the catalog.");
        }
        return book;
    }

    /**
     * Checks if a book exists in the catalog.
     */
    public boolean exists(String id) {
        return id != null && bookMap.containsKey(id.trim().toUpperCase());
    }

    /**
     * Returns an unmodifiable list of all books, sorted by ID.
     */
    public List<Book> getAllBooks() {
        return bookMap.values().stream()
                .sorted(Comparator.comparing(Book::getId))
                .collect(Collectors.toList());
    }

    /**
     * Generic filter method using Java 8 Predicate and Lambda expressions.
     */
    public List<Book> filterBooks(Predicate<Book> predicate) {
        return bookMap.values().stream()
                .filter(predicate)
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Search books by title keyword using lambdas.
     */
    public List<Book> searchByTitle(String query) {
        String lower = query.toLowerCase();
        return filterBooks(book -> book.getTitle().toLowerCase().contains(lower));
    }

    /**
     * Search books by author keyword using lambdas.
     */
    public List<Book> searchByAuthor(String query) {
        String lower = query.toLowerCase();
        return filterBooks(book -> book.getAuthor().toLowerCase().contains(lower));
    }

    /**
     * Search books by category using lambdas.
     */
    public List<Book> searchByCategory(String category) {
        String lower = category.toLowerCase();
        return filterBooks(book -> book.getCategory().toLowerCase().contains(lower));
    }

    /**
     * Filter books by availability status.
     */
    public List<Book> filterByAvailability(boolean available) {
        return filterBooks(book -> book.isAvailable() == available);
    }

    /**
     * Automatically generates a recommended next Book ID (e.g. B101, B102, ...).
     */
    public String generateNextBookId() {
        int maxId = 100;
        for (String id : bookMap.keySet()) {
            if (id.startsWith("B")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "B" + (maxId + 1);
    }

    public Map<String, Book> getBookMap() {
        return bookMap;
    }
}
