package com.library.exception;

/**
 * Base exception for all Library Management System domain exceptions.
 */
public class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
