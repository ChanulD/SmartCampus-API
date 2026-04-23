package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JAX-RS resource for sensor management.
 *
 * Base path: /sensors
 *
 * GET    /sensors                      – list all sensors, optional ?type= filter (200)
 * POST   /sensors                      – create a sensor (201 / 400 / 409 / 422)
 * GET    /sensors/{sensorId}           – get sensor by id (200 / 404)
 * ANY    /sensors/{sensorId}/readings  – sub-resource locator → SensorReadingResource
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @Context
    private UriInfo uriInfo;

    // ── GET /sensors[?type=] ──────────────────────────────────────────────────

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = store.getAllSensors().stream()
                .filter(s -> type == null || type.isBlank()
                          || s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    // ── POST /sensors ─────────────────────────────────────────────────────────

    @POST
    public Response createSensor(Sensor sensor) {
        // Validate: id
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Bad Request",
                            "Sensor 'id' must not be blank."))
                    .build();
        }
        // Validate: type
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Bad Request",
                            "Sensor 'type' must not be blank."))
                    .build();
        }
        // Validate: roomId reference (422 if the linked room doesn't exist)
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw new LinkedResourceNotFoundException(
                    "Sensor 'roomId' is required and must reference an existing room.");
        }
        if (!store.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Room with id '" + sensor.getRoomId() + "' does not exist. "
                    + "Create the room first before assigning a sensor to it.");
        }
        // Conflict: sensor id already exists
        if (store.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(409, "Conflict",
                            "Sensor with id '" + sensor.getId() + "' already exists."))
                    .build();
        }
        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        store.saveSensor(sensor);

        // Link sensor id into the parent room's sensorIds list
        Room room = store.getRoom(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().add(sensor.getId());
        }

        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(sensor.getId())
                              .build();
        return Response.created(location).entity(sensor).build();
    }

    // ── GET /sensors/{sensorId} ───────────────────────────────────────────────

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Not Found",
                            "Sensor with id '" + sensorId + "' was not found."))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // ── Sub-resource locator: /sensors/{sensorId}/readings ───────────────────

    /**
     * Sub-resource locator — delegates all /sensors/{sensorId}/readings/* requests
     * to {@link SensorReadingResource}.
     *
     * <p>Note: this method has NO HTTP verb annotation (@GET, @POST, etc.).
     * JAX-RS recognises it as a locator because it returns an object, not data.
     * The returned instance handles the request using its own @GET / @POST methods.</p>
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
