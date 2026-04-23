package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Catch-all exception mapper for any unhandled {@link Throwable}.
 *
 * <p><strong>Security rationale — why we never expose stack traces to clients:</strong><br>
 * Stack traces reveal (1) internal class names and package structure, helping
 * attackers map the application; (2) library names and versions, enabling targeted
 * CVE exploits; (3) file-system paths on the server; (4) database/query details if
 * present; and (5) application logic flow, disclosing what conditions cause failures.
 * An attacker can leverage this information to craft targeted exploits.<br><br>
 * The fix: log the full stack trace server-side at SEVERE level so engineers can
 * diagnose problems, and return only a generic message to the client.</p>
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full detail server-side only — never expose to the client
        LOG.log(Level.SEVERE, "Unhandled exception caught by GlobalExceptionMapper", exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(
                        500,
                        "Internal Server Error",
                        "An unexpected error occurred. Please contact the system administrator."))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
