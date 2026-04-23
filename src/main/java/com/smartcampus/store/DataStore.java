package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store for the Smart Campus API.
 *
 * <p><strong>Why ConcurrentHashMap?</strong><br>
 * JAX-RS creates a NEW resource class instance for every HTTP request (per-request scope).
 * Multiple concurrent requests therefore run in separate threads, all sharing this one
 * DataStore instance. ConcurrentHashMap allows safe concurrent reads and fine-grained
 * locking on writes, preventing data corruption without blocking every thread on a
 * single coarse lock.</p>
 */
public class DataStore {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static final DataStore INSTANCE = new DataStore();

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ── Internal storage ──────────────────────────────────────────────────────

    /** Room map: roomId → Room */
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    /** Sensor map: sensorId → Sensor */
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    /**
     * Readings map: sensorId → synchronized list of SensorReadings.
     * The inner list uses Collections.synchronizedList so that iteration
     * inside getReadingsForSensor is safe under concurrent access.
     */
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    // ── Constructor with seed data ────────────────────────────────────────────

    private DataStore() {
        seedRooms();
        seedSensors();
        seedReadings();
    }

    private void seedRooms() {
        saveRoom(new Room("LIB-301", "Library Quiet Study", 50));
        saveRoom(new Room("LAB-102", "Computer Lab 102",    30));
        saveRoom(new Room("HALL-01", "Main Hall",           200));
    }

    private void seedSensors() {
        Sensor temp = new Sensor("TEMP-001", "Temperature", "ACTIVE",      22.5, "LIB-301");
        Sensor co2  = new Sensor("CO2-001",  "CO2",         "ACTIVE",      415.0, "LAB-102");
        Sensor occ  = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE",  0.0, "LIB-301");

        saveSensor(temp);
        saveSensor(co2);
        saveSensor(occ);

        // Link sensors into their rooms' sensorIds lists
        rooms.get("LIB-301").getSensorIds().add("TEMP-001");
        rooms.get("LIB-301").getSensorIds().add("OCC-001");
        rooms.get("LAB-102").getSensorIds().add("CO2-001");
    }

    private void seedReadings() {
        // Seed one reading for each ACTIVE sensor
        addReading("TEMP-001", new SensorReading(22.5));
        addReading("CO2-001",  new SensorReading(415.0));
    }

    // ── Room operations ───────────────────────────────────────────────────────

    /** Returns an unmodifiable snapshot of all rooms. */
    public Collection<Room> getAllRooms() {
        return Collections.unmodifiableCollection(rooms.values());
    }

    /** Returns the Room for the given id, or null if not found. */
    public Room getRoom(String id) {
        return rooms.get(id);
    }

    /** Saves (inserts or replaces) a room. */
    public void saveRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    /** Removes the room with the given id. No-op if not found. */
    public void deleteRoom(String id) {
        rooms.remove(id);
    }

    // ── Sensor operations ─────────────────────────────────────────────────────

    /** Returns an unmodifiable snapshot of all sensors. */
    public Collection<Sensor> getAllSensors() {
        return Collections.unmodifiableCollection(sensors.values());
    }

    /** Returns the Sensor for the given id, or null if not found. */
    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    /** Saves (inserts or replaces) a sensor. */
    public void saveSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    /**
     * Removes a sensor and its associated readings.
     * Also removes the sensor id from its parent room's sensorIds list.
     *
     * @param id the sensor id to delete
     */
    public void deleteSensor(String id) {
        Sensor sensor = sensors.remove(id);
        sensorReadings.remove(id);

        // Remove from parent room's sensorIds list (cascade)
        if (sensor != null && sensor.getRoomId() != null) {
            Room room = rooms.get(sensor.getRoomId());
            if (room != null) {
                room.getSensorIds().remove(id);
            }
        }
    }

    // ── Reading operations ────────────────────────────────────────────────────

    /**
     * Returns a snapshot copy of readings for the given sensor.
     * A copy is returned so callers cannot mutate the internal list.
     *
     * @param sensorId the sensor whose readings to fetch
     * @return ordered list of readings, or an empty list if none exist
     */
    public List<SensorReading> getReadingsForSensor(String sensorId) {
        List<SensorReading> list = sensorReadings.get(sensorId);
        if (list == null) return Collections.emptyList();
        synchronized (list) {
            return new ArrayList<>(list); // snapshot copy
        }
    }

    /**
     * Appends a reading to the sensor's reading list.
     * Uses computeIfAbsent to safely initialise the list on first use.
     *
     * @param sensorId the owning sensor id
     * @param reading  the reading to append
     */
    public void addReading(String sensorId, SensorReading reading) {
        sensorReadings
                .computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(reading);
    }

    /** Returns true if a room with the given id exists. */
    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    /** Returns true if a sensor with the given id exists. */
    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }
}
