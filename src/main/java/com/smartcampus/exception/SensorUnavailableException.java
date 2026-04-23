package com.smartcampus.exception;

/**
 * Thrown when a POST to /readings is attempted on a sensor whose status is "MAINTENANCE".
 * Mapped to HTTP 403 Forbidden by {@link SensorUnavailableExceptionMapper}.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
