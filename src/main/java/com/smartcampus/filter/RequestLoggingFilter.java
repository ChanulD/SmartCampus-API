package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;
import java.util.logging.Logger;

/**
 * JAX-RS filter that:
 *   1. Logs every inbound request (method, URI, timestamp) on the way IN.
 *   2. Logs the response status on the way OUT.
 *   3. Adds CORS headers so the API can be called from browser clients.
 */
@Provider
public class RequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class.getName());

    // ── Inbound (request) ────────────────────────────────────────────────────

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Store start time for latency calculation
        requestContext.setProperty("startTime", System.currentTimeMillis());

        LOG.info(String.format("[REQUEST]  %s %s  @ %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                Instant.now()));
    }

    // ── Outbound (response) ──────────────────────────────────────────────────

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        // CORS headers - allow all origins for development
        responseContext.getHeaders().add("Access-Control-Allow-Origin",  "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept");

        // Calculate elapsed time
        Long startTime = (Long) requestContext.getProperty("startTime");
        long elapsed   = startTime != null ? System.currentTimeMillis() - startTime : -1;

        LOG.info(String.format("[RESPONSE] %s %s  → %d  (%dms)",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                responseContext.getStatus(),
                elapsed));
    }
}
