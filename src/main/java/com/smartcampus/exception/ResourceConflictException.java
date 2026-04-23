package com.smartcampus.exception;

/**
 * Thrown when a client attempts to create a resource that already exists (HTTP 409).
 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
