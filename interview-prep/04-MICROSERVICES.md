# Microservices — Interview Preparation

## What are Microservices?

An architectural style where an application is built as a collection of small, independently deployable services, each owning a specific business capability and communicating over the network.

### Monolith vs Microservices

| | Monolith | Microservices |
|--|----------|---------------|
| Deployment | One unit | Many independent units |
| Scaling | Scale whole app | Scale individual services |
| Tech stack | Single | Polyglot (different per service) |
| Failure | One bug can crash all | Isolated failures |
| Complexity | Simpler initially | Distributed system complexity |
| Data | Shared database | Database per service |

> **Your example:** "In my AI Contract Analyzer, I split analysis and audit logging into two services. The audit service could go down for maintenance without affecting the core analysis, thanks to fault isolation."

---

## Microservices Design Patterns

### 1. Database per Service
Each service owns its data. No shared database. Prevents tight coupling.

### 2. API Gateway
Single entry point for all clients. Handles routing, authentication, rate limiting, aggregation.
```
Client → API Gateway → [Service A, Service B, Service C]
```
Tools: Spring Cloud Gateway, Kong, AWS API Gateway.

### 3. Service Discovery
Services register themselves; others find them by name (not hardcoded IPs).
- **Client-side:** Netflix Eureka
- **Server-side:** Kubernetes Service DNS (what you used with Docker DNS)

### 4. Circuit Breaker (you implemented this with Resilience4j)
Prevents cascading failures. If a service fails repeatedly, "open" the circuit and fail fast.
```
CLOSED (normal) → [failures exceed threshold] → OPEN (reject fast)
                → [after wait] → HALF_OPEN (test) → CLOSED or OPEN
```
```java
@CircuitBreaker(name = "auditService", fallbackMethod = "fallback")
public Mono<String> logAudit(...) { ... }

private Mono<String> fallback(..., Throwable t) {
    return Mono.just("Audit service unavailable");
}
```

### 5. Saga Pattern (distributed transactions)
Since there's no shared DB, use a sequence of local transactions with compensating actions on failure.

Two types:
- **Choreography** — services publish events, others react (no central coordinator)
- **Orchestration** — a central orchestrator directs each step

```
Order Saga:
1. Create Order → 2. Reserve Inventory → 3. Process Payment
If payment fails → compensate: release inventory, cancel order
```

### 6. CQRS (Command Query Responsibility Segregation) — on your resume
Separate the write model (commands) from the read model (queries).
```
Commands (write) → Write DB → events → Read DB ← Queries (read)
```
> **Why:** Reads and writes have different needs. You can optimize/scale them separately. Often paired with Event Sourcing.

### 7. Strangler Fig (modernization pattern)
Gradually replace a monolith by routing specific features to new microservices, until the monolith is "strangled".
> **Your relevance:** Your resume mentions "system modernization" — this is the pattern to cite.

### 8. Sidecar Pattern
Deploy a helper container alongside your service (logging, proxy). Used in service mesh (Istio).

### 9. Bulkhead Pattern
Isolate resources (thread pools) per dependency, so one slow service doesn't exhaust all threads.

---

## Inter-Service Communication

### Synchronous (HTTP/REST)
```java
// WebClient (reactive, non-blocking) — you used this
webClient.post().uri("/api/audit/log").bodyValue(body)
    .retrieve().bodyToMono(String.class);

// RestTemplate (blocking, legacy)
restTemplate.postForObject(url, body, String.class);

// Feign (declarative HTTP client)
@FeignClient(name = "audit-service")
public interface AuditClient {
    @PostMapping("/api/audit/log")
    String log(@RequestBody AuditRequest req);
}
```

### Asynchronous (Messaging — Kafka/RabbitMQ)
```java
// Producer
kafkaTemplate.send("audit-topic", message);

// Consumer
@KafkaListener(topics = "audit-topic")
public void consume(String message) { ... }
```

### When to use which?

| Synchronous (REST) | Asynchronous (Kafka) |
|--------------------|----------------------|
| Need immediate response | Fire-and-forget |
| Simple request-reply | Event-driven |
| Tight coupling acceptable | Loose coupling |
| Lower throughput | High throughput |

---

## Observability (The 3 Pillars)

### 1. Logging
Centralized logs (ELK Stack: Elasticsearch, Logstash, Kibana; or Loki + Grafana).

### 2. Metrics
Numeric measurements over time (Prometheus + Grafana). Spring Actuator + Micrometer exposes these.

### 3. Distributed Tracing (you used Zipkin)
Track a single request across multiple services with a unique `traceId`.
```
Request [traceId: abc123]
  → Service A [spanId: 1]
  → Service B [spanId: 2, parent: 1]
  → Service C [spanId: 3, parent: 2]
```
Tools: Zipkin, Jaeger. Spring uses Micrometer Tracing.

---

## Resilience Patterns (Resilience4j)

| Pattern | Purpose |
|---------|---------|
| Circuit Breaker | Stop calling a failing service |
| Retry | Retry failed calls (with backoff) |
| Rate Limiter | Limit calls per time window |
| Bulkhead | Isolate resource pools |
| Time Limiter | Timeout long calls |

```java
@Retry(name = "auditService", fallbackMethod = "fallback")
@CircuitBreaker(name = "auditService")
@RateLimiter(name = "auditService")
public Mono<String> callAudit() { ... }
```

---

## Configuration Management

- **Spring Cloud Config** — centralized config from a Git repo
- **Kubernetes ConfigMaps/Secrets** — inject config as env vars
- **Vault / Key Vault** (on your resume) — secure secret storage

---

## Data Consistency in Microservices

### Eventual Consistency
Since each service has its own DB, you can't use ACID transactions across services. Instead, data becomes consistent "eventually" through events.

### Distributed Transaction approaches
1. **Saga** (preferred) — local transactions + compensation
2. **2PC (Two-Phase Commit)** — coordinator asks all to prepare, then commit. Avoided (blocking, doesn't scale).
3. **Outbox Pattern** — write event to an outbox table in the same transaction, then publish reliably.

---

## Common Microservices Interview Questions

**Q: How do you handle a failure in one microservice?**
> Circuit breaker to fail fast, fallback methods for graceful degradation, retries with exponential backoff for transient failures, and async messaging so events aren't lost.

**Q: How do services find each other?**
> Service discovery — either a registry like Eureka, or platform DNS like Kubernetes Services. In my project, Docker DNS resolved service names on a shared network.

**Q: How do you ensure data consistency across services?**
> Eventual consistency via events. For multi-step operations, the Saga pattern with compensating transactions. Avoid distributed 2PC.

**Q: How do you secure microservices?**
> API Gateway for authentication (JWT/OAuth2), service-to-service auth (mTLS or tokens), secrets in Vault, network policies to restrict traffic.

**Q: What are the challenges of microservices?**
> Distributed system complexity, network latency, data consistency, debugging across services (needs tracing), operational overhead (many deployments), testing integration.

**Q: How do you decide service boundaries?**
> By business capability (Domain-Driven Design bounded contexts). Each service should own one cohesive domain, minimize cross-service calls, and align with team ownership.

**Q: How do you version microservice APIs?**
> URL versioning (`/v1/`, `/v2/`), header versioning, or content negotiation. Maintain backward compatibility, deprecate gracefully.

**Q: 12-Factor App principles?**
> Guidelines for cloud-native apps: config in environment, stateless processes, explicit dependencies, logs as streams, dev/prod parity, etc. Worth reviewing before the interview.
