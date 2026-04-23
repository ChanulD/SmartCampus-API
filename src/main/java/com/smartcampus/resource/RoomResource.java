package com.smartcampus.resource;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.store.RoomStore;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;
import java.net.URI;
import java.util.List;

/**
 * REST resource for campus rooms.
 *
 * Base path: /api/v1/rooms
 *
 * GET    /rooms          - list all rooms
 * GET    /rooms/{id}     - get a single room
 * POST   /rooms          - create a room
 * PUT    /rooms/{id}     - replace a room
 * DELETE /rooms/{id}     - delete a room
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @Inject
    private RoomStore roomStore;

    @Context
    private UriInfo uriInfo;

    // ── GET /rooms ────────────────────────────────────────────────────────────

    @GET
    public List<Room> getAllRooms() {
        return roomStore.findAll();
    }

    // ── GET /rooms/{id} ───────────────────────────────────────────────────────

    @GET
    @Path("/{id}")
    public Room getRoomById(@PathParam("id") String id) {
        return roomStore.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    // ── POST /rooms ───────────────────────────────────────────────────────────

    @POST
    public Response createRoom(Room room) {
        validateRoom(room);
        Room created = roomStore.add(room);
        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(created.getId())
                              .build();
        return Response.created(location).entity(created).build();
    }

    // ── PUT /rooms/{id} ───────────────────────────────────────────────────────

    @PUT
    @Path("/{id}")
    public Response updateRoom(@PathParam("id") String id, Room room) {
        validateRoom(room);
        Room updated = roomStore.update(id, room)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
        return Response.ok(updated).build();
    }

    // ── DELETE /rooms/{id} ────────────────────────────────────────────────────

    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        if (!roomStore.delete(id)) {
            throw new ResourceNotFoundException("Room", id);
        }
        return Response.noContent().build();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateRoom(Room room) {
        if (room == null) {
            throw new BadRequestException("Request body must not be empty.");
        }
        if (room.getName() == null || room.getName().isBlank()) {
            throw new BadRequestException("Room 'name' is required.");
        }
        if (room.getBuilding() == null || room.getBuilding().isBlank()) {
            throw new BadRequestException("Room 'building' is required.");
        }
    }
}
