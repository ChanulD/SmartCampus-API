package com.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Represents a single data reading produced by a sensor.
 *
 * value    – the raw measurement (e.g. 22.5 for °C)
 * unit     – measurement unit (e.g. "°C", "%", "lux")
 * recordedAt – ISO-8601 timestamp of when the reading was captured
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorReading {

    private String id;
    private String sensorId;
    private double value;
    private String unit;
    private String recordedAt; // ISO-8601 string for simplicity (no DB layer)

    // ── Constructors ──────────────────────────────────────────────────────────

    public SensorReading() {}

    public SensorReading(String id, String sensorId, double value, String unit) {
        this.id         = id;
        this.sensorId   = sensorId;
        this.value      = value;
        this.unit       = unit;
        this.recordedAt = Instant.now().toString();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getId()              { return id; }
    public void   setId(String id)     { this.id = id; }

    public String getSensorId()                  { return sensorId; }
    public void   setSensorId(String sensorId)   { this.sensorId = sensorId; }

    public double getValue()               { return value; }
    public void   setValue(double value)   { this.value = value; }

    public String getUnit()              { return unit; }
    public void   setUnit(String unit)   { this.unit = unit; }

    public String getRecordedAt()                    { return recordedAt; }
    public void   setRecordedAt(String recordedAt)   { this.recordedAt = recordedAt; }
}
