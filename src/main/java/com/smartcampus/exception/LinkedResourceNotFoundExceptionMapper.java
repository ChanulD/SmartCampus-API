// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link LinkedResourceNotFoundException} to HTTP 422 Unprocessable Entity.
 *
 * <p>JAX-RS {@link Response.Status} enum does not include 422, so the integer
 * value is used directly with {@link Response#status(int)}.</p>
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        return Response.status(422)
                .entity(new ErrorResponse(422, "Unprocessable Entity", exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
