package com.smartcampus.resource;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.SensorReadingStore;
import com.smartcampus.store.SensorStore;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

/**
 * REST resource for sensor readings.
 *
 * Nested under sensor: /api/v1/sensors/{sensorId}/readings
 *
 * GET    /sensors/{sensorId}/readings           - list readings for a sensor
 * GET    /sensors/{sensorId}/readings/{id}      - get a single reading
 * POST   /sensors/{sensorId}/readings           - record a new reading
 * DELETE /sensors/{sensorId}/readings/{id}      - delete a reading
 */
@Path("/sensors/{sensorId}/readings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    @Inject
    private SensorReadingStore readingStore;

    @Inject
    private SensorStore sensorStore;

    @Context
    private UriInfo uriInfo;

    // ── GET /sensors/{sensorId}/readings ──────────────────────────────────────

    @GET
    public List<SensorReading> getReadingsForSensor(@PathParam("sensorId") String sensorId) {
        verifySensorExists(sensorId);
        return readingStore.findBySensorId(sensorId);
    }

    // ── GET /sensors/{sensorId}/readings/{id} ─────────────────────────────────

    @GET
    @Path("/{id}")
    public SensorReading getReadingById(@PathParam("sensorId") String sensorId,
                                        @PathParam("id")       String id) {
        verifySensorExists(sensorId);
        SensorReading reading = readingStore.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SensorReading", id));
        // Guard: ensure the reading belongs to the sensor in the URL
        if (!sensorId.equals(reading.getSensorId())) {
            throw new ResourceNotFoundException("SensorReading", id);
        }
        return reading;
    }

    // ── POST /sensors/{sensorId}/readings ─────────────────────────────────────

    @POST
    public Response addReading(@PathParam("sensorId") String sensorId,
                               SensorReading reading) {
        verifySensorExists(sensorId);
        if (reading == null) {
            throw new BadRequestException("Request body must not be empty.");
        }
        // Force sensorId from URL path (ignore any body value)
        reading.setSensorId(sensorId);

        SensorReading created = readingStore.add(reading);
        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(created.getId())
                              .build();
        return Response.created(location).entity(created).build();
    }

    // ── DELETE /sensors/{sensorId}/readings/{id} ──────────────────────────────

    @DELETE
    @Path("/{id}")
    public Response deleteReading(@PathParam("sensorId") String sensorId,
                                  @PathParam("id")       String id) {
        verifySensorExists(sensorId);
        if (!readingStore.delete(id)) {
            throw new ResourceNotFoundException("SensorReading", id);
        }
        return Response.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void verifySensorExists(String sensorId) {
        if (!sensorStore.exists(sensorId)) {
            throw new ResourceNotFoundException("Sensor", sensorId);
        }
    }
}
