# Contract Audit Service — Step-by-Step Code Explanation

## Overview

The `contract-audit-service` is a minimal reactive microservice built with **Spring Boot 3.3** and **Spring WebFlux**. It exposes a single POST endpoint that accepts contract audit data, logs it to the console, stores it in memory, and returns a success message. It runs inside a Docker container on port **8082** and communicates with other services (like `contract-analyser-spring-ai`) over a shared Docker network.

---

## Project Structure

```
contract-audit-service/
├── .gitignore                  # Files excluded from version control
├── Dockerfile                  # Multi-stage Docker build instructions
├── docker-compose.yml          # Container orchestration config
├── pom.xml                     # Maven project descriptor & dependencies
└── src/main/
    ├── java/com/contract/audit/
    │   ├── ContractAuditServiceApplication.java   # Entry point
    │   ├── dto/
    │   │   └── AuditRequest.java                  # Data Transfer Object (Record)
    │   └── controller/
    │       └── AuditController.java               # REST Controller
    └── resources/
        └── application.properties                 # App configuration
```

---

## Where It Starts — Application Entry Point

### File: `ContractAuditServiceApplication.java`

```java
@SpringBootApplication
public class ContractAuditServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContractAuditServiceApplication.class, args);
    }
}
```

**What happens here:**
1. The JVM starts and invokes `main()`.
2. `SpringApplication.run(...)` boots the Spring context.
3. Spring scans all classes in `com.contract.audit` and its sub-packages for annotations like `@RestController`, `@Service`, etc.
4. An embedded **Netty** server (not Tomcat — because we use WebFlux) starts listening on port `8082`.
5. The application is now ready to receive HTTP requests.

---

## The Input — What the Service Accepts

### File: `AuditRequest.java` (DTO)

```java
public record AuditRequest(
        String contractName,
        String status,
        int wordCount
) {}
```

**What is a record?**
- A Java `record` (introduced in Java 16) is an immutable data carrier.
- The compiler auto-generates: constructor, `getters` (e.g., `contractName()`), `equals()`, `hashCode()`, and `toString()`.
- No need for Lombok `@Data` or manual boilerplate.

**Expected JSON input:**

```json
{
  "contractName": "NDA-2024",
  "status": "ANALYZED",
  "wordCount": 1500
}
```

| Field          | Type    | Description                                  |
|----------------|---------|----------------------------------------------|
| `contractName` | String  | Name/identifier of the contract              |
| `status`       | String  | Current processing status of the contract    |
| `wordCount`    | int     | Number of words in the contract document     |

---

## The Processing — Controller & Business Logic

### File: `AuditController.java`

```java
@Slf4j
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final List<AuditRequest> auditLog = new CopyOnWriteArrayList<>();

    @PostMapping("/log")
    public Mono<String> logAudit(@RequestBody AuditRequest request) {
        log.info("Audit received -> contract: '{}', status: '{}', wordCount: {}",
                request.contractName(), request.status(), request.wordCount());

        auditLog.add(request);

        return Mono.just("Audit logged successfully. Total entries: " + auditLog.size());
    }
}
```

### Step-by-step execution when a request arrives:

| Step | What Happens |
|------|-------------|
| 1    | An HTTP POST request hits `http://<host>:8082/api/audit/log` |
| 2    | Spring WebFlux deserializes the JSON body into an `AuditRequest` record |
| 3    | `@Slf4j` (Lombok) provides a logger — the incoming data is printed to the console |
| 4    | The request object is added to `auditLog`, a thread-safe `CopyOnWriteArrayList` |
| 5    | A `Mono<String>` is returned with a success message and the current total count |
| 6    | Spring serializes the string and sends it back as the HTTP response body |

### Key annotations explained:

| Annotation         | Purpose                                                  |
|--------------------|----------------------------------------------------------|
| `@Slf4j`           | Lombok — injects a `log` field for logging               |
| `@RestController`  | Marks this class as a REST API controller                |
| `@RequestMapping`  | Sets the base URL path (`/api/audit`)                    |
| `@PostMapping`     | Maps HTTP POST to this method (`/log`)                   |
| `@RequestBody`     | Tells Spring to deserialize the request body into the parameter |

### Why `CopyOnWriteArrayList`?

- Thread-safe without explicit synchronization.
- Optimized for read-heavy workloads (writes copy the entire array).
- Perfect for a simple in-memory log in a learning project.
- Data lives only while the container runs — restarting clears it.

### Why `Mono<String>`?

- `Mono` is a Reactor type representing 0 or 1 asynchronous result.
- WebFlux uses reactive types (`Mono`, `Flux`) instead of blocking return types.
- This keeps the service non-blocking and event-driven on Netty.

---

## The Output — What the Service Returns

**HTTP Response:**

| Property     | Value                                          |
|--------------|------------------------------------------------|
| Status Code  | `200 OK`                                       |
| Content-Type | `text/plain`                                   |
| Body         | `Audit logged successfully. Total entries: 1`  |

**Console output (Docker logs):**

```
INFO  c.c.a.controller.AuditController - Audit received -> contract: 'NDA-2024', status: 'ANALYZED', wordCount: 1500
```

---

## Configuration

### File: `application.properties`

```properties
server.port=8082
spring.application.name=contract-audit-service
```

| Property                  | Purpose                                         |
|---------------------------|-------------------------------------------------|
| `server.port=8082`        | Netty listens on port 8082 inside the container |
| `spring.application.name` | Identifies the service in logs and discovery    |

---

## Containerization — Dockerfile

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build stages explained:

| Stage | Image Used                     | Purpose                                      |
|-------|--------------------------------|----------------------------------------------|
| 1     | `maven:3.9-eclipse-temurin-21` | Compiles Java source and packages a JAR      |
| 2     | `eclipse-temurin:21-jre`       | Lightweight runtime to execute the JAR       |

### Why multi-stage?

- The build stage has Maven + JDK (~800MB). The run stage has only JRE (~200MB).
- Final image is small and contains no source code or build tools.
- Dependencies are cached in a separate Docker layer (`dependency:go-offline`) so only source code changes trigger a recompile.

---

## Infrastructure — docker-compose.yml

```yaml
version: "3.8"

services:
  contract-audit-service:
    build: .
    container_name: contract-audit-service
    ports:
      - "8082:8082"
    networks:
      - contract-network

networks:
  contract-network:
    external: true
```

### What each section does:

| Section                    | Purpose                                                        |
|----------------------------|----------------------------------------------------------------|
| `build: .`                 | Builds the image using the Dockerfile in current directory     |
| `container_name`           | Fixes the container name (used as DNS hostname on the network) |
| `ports: "8082:8082"`       | Maps host port 8082 to container port 8082                     |
| `networks: contract-network` | Attaches to the shared Docker network                       |
| `external: true`           | Network must already exist (created manually with `docker network create`) |

### How inter-service communication works:

```
┌──────────────────────────┐        HTTP POST         ┌──────────────────────────┐
│ contract-analyser-spring-ai │ ──────────────────────> │  contract-audit-service   │
│        (port 8081)         │                          │       (port 8082)         │
└──────────────────────────┘                          └──────────────────────────┘
              │                                                     │
              └──────────── contract-network (Docker DNS) ──────────┘
```

From inside `contract-analyser-spring-ai`, the call would be:
```
POST http://contract-audit-service:8082/api/audit/log
```

Docker resolves `contract-audit-service` to the correct container IP automatically.

---

## Where It Ends — Request Lifecycle Summary

```
[Client / Other Service]
        │
        │  POST /api/audit/log  { JSON body }
        ▼
[Netty Server — port 8082]
        │
        │  Deserialize JSON → AuditRequest record
        ▼
[AuditController.logAudit()]
        │
        ├── 1. Log to console (SLF4J)
        ├── 2. Store in CopyOnWriteArrayList (in-memory)
        └── 3. Return Mono<String> success message
        │
        ▼
[HTTP 200 Response — plain text success message]
```

---

## How to Run (No Java/IDE Needed)

```bash
# 1. Create the shared network (only once)
docker network create contract-network

# 2. Build and start the service
docker-compose up --build

# 3. Test with curl
curl -X POST http://localhost:8082/api/audit/log \
  -H "Content-Type: application/json" \
  -d '{"contractName":"NDA-2024","status":"ANALYZED","wordCount":1500}'

# 4. View logs
docker logs contract-audit-service

# 5. Stop the service
docker-compose down
```

---

## Dependencies Summary (pom.xml)

| Dependency                          | Purpose                                    |
|-------------------------------------|--------------------------------------------|
| `spring-boot-starter-webflux`       | Reactive web framework (Netty + Reactor)   |
| `lombok`                            | Reduces boilerplate (`@Slf4j` for logging) |
| `spring-boot-maven-plugin`          | Packages the app as an executable JAR      |

---

## Key Concepts Demonstrated

| Concept                  | Where It's Used                              |
|--------------------------|----------------------------------------------|
| Reactive Programming     | `Mono<String>` return type in controller     |
| Java Records             | `AuditRequest` — immutable DTO               |
| Non-blocking I/O         | Netty server (via WebFlux starter)           |
| Multi-stage Docker Build | Dockerfile — separate build and run stages   |
| Service Networking       | docker-compose + external Docker network     |
| Thread Safety            | `CopyOnWriteArrayList` for concurrent access |
| Lombok                   | `@Slf4j` annotation for zero-config logging  |
