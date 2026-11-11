package com.library.exception;

/**
 * Thrown when a library member attempts to borrow more books than their allowed limit.
 */
public class MaxBooksLimitExceededException extends LibraryException {
    public MaxBooksLimitExceededException(String message) {
        super(message);
    }
}
