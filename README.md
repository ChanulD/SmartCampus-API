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

## Part 1 — Service Architecture & Setup
## Q1 — JAX-RS Resource Lifecycle & Thread Safety
By default, JAX-RS creates a new instance of every resource class for each incoming HTTP request (per-request scope). This means the JAX-RS runtime does not treat resource classes as singletons a fresh object is instantiated for every call and then discarded. 
Because a new instance is created per request, any instance-level fields are reset on every invocation. Storing shared data in an instance field would mean that data is lost after each request completes. To persist and share state across requests, an external singleton is required. This project uses DataStore a statically, eagerly-initialized singleton for that purpose. 
Since multiple threads may handle concurrent requests simultaneously, all maps in DataStore use ConcurrentHashMap. This provides atomic, thread-safe get, put, and remove operations without requiring explicit synchronized blocks, thereby preventing race conditions such as two concurrent POST requests both inserting the same sensor ID and corrupting the collection.

## Q2 — Why HATEOAS Is a Hallmark of Advanced RESTful Design
HATEOAS (Hypermedia as the Engine of Application State) means the server embeds hyperlinks inside every response, pointing to related resources and valid next actions. For example, the discovery endpoint at /api/v1 returns "_links": { "rooms": "/api/v1/rooms", "sensors": "/api/v1/sensors" }.
Key benefits over static documentation:
•	Single entry point: A client only needs to know the base URL. It can navigate the entire API dynamically by following links in responses, rather than memorizing every endpoint from external docs. 
•	Decoupling: If server-side URL structures change, only the embedded links in responses need updating. Clients that follow links rather than hard-coding paths are unaffected by URL changes. 
•	Self-documenting runtime behavior: The set of links present in a response indicates exactly which actions are currently valid for that resource state, reducing the chance of clients attempting invalid operations. 
•	Always up-to-date: Static documentation goes stale. Hypermedia links are generated live from the server and always reflect the current API structure.


## Part 2 — Room Management
## Q1 — IDs Only vs Full Objects in List Responses
When returning a collection of rooms, two common approaches exist: 
Returning only IDs (e.g., ["LIB-301", "LAB-102", "HALL-01"]): Very low bandwidth, but forces clients to make N additional GET requests to retrieve details for each room. For a list of 100 rooms this means 100 extra HTTP round-trips, adding significant latency and additional server load. 
Returning full objects (chosen approach): Slightly larger payload, but the client receives everything it needs in a single round-trip. For typical campus management dashboards that immediately display room names, capacities, and sensor counts, this eliminates redundant follow-up requests. For this API's expected usage patterns and collection sizes, full objects provide a significantly better developer experience with minimal bandwidth cost.

## Q2 — Is DELETE Idempotent?
Yes, in terms of server state — though the HTTP response codes differ between calls. REST defines idempotency as: making the same request multiple times produces the same resulting server state. 
In this implementation, a first DELETE /api/v1/rooms/HALL-01 (assuming the room has no sensors) removes the room and returns 204 No Content. A second identical request finds the room already gone and returns 404 Not Found. The server state is identical after both calls — the room is absent — satisfying the idempotency requirement. The differing status codes (204 vs 404) are acceptable and widely considered correct REST behaviour: a 404 on a repeated DELETE communicates "the resource you wanted gone is indeed gone."






## Part 3 — Sensor Operations & Linking
## Q1 — Consequences of @Consumes(APPLICATION_JSON) Mismatch
The @Consumes(MediaType.APPLICATION_JSON) annotation instructs the JAX-RS runtime to only accept requests whose Content-Type header is application/json. If a client sends Content-Type: text/plain or Content-Type: application/xml, Jersey responds immediately with HTTP 415 Unsupported Media Type before the resource method is even invoked  no custom code is required. 
This provides a strong content-type contract enforced entirely at the framework level: malformed or wrongly-typed requests are rejected automatically, protecting the resource method from receiving data that Jackson cannot deserialise into the target POJO.

## Q2 — @QueryParam vs Path Segment for Filtering
Design	Example	Semantics
Query Parameter	/api/v1/sensors?type=CO2	Filters a collection
Path Segment	/api/v1/sensors/type/CO2	Implies CO2 is a uniquely addressable resource

Query parameters are semantically correct for filtering because they narrow down a collection rather than address a distinct resource. A path like /sensors/type/CO2 incorrectly implies there is a stable, individually addressable resource at that URL — which is not the case, as CO2 is a filter criterion, not a resource identifier. 
Additionally, query parameters are naturally optional: omitting ?type= returns the full list with no code changes. Multiple filters can be combined (e.g., ?type=CO2&status=ACTIVE) without complicating the URL path structure. This is the standard REST convention for search and filter operations as established by RFC 3986, and is universally understood by client developers.



## Part 4 — Deep Nesting with Sub-Resources
## Q1 — Architectural Benefits of the Sub-Resource Locator Pattern
The Sub-Resource Locator pattern allows SensorResource to delegate all processing for /{sensorId}/readings to a dedicated SensorReadingResource class by returning an instance of it from a method annotated only with @Path (no HTTP method annotation). Benefits include:
1.	Separation of concerns: Each class has a single responsibility. SensorResource handles sensor CRUD; SensorReadingResource handles reading history. Neither class needs to know about the other's internal logic.
2.	Reduced class size and complexity: A monolithic resource handling every nested endpoint would grow to hundreds of lines, making it hard to read, test, and maintain. Delegation keeps each file focused.
3.	Contextual instantiation: SensorReadingResource receives sensorId as a constructor argument, allowing all its methods to contextualise their logic without parsing path parameters themselves.
4.	Scalability: Adding new nested sub-resources (e.g., /sensors/{id}/alerts) requires only a new class and one additional locator method  existing classes are untouched, respecting the Open/Closed Principle.

## Part 5 — Advanced Error Handling, Exception Mapping & Logging
## Q1 — Why HTTP 422 Over 404 for a Missing Reference in a JSON Payload
•	404 Not Found signals that the requested URL resource does not exist. It applies when the path itself is invalid.
•	422 Unprocessable Entity signals that the request was syntactically valid JSON, correctly formed, and targeted a valid URL — but contains a semantic error inside the payload.
When a client POSTs { "roomId": "HELLO-001" } to /api/v1/sensors, the URL /api/v1/sensors is perfectly valid (404 does not apply). The JSON parses correctly (400 does not apply). The problem is the value of roomId — it references a room that does not exist. HTTP 422 precisely communicates "I understood and parsed your request, but I cannot process it because the content is semantically invalid." This gives API consumers a clear, unambiguous signal to fix the payload value, not the URL structure.



## Q2 — Cybersecurity Risks of Exposing Java Stack Traces
Exposing raw Java stack traces to external API consumers reveals information that can be exploited:
1.	Internal package structure: Class names like com.smartcampus.store.DataStore expose the exact package layout, helping attackers understand the codebase and craft targeted exploits.
2.	Framework and library versions: Stack traces often include library identifiers (e.g., jersey-server-2.40). Attackers can search CVE databases for known vulnerabilities in those exact versions.
3.	Business logic flow: The call stack reveals which methods were invoked in what order, exposing internal processing logic that should remain opaque to external users.
4.	File and line numbers: These reveal the codebase structure and can help attackers identify specific vulnerable lines to target.
5.	Data layer details: If an ORM or query layer is involved, stack traces can expose table names, column names, or connection details.
The GlobalExceptionMapper<Throwable> mitigates all of these risks by logging the full stack trace server-side only (for developers) while returning a generic "An unexpected error occurred" message to the client — leaking nothing about the internal system.

## Q3 — Why Use JAX-RS Filters Instead of Manual Logging in Every Method
JAX-RS filters (ContainerRequestFilter / ContainerResponseFilter) apply automatically to every single request and response without any modification to existing resource methods. The alternative — manually inserting Logger.info() into each endpoint — has serious disadvantages:
1.	DRY violation: Logging code is duplicated across every resource method. Any change (log format, correlation ID, log level) requires editing every file.
2.	Human error: A developer writing a new endpoint can simply forget to add the logging statement, creating silent gaps in observability.
3.	Separation of concerns: Logging is a cross-cutting infrastructure concern and does not belong mixed with business domain logic.
4.	Guaranteed coverage: Filters are invoked by the JAX-RS framework regardless of which endpoint is called or which exception path is taken — no code path is silently missed.
5.	Easy extension: Adding rate-limit tracking, authentication checks, or a request correlation ID to every call requires changing only the single filter class, not every resource method throughout the codebase.
