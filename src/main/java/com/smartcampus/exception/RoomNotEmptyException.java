// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
package com.smartcampus.exception;

/**
 * Thrown when a DELETE is attempted on a Room that still has sensors assigned.
 * Mapped to HTTP 409 Conflict by {@link RoomNotEmptyExceptionMapper}.
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }
}
