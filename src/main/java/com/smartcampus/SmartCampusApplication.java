package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JAX-RS application entry point.
 *
 * Extends ResourceConfig (Jersey's enhanced Application) to enable:
 *   - Auto-scanning of all JAX-RS resources under com.smartcampus
 *   - Jackson JSON serialisation / deserialisation via JacksonFeature
 *
 * All endpoints will be reachable at /api/v1/...
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // Auto-discover @Path, @Provider, @ExceptionMapper, etc. in the base package
        packages("com.smartcampus");

        // Enable Jackson for JSON marshalling
        register(JacksonFeature.class);
    }
}
