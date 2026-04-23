package com.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a physical sensor installed in a campus room.
 *
 * Fields are intentionally minimal; extend to match your coursework schema.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sensor {

    private String id;
    private String name;
    private String type;   // e.g. TEMPERATURE, HUMIDITY, MOTION
    private String roomId;
    private String status; // ACTIVE | INACTIVE

    // ── Constructors ────────────────────────────────────────────────────────

    public Sensor() {}

    public Sensor(String id, String name, String type, String roomId, String status) {
        this.id     = id;
        this.name   = name;
        this.type   = type;
        this.roomId = roomId;
        this.status = status;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getId()              { return id; }
    public void   setId(String id)     { this.id = id; }

    public String getName()            { return name; }
    public void   setName(String name) { this.name = name; }

    public String getType()            { return type; }
    public void   setType(String type) { this.type = type; }

    public String getRoomId()              { return roomId; }
    public void   setRoomId(String roomId) { this.roomId = roomId; }

    public String getStatus()              { return status; }
    public void   setStatus(String status) { this.status = status; }
}
