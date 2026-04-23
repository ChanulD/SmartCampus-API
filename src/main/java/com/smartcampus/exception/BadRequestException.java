package com.smartcampus.exception;

/**
 * Thrown when a client submits a request that violates business rules (HTTP 400).
 * Example: creating a sensor without a required field.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
