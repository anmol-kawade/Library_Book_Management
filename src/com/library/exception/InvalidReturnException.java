package com.library.exception;

/**
 * Thrown when attempting to return a book that was never issued,
 * or is already marked as returned, or has no active issue record.
 */
public class InvalidReturnException extends LibraryException {
    public InvalidReturnException(String message) {
        super(message);
    }
}
