// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps Jersey's own {@link WebApplicationException} subclasses to a structured
 * JSON {@link ErrorResponse}.
 *
 * <p>Without this mapper, Jersey's built-in handling for 404 (resource not found),
 * 405 (method not allowed), 406 (not acceptable), 415 (unsupported media type), etc.
 * returns an HTML error page rather than a JSON body, bypassing
 * {@link GlobalExceptionMapper} entirely.  This mapper intercepts those exceptions
 * before they reach the default Jersey handler and wraps them in the same
 * {@link ErrorResponse} format used throughout the API.</p>
 *
 * <p>Ordering note: JAX-RS resolves the most specific mapper first.  Because
 * {@link WebApplicationException} is a concrete superclass of all Jersey runtime
 * exceptions, this mapper handles exactly those cases while the narrower
 * custom mappers ({@link RoomNotEmptyExceptionMapper}, etc.) continue to handle
 * their own types.</p>
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response original = exception.getResponse();
        int    statusCode = original.getStatus();
        String reason     = Response.Status.fromStatusCode(statusCode) != null
                            ? Response.Status.fromStatusCode(statusCode).getReasonPhrase()
                            : "Error";

        return Response.status(statusCode)
                .entity(new ErrorResponse(statusCode, reason, exception.getMessage() != null
                        ? exception.getMessage()
                        : reason))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
