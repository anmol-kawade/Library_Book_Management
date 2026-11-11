package com.library.exception;

/**
 * Thrown when attempting to issue a book that is currently checked out or unavailable.
 */
public class BookNotAvailableException extends LibraryException {
    public BookNotAvailableException(String message) {
        super(message);
    }
}
