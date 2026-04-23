// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical room in the Smart Campus.
 * A room may contain zero or more sensors, tracked by their IDs.
 */
public class Room {

    private String       id;
    private String       name;
    private int          capacity;
    private List<String> sensorIds;

    // ── Constructors ──────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialisation. */
    public Room() {
        this.sensorIds = new ArrayList<>();
    }

    /**
     * Convenience constructor for creating a room without pre-assigned sensors.
     *
     * @param id       unique room identifier
     * @param name     human-readable room name
     * @param capacity maximum occupancy of the room
     */
    public Room(String id, String name, int capacity) {
        this.id        = id;
        this.name      = name;
        this.capacity  = capacity;
        this.sensorIds = new ArrayList<>();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getSensorIds() {
        return sensorIds;
    }

    /**
     * Sets the list of sensor IDs for this room.
     * Guards against null: if {@code null} is passed, an empty list is used instead.
     *
     * @param sensorIds list of sensor ID strings, or null
     */
    public void setSensorIds(List<String> sensorIds) {
        this.sensorIds = (sensorIds != null) ? sensorIds : new ArrayList<>();
    }
}
