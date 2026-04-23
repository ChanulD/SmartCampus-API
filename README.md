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
The JAX-RS specification defines a per-request lifecycle as the default behaviour for resource classes. Rather than reusing a shared instance across calls, the runtime constructs a brand-new object for every HTTP request that arrives and disposes of it once the response has been sent. 
This has a direct consequence for state management: any field declared on the resource class is re-initialised with each new object, so it cannot hold data between requests. To maintain shared, persistent application state, the data must live outside the resource class entirely. In this project that responsibility belongs to DataStore, a static singleton that is initialised once when the application starts and remains alive for its entire lifetime. 
Concurrent HTTP requests are handled on separate threads, which means multiple threads can access DataStore simultaneously. To guard against race conditions for instance, two simultaneous POST requests attempting to register the same sensor ID all internal maps use ConcurrentHashMap. Its built-in atomic operations for reads, writes, and deletions eliminate the need for manual synchronized blocks while still guaranteeing thread-safe access.

## Q2 — Why HATEOAS Is a Hallmark of Advanced RESTful Design
HATEOAS Hypermedia as the Engine of Application State is the principle that every API response should carry navigational links describing what the client can do next, rather than leaving clients to rely on out-of-band documentation. In this project the root discovery endpoint at /api/v1 illustrates this directly: its response includes a links object mapping resource names such as rooms and sensors to their respective collection URLs. 
This approach offers several concrete advantages over static documentation: 
•	Single known entry point: Consumers only need to bookmark the base URL. Every subsequent action can be discovered at runtime by reading the links embedded in each response, removing the need to memorize or hard-code individual paths. 
•	Resilience to URL changes: When the server-side routing is restructured, only the generated link values in responses need updating. Clients that navigate by following links — rather than constructing URLs manually — are completely insulated from such changes. 
•	Runtime discoverability: The links returned alongside a resource communicate precisely which operations are valid at that moment in time, giving developers an accurate, live picture of the API's capabilities rather than a potentially outdated spec.
•	Documentation that cannot go stale: Unlike a written specification, hypermedia links are produced directly by the running server, so they always reflect the true current state of the API. 

## Part 2 — Room Management
## Q1 — IDs Only vs Full Objects in List Responses
There are two common strategies for representing a collection in an API response, each with different trade-offs in terms of network efficiency and client complexity: 
ID-only responses (e.g., ["LIB-301", "LAB-102", "HALL-01"]) keep the initial payload small, but every consumer that needs room details must then fire a separate GET request per room. At scale say, a dashboard listing 100 rooms this produces 100 additional HTTP calls, substantially increasing both round-trip latency and server CPU load. 
Full-object responses (the approach taken here) transfer a slightly larger payload upfront but allow the client to render a complete view name, capacity, sensor count from a single request. Given the typical usage patterns of a campus facilities dashboard and the realistic sizes of these collections, the marginal bandwidth cost is far outweighed by the improvement in responsiveness and simplicity of the client code.

## Q2 — Is DELETE Idempotent?
From a server-state perspective, yes DELETE is idempotent in this implementation, even though the HTTP status code changes between the first and subsequent calls. Idempotency in REST means that repeating the same operation any number of times leaves the system in the same final state. 
Consider issuing DELETE /api/v1/rooms/HALL-01 twice, assuming the room carries no sensors on the first attempt. The initial call successfully removes the room and responds with 204 No Content. The follow-up call targets a room that no longer exists, so the server replies with 404 Not Found. In both cases the outcome is the same: HALL-01 is absent from the system. The 404 is not a sign that idempotency has broken down it simply confirms to the client that the resource they wanted removed is, in fact, gone. This is widely regarded as correct and expected REST behaviour.


## Part 3 — Sensor Operations & Linking
## Q1 — Consequences of @Consumes(APPLICATION_JSON) Mismatch
Annotating a POST method with @Consumes(MediaType.APPLICATION_JSON) instructs the JAX-RS framework to only route the request to that method when the client's Content-Type header declares application/json. Should a client instead submit the request with a header such as text/plain or application/xml, Jersey intercepts the mismatch before the method body is ever executed and immediately returns HTTP 415 Unsupported Media Type. 
This framework-level enforcement acts as an early gate: incorrectly typed payloads are rejected before they reach the Jackson deserialiser, ensuring that the resource method only ever receives data that can be safely mapped to the target POJO. No custom validation code is needed to handle this scenario.

## Q2 — @QueryParam vs Path Segment for Filtering
Design	Example	Semantics
Query Parameter	/api/v1/sensors?type=CO2	Filters a collection
Path Segment	/api/v1/sensors/type/CO2	Implies CO2 is a uniquely addressable resource

Query parameters are the semantically appropriate tool for filtering because they express a constraint applied to a collection, rather than identifying a specific resource. A URL such as /sensors/type/CO2 carries the unintended implication that CO2 is itself a distinct, bookmarkable resource which is misleading, since it is merely a filtering criterion. 
Query parameters also compose naturally: they are optional by design (omitting ?type= simply returns the full unfiltered collection), and multiple criteria can be layered together (e.g., ?type=CO2&status=ACTIVE) without restructuring the URL path. This pattern aligns with RFC 3986's intended use of the query component and matches the expectations of practically every HTTP client developer.



## Part 4 — Deep Nesting with Sub-Resources
## Q1 — Architectural Benefits of the Sub-Resource Locator Pattern
A sub-resource locator is a method annotated solely with @Path carrying no HTTP verb annotation that returns an instance of another class. In this project, SensorResource uses this mechanism to hand off all requests under /{sensorId}/readings to a dedicated SensorReadingResource. The benefits of this delegation are significant: 
1.	Single responsibility: Each class owns one well-defined slice of the API. SensorResource is concerned exclusively with sensor lifecycle operations; SensorReadingResource focuses entirely on reading history. Neither bleeds into the other's domain. 
2.	Manageable file size: Placing every nested route inside one class would produce a bloated, hard-to-navigate controller. Distributing logic across focused classes keeps each file short and straightforward to unit-test. 
3.	Implicit context passing: SensorReadingResource accepts sensorId through its constructor, so every method in that class automatically has the relevant context without needing to re-extract it from the path on each call. 
4.	Extensibility: Introducing an additional nested resource such as /sensors/{id}/alerts means writing one new class and adding a single locator method. All existing code stays unchanged, which is a direct application of the Open/Closed Principle.


## Part 5 — Advanced Error Handling, Exception Mapping & Logging
## Q1 — Why HTTP 422 Over 404 for a Missing Reference in a JSON Payload
Choosing the right status code hinges on precisely what went wrong in the request: 
•	404 Not Found is the appropriate response when the URL itself does not correspond to any known resource the path is unrecognised. 
•	422 Unprocessable Entity applies when the URL is valid, the JSON is well-formed, but the semantic content of the payload cannot be acted upon. 
When a client POSTs { "roomId": "GHOST-99" } to /api/v1/sensors, the endpoint exists and the JSON parses without error, so neither 404 nor 400 applies. The issue is that the supplied roomId value references an entity that is not present in the system. Returning 422 tells the consumer exactly what happened: "your request was structurally correct, but the data inside it refers to something that doesn't exist please fix the value, not the URL." This precision makes debugging far quicker for API clients.

## Q2 — Cybersecurity Risks of Exposing Java Stack Traces
Allowing raw stack traces to reach external consumers is a significant security mistake, as they hand attackers a detailed map of the application's internals: 
•	Package and class names: Fully qualified names like com.smartcampus.store.DataStore reveal the project's internal structure, making it easier to reason about the attack surface. 
•	Dependency fingerprinting: Stack frames typically include third-party library names and versions (e.g., jersey-server-2.40), which an attacker can cross-reference against known CVE databases to find exploitable vulnerabilities. 
•	Execution flow disclosure: The sequence of method calls in a trace exposes the internal control flow of the application — logic that should be entirely opaque to outside parties. 
•	Source file and line references: These can help an attacker narrow down exactly which lines of code are responsible for handling sensitive operations. 
•	Persistence layer hints: When a database or ORM layer is in use, traces may inadvertently reveal table names, column identifiers, or connection-string fragments. 
The GlobalExceptionMapper addresses all of these concerns in one place: the complete stack trace is written to the server log for developer visibility, while the external caller receives only a generic 500 Internal Server Error message with no internal details.

## Q3 — Why Use JAX-RS Filters Instead of Manual Logging in Every Method
Placing Logger.info() calls manually inside every resource method is a workable but fragile approach. JAX-RS filters specifically ContainerRequestFilter and ContainerResponseFilter offer a structurally superior alternative for the following reasons: 
• Avoids code duplication: A single filter class covers the entire API. With manual logging, the same boilerplate must be 1.reproduced in every method, and any update to the log format means editing every one of those locations. 
• Reduces human error: When a developer adds a new endpoint, it is easy to forget to include a logging call. A filter provides coverage automatically, with no action required from the developer. 
• Preserves clean architecture: Logging is infrastructure, not business logic. Mixing the two inside a resource method makes the code harder to read and test. Filters enforce a clean boundary between the two concerns. 
• Complete and reliable coverage: The JAX-RS framework guarantees that registered filters run on every request and response, including paths that end in exceptions. Manual logging silently misses error paths unless the developer explicitly handles them. 
• Centralised evolution: Enriching the logs with a correlation ID, authentication token, or timing metric requires a single change in one file not a refactor spanning every resource class in the project.
