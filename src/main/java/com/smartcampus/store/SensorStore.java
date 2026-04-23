package com.smartcampus.store;

import com.smartcampus.model.Sensor;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory store for Sensor data.
 * Singleton scoped via HK2 - no database required.
 */
@Singleton
public class SensorStore {

    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private int idCounter = 1;

    public SensorStore() {
        // Seed with sample data
        add(new Sensor("S001", "Temp Sensor A1", "TEMPERATURE", "R001", "ACTIVE"));
        add(new Sensor("S002", "Humidity Sensor B1", "HUMIDITY",    "R002", "ACTIVE"));
        add(new Sensor("S003", "Motion Sensor C3",  "MOTION",       "R003", "INACTIVE"));
    }

    /** Returns all sensors. */
    public List<Sensor> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(sensors.values()));
    }

    /** Returns all sensors belonging to a specific room. */
    public List<Sensor> findByRoomId(String roomId) {
        return sensors.values().stream()
                .filter(s -> roomId.equals(s.getRoomId()))
                .collect(Collectors.toList());
    }

    /** Returns the sensor with the given id, or empty if not found. */
    public Optional<Sensor> findById(String id) {
        return Optional.ofNullable(sensors.get(id));
    }

    /** Persists a new sensor, generating an id if one is not set. */
    public Sensor add(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            sensor.setId("S" + String.format("%03d", idCounter++));
        }
        sensors.put(sensor.getId(), sensor);
        return sensor;
    }

    /** Replaces an existing sensor. Returns updated or empty if not found. */
    public Optional<Sensor> update(String id, Sensor updated) {
        if (!sensors.containsKey(id)) return Optional.empty();
        updated.setId(id);
        sensors.put(id, updated);
        return Optional.of(updated);
    }

    /** Removes a sensor by id. Returns true if it existed. */
    public boolean delete(String id) {
        return sensors.remove(id) != null;
    }

    /** Returns true if sensor exists. */
    public boolean exists(String id) {
        return sensors.containsKey(id);
    }
}
