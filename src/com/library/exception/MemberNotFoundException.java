package com.library.exception;

/**
 * Thrown when a member with the specified ID is not found in the registry.
 */
public class MemberNotFoundException extends LibraryException {
    public MemberNotFoundException(String message) {
        super(message);
    }
}
