// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;

/**
 * JAX-RS resource for campus room management.
 *
 * Base path (relative to @ApplicationPath): /rooms
 *
 * GET    /rooms             – list all rooms (200)
 * POST   /rooms             – create a room (201 / 400 / 409)
 * GET    /rooms/{roomId}    – get room by id (200 / 404)
 * DELETE /rooms/{roomId}    – delete a room (204 / 404 / 409)
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @Context
    private UriInfo uriInfo;

    // ── GET /rooms ────────────────────────────────────────────────────────────

    @GET
    public Response getAllRooms() {
        return Response.ok(store.getAllRooms()).build();
    }

    // ── POST /rooms ───────────────────────────────────────────────────────────

    @POST
    public Response createRoom(Room room) {
        // Validate: id
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Bad Request", "Room 'id' must not be blank."))
                    .build();
        }
        // Validate: name
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Bad Request", "Room 'name' must not be blank."))
                    .build();
        }
        // Conflict check
        if (store.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(409, "Conflict",
                            "Room with id '" + room.getId() + "' already exists."))
                    .build();
        }
        // Guard sensorIds
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        store.saveRoom(room);

        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(room.getId())
                              .build();
        return Response.created(location).entity(room).build();
    }

    // ── GET /rooms/{roomId} ───────────────────────────────────────────────────

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Not Found",
                            "Room with id '" + roomId + "' was not found."))
                    .build();
        }
        return Response.ok(room).build();
    }

    // ── DELETE /rooms/{roomId} ────────────────────────────────────────────────

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Not Found",
                            "Room with id '" + roomId + "' was not found."))
                    .build();
        }
        // Business rule: room must be empty of sensors before deletion
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted. It has "
                    + room.getSensorIds().size() + " active sensor(s) assigned.");
        }
        store.deleteRoom(roomId);
        return Response.noContent().build();
    }
}
