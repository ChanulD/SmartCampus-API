package com.smartcampus.model;

/**
 * Represents a sensor installed in a campus room.
 *
 * Valid status values:
 *   "ACTIVE"      – sensor is online and reporting readings
 *   "MAINTENANCE" – sensor is temporarily offline for servicing
 *   "OFFLINE"     – sensor is unreachable or decommissioned
 */
public class Sensor {

    private String id;
    private String type;
    private String status;
    private double currentValue;
    private String roomId;

    // ── Constructors ──────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialisation. */
    public Sensor() {
    }

    /**
     * Full constructor for creating a sensor with all fields.
     *
     * @param id           unique sensor identifier
     * @param type         sensor category (e.g. "TEMPERATURE", "HUMIDITY", "MOTION")
     * @param status       operational status: "ACTIVE", "MAINTENANCE", or "OFFLINE"
     * @param currentValue most recent measurement recorded by this sensor
     * @param roomId       ID of the room this sensor is installed in
     */
    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id           = id;
        this.type         = type;
        this.status       = status;
        this.currentValue = currentValue;
        this.roomId       = roomId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
