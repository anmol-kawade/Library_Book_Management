package com.library.exception;

/**
 * Thrown when a book with a specified ID or search criteria is not found.
 */
public class BookNotFoundException extends LibraryException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
