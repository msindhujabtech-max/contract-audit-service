# Microservices Integration Guide — Complete Technical Documentation

## Project: AI Contract Analyzer with Audit Service

This document covers every integration pattern used across both services:
**Kafka**, **Kubernetes**, **Redis**, **Terraform**, and **WebClient**.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [WebClient Integration (Synchronous HTTP)](#webclient-integration)
3. [Kafka Integration (Asynchronous Messaging)](#kafka-integration)
4. [Redis Integration (Caching & Rate Limiting)](#redis-integration)
5. [Kubernetes Deployment](#kubernetes-deployment)
6. [Terraform Infrastructure as Code](#terraform-infrastructure-as-code)
7. [How All Technologies Work Together](#how-all-technologies-work-together)
8. [Interview Quick Reference](#interview-quick-reference)

---

## Architecture Overview

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        GCP VM (34.70.230.73)                                │
│                                                                            │
│  ┌─────────┐   ┌─────────────┐   ┌───────────────────┐   ┌────────────┐  │
│  │Frontend │   │   Backend   │   │  Audit Service    │   │   Ollama   │  │
│  │ React   │──>│ Spring Boot │──>│  Spring Boot      │   │ LLM + Embed│  │
│  │ :3000   │   │   :8000     │   │    :8082          │   │  :11434    │  │
│  └─────────┘   └──────┬──────┘   └────────┬──────────┘   └────────────┘  │
│                        │                    │                               │
│              ┌─────────┼────────────────────┼───────────┐                  │
│              │         │      Kafka         │           │                  │
│              │         │    ┌───────┐       │           │                  │
│              │         └───>│ Topic │───────┘           │                  │
│              │              └───────┘                   │                  │
│              │         contract-audit-topic             │                  │
│              └─────────────────────────────────────────┘                  │
│                                                                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │  PostgreSQL  │  │    Redis     │  │  Kafka   │  │   Zookeeper      │  │
│  │  + pgvector  │  │    Cache     │  │  Broker  │  │   (Kafka coord)  │  │
│  │   :5432      │  │   :6379     │  │  :9092   │  │   :2181          │  │
│  └──────────────┘  └──────────────┘  └──────────┘  └──────────────────┘  │
└────────────────────────────────────────────────────────────────────────────┘
```

### Two Communication Patterns

| Pattern | Technology | Trigger | Behavior |
|---------|-----------|---------|----------|
| Synchronous | WebClient (HTTP) | File Upload | Backend waits for response, shows in frontend |
| Asynchronous | Kafka | Chat Question | Backend publishes and moves on, email sent later |

---

## WebClient Integration

### What is WebClient?

WebClient is Spring WebFlux's non-blocking HTTP client. It replaces the older `RestTemplate` for making HTTP calls between microservices.

### Where it's used

**File:** `contract-analyser-spring-ai/backend/.../service/AuditService.java`

```java
@Service
public class AuditService {

    private final WebClient webClient;

    public AuditService(@Value("${app.audit.base-url}") String auditBaseUrl) {
        this.webClient = WebClient.builder().baseUrl(auditBaseUrl).build();
    }

    public Mono<String> logAudit(String contractName, String status, int wordCount) {
        Map<String, Object> body = Map.of(
                "contractName", contractName,
                "status", status,
                "wordCount", wordCount
        );

        return webClient.post()
                .uri("/api/audit/log")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(resp -> log.info("Audit logged: {}", resp))
                .doOnError(err -> log.warn("Audit service unavailable: {}", err.getMessage()))
                .onErrorResume(e -> Mono.just("Audit service unavailable"));
    }
}
```

### How it works step by step

```
1. User uploads PDF
2. DocumentIngestionService processes the file
3. After processing, calls auditService.logAudit() 
4. WebClient sends HTTP POST to http://contract-audit-service:8082/api/audit/log
5. Audit service receives it, logs it, returns success message
6. Response is included in the upload response → shown in frontend
```

### Key concepts

| Concept | Explanation |
|---------|-------------|
| `WebClient.builder()` | Creates a configured HTTP client instance |
| `.baseUrl(...)` | Sets the target service URL (resolved via Docker DNS) |
| `.bodyValue(body)` | Serializes the Map to JSON automatically |
| `.retrieve()` | Executes the request |
| `.bodyToMono(String.class)` | Converts response body to a reactive Mono |
| `.onErrorResume(...)` | Graceful fallback if audit service is down |
| `Mono<String>` | Reactive type — represents 0 or 1 async result |

### Configuration

**application.yml:**
```yaml
app:
  audit:
    base-url: ${AUDIT_SERVICE_URL:http://localhost:8082}
```

**docker-compose.yml:**
```yaml
environment:
  AUDIT_SERVICE_URL: http://contract-audit-service:8082
```

Docker DNS resolves `contract-audit-service` to the container's IP on `contract-network`.

### When to use WebClient vs Kafka

| Use WebClient when... | Use Kafka when... |
|----------------------|-------------------|
| You need the response immediately | You don't need a response |
| The caller must know if it succeeded | Fire-and-forget is acceptable |
| Simple request-response pattern | Event-driven / pub-sub pattern |
| Low volume, critical operations | High volume, can tolerate delay |

---

## Kafka Integration

### What is Kafka?

Apache Kafka is a distributed message broker. Producers publish messages to **topics**, and consumers subscribe to those topics. Messages are persisted until consumed, so if a consumer is down, messages wait.

### Architecture in this project

```
┌──────────────────────┐         ┌─────────────┐         ┌──────────────────────┐
│  contract-analyser   │         │    Kafka    │         │ contract-audit-service│
│   (PRODUCER)         │────────>│   Broker    │────────>│   (CONSUMER)         │
│                      │ publish │             │ consume │                      │
│ AuditKafkaProducer   │         │  Topic:     │         │ AuditKafkaConsumer   │
│                      │         │  contract-  │         │         │            │
│                      │         │  audit-topic│         │         ▼            │
└──────────────────────┘         └─────────────┘         │ EmailNotification    │
                                                         │   Service            │
                                                         │         │            │
                                                         │         ▼            │
                                                         │   Gmail SMTP         │
                                                         │   📧 Email sent      │
                                                         └──────────────────────┘
```

### Producer (Analyser Service)

**File:** `contract-analyser-spring-ai/backend/.../service/AuditKafkaProducer.java`

```java
@Service
public class AuditKafkaProducer {

    private static final String TOPIC = "contract-audit-topic";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendAuditEvent(String contractName, String status, int wordCount,
                               String question, String answer) {
        Map<String, Object> event = Map.of(
                "contractName", contractName,
                "status", status,
                "wordCount", wordCount,
                "question", question,
                "answer", answer
        );
        String message = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(TOPIC, contractName, message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka event sent: offset={}", result.getRecordMetadata().offset());
                    } else {
                        log.warn("Failed: {}", ex.getMessage());
                    }
                });
    }
}
```

### Consumer (Audit Service)

**File:** `contract-audit-service/.../kafka/AuditKafkaConsumer.java`

```java
@Component
public class AuditKafkaConsumer {

    private final List<AuditRequest> kafkaAuditLog = new CopyOnWriteArrayList<>();
    private final EmailNotificationService emailService;

    @KafkaListener(topics = "contract-audit-topic", groupId = "audit-service-group")
    public void consumeAuditEvent(String message) {
        AuditRequest request = objectMapper.readValue(message, AuditRequest.class);

        log.info("Kafka audit received -> contract: '{}'", request.contractName());
        kafkaAuditLog.add(request);

        // Send email with question + answer
        emailService.sendAuditNotification(request);
    }
}
```

### How it works step by step

```
1. User asks a chat question
2. RagService processes it, gets AI response
3. In doOnComplete(), calls auditKafkaProducer.sendAuditEvent(...)
4. Producer serializes the event to JSON and publishes to "contract-audit-topic"
5. KafkaTemplate.send() returns immediately (non-blocking)
6. User sees the AI response in the chat (no waiting for audit)
7. Meanwhile... Kafka broker stores the message
8. AuditKafkaConsumer picks it up via @KafkaListener
9. Consumer logs it + sends email notification
10. Email arrives at m.sindhujabtech@gmail.com with question + answer
```

### Key Kafka Concepts

| Concept | Explanation |
|---------|-------------|
| **Topic** | Named channel for messages (`contract-audit-topic`) |
| **Producer** | Publishes messages to a topic |
| **Consumer** | Subscribes to a topic and processes messages |
| **Consumer Group** | Multiple consumers can share work — each message goes to one consumer in the group |
| **Offset** | Position of a message in the topic (like an index) |
| **Broker** | Kafka server that stores and serves messages |
| **Zookeeper** | Coordinates Kafka brokers (being replaced by KRaft in newer versions) |
| **Partition** | Topics are split into partitions for parallelism |

### Configuration

**Producer (application.yml):**
```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

**Consumer (application.properties):**
```properties
spring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.consumer.group-id=audit-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

### Docker Compose (Kafka + Zookeeper)

```yaml
zookeeper:
  image: confluentinc/cp-zookeeper:7.5.0
  environment:
    ZOOKEEPER_CLIENT_PORT: 2181
    ZOOKEEPER_TICK_TIME: 2000

kafka:
  image: confluentinc/cp-kafka:7.5.0
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
  depends_on:
    - zookeeper
```

### Why Kafka over direct HTTP for chat events?

1. **Decoupling** — Analyser doesn't need to know if audit service exists
2. **Reliability** — If audit service is down, messages wait in Kafka
3. **Scalability** — Multiple consumers can process from the same topic
4. **No blocking** — Producer returns instantly, doesn't wait
5. **Replay** — Messages can be re-consumed if needed

---

## Redis Integration

### What is Redis?

Redis is an in-memory key-value store used here for **caching AI responses** and **rate limiting**.

### Where it's used

| Feature | Service | Purpose |
|---------|---------|---------|
| Response Caching | CacheService | Cache AI answers to avoid re-calling the LLM |
| Rate Limiting | RateLimiterService | Limit users to 20 requests/minute |
| Chat History | ChatHistoryService | Store conversation history (survives browser refresh) |

### CacheService (Response Caching)

**File:** `contract-analyser-spring-ai/backend/.../service/CacheService.java`

```java
@Service
public class CacheService {

    private final ReactiveStringRedisTemplate redisTemplate;

    // Generate unique cache key per user + question
    public String generateCacheKey(Long contractId, Long userId, String question) {
        return "cache:" + contractId + ":" + userId + ":" + question.hashCode();
    }

    // Store response in Redis with TTL
    public Mono<Boolean> cacheResponse(String key, String response) {
        return redisTemplate.opsForValue()
                .set(key, response, Duration.ofMinutes(60));
    }

    // Retrieve cached response
    public Mono<String> getCachedResponse(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // Invalidate cache when new document is uploaded
    public Mono<Long> invalidateContractCache(Long contractId) {
        // Delete all keys matching pattern
    }
}
```

### How caching works

```
User asks: "What are the payment terms?"
         │
         ▼
  ┌─── Cache HIT? ───┐
  │                   │
  YES                 NO
  │                   │
  ▼                   ▼
Return cached    Call Ollama LLM
response         (expensive, slow)
(instant)              │
                       ▼
                 Cache the response
                 in Redis (TTL: 60min)
                       │
                       ▼
                 Return to user
```

### Rate Limiting

```java
public Mono<Boolean> isAllowed(Long userId) {
    String key = "ratelimit:" + userId;
    return redisTemplate.opsForValue().increment(key)
            .flatMap(count -> {
                if (count == 1) {
                    // First request — set expiry to 1 minute
                    return redisTemplate.expire(key, Duration.ofMinutes(1))
                            .thenReturn(true);
                }
                return Mono.just(count <= 20); // 20 requests per minute
            });
}
```

### Configuration

```yaml
spring:
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST:localhost}
      port: ${SPRING_DATA_REDIS_PORT:6379}

app:
  cache:
    response-ttl-minutes: 60
    rate-limit-requests-per-minute: 20
```

### Key Redis Concepts

| Concept | How it's used |
|---------|---------------|
| **Key-Value Store** | `cache:1:101:12345` → "AI response text" |
| **TTL (Time-to-Live)** | Cached responses expire after 60 minutes |
| **Atomic Increment** | Rate limiting counter per user per minute |
| **Reactive Driver** | `ReactiveStringRedisTemplate` — non-blocking operations |
| **Pattern Matching** | Delete all keys matching `cache:1:*` on new upload |

---

## Kubernetes Deployment

### What is Kubernetes (K8s)?

Kubernetes is a container orchestration platform. While Docker Compose runs containers on a single machine, Kubernetes manages containers across multiple machines with auto-scaling, self-healing, and rolling updates.

### Project K8s Structure

```
k8s/
├── namespace.yaml       # Isolates resources in "contract-analyzer" namespace
├── configmap.yaml       # Environment variables (non-secret)
├── secrets.yaml         # Sensitive data (passwords, keys)
├── postgres.yaml        # PostgreSQL StatefulSet + Service
├── redis.yaml           # Redis Deployment + Service
├── ollama.yaml          # Ollama Deployment + Service
├── backend.yaml         # Spring Boot backend Deployment + Service
├── frontend.yaml        # React frontend Deployment + Service
├── ingress.yaml         # External access routing
├── hpa.yaml             # Horizontal Pod Autoscaler
└── README.md            # K8s deployment instructions
```

### Key K8s Concepts Used

| Concept | File | Purpose |
|---------|------|---------|
| **Namespace** | namespace.yaml | Isolate all project resources |
| **Deployment** | backend.yaml | Manages pods (replicas, rolling updates) |
| **Service** | backend.yaml | Internal DNS for pod-to-pod communication |
| **ConfigMap** | configmap.yaml | Inject env vars into containers |
| **Secret** | secrets.yaml | Store passwords securely |
| **Ingress** | ingress.yaml | Route external traffic to services |
| **HPA** | hpa.yaml | Auto-scale pods based on CPU usage |
| **StatefulSet** | postgres.yaml | For databases that need persistent identity |

### Example: Backend Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: contract-backend
  namespace: contract-analyzer
spec:
  replicas: 2                    # Run 2 instances
  selector:
    matchLabels:
      app: contract-backend
  template:
    spec:
      containers:
        - name: backend
          image: contract-backend:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_DATASOURCE_URL
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: DATABASE_URL
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
```

### Example: HPA (Auto-Scaling)

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: backend-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: contract-backend
  minReplicas: 2
  maxReplicas: 5
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70   # Scale up when CPU > 70%
```

### Docker Compose vs Kubernetes

| Feature | Docker Compose | Kubernetes |
|---------|---------------|------------|
| Scale | Single machine | Multi-machine cluster |
| Auto-healing | No (manual restart) | Yes (restarts crashed pods) |
| Auto-scaling | No | Yes (HPA) |
| Rolling updates | Downtime during rebuild | Zero-downtime deploys |
| Load balancing | Port mapping | Built-in Service load balancer |
| Secrets management | env vars in file | Encrypted Secrets resource |
| Best for | Development, small deploys | Production, large scale |

---

## Terraform Infrastructure as Code

### What is Terraform?

Terraform lets you define cloud infrastructure (VMs, networks, firewalls) as code. Instead of clicking through the GCP Console, you write `.tf` files and run `terraform apply`.

### Project Terraform Structure

```
terraform/
├── main.tf                    # VM, firewall, static IP definitions
├── variables.tf               # Configurable inputs (project ID, zone)
├── outputs.tf                 # Prints URLs after deployment
├── terraform.tfvars.example   # Template for user configuration
└── .gitignore                 # Excludes state files from Git
```

### What Terraform Creates

```
terraform apply
      │
      ├── google_compute_address    → Static IP (e.g., 34.70.230.73)
      ├── google_compute_firewall   → Opens ports 3000, 8000, 8082
      └── google_compute_instance   → VM with Docker + both services deployed
```

### main.tf Key Sections

**Static IP:**
```hcl
resource "google_compute_address" "app_ip" {
  name   = "contract-app-ip"
  region = var.region
}
```

**Firewall:**
```hcl
resource "google_compute_firewall" "allow_app_ports" {
  name    = "allow-contract-app-traffic"
  network = "default"
  allow {
    protocol = "tcp"
    ports    = ["3000", "8000", "8082", "22"]
  }
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["contract-app"]
}
```

**VM with startup script:**
```hcl
resource "google_compute_instance" "contract_vm" {
  name         = "contract-analyzer-vm"
  machine_type = var.machine_type
  tags         = ["contract-app", "http-server"]

  metadata_startup_script = <<-EOF
    # Install Docker
    # Clone repos
    # docker compose up -d --build
  EOF
}
```

### Key Terraform Concepts

| Concept | Explanation |
|---------|-------------|
| **Provider** | Plugin for a cloud platform (google, aws, azure) |
| **Resource** | A piece of infrastructure to create (VM, IP, firewall) |
| **Variable** | Configurable input (project ID, region) |
| **Output** | Values displayed after apply (public IP, URLs) |
| **State** | Terraform tracks what it created in `terraform.tfstate` |
| **Plan** | Preview what will change before applying |
| **Apply** | Create/update infrastructure |
| **Destroy** | Delete everything Terraform created |

### Terraform Workflow

```bash
terraform init      # Download provider plugins
terraform plan      # Preview changes (dry run)
terraform apply     # Create infrastructure (type "yes")
terraform destroy   # Delete everything (type "yes")
```

### Why Terraform?

| Without Terraform | With Terraform |
|-------------------|----------------|
| "Click here, then there, set this value..." | `terraform apply` |
| Can't reproduce the setup reliably | Identical setup every time |
| Colleague asks 50 questions | Colleague runs 4 commands |
| Forget a firewall rule | Infrastructure is version-controlled |
| Manual cleanup when done | `terraform destroy` |

---

## How All Technologies Work Together

### Complete Request Flow

```
┌─────────── UPLOAD FLOW (WebClient + Redis) ───────────────────────────┐
│                                                                        │
│  Browser → Frontend(:3000) → Backend(:8080)                            │
│                                    │                                   │
│                    ┌───────────────┼───────────────────┐               │
│                    │               │                   │               │
│                    ▼               ▼                   ▼               │
│            PostgreSQL         Redis Cache       WebClient HTTP         │
│            (store vectors)   (invalidate)      POST to :8082          │
│                                                      │                │
│                                                      ▼                │
│                                              Audit Service            │
│                                              logs + responds          │
│                                                      │                │
│                                                      ▼                │
│                                              Frontend shows:          │
│                                      "✓ processed | 🛡️ Audit logged" │
└────────────────────────────────────────────────────────────────────────┘

┌─────────── CHAT FLOW (Kafka + Redis + Email) ─────────────────────────┐
│                                                                        │
│  Browser → Frontend(:3000) → Backend(:8080)                            │
│                                    │                                   │
│                    ┌───────────────┼───────────────────┐               │
│                    │               │                   │               │
│                    ▼               ▼                   ▼               │
│            PostgreSQL         Redis Cache         Kafka Topic          │
│            (vector search)   (check/store)    (publish event)         │
│                    │                                   │               │
│                    ▼                                   │ (async)       │
│              Ollama LLM                               │               │
│            (generate answer)                          ▼               │
│                    │                          Audit Consumer           │
│                    ▼                                   │               │
│            Stream response                            ▼               │
│            back to frontend                    Email Service           │
│                                                      │               │
│                                                      ▼               │
│                                               📧 Gmail inbox          │
└────────────────────────────────────────────────────────────────────────┘
```

### Technology Responsibility Matrix

| Technology | Role | Where |
|-----------|------|-------|
| **Spring WebFlux** | Non-blocking reactive web framework | Both services |
| **WebClient** | Synchronous HTTP calls between services | Analyser → Audit (uploads) |
| **Kafka** | Asynchronous event-driven messaging | Analyser → Audit (chat) |
| **Redis** | Caching, rate limiting, chat history | Analyser backend |
| **PostgreSQL + pgvector** | Vector storage for RAG embeddings | Analyser backend |
| **Ollama** | Local LLM + embedding model | Analyser backend |
| **Docker Compose** | Container orchestration (dev/single VM) | Both services |
| **Kubernetes** | Container orchestration (production/scale) | Both services |
| **Terraform** | Infrastructure provisioning on GCP | One-time setup |
| **Gmail SMTP** | Email notifications from Kafka events | Audit service |

---

## Interview Quick Reference

### "Explain your microservices architecture"

> We have two independently deployable services: a contract analyser (AI/RAG) and an audit service (logging/notifications). They communicate via two patterns — synchronous HTTP using WebClient for critical operations like file uploads, and asynchronous Kafka messaging for non-critical events like chat audit logs. Both run in Docker containers on a shared network where Docker DNS handles service discovery.

### "Why did you use WebClient instead of RestTemplate?"

> WebClient is the reactive, non-blocking HTTP client in Spring WebFlux. Since our entire stack is reactive (WebFlux + Reactor), using RestTemplate would block the event loop. WebClient returns a `Mono<T>` that integrates naturally with our reactive chain. It also supports streaming, backpressure, and has built-in timeout/retry capabilities.

### "Why Kafka instead of just HTTP for everything?"

> For chat events, we don't need a synchronous response. Kafka gives us: decoupling (producer doesn't know about the consumer), reliability (messages persist if consumer is down), scalability (multiple consumers can process from the same topic), and non-blocking behavior (producer returns instantly). The audit service can be down during maintenance without losing any events.

### "How does Redis help with performance?"

> Redis serves three purposes: (1) Response caching — we cache AI-generated answers so identical questions return instantly without re-calling the expensive LLM. (2) Rate limiting — using atomic increment with TTL, we limit users to 20 requests/minute. (3) Chat history — conversation context survives browser refreshes. All operations are reactive using `ReactiveStringRedisTemplate`.

### "Docker Compose vs Kubernetes — when to use which?"

> Docker Compose is for single-machine deployments and development. Kubernetes is for production — it adds auto-scaling (HPA), self-healing (pod restarts), rolling updates (zero downtime), and multi-node clustering. Our project has both: Compose for the GCP VM deployment, and K8s manifests ready for production scaling.

### "Explain Terraform in one sentence"

> Terraform lets us define our entire GCP infrastructure (VM, firewall, IP) as code in `.tf` files, so anyone can recreate the identical environment by running `terraform apply` — no manual clicking, no forgotten steps.

### "How do you handle failure between services?"

> For WebClient: we use `.onErrorResume()` to return a fallback message if the audit service is down — the upload still succeeds. For Kafka: messages persist in the topic until the consumer comes back online, so no events are lost. Both patterns are fault-tolerant by design.

---

## Quick Commands Reference

### Deploy on GCP (from scratch)

```bash
cd terraform && terraform init && terraform apply
```

### Redeploy after code changes (on VM)

```bash
cd ~/contract-audit-service && git pull && docker compose down && docker compose up -d --build
cd ~/contract-analyser-spring-ai && git pull && docker compose down && docker compose up -d --build
```

### View audit logs

```
Browser: http://34.70.230.73:8082/api/audit/logs
```

### Check container logs

```bash
docker logs contract-backend --tail 20
docker logs contract-audit-service --tail 20
docker logs contract-kafka --tail 20
```

### Tear down

```bash
cd terraform && terraform destroy
```
