# Smart Campus Sensor & Room Management API

A JAX-RS (Jersey 2.40) REST API for managing university campus rooms and IoT sensors, packaged as a WAR and deployed on Apache Tomcat 9. All data is stored in-memory using a thread-safe singleton — no database required.

---

## Build & Run

### Prerequisites

| Tool  | Version |
|-------|---------|
| Java  | 11+     |
| Maven | 3.6+    |
| Tomcat | 9.x (optional — Cargo provides embedded) |

### Option A — Embedded Tomcat via Cargo (recommended for development)

```bash
mvn clean package
mvn cargo:run
```

API is available at: **http://localhost:8080/api/v1**

### Option B — Deploy WAR to standalone Tomcat

```bash
mvn clean package
# Copy WAR to Tomcat:
cp target/smart-campus-api.war $CATALINA_HOME/webapps/ROOT.war
# Start Tomcat, then access:
# http://localhost:8080/api/v1
```

---

## Project Structure

```
src/main/java/com/smartcampus/
├── SmartCampusApplication.java         ← @ApplicationPath("/api/v1"), ResourceConfig
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   ├── SensorReading.java
│   └── ErrorResponse.java
├── store/
│   └── DataStore.java                  ← Thread-safe singleton (ConcurrentHashMap)
├── resource/
│   ├── DiscoveryResource.java          ← GET /api/v1  (HATEOAS root)
│   ├── RoomResource.java               ← CRUD /api/v1/rooms
│   ├── SensorResource.java             ← CRUD /api/v1/sensors
│   └── SensorReadingResource.java      ← Sub-resource /api/v1/sensors/{id}/readings
├── exception/
│   ├── RoomNotEmptyException.java           → HTTP 409
│   ├── LinkedResourceNotFoundException.java → HTTP 422
│   ├── SensorUnavailableException.java      → HTTP 403
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── GlobalExceptionMapper.java           → HTTP 500 (catch-all)
└── filter/
    └── LoggingFilter.java              ← Logs every request + response
```

---

## API Endpoints

### Discovery
| Method | Path       | Description          | Status |
|--------|-----------|----------------------|--------|
| `GET`  | `/api/v1` | HATEOAS API root     | 200    |

### Rooms — `/api/v1/rooms`
| Method   | Path                   | Description         | Success | Errors      |
|----------|------------------------|---------------------|---------|-------------|
| `GET`    | `/rooms`               | List all rooms      | 200     |             |
| `POST`   | `/rooms`               | Create a room       | 201     | 400, 409    |
| `GET`    | `/rooms/{roomId}`      | Get room by ID      | 200     | 404         |
| `DELETE` | `/rooms/{roomId}`      | Delete a room       | 204     | 404, 409    |

### Sensors — `/api/v1/sensors`
| Method | Path                              | Description            | Success | Errors         |
|--------|----------------------------------|------------------------|---------|----------------|
| `GET`  | `/sensors[?type=]`               | List all / filter      | 200     |                |
| `POST` | `/sensors`                       | Create a sensor        | 201     | 400, 409, 422  |
| `GET`  | `/sensors/{sensorId}`            | Get sensor by ID       | 200     | 404            |

### Sensor Readings — `/api/v1/sensors/{sensorId}/readings`
| Method | Path                                   | Description       | Success | Errors      |
|--------|----------------------------------------|-------------------|---------|-------------|
| `GET`  | `/sensors/{sensorId}/readings`         | Get reading history | 200   | 404         |
| `POST` | `/sensors/{sensorId}/readings`         | Add a new reading | 201     | 403, 404    |

---

## Sample cURL Commands

### 1 — API Discovery (HATEOAS)
```bash
curl -s http://localhost:8080/api/v1 | python -m json.tool
```

### 2 — List all pre-seeded rooms
```bash
curl -s http://localhost:8080/api/v1/rooms
```

### 3 — Create a new room
```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CONF-101","name":"Conference Room","capacity":20}'
```

### 4 — Create a sensor linked to a room
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-002","type":"Temperature","status":"ACTIVE","currentValue":0,"roomId":"LAB-102"}'
```

### 5 — Filter sensors by type
```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 6 — Post a sensor reading
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.3}'
```

### 7a — Delete a room with no sensors (204)
```bash
curl -s -o /dev/null -w "%{http_code}" \
  -X DELETE http://localhost:8080/api/v1/rooms/CONF-101
# → 204
```

### 7b — Delete a room with sensors (409 Conflict)
```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
# → {"status":409,"error":"Conflict","message":"Room 'LIB-301' cannot be deleted..."}
```

---

## Error Response Format

All errors return a consistent JSON body — **no HTML stack traces are ever exposed to clients**.

```json
{
  "status"    : 404,
  "error"     : "Not Found",
  "message"   : "Sensor with id 'S999' was not found.",
  "timestamp" : 1714000000000
}
```

---

## Pre-seeded Data

### Rooms
| ID       | Name                  | Capacity |
|----------|-----------------------|----------|
| LIB-301  | Library Quiet Study   | 50       |
| LAB-102  | Computer Lab 102      | 30       |
| HALL-01  | Main Hall             | 200      |

### Sensors
| ID       | Type        | Status      | Value | Room    |
|----------|-------------|-------------|-------|---------|
| TEMP-001 | Temperature | ACTIVE      | 22.5  | LIB-301 |
| CO2-001  | CO2         | ACTIVE      | 415.0 | LAB-102 |
| OCC-001  | Occupancy   | MAINTENANCE | 0.0   | LIB-301 |

---

## Report — Conceptual Questions

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new resource class instance for every incoming HTTP request** (per-request scope). This means instance variables are not shared between requests — each request gets a fresh object. If resources stored data as instance variables, concurrent requests would each have their own copy and no data would ever persist across calls.

To solve this, shared mutable state must live **outside** the resource class — in a singleton `DataStore` backed by `ConcurrentHashMap`. `ConcurrentHashMap` allows safe concurrent reads and fine-grained segment-level locking on writes, preventing data corruption without forcing every thread to wait on a single coarse lock. This prevents both data loss and race conditions under concurrent request load.

---

### Part 1.2 — HATEOAS

**HATEOAS** (Hypermedia as the Engine of Application State) means API responses include hypermedia links to related actions and resources, not just data. For example, the `GET /api/v1` response includes `"_links": { "rooms": "/api/v1/rooms", "sensors": "/api/v1/sensors" }`.

**Benefits over static documentation:**
- Clients can **discover available operations at runtime** without consulting external docs.
- APIs can **evolve their URL structure** without breaking clients (clients follow links, not hardcoded URLs).
- Reduces onboarding friction — a new client can start from the root and navigate the entire API.
- Mirrors how browsers navigate the web: humans follow links, not memorised paths.

---

### Part 2.1 — IDs vs Full Objects in List Responses

Returning **only IDs** is bandwidth-efficient but forces clients to make N additional requests to fetch each object (the "N+1 problem"), increasing latency and server load. Returning **full objects** increases payload size but eliminates all round-trips.

For a campus management API with moderate entity counts (dozens of rooms/sensors), returning full objects is preferred for usability and simplicity. Large-scale APIs can support both via a query parameter (e.g., `?expand=true`).

---

### Part 2.2 — DELETE Idempotency

Yes, `DELETE` is **idempotent** by REST convention. The first DELETE removes the room and returns 204. Subsequent identical DELETE requests return 404 (room not found). Both outcomes result in the **same final server state**: the room does not exist.

Idempotency refers to server-side state, not response codes — the HTTP specification explicitly clarifies this distinction. Returning 404 on a repeated DELETE is correct and does not violate idempotency.

---

### Part 3.1 — @Consumes and Media Type Mismatches

If a client sends `Content-Type: text/plain` to an endpoint annotated `@Consumes(APPLICATION_JSON)`, the JAX-RS runtime returns **HTTP 415 Unsupported Media Type automatically**, before the resource method is ever invoked. The runtime checks the `Content-Type` header against all `@Consumes` declarations and rejects mismatches at the framework layer. The Jackson `MessageBodyReader` only activates for `application/json` content.

---

### Part 3.2 — @QueryParam vs Path Segment for Filtering

Query parameters (`?type=CO2`) are semantically correct for filtering because the **resource identity doesn't change** — `/sensors` is still the sensors collection. A path segment (`/sensors/type/CO2`) implies a distinct resource at that path, which is semantically incorrect for a filtered view.

Additional advantages of query params:
- Support multiple filters naturally: `?type=CO2&status=ACTIVE`
- No URL changes required to add new filter criteria
- Industry standard (GitHub, Twitter, Stripe APIs all use query params for filtering)
- Path remains clean and consistent for bookmarking and caching

---

### Part 4.1 — Sub-Resource Locator Benefits

The sub-resource locator pattern (returning an object instance from a method with `@Path` but no HTTP verb) allows complex APIs to **delegate to specialised classes**. Benefits:

1. **Single Responsibility** — `SensorResource` handles sensors; `SensorReadingResource` handles readings only.
2. **Independent testability** — each class can be unit-tested in isolation.
3. **No bloat** — adding `/readings` endpoints does not inflate `SensorResource`.
4. **Mirrors the domain hierarchy** — readings belong to sensors; the class structure matches the data model.
5. **Clean URL nesting** — JAX-RS naturally produces `/sensors/{id}/readings` without manual path string concatenation.

---

### Part 5.2 — 422 vs 404 for Missing roomId Reference

**404 Not Found** means the requested URL was not found. In this case, the URL `/api/v1/sensors` is perfectly valid. The **problem is inside the request body** — the `roomId` field references a non-existent entity.

**422 Unprocessable Entity** means the request was syntactically well-formed (valid JSON) but **semantically invalid** — a business rule was violated (the room must exist before a sensor can be assigned to it). 422 communicates "the data is wrong", not "the URL is wrong". This precision helps API clients display the correct user-facing error message and take corrective action.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

Stack traces expose:
1. **Internal class names and package structure** — helps attackers map the application.
2. **Library names and exact versions** — enables targeted CVE exploit lookups.
3. **File-system paths** on the server (e.g., `/home/app/target/classes/...`).
4. **Database or query details** if an SQL exception propagates.
5. **Application logic flow** — reveals what conditions trigger failures, aiding targeted fuzzing.

The correct approach is to **log the full stack trace server-side at SEVERE level** (so engineers can diagnose issues) and return only a generic message to the client: `"An unexpected error occurred. Please contact the system administrator."` This is enforced by `GlobalExceptionMapper`.

---

### Part 5.5 — Filters vs Manual Logging

Using a JAX-RS `ContainerRequestFilter`/`ContainerResponseFilter` for logging is superior to inserting `Logger.info()` calls in every resource method because:

1. **Single point of truth** — new endpoints are automatically logged with zero filter changes.
2. **No risk of omission** — impossible to "forget" to add logging to a new method.
3. **Consistent format** — every log line is guaranteed to use the same structure.
4. **Separation of Concerns** — resource methods focus purely on business logic; the filter owns logging.
5. **AOP alignment** — mirrors Aspect-Oriented Programming patterns used in production systems (Spring AOP, CDI interceptors).
6. **Easy to toggle** — comment out `@Provider` to disable all request/response logging globally in one place.
