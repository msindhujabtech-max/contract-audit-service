# Differences (X vs Y) — Spring, Microservices & REST

Each has a quick table, followed by a plain-English explanation for when the table alone isn't enough.

## Spring vs Spring Boot
| Spring | Spring Boot |
|--------|-------------|
| Framework, manual config | Opinionated, auto-config |
| XML/Java config heavy | Minimal config |
| External server | Embedded server |
| Manual dependency setup | Starter dependencies |

**Explanation:** Spring is a powerful but "assembly required" framework — you wire everything manually (data source, view resolver, server). Spring Boot sits on top and makes smart default decisions for you (auto-configuration), bundles an embedded Tomcat/Netty so there's no separate server to install, and gives you "starter" dependencies that pull in everything you need with one line. In short: Spring Boot = Spring + sensible defaults + zero XML, so you focus on business logic.

## Spring MVC vs Spring WebFlux
| Spring MVC | Spring WebFlux |
|------------|----------------|
| Blocking, synchronous | Non-blocking, reactive |
| Thread-per-request | Event loop |
| Tomcat (servlet) | Netty |
| Returns Object/List | Returns Mono/Flux |
| Traditional CRUD | High concurrency, streaming |

**Explanation:** **Spring MVC** uses one thread per request — that thread waits (blocks) while the DB or network responds, so 200 concurrent requests need ~200 threads. **WebFlux** is reactive/non-blocking — a small number of threads (event loop) handle thousands of requests by never waiting; when a request is waiting on I/O, the thread serves others. MVC returns plain objects; WebFlux returns `Mono`/`Flux`. Use MVC for traditional apps, WebFlux for high concurrency and streaming (which is why your project uses it).

## @Component vs @Service vs @Repository vs @Controller
| Annotation | Layer | Special |
|-----------|-------|---------|
| @Component | Generic | None |
| @Service | Business logic | Semantic only |
| @Repository | Data access | Exception translation |
| @Controller | Web (views) | Returns view names |
| @RestController | Web (REST) | @Controller + @ResponseBody |

**Explanation:** All of these register a class as a Spring-managed bean. They're mostly the SAME technically, but they document intent (which layer the class belongs to). `@Repository` has one real extra: it automatically translates database-specific exceptions into Spring's consistent `DataAccessException`. `@RestController` is a real shortcut = `@Controller` + `@ResponseBody`, so methods return JSON data instead of view names.

## @Controller vs @RestController
| @Controller | @RestController |
|-------------|-----------------|
| Returns view name | Returns data (JSON) |
| Needs @ResponseBody per method | @ResponseBody built-in |
| MVC pages | REST APIs |

**Explanation:** `@Controller` is for traditional web apps that return HTML page names (views), which a template engine renders. `@RestController` is for REST APIs that return data (usually JSON) directly in the response body. With `@Controller` you'd add `@ResponseBody` on each method to return data; `@RestController` includes that automatically.

## Constructor vs Setter vs Field Injection
| Constructor | Setter | Field |
|-------------|--------|-------|
| Immutable (final) | Mutable | Mutable |
| Mandatory deps | Optional deps | Hidden deps |
| Testable without Spring | Testable | Hard to test |
| Recommended | Sometimes | Avoid |

**Explanation:** Three ways Spring injects dependencies. **Constructor injection** (recommended) passes dependencies through the constructor — they can be `final` (immutable), they're clearly mandatory, and you can create the object in a test without Spring. **Setter injection** uses setter methods — good for optional dependencies. **Field injection** (`@Autowired` on a field) looks clean but hides dependencies and can't be tested without reflection/Spring — avoid it.

## @Autowired vs @Qualifier vs @Primary
| @Autowired | @Qualifier | @Primary |
|-----------|------------|----------|
| Inject by type | Pick by name | Default choice |
| Fails if multiple | Resolves ambiguity | Preferred bean |

**Explanation:** `@Autowired` tells Spring to inject a dependency by its type. But if TWO beans of the same type exist, Spring doesn't know which to pick and fails. `@Qualifier("beanName")` resolves this by naming exactly which bean you want. `@Primary` marks one bean as the default choice when there's ambiguity (used when you don't want to add `@Qualifier` everywhere).

## BeanFactory vs ApplicationContext
| BeanFactory | ApplicationContext |
|-------------|--------------------|
| Basic container | Advanced container |
| Lazy loading | Eager loading (singletons) |
| No AOP/events/i18n | AOP, events, i18n, more |
| Rarely used | Standard |

**Explanation:** Both are Spring IoC containers that create and manage beans. `BeanFactory` is the bare-bones version that creates beans lazily (only when requested). `ApplicationContext` is the full-featured superset used in real apps — it eagerly creates singletons at startup and adds AOP, event publishing, internationalization, and more. In practice you always use ApplicationContext.

## Singleton vs Prototype scope
| Singleton | Prototype |
|-----------|-----------|
| One instance per container | New instance each request |
| Default | Explicit |
| Shared state risk | Independent |

**Explanation:** A **singleton** bean (Spring's default) has exactly ONE instance shared across the whole application — efficient, but you must avoid storing request-specific mutable state in it (thread-safety risk). A **prototype** bean creates a NEW instance every time it's requested — use it when each user/request needs its own independent object with its own state.

## @RequestParam vs @PathVariable
| @RequestParam | @PathVariable |
|---------------|---------------|
| Query string `?id=5` | URL path `/user/5` |
| Optional, defaults | Part of resource path |
| Filtering/sorting | Resource identity |

**Explanation:** Both pull values out of the URL. `@PathVariable` grabs a value that's PART of the path itself, identifying a resource: `/users/5` → id=5. `@RequestParam` grabs a value from the query string after `?`, typically for filtering/sorting/paging: `/users?name=bob&page=2`. Rule of thumb: path variable = which resource; request param = how to filter it.

## @RequestBody vs @ResponseBody
| @RequestBody | @ResponseBody |
|--------------|---------------|
| Deserialize request → object | Serialize object → response |
| Incoming JSON | Outgoing JSON |

**Explanation:** `@RequestBody` takes the incoming JSON in the request and converts (deserializes) it into a Java object — used on POST/PUT method parameters. `@ResponseBody` takes your returned Java object and converts (serializes) it into JSON for the response. `@RestController` applies `@ResponseBody` automatically to every method.

## JPA vs Hibernate
| JPA | Hibernate |
|-----|-----------|
| Specification (interface) | Implementation of JPA |
| Standard | Vendor |
| javax/jakarta.persistence | org.hibernate |

**Explanation:** **JPA** (Java Persistence API) is just a SPECIFICATION — a set of interfaces and rules for object-relational mapping, with no actual code. **Hibernate** is the most popular IMPLEMENTATION of that spec (the real working library). Analogy: JPA is the blueprint/standard, Hibernate is the actual product built to that standard. You code against JPA interfaces so you could swap Hibernate for another provider (EclipseLink).

## save() vs saveAndFlush() (JPA)
| save() | saveAndFlush() |
|--------|----------------|
| Persists, may delay DB write | Immediately flushes to DB |

**Explanation:** `save()` schedules the entity to be persisted but may wait (batch the actual SQL until the transaction commits). `saveAndFlush()` forces the change to be written to the database IMMEDIATELY. Use `saveAndFlush()` when you need the DB updated right away — for example, before running a native query that must see the new row.

## get() vs load() (Hibernate)
| get() | load() |
|-------|--------|
| Hits DB immediately | Lazy proxy |
| Returns null if not found | Throws exception |

**Explanation:** Hibernate's `get()` immediately queries the database and returns the object, or `null` if it doesn't exist. `load()` returns a lightweight PROXY without hitting the DB right away — it only loads data when you actually access a field, and throws an exception if the row doesn't exist. Use `get()` when you're unsure the record exists; `load()` when you're sure and want lazy loading.

## @Transactional REQUIRED vs REQUIRES_NEW
| REQUIRED | REQUIRES_NEW |
|----------|--------------|
| Join existing or create | Always new transaction |
| Default | Suspends current |

**Explanation:** Transaction propagation controls what happens when one transactional method calls another. **REQUIRED** (default) — if a transaction already exists, join it; otherwise start one. Everything commits or rolls back together. **REQUIRES_NEW** — always start a brand-new independent transaction, temporarily suspending the caller's. Useful when you want something (like an audit log) to commit even if the outer transaction rolls back.

## Filter vs Interceptor
| Filter | Interceptor |
|--------|-------------|
| Servlet level | Spring level |
| Before DispatcherServlet | Around controller |
| Raw request/response | Handler + model access |

**Explanation:** Both intercept requests, but at different layers. A **Filter** works at the Servlet level, BEFORE Spring's DispatcherServlet even sees the request — it deals with raw HTTP request/response (good for logging, CORS, compression, authentication). An **Interceptor** works INSIDE Spring, wrapping controller methods — it has access to the handler and the model (good for app-specific pre/post processing).

## Mono vs Flux
| Mono | Flux |
|------|------|
| 0 or 1 element | 0 to N elements |
| Single result | Stream of results |

**Explanation:** These are the two reactive types in Project Reactor (used by WebFlux). **Mono** represents a single asynchronous result — 0 or 1 item (like fetching one user by ID). **Flux** represents a stream of 0 to many items (like streaming a list of users or a live feed). Both are lazy and only start when something subscribes.

## WebClient vs RestTemplate
| WebClient | RestTemplate |
|-----------|--------------|
| Non-blocking, reactive | Blocking |
| Returns Mono/Flux | Returns object |
| Modern (WebFlux) | Legacy (maintenance mode) |
| Streaming support | No streaming |

**Explanation:** Both make HTTP calls to other services. **RestTemplate** is the old blocking client — the calling thread waits for the response. **WebClient** is the modern non-blocking, reactive client — it returns a `Mono`/`Flux` and doesn't tie up a thread while waiting. RestTemplate is in maintenance mode (no new features). In a reactive stack like yours, WebClient is the right choice.

## WebClient vs Feign
| WebClient | Feign |
|-----------|-------|
| Programmatic, reactive | Declarative (interface) |
| Fine-grained control | Less boilerplate |
| WebFlux | Spring Cloud |

**Explanation:** Both call other services over HTTP. With **WebClient** you write the call programmatically (build the request, handle the response) — full control, reactive. With **Feign** you just declare an interface with annotations, and Spring Cloud generates the HTTP call for you — less code, very readable. Use WebClient for reactive/fine control; Feign for clean, declarative service-to-service calls.

---

# Microservices Differences

## Monolith vs Microservices
| Monolith | Microservices |
|----------|---------------|
| Single deployable | Many independent services |
| Scale whole app | Scale per service |
| One database | Database per service |
| Simple start | Distributed complexity |
| One failure = all down | Isolated failures |

**Explanation:** A **monolith** is one big application deployed as a single unit — simple to build and deploy at first, but everything scales and fails together, and it gets hard to maintain as it grows. **Microservices** split the app into small independent services, each owning one capability and its own database. You can scale/deploy/update each separately and one failing doesn't crash the rest — but you take on distributed-system complexity (network calls, data consistency, monitoring across services).

## Microservices vs SOA
| Microservices | SOA |
|---------------|-----|
| Fine-grained | Coarse-grained |
| Lightweight (REST/messaging) | ESB (heavy middleware) |
| Database per service | Shared databases common |
| Independent deploy | Often coupled |

**Explanation:** Both are service-based architectures. **SOA** (Service-Oriented Architecture) is the older, coarser approach — larger services often talking through a heavy central "Enterprise Service Bus" (ESB) and sharing databases. **Microservices** are a finer-grained evolution — small, independent services with their own databases, communicating with lightweight REST or messaging, deployable independently. Microservices = SOA done in a lighter, more decoupled way.

## Synchronous vs Asynchronous communication
| Synchronous (REST) | Asynchronous (Kafka) |
|--------------------|----------------------|
| Wait for response | Fire-and-forget |
| Tight coupling | Loose coupling |
| Immediate consistency | Eventual consistency |
| Request-reply | Event-driven |

**Explanation:** **Synchronous** — the caller sends a request and WAITS for the response before continuing (like a phone call). Simple, but the caller is blocked and depends on the callee being up. **Asynchronous** — the caller sends a message and moves on immediately; the receiver processes it later (like sending a text). More resilient and scalable, but the result isn't immediate (eventual consistency). Your project uses both: sync for uploads, async (Kafka) for chat events.

## WebClient (HTTP) vs Kafka (messaging) — YOUR PROJECT
| WebClient/HTTP | Kafka |
|----------------|-------|
| Synchronous | Asynchronous |
| Caller waits | Caller continues |
| Fails if service down | Message persists |
| 1-to-1 | 1-to-many (pub/sub) |
| Immediate response | Delayed processing |
| Used for file upload | Used for chat events |

**Explanation:** This is your key project comparison. With **WebClient (HTTP)** the analyser calls the audit service directly and waits for the reply — if the audit service is down, the call fails. With **Kafka** the analyser just publishes an event to a topic and moves on; Kafka stores the message until the audit consumer processes it, so even if the consumer is temporarily down, no event is lost. HTTP is 1-to-1 request/reply; Kafka is 1-to-many publish/subscribe (many consumers can read the same topic).

## Kafka vs RabbitMQ
| Kafka | RabbitMQ |
|-------|----------|
| Log-based streaming | Traditional message queue |
| Messages persist (replay) | Deleted after consume |
| Very high throughput | Moderate throughput |
| Pull-based | Push-based |
| Partitions for scale | Exchanges/queues |
| Event streaming | Task queues, routing |

**Explanation:** Both are message brokers. **Kafka** is a distributed commit log — messages are retained (even after being read) so consumers can replay history, and it handles huge throughput via partitions. Consumers pull at their own pace. **RabbitMQ** is a traditional queue — a message is delivered and then removed; it excels at complex routing and task distribution, pushing messages to consumers. Use Kafka for event streaming/high volume/replay; RabbitMQ for task queues and flexible routing.

## Orchestration vs Choreography (Saga)
| Orchestration | Choreography |
|---------------|--------------|
| Central coordinator | Event-driven, no coordinator |
| Easier to monitor | More decoupled |
| Single point of logic | Distributed logic |

**Explanation:** Two ways to coordinate a multi-service business transaction (Saga). **Orchestration** uses a central "conductor" service that tells each service what to do step by step — easy to understand and monitor, but the coordinator is a central point. **Choreography** has no central boss — each service reacts to events and emits its own events, so the flow emerges from their interactions — more decoupled but harder to trace. Think orchestra (conductor) vs dance (dancers reacting to each other).

## API Gateway vs Load Balancer
| API Gateway | Load Balancer |
|-------------|---------------|
| Layer 7 (application) | Layer 4/7 |
| Routing, auth, rate limit, aggregation | Distribute traffic |
| Smart | Simple distribution |

**Explanation:** A **Load Balancer** simply spreads incoming traffic across multiple identical servers so no single one is overwhelmed. An **API Gateway** is smarter — it's the single front door to all your microservices, doing routing to the right service, authentication, rate limiting, request aggregation, and more. A gateway often uses load balancing internally, but adds application-level intelligence on top.

## Service Registry vs API Gateway
| Service Registry | API Gateway |
|------------------|-------------|
| Services find each other | Client entry point |
| Eureka, Consul | Spring Cloud Gateway |
| Internal discovery | External routing |

**Explanation:** A **Service Registry** (Eureka, Consul) is an internal phone book — services register their addresses and look each other up by name instead of hardcoding IPs. An **API Gateway** is the external front door where clients enter, which then routes to internal services. The gateway often consults the registry to find where to route. Registry = internal discovery; gateway = external entry point.

## Circuit Breaker vs Retry
| Circuit Breaker | Retry |
|-----------------|-------|
| Stops calling failing service | Retries the call |
| Prevents cascading failure | Handles transient failure |
| Opens after threshold | Attempts N times |

**Explanation:** Both handle failures but oppositely. **Retry** tries the same call again a few times — good for TRANSIENT glitches (a brief network blip). **Circuit Breaker** does the opposite when failures persist — it STOPS calling a service that keeps failing, returning a fallback immediately, so you don't waste time and overload a struggling service. Often used together: retry for blips, circuit breaker to give up when it's clearly down.

## Circuit Breaker states: CLOSED vs OPEN vs HALF_OPEN
| CLOSED | OPEN | HALF_OPEN |
|--------|------|-----------|
| Normal, calls pass | Rejects immediately | Allows test calls |
| Counting failures | After threshold | After wait period |

**Explanation:** Think of an electrical breaker. **CLOSED** = normal, calls flow through while it counts failures. If failures exceed the threshold (say 50%), it flips to **OPEN** = it stops all calls and returns the fallback instantly, giving the failing service time to recover. After a wait (say 30s), it goes **HALF_OPEN** = it lets a few test calls through; if they succeed it goes back to CLOSED, if they fail it returns to OPEN. This is exactly the Resilience4j setup you added.

## Horizontal vs Vertical Scaling
| Horizontal | Vertical |
|------------|----------|
| Add more machines | Bigger machine |
| Scale out | Scale up |
| No downtime | Often needs restart |
| Distributed complexity | Hardware limit |

**Explanation:** **Vertical scaling** = make one server bigger (more CPU/RAM) — simple but has a hardware ceiling and usually needs a restart. **Horizontal scaling** = add more servers and spread the load — no single ceiling, no downtime, and it's how cloud/microservices scale, but it adds complexity (load balancing, distributed state). Microservices favor horizontal scaling.

## Strong vs Eventual Consistency
| Strong | Eventual |
|--------|----------|
| Immediate consistency | Consistent over time |
| Single DB / 2PC | Event-driven |
| Lower availability | Higher availability |

**Explanation:** **Strong consistency** — everyone sees the latest data immediately after a write (like a bank balance in a single database). Safe but harder to scale and less available. **Eventual consistency** — after a write, different services may briefly see old data, but they all converge to the correct value soon (common in microservices using events). You trade instant accuracy for availability and scalability. Choose based on whether stale-for-a-moment data is acceptable.

---

# REST Differences

## REST vs SOAP
| REST | SOAP |
|------|------|
| Architectural style | Protocol |
| JSON (usually) | XML only |
| Lightweight | Heavy |
| Stateless | Can be stateful |
| HTTP | HTTP, SMTP, etc. |

**Explanation:** **REST** is a lightweight architectural STYLE using plain HTTP and usually JSON — simple, fast, and dominant for web/mobile APIs. **SOAP** is a strict PROTOCOL using XML with formal contracts (WSDL) and built-in standards for security/transactions — heavier, more rigid, still used in some enterprise/banking/legacy systems. REST for most modern APIs; SOAP when you need formal contracts and enterprise standards.

## REST vs GraphQL
| REST | GraphQL |
|------|---------|
| Fixed endpoints | Single endpoint |
| Over/under-fetching | Client picks fields |
| Multiple round-trips | One query |
| Simpler | Learning curve |

**Explanation:** With **REST** you have fixed endpoints that return fixed data shapes — you might get too much (over-fetching) or need several calls to gather related data (under-fetching). With **GraphQL** there's one endpoint and the CLIENT specifies exactly which fields it wants in a single query — no over/under-fetching. REST is simpler and cache-friendly; GraphQL is flexible but adds complexity. Great for apps with many varied data needs (mobile).

## PUT vs POST
| PUT | POST |
|-----|------|
| Update/replace | Create |
| Idempotent | Not idempotent |
| Client sets URL/ID | Server assigns ID |

**Explanation:** **POST** creates a new resource; the server usually assigns the ID; calling it twice creates TWO resources (not idempotent). **PUT** creates or replaces a resource at a URL the client specifies; calling it twice has the same effect as once (idempotent) because you're setting a known resource to a known state. Use POST to create when the server generates the ID; PUT to create/replace at a known location.

## PUT vs PATCH
| PUT | PATCH |
|-----|-------|
| Replace entire resource | Partial update |
| Idempotent | May not be |

**Explanation:** **PUT** replaces the ENTIRE resource — you send the full object, and any field you omit is wiped. **PATCH** applies a PARTIAL update — you send only the fields you want to change, leaving the rest untouched. Use PUT for full replacement, PATCH for small edits (like updating just an email).

## Authentication vs Authorization
| Authentication | Authorization |
|----------------|---------------|
| Who are you? | What can you do? |
| Login/identity | Permissions/roles |
| 401 if fails | 403 if fails |

**Explanation:** **Authentication** verifies WHO you are (login with username/password or token) — failing it returns 401 Unauthorized. **Authorization** decides WHAT you're allowed to do once identified (roles/permissions) — failing it returns 403 Forbidden. Analogy: authentication is showing your ID to enter a building; authorization is whether your badge opens a specific door. Authentication always comes first.

## JWT vs Session
| JWT | Session |
|-----|---------|
| Stateless (client stores) | Stateful (server stores) |
| Scales easily | Needs sticky/shared session |
| Can't easily revoke | Easy to revoke |

**Explanation:** Two ways to keep a user logged in. **Session** — the server stores session data and gives the client a session ID; the server must remember every session (stateful), which is harder to scale across many servers. **JWT** — the server issues a signed token containing the user info; the CLIENT stores it and sends it each request; the server just verifies the signature (stateless), which scales beautifully across servers. Downside of JWT: it's valid until it expires and is hard to revoke early. Sessions are easy to revoke (just delete server-side).
