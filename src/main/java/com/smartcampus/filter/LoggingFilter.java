package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS filter that logs every inbound request and outbound response.
 *
 * <p><strong>Why a filter instead of Logger.info() in every resource method?</strong><br>
 * Using a {@link ContainerRequestFilter}/{@link ContainerResponseFilter} for cross-cutting
 * concerns (logging, authentication, CORS) is superior because:
 * <ol>
 *   <li><strong>Single point of change</strong> — a new endpoint is logged automatically
 *       with zero changes to the filter.</li>
 *   <li><strong>No risk of omission</strong> — impossible to "forget" to log a new endpoint.</li>
 *   <li><strong>Consistent format</strong> — every log line is guaranteed to look identical.</li>
 *   <li><strong>Separation of Concerns</strong> — resource methods focus purely on business
 *       logic; the filter owns the cross-cutting logging concern.</li>
 *   <li><strong>AOP alignment</strong> — mirrors the Aspect-Oriented Programming pattern
 *       used in production systems (Spring AOP, CDI interceptors).</li>
 *   <li><strong>Easy to toggle</strong> — comment out {@code @Provider} to disable all
 *       request/response logging in one place.</li>
 * </ol>
 * </p>
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    // ── Inbound ───────────────────────────────────────────────────────────────

    /**
     * Logs the incoming HTTP method and full URI before the resource method runs.
     *
     * Format: {@code [REQUEST ] --> {METHOD} {URI}}
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info(String.format("[REQUEST ] --> %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }

    // ── Outbound ──────────────────────────────────────────────────────────────

    /**
     * Logs the HTTP status code returned after the resource method completes.
     *
     * Format: {@code [RESPONSE] <-- {METHOD} {URI} | HTTP {statusCode}}
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOG.info(String.format("[RESPONSE] <-- %s %s | HTTP %d",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                responseContext.getStatus()));
    }
}
