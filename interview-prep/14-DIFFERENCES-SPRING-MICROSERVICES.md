# Differences (X vs Y) — Spring, Microservices & REST

## Spring vs Spring Boot
| Spring | Spring Boot |
|--------|-------------|
| Framework, manual config | Opinionated, auto-config |
| XML/Java config heavy | Minimal config |
| External server | Embedded server |
| Manual dependency setup | Starter dependencies |

## Spring MVC vs Spring WebFlux
| Spring MVC | Spring WebFlux |
|------------|----------------|
| Blocking, synchronous | Non-blocking, reactive |
| Thread-per-request | Event loop |
| Tomcat (servlet) | Netty |
| Returns Object/List | Returns Mono/Flux |
| Traditional CRUD | High concurrency, streaming |

## @Component vs @Service vs @Repository vs @Controller
| Annotation | Layer | Special |
|-----------|-------|---------|
| @Component | Generic | None |
| @Service | Business logic | Semantic only |
| @Repository | Data access | Exception translation |
| @Controller | Web (views) | Returns view names |
| @RestController | Web (REST) | @Controller + @ResponseBody |

## @Controller vs @RestController
| @Controller | @RestController |
|-------------|-----------------|
| Returns view name | Returns data (JSON) |
| Needs @ResponseBody per method | @ResponseBody built-in |
| MVC pages | REST APIs |

## Constructor vs Setter vs Field Injection
| Constructor | Setter | Field |
|-------------|--------|-------|
| Immutable (final) | Mutable | Mutable |
| Mandatory deps | Optional deps | Hidden deps |
| Testable without Spring | Testable | Hard to test |
| Recommended | Sometimes | Avoid |

## @Autowired vs @Qualifier vs @Primary
| @Autowired | @Qualifier | @Primary |
|-----------|------------|----------|
| Inject by type | Pick by name | Default choice |
| Fails if multiple | Resolves ambiguity | Preferred bean |

## BeanFactory vs ApplicationContext
| BeanFactory | ApplicationContext |
|-------------|--------------------|
| Basic container | Advanced container |
| Lazy loading | Eager loading (singletons) |
| No AOP/events/i18n | AOP, events, i18n, more |
| Rarely used | Standard |

## Singleton vs Prototype scope
| Singleton | Prototype |
|-----------|-----------|
| One instance per container | New instance each request |
| Default | Explicit |
| Shared state risk | Independent |

## @RequestParam vs @PathVariable
| @RequestParam | @PathVariable |
|---------------|---------------|
| Query string `?id=5` | URL path `/user/5` |
| Optional, defaults | Part of resource path |
| Filtering/sorting | Resource identity |

## @RequestBody vs @ResponseBody
| @RequestBody | @ResponseBody |
|--------------|---------------|
| Deserialize request → object | Serialize object → response |
| Incoming JSON | Outgoing JSON |

## JPA vs Hibernate
| JPA | Hibernate |
|-----|-----------|
| Specification (interface) | Implementation of JPA |
| Standard | Vendor |
| javax/jakarta.persistence | org.hibernate |

## save() vs saveAndFlush() (JPA)
| save() | saveAndFlush() |
|--------|----------------|
| Persists, may delay DB write | Immediately flushes to DB |

## get() vs load() (Hibernate)
| get() | load() |
|-------|--------|
| Hits DB immediately | Lazy proxy |
| Returns null if not found | Throws exception |

## @Transactional REQUIRED vs REQUIRES_NEW
| REQUIRED | REQUIRES_NEW |
|----------|--------------|
| Join existing or create | Always new transaction |
| Default | Suspends current |

## Filter vs Interceptor
| Filter | Interceptor |
|--------|-------------|
| Servlet level | Spring level |
| Before DispatcherServlet | Around controller |
| Raw request/response | Handler + model access |

## Mono vs Flux
| Mono | Flux |
|------|------|
| 0 or 1 element | 0 to N elements |
| Single result | Stream of results |

## WebClient vs RestTemplate
| WebClient | RestTemplate |
|-----------|--------------|
| Non-blocking, reactive | Blocking |
| Returns Mono/Flux | Returns object |
| Modern (WebFlux) | Legacy (maintenance mode) |
| Streaming support | No streaming |

## WebClient vs Feign
| WebClient | Feign |
|-----------|-------|
| Programmatic, reactive | Declarative (interface) |
| Fine-grained control | Less boilerplate |
| WebFlux | Spring Cloud |

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

## Microservices vs SOA
| Microservices | SOA |
|---------------|-----|
| Fine-grained | Coarse-grained |
| Lightweight (REST/messaging) | ESB (heavy middleware) |
| Database per service | Shared databases common |
| Independent deploy | Often coupled |

## Synchronous vs Asynchronous communication
| Synchronous (REST) | Asynchronous (Kafka) |
|--------------------|----------------------|
| Wait for response | Fire-and-forget |
| Tight coupling | Loose coupling |
| Immediate consistency | Eventual consistency |
| Request-reply | Event-driven |

## WebClient (HTTP) vs Kafka (messaging) — YOUR PROJECT
| WebClient/HTTP | Kafka |
|----------------|-------|
| Synchronous | Asynchronous |
| Caller waits | Caller continues |
| Fails if service down | Message persists |
| 1-to-1 | 1-to-many (pub/sub) |
| Immediate response | Delayed processing |
| Used for file upload | Used for chat events |

## Kafka vs RabbitMQ
| Kafka | RabbitMQ |
|-------|----------|
| Log-based streaming | Traditional message queue |
| Messages persist (replay) | Deleted after consume |
| Very high throughput | Moderate throughput |
| Pull-based | Push-based |
| Partitions for scale | Exchanges/queues |
| Event streaming | Task queues, routing |

## Orchestration vs Choreography (Saga)
| Orchestration | Choreography |
|---------------|--------------|
| Central coordinator | Event-driven, no coordinator |
| Easier to monitor | More decoupled |
| Single point of logic | Distributed logic |

## API Gateway vs Load Balancer
| API Gateway | Load Balancer |
|-------------|---------------|
| Layer 7 (application) | Layer 4/7 |
| Routing, auth, rate limit, aggregation | Distribute traffic |
| Smart | Simple distribution |

## Service Registry vs API Gateway
| Service Registry | API Gateway |
|------------------|-------------|
| Services find each other | Client entry point |
| Eureka, Consul | Spring Cloud Gateway |
| Internal discovery | External routing |

## Circuit Breaker vs Retry
| Circuit Breaker | Retry |
|-----------------|-------|
| Stops calling failing service | Retries the call |
| Prevents cascading failure | Handles transient failure |
| Opens after threshold | Attempts N times |

## Circuit Breaker states: CLOSED vs OPEN vs HALF_OPEN
| CLOSED | OPEN | HALF_OPEN |
|--------|------|-----------|
| Normal, calls pass | Rejects immediately | Allows test calls |
| Counting failures | After threshold | After wait period |

## Horizontal vs Vertical Scaling
| Horizontal | Vertical |
|------------|----------|
| Add more machines | Bigger machine |
| Scale out | Scale up |
| No downtime | Often needs restart |
| Distributed complexity | Hardware limit |

## Strong vs Eventual Consistency
| Strong | Eventual |
|--------|----------|
| Immediate consistency | Consistent over time |
| Single DB / 2PC | Event-driven |
| Lower availability | Higher availability |

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

## REST vs GraphQL
| REST | GraphQL |
|------|---------|
| Fixed endpoints | Single endpoint |
| Over/under-fetching | Client picks fields |
| Multiple round-trips | One query |
| Simpler | Learning curve |

## PUT vs POST
| PUT | POST |
|-----|------|
| Update/replace | Create |
| Idempotent | Not idempotent |
| Client sets URL/ID | Server assigns ID |

## PUT vs PATCH
| PUT | PATCH |
|-----|-------|
| Replace entire resource | Partial update |
| Idempotent | May not be |

## Authentication vs Authorization
| Authentication | Authorization |
|----------------|---------------|
| Who are you? | What can you do? |
| Login/identity | Permissions/roles |
| 401 if fails | 403 if fails |

## JWT vs Session
| JWT | Session |
|-----|---------|
| Stateless (client stores) | Stateful (server stores) |
| Scales easily | Needs sticky/shared session |
| Can't easily revoke | Easy to revoke |
