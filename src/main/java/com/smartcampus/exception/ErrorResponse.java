package com.smartcampus.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Uniform JSON error body returned by the GlobalExceptionMapper.
 *
 * Example response:
 * {
 *   "status"    : 404,
 *   "error"     : "Not Found",
 *   "message"   : "Sensor with id 'S999' was not found.",
 *   "timestamp" : "2026-04-23T12:00:00Z"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int    status;
    private String error;
    private String message;
    private String timestamp;

    public ErrorResponse() {}

    public ErrorResponse(int status, String error, String message) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.timestamp = Instant.now().toString();
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int    getStatus()              { return status; }
    public void   setStatus(int status)    { this.status = status; }

    public String getError()               { return error; }
    public void   setError(String error)   { this.error = error; }

    public String getMessage()                 { return message; }
    public void   setMessage(String message)   { this.message = message; }

    public String getTimestamp()                   { return timestamp; }
    public void   setTimestamp(String timestamp)   { this.timestamp = timestamp; }
}
