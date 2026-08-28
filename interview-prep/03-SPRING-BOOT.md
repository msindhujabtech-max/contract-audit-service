# Spring & Spring Boot — Interview Preparation

## What is Spring? What is Spring Boot?

**Spring Framework** — a comprehensive framework for Java enterprise apps. Core feature is **Dependency Injection (DI)** and **Inversion of Control (IoC)**.

**Spring Boot** — built on top of Spring, removes boilerplate with:
- Auto-configuration (sensible defaults)
- Embedded servers (Tomcat/Netty — no external deployment)
- Starter dependencies (`spring-boot-starter-web`)
- Production-ready features (Actuator)

> **One-liner:** "Spring Boot is Spring with opinionated defaults and zero XML configuration, so you focus on business logic."

---

## Inversion of Control (IoC) & Dependency Injection (DI)

**IoC:** Instead of your code creating dependencies, the framework creates and injects them. Control is "inverted" to the container.

**DI:** The mechanism — the container injects dependencies into your objects.

### Three types of injection

```java
// 1. Constructor injection (RECOMMENDED — immutable, testable)
@Service
public class OrderService {
    private final PaymentService paymentService;
    public OrderService(PaymentService paymentService) {  // injected
        this.paymentService = paymentService;
    }
}

// 2. Setter injection
@Autowired
public void setPaymentService(PaymentService ps) { this.paymentService = ps; }

// 3. Field injection (avoid — hard to test)
@Autowired
private PaymentService paymentService;
```

> **Why constructor injection is best:** dependencies are final (immutable), mandatory dependencies are explicit, and you can test without Spring.

---

## Spring Beans & Lifecycle

A **bean** is an object managed by the Spring IoC container.

### Bean scopes

| Scope | Description |
|-------|-------------|
| singleton (default) | One instance per container |
| prototype | New instance every request |
| request | One per HTTP request (web) |
| session | One per HTTP session (web) |

**What the scope actually controls:** it's *how many* copies of the bean the container hands out. `singleton` (the default) means every class that asks for, say, `AuditService` gets the *same* shared object — perfect for stateless services. `prototype` gives a *fresh* object each time it's injected — use it when the bean holds per-use mutable state. `request`/`session` tie the lifetime to a web request or user session. A common bug: injecting a stateful field into a singleton, since all threads then share it.

```java
@Service                       // singleton by default — one shared instance
public class AuditService { }

@Component
@Scope("prototype")            // brand-new instance on every injection/lookup
public class ReportBuilder {
    private final List<String> lines = new ArrayList<>();  // safe: not shared
}
// Two injections of ReportBuilder == two different objects;
// two injections of AuditService == the exact same object.
```

### Bean lifecycle
```
Instantiate → Populate properties → @PostConstruct → Bean ready → @PreDestroy → Destroyed
```
```java
@Component
public class MyBean {
    @PostConstruct
    public void init() { System.out.println("Bean initialized"); }
    @PreDestroy
    public void cleanup() { System.out.println("Bean destroyed"); }
}
```

---

## Key Annotations (memorize these)

### Stereotype annotations
| Annotation | Purpose |
|-----------|---------|
| `@Component` | Generic Spring-managed bean |
| `@Service` | Business logic layer |
| `@Repository` | Data access layer (adds exception translation) |
| `@Controller` | Web MVC controller (returns views) |
| `@RestController` | `@Controller` + `@ResponseBody` (returns JSON) |

**They're all `@Component` under the hood** — Spring scans and registers each as a bean. The difference is intent (which layer a class belongs to) plus a couple of extras: `@Repository` translates raw JDBC/JPA exceptions into Spring's `DataAccessException` hierarchy, and `@RestController` bundles `@ResponseBody` so return values are serialized straight to JSON instead of resolved as a view name. Using the right one makes each class's role obvious at a glance.

```java
@RestController                 // returns JSON, not a web page
public class AuditController {
    private final AuditService service;   // the @Service below
    public AuditController(AuditService service) { this.service = service; }
}

@Service                        // "business logic lives here"
public class AuditService { }

@Repository                     // DB exceptions become DataAccessException automatically
public interface AuditRepository extends JpaRepository<AuditLog, Long> { }
```

### Configuration
| Annotation | Purpose |
|-----------|---------|
| `@Configuration` | Class with bean definitions |
| `@Bean` | Method producing a bean |
| `@Autowired` | Inject a dependency |
| `@Qualifier` | Choose between multiple beans of same type |
| `@Value` | Inject a property value |
| `@Profile` | Bean active only in certain profiles |

### Web
| Annotation | Purpose |
|-----------|---------|
| `@RequestMapping` | Map URL to method |
| `@GetMapping`, `@PostMapping` | Shortcut for GET/POST |
| `@PathVariable` | Extract from URL path `/user/{id}` |
| `@RequestParam` | Extract query param `?name=x` |
| `@RequestBody` | Deserialize request body to object |
| `@ResponseStatus` | Set HTTP status |

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { ... }

    @GetMapping
    public List<User> search(@RequestParam String name) { ... }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) { ... }
}
```

---

## Spring Boot Auto-Configuration

`@SpringBootApplication` is a combination of:
```java
@SpringBootApplication
= @Configuration        // marks config class
+ @EnableAutoConfiguration  // auto-configures based on classpath
+ @ComponentScan        // scans for beans in package
```

**How auto-config works:** Spring Boot checks the classpath. If it sees `spring-boot-starter-web`, it auto-configures Tomcat, DispatcherServlet, Jackson, etc. Controlled by `@ConditionalOnClass`, `@ConditionalOnMissingBean`.

---

## Spring AOP (Aspect-Oriented Programming)

Separates cross-cutting concerns (logging, security, transactions) from business logic.

```java
@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.app.service.*.*(..))")
    public void logBefore(JoinPoint jp) {
        System.out.println("Calling: " + jp.getSignature().getName());
    }

    @Around("@annotation(Timed)")
    public Object measureTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        System.out.println("Took: " + (System.currentTimeMillis() - start) + "ms");
        return result;
    }
}
```

**Key terms:**
- **Aspect** — the module (logging)
- **Join point** — a point in execution (method call)
- **Advice** — action taken (@Before, @After, @Around)
- **Pointcut** — expression selecting join points

---

## Spring Data JPA

```java
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}

public interface UserRepository extends JpaRepository<User, Long> {
    // Query derived from method name — no SQL needed
    List<User> findByName(String name);
    List<User> findByAgeGreaterThan(int age);

    @Query("SELECT u FROM User u WHERE u.name LIKE %:kw%")
    List<User> search(@Param("kw") String keyword);
}
```

### @Transactional
```java
@Transactional
public void transfer(Long from, Long to, double amt) {
    debit(from, amt);
    credit(to, amt);
    // If any fails, entire transaction rolls back
}
```

---

## Spring Boot Profiles

Different config per environment.
```yaml
# application.yml
spring:
  profiles:
    active: dev
---
spring:
  config:
    activate:
      on-profile: dev
server:
  port: 8080
---
spring:
  config:
    activate:
      on-profile: prod
server:
  port: 80
```
```java
@Profile("dev")
@Bean
public DataSource devDataSource() { ... }
```

---

## Spring Boot Actuator (you used this)

Production monitoring endpoints.
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```
| Endpoint | Shows |
|----------|-------|
| `/actuator/health` | App + dependencies health |
| `/actuator/metrics` | JVM, memory, HTTP metrics |
| `/actuator/info` | Custom app info |

**Why these matter operationally:** these are plain HTTP endpoints Spring Boot exposes for free once Actuator is on the classpath, and your infrastructure calls them for you. Kubernetes hits `/actuator/health` as a *liveness/readiness probe* — if it returns `DOWN`, the platform stops routing traffic to that pod or restarts it. Prometheus scrapes `/actuator/metrics` (or `/actuator/prometheus`) to graph memory, request latency, and circuit-breaker state in Grafana. So health checks and dashboards work without you writing a single monitoring endpoint.

```java
// A custom health indicator: /actuator/health now reports the audit dependency too
@Component
public class AuditServiceHealth implements HealthIndicator {
    @Override
    public Health health() {
        boolean reachable = pingAuditService();
        return reachable ? Health.up().build()
                         : Health.down().withDetail("audit-service", "unreachable").build();
    }
}
```
```yaml
# Kubernetes wiring the endpoint to a readiness probe
readinessProbe:
  httpGet: { path: /actuator/health, port: 8080 }
  initialDelaySeconds: 10
```

---

## Spring MVC vs Spring WebFlux (you use WebFlux)

| | Spring MVC | Spring WebFlux |
|--|-----------|----------------|
| Model | Blocking, thread-per-request | Non-blocking, reactive |
| Server | Tomcat | Netty |
| Return types | Object, List | Mono, Flux |
| Best for | Traditional CRUD | High concurrency, streaming |

**The core trade-off is how threads are used.** In MVC, each incoming request grabs one thread and *holds* it until the work finishes — if that work waits on a slow downstream call, the thread sits idle but blocked, so 200 slow requests can exhaust a 200-thread pool. In WebFlux, a request that's waiting (e.g., on a `WebClient` call to the audit service) *releases* its thread back to a small event-loop pool and resumes later when data arrives, so a handful of threads serve thousands of concurrent, mostly-waiting requests. That's exactly why the analyzer uses WebFlux + `WebClient`: it fans out to slow AI/audit calls without tying up a thread per in-flight request.

```java
// MVC: this thread is BLOCKED for the whole 2 seconds the audit call takes
@GetMapping("/mvc/{id}")
public AuditLog mvc(@PathVariable Long id) {
    return restTemplate.getForObject("/audit/" + id, AuditLog.class); // thread parked here
}

// WebFlux: the thread is freed while waiting; resumes when the Mono emits
@GetMapping("/reactive/{id}")
public Mono<AuditLog> reactive(@PathVariable Long id) {
    return webClient.get().uri("/audit/{id}", id)
                    .retrieve().bodyToMono(AuditLog.class);  // no thread held while waiting
}
```

```java
// MVC (blocking)
@GetMapping("/user/{id}")
public User getUser(@PathVariable Long id) {
    return userService.findById(id);  // blocks thread
}

// WebFlux (reactive)
@GetMapping("/user/{id}")
public Mono<User> getUser(@PathVariable Long id) {
    return userService.findById(id);  // non-blocking, returns Mono
}
```

### Mono vs Flux
- `Mono<T>` — 0 or 1 element (single result)
- `Flux<T>` — 0 to N elements (stream)

```java
Mono<String> mono = Mono.just("Hello");
Flux<Integer> flux = Flux.just(1, 2, 3).map(n -> n * 2);
flux.subscribe(System.out::println);  // 2, 4, 6
```

---

## Common Spring Interview Questions

**Q: What is the difference between @Component, @Service, @Repository?**
> Functionally similar (all beans), but semantically different. `@Repository` adds automatic exception translation for DB errors. They document intent and layer.

**Q: How does Spring handle a circular dependency?**
> For field/setter injection, Spring resolves it using a 3-level cache (early bean references). For constructor injection, it throws an error — a sign to redesign.

**Q: Singleton bean vs Singleton pattern?**
> Spring singleton = one instance per container. GoF Singleton = one instance per JVM/classloader. Spring's is scoped to the container.

**Q: What is @Transactional propagation?**
> Defines how transactions relate. `REQUIRED` (default) — join existing or create new. `REQUIRES_NEW` — always new. `NESTED` — savepoint within parent.

**Q: How do you secure a Spring Boot REST API?**
> Spring Security with JWT tokens, OAuth2, or basic auth. Configure `SecurityFilterChain`, validate tokens in a filter, use `@PreAuthorize` for method-level security.

**Q: What is a filter vs interceptor?**
> Filter (Servlet level) runs before DispatcherServlet, works on raw request/response. Interceptor (Spring level) runs around controller methods, has access to handler and model.
