// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JAX-RS application entry point.
 *
 * <p>Extends {@link ResourceConfig} (Jersey's enhanced Application subclass) to enable:
 * <ul>
 *   <li>Auto-scanning of all JAX-RS resources, providers, filters, and mappers
 *       under the {@code com.smartcampus} package.</li>
 *   <li>Jackson JSON serialisation / deserialisation via {@link JacksonFeature}.</li>
 * </ul>
 *
 * <p>The in-memory {@code DataStore} is a plain singleton accessed via
 * {@code DataStore.getInstance()} inside each resource — no HK2 binding required.</p>
 *
 * All endpoints are reachable at {@code /api/v1/...}
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // Auto-discover @Path, @Provider, @ExceptionMapper, @PreMatching, etc.
        packages("com.smartcampus");

        // Enable Jackson for JSON marshalling / unmarshalling
        register(JacksonFeature.class);
    }
}
