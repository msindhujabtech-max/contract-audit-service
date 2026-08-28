# Quick Revision Cheat Sheet — Last-Minute Review

Read this the morning of the interview.

---

## Java — Rapid Fire

- **Java 8:** Lambdas, Streams, Optional, functional interfaces, default methods, java.time
- **Java 11:** var, HttpClient, String methods (isBlank, repeat, strip)
- **Java 17:** Records, sealed classes, pattern matching, switch expressions, text blocks
- **Java 21:** Virtual threads, record patterns, sequenced collections
- **String immutable:** security, thread-safety, string pool, hashcode caching
- **HashMap:** buckets + hashCode/equals; Java 8 uses tree for big buckets (O(log n))
- **ArrayList vs LinkedList:** array (fast get) vs linked nodes (fast insert)
- **equals + hashCode:** always override together
- **volatile:** visibility only. **synchronized:** visibility + atomicity. **Atomic:** CAS, lock-free
- **Virtual threads:** JVM-managed, cheap, millions possible, great for blocking I/O

## Spring — Rapid Fire

- **IoC/DI:** container creates & injects dependencies; prefer constructor injection
- **Bean scopes:** singleton (default), prototype, request, session
- **@SpringBootApplication** = @Configuration + @EnableAutoConfiguration + @ComponentScan
- **@Component/@Service/@Repository/@Controller** — stereotypes; @Repository adds exception translation
- **@Transactional** — declarative transactions; propagation REQUIRED (default), REQUIRES_NEW
- **AOP** — cross-cutting concerns (logging, security) via aspects/advice/pointcuts
- **WebFlux:** reactive, non-blocking, Netty, Mono (0-1), Flux (0-N)
- **Actuator:** /health, /metrics, /info

## Microservices — Rapid Fire

- **Patterns:** API Gateway, Service Discovery, Circuit Breaker, Saga, CQRS, Database-per-service
- **Circuit Breaker states:** CLOSED → OPEN → HALF_OPEN
- **Sync:** REST/WebClient/Feign. **Async:** Kafka/RabbitMQ
- **Saga:** distributed transactions via local txns + compensation (choreography/orchestration)
- **CQRS:** separate read and write models
- **Observability:** logging + metrics + tracing (Zipkin)
- **Resilience4j:** circuit breaker, retry, rate limiter, bulkhead

## REST — Rapid Fire

- **Stateless, uniform interface, cacheable**
- **GET (safe, idempotent), POST (create), PUT (replace, idempotent), PATCH (partial), DELETE (idempotent)**
- **Status:** 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 429 Too Many, 500 Server Error, 503 Unavailable
- **@RestControllerAdvice** for global exception handling
- **Auth:** stateless JWT/OAuth2

## Database — Rapid Fire

- **JOINs:** INNER (match both), LEFT (all left), RIGHT, FULL, SELF
- **WHERE (rows) vs HAVING (groups)**
- **ACID:** Atomicity, Consistency, Isolation, Durability
- **Isolation levels:** READ UNCOMMITTED → READ COMMITTED → REPEATABLE READ → SERIALIZABLE
- **Index:** B-tree, speeds reads, slows writes
- **Clustered (physical order, 1/table) vs non-clustered (pointers, many)**
- **DELETE (rows, rollback) vs TRUNCATE (all, fast) vs DROP (structure)**
- **Liquibase:** DB schema version control, changeSets, rollback
- **pgvector:** vector search, cosine `<=>`, HNSW index

## DevOps — Rapid Fire

- **Docker:** image (template), container (instance), multi-stage build (smaller image)
- **K8s:** Pod, Deployment, Service, ConfigMap, Secret, Ingress, HPA; self-healing, rolling updates
- **Liveness (restart) vs Readiness (traffic) probes**
- **Kafka:** topic, partition, producer, consumer group, offset, broker
- **Delivery:** at-most-once, at-least-once, exactly-once
- **Maven lifecycle:** validate→compile→test→package→verify→install→deploy
- **Terraform:** provider, resource, plan, apply, destroy, state
- **CI/CD:** Jenkins pipeline (build→test→deploy)

## Testing — Rapid Fire

- **JUnit 5:** @Test, @BeforeEach, @ParameterizedTest, assertEquals, assertThrows
- **Mockito:** @Mock, @InjectMocks, when().thenReturn(), verify()
- **PowerMock:** static/private/final (Mockito 3.4+ can do statics now)
- **@SpringBootTest (integration), @WebMvcTest (web), @DataJpaTest (repo)**
- **StepVerifier** for testing Mono/Flux
- **Testing pyramid:** many unit, some integration, few E2E

## ATG — Rapid Fire

- **Nucleus:** IoC container, components in .properties files
- **Modules:** DCS (commerce), DPS (personalization), BCC (business tool), FUL (fulfillment)
- **Repository/GSA:** ORM, item descriptors, RQL queries
- **Droplet:** view component (display). **Form Handler:** processes forms (actions)
- **Pipeline:** chain of processors (commitOrder for checkout)
- **Order:** commerce items, shipping groups, payment groups
- **Endeca:** search + guided navigation, cartridges, MDEX

## Gen AI — Rapid Fire

- **RAG:** chunk docs → embed → store in vector DB → retrieve similar → inject into prompt → LLM answers
- **Embedding:** vector capturing meaning; similar meaning = nearby vectors
- **Vector DB:** pgvector; cosine similarity; HNSW/IVFFlat index
- **LangChain:** chains/agents/tools. **LangGraph:** stateful graph workflows. **LlamaIndex:** RAG-focused
- **Reduce hallucination:** RAG grounding, strict system prompt, low temperature, "say I don't know"
- **Ollama:** local LLM (privacy, no API cost)
- **Fine-tuning vs RAG:** RAG cheaper, instant updates, private; fine-tuning changes weights

---

## Your Project One-Liners (say these confidently)

- "Built a RAG-based AI Contract Analyzer with Spring AI, WebFlux, PostgreSQL/pgvector, and Ollama."
- "Split it into microservices — analyser and audit — communicating via WebClient (sync) and Kafka (async)."
- "Added Circuit Breaker (Resilience4j) for fault tolerance and Zipkin for distributed tracing."
- "Deployed on GCP with Docker Compose, Kubernetes manifests, and Terraform for infrastructure as code."
- "Used Redis for response caching and rate limiting, cutting redundant LLM calls."

---

## Final Confidence Tips

1. **Answer format:** Concept → Why → Your real example
2. **Don't bluff** — if you don't know, say "I haven't used X directly, but my understanding is..." then reason
3. **Lead with experience** — 12 years is your edge; tie answers to real projects
4. **Think aloud** in design questions — they want your reasoning, not just the answer
5. **Ask clarifying questions** before designing
6. **Stay calm** — you've done this work for over a decade. You know this.

Good luck, Sindhuja!
