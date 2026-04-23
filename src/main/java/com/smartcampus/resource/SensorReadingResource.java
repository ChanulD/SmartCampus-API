// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.UUID;

/**
 * Sub-resource class for sensor reading history.
 *
 * <p>This class has NO class-level {@code @Path} annotation. Its path is determined
 * entirely by the locator method in {@link SensorResource} that returns it
 * ({@code @Path("/{sensorId}/readings")}). Adding {@code @Path} here would cause
 * double-path registration and a 404 on every request — a common sub-resource bug.</p>
 *
 * Effective paths:
 *   GET  /sensors/{sensorId}/readings      – list reading history (200 / 404)
 *   POST /sensors/{sensorId}/readings      – add a new reading    (201 / 403 / 404)
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String    sensorId;
    private final DataStore store;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
        this.store    = DataStore.getInstance();
    }

    // ── GET /sensors/{sensorId}/readings ──────────────────────────────────────

    @GET
    public Response getReadings() {
        if (store.getSensor(sensorId) == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Not Found",
                            "Sensor with id '" + sensorId + "' was not found."))
                    .build();
        }
        List<SensorReading> readings = store.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    // ── POST /sensors/{sensorId}/readings ─────────────────────────────────────

    @POST
    public Response addReading(SensorReading reading) {
        // Verify sensor exists
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Not Found",
                            "Sensor with id '" + sensorId + "' was not found."))
                    .build();
        }
        // Issue 3 fix: check ONLY for MAINTENANCE, not OFFLINE
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is under MAINTENANCE and cannot accept new readings.");
        }
        // Issue 2 fix: auto-generate id/timestamp — never mutate via getReadingsForSensor()
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Persist via DataStore (thread-safe computeIfAbsent path)
        store.addReading(sensorId, reading);

        // Side effect: update the sensor's currentValue to the latest reading
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
