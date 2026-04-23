// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
package com.smartcampus.model;

/**
 * Immutable JSON response body returned by the global exception mapper
 * whenever the API needs to signal an error to the client.
 *
 * Example JSON output:
 * <pre>
 * {
 *   "status"    : 404,
 *   "error"     : "Not Found",
 *   "message"   : "Sensor with id 'S999' was not found.",
 *   "timestamp" : 1714000000000
 * }
 * </pre>
 *
 * The {@code timestamp} field is set automatically to the epoch-millisecond
 * value of the moment the error response is constructed.
 */
public class ErrorResponse {

    private final int    status;
    private final String error;
    private final String message;
    private final long   timestamp;

    /**
     * Creates an error response, capturing the current time automatically.
     *
     * @param status  HTTP status code (e.g. 404, 400, 500)
     * @param error   short HTTP reason phrase (e.g. "Not Found")
     * @param message detailed human-readable description of the error
     */
    public ErrorResponse(int status, String error, String message) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.timestamp = System.currentTimeMillis();
    }

    // ── Getters (read-only – no setters on an immutable response) ─────────────

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
