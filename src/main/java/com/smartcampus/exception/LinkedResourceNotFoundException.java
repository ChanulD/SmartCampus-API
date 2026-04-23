package com.smartcampus.exception;

/**
 * Thrown when a Sensor is created with a roomId that does not exist in the DataStore.
 * Mapped to HTTP 422 Unprocessable Entity by {@link LinkedResourceNotFoundExceptionMapper}.
 *
 * <p>422 is used instead of 404 because the request URL (/api/v1/sensors) is valid;
 * the problem is inside the request body — the referenced roomId is semantically invalid.</p>
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
