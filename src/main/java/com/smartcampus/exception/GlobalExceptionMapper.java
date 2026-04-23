package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converts all application exceptions into structured JSON error responses.
 *
 * Handles:
 *   ResourceNotFoundException  → 404 Not Found
 *   BadRequestException        → 400 Bad Request
 *   ResourceConflictException  → 409 Conflict
 *   Any other Throwable        → 500 Internal Server Error
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {

        if (exception instanceof ResourceNotFoundException) {
            return buildResponse(Response.Status.NOT_FOUND, exception.getMessage());
        }

        if (exception instanceof BadRequestException) {
            return buildResponse(Response.Status.BAD_REQUEST, exception.getMessage());
        }

        if (exception instanceof ResourceConflictException) {
            return buildResponse(Response.Status.CONFLICT, exception.getMessage());
        }

        // Fallback - 500 Internal Server Error
        return buildResponse(
                Response.Status.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + exception.getMessage()
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Response buildResponse(Response.Status status, String message) {
        ErrorResponse body = new ErrorResponse(
                status.getStatusCode(),
                status.getReasonPhrase(),
                message
        );
        return Response.status(status)
                       .entity(body)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
