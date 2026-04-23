package com.smartcampus.store;

import com.smartcampus.model.Room;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for Room data.
 * Singleton scoped via HK2 - no database required.
 */
@Singleton
public class RoomStore {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private int idCounter = 1;

    public RoomStore() {
        // Seed with sample data
        add(new Room("R001", "Server Room A", "Block A", 1));
        add(new Room("R002", "Lecture Hall B1", "Block B", 2));
        add(new Room("R003", "Lab 3C", "Block C", 3));
    }

    /** Returns all rooms as an unmodifiable list. */
    public List<Room> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(rooms.values()));
    }

    /** Returns the room with the given id, or empty if not found. */
    public Optional<Room> findById(String id) {
        return Optional.ofNullable(rooms.get(id));
    }

    /** Persists a new room, generating an id if one is not set. */
    public Room add(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            room.setId("R" + String.format("%03d", idCounter++));
        }
        rooms.put(room.getId(), room);
        return room;
    }

    /** Replaces an existing room. Returns the updated room or empty if not found. */
    public Optional<Room> update(String id, Room updated) {
        if (!rooms.containsKey(id)) return Optional.empty();
        updated.setId(id);
        rooms.put(id, updated);
        return Optional.of(updated);
    }

    /** Removes a room by id. Returns true if it existed. */
    public boolean delete(String id) {
        return rooms.remove(id) != null;
    }

    /** Returns true if the room id exists in the store. */
    public boolean exists(String id) {
        return rooms.containsKey(id);
    }
}
