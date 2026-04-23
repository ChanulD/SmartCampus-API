package com.smartcampus.resource;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.RoomStore;
import com.smartcampus.store.SensorStore;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

/**
 * REST resource for sensors.
 *
 * Flat endpoints:     /api/v1/sensors
 * Nested endpoints:   /api/v1/rooms/{roomId}/sensors
 *
 * GET    /sensors              - list all sensors
 * GET    /sensors/{id}         - get a single sensor
 * POST   /sensors              - create a sensor
 * PUT    /sensors/{id}         - replace a sensor
 * DELETE /sensors/{id}         - delete a sensor
 *
 * GET    /rooms/{roomId}/sensors        - list sensors for a room
 * POST   /rooms/{roomId}/sensors        - create sensor in a room
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @Inject
    private SensorStore sensorStore;

    @Inject
    private RoomStore roomStore;

    @Context
    private UriInfo uriInfo;

    // ── GET /sensors ──────────────────────────────────────────────────────────

    @GET
    public List<Sensor> getAllSensors() {
        return sensorStore.findAll();
    }

    // ── GET /sensors/{id} ─────────────────────────────────────────────────────

    @GET
    @Path("/{id}")
    public Sensor getSensorById(@PathParam("id") String id) {
        return sensorStore.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
    }

    // ── POST /sensors ─────────────────────────────────────────────────────────

    @POST
    public Response createSensor(Sensor sensor) {
        validateSensor(sensor);
        // Verify referenced room exists
        if (sensor.getRoomId() != null && !roomStore.exists(sensor.getRoomId())) {
            throw new ResourceNotFoundException("Room", sensor.getRoomId());
        }
        Sensor created = sensorStore.add(sensor);
        URI location   = uriInfo.getAbsolutePathBuilder()
                                .path(created.getId())
                                .build();
        return Response.created(location).entity(created).build();
    }

    // ── PUT /sensors/{id} ─────────────────────────────────────────────────────

    @PUT
    @Path("/{id}")
    public Response updateSensor(@PathParam("id") String id, Sensor sensor) {
        validateSensor(sensor);
        Sensor updated = sensorStore.update(id, sensor)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
        return Response.ok(updated).build();
    }

    // ── DELETE /sensors/{id} ──────────────────────────────────────────────────

    @DELETE
    @Path("/{id}")
    public Response deleteSensor(@PathParam("id") String id) {
        if (!sensorStore.delete(id)) {
            throw new ResourceNotFoundException("Sensor", id);
        }
        return Response.noContent().build();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateSensor(Sensor sensor) {
        if (sensor == null) {
            throw new BadRequestException("Request body must not be empty.");
        }
        if (sensor.getName() == null || sensor.getName().isBlank()) {
            throw new BadRequestException("Sensor 'name' is required.");
        }
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            throw new BadRequestException("Sensor 'type' is required.");
        }
    }
}
