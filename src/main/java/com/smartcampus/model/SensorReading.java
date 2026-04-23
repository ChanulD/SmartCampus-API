// Name : W.A.C.D.Wijesinghe / IIT ID : 20241938 / UoW ID : w2151956
package com.smartcampus.model;

import java.util.UUID;

/**
 * Represents a single timestamped data reading captured by a sensor.
 *
 * The {@code timestamp} field stores epoch milliseconds
 * (compatible with {@link System#currentTimeMillis()}).
 */
public class SensorReading {

    private String id;
    private long   timestamp;
    private double value;

    // ── Constructors ──────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialisation. */
    public SensorReading() {
    }

    /**
     * Convenience constructor that auto-generates a UUID id and captures
     * the current system time as the timestamp.
     *
     * @param value the measurement value recorded by the sensor
     */
    public SensorReading(double value) {
        this.id        = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value     = value;
    }

    /**
     * Full constructor for explicitly supplying all fields.
     *
     * @param id        unique reading identifier (UUID string recommended)
     * @param timestamp epoch milliseconds when the reading was captured
     * @param value     the measurement value recorded by the sensor
     */
    public SensorReading(String id, long timestamp, double value) {
        this.id        = id;
        this.timestamp = timestamp;
        this.value     = value;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
