// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
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
