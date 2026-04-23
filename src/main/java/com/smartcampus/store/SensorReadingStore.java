package com.smartcampus.store;

import com.smartcampus.model.SensorReading;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory store for SensorReading data.
 * Singleton scoped via HK2 - no database required.
 */
@Singleton
public class SensorReadingStore {

    private final Map<String, SensorReading> readings = new ConcurrentHashMap<>();
    private int idCounter = 1;

    public SensorReadingStore() {
        // Seed with sample readings
        add(new SensorReading("SR001", "S001", 22.5, "°C"));
        add(new SensorReading("SR002", "S001", 23.1, "°C"));
        add(new SensorReading("SR003", "S002", 65.0, "%"));
    }

    /** Returns all readings. */
    public List<SensorReading> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(readings.values()));
    }

    /** Returns all readings for a specific sensor. */
    public List<SensorReading> findBySensorId(String sensorId) {
        return readings.values().stream()
                .filter(r -> sensorId.equals(r.getSensorId()))
                .collect(Collectors.toList());
    }

    /** Returns a single reading by id, or empty if not found. */
    public Optional<SensorReading> findById(String id) {
        return Optional.ofNullable(readings.get(id));
    }

    /** Persists a new reading, generating an id if one is not set. */
    public SensorReading add(SensorReading reading) {
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId("SR" + String.format("%03d", idCounter++));
        }
        readings.put(reading.getId(), reading);
        return reading;
    }

    /** Removes a reading by id. Returns true if it existed. */
    public boolean delete(String id) {
        return readings.remove(id) != null;
    }

    /** Deletes all readings belonging to a given sensor (cascade support). */
    public void deleteBySensorId(String sensorId) {
        readings.values().removeIf(r -> sensorId.equals(r.getSensorId()));
    }
}
