# REST API — Interview Preparation

## What is REST?

REST (Representational State Transfer) is an architectural style for designing networked APIs using HTTP. Resources are identified by URLs and manipulated using standard HTTP methods.

### REST Principles (constraints)

1. **Client-Server** — separation of concerns
2. **Stateless** — each request contains all info; server stores no session
3. **Cacheable** — responses can be cached
4. **Uniform Interface** — consistent resource naming and methods
5. **Layered System** — client doesn't know if it talks to server or intermediary
6. **Code on Demand** (optional) — server can send executable code

> **Most important for interviews:** Stateless and Uniform Interface.

---

## HTTP Methods (verbs)

| Method | Purpose | Idempotent? | Safe? |
|--------|---------|-------------|-------|
| GET | Retrieve resource | Yes | Yes |
| POST | Create resource | No | No |
| PUT | Update/replace entire resource | Yes | No |
| PATCH | Partial update | No | No |
| DELETE | Remove resource | Yes | No |

- **Idempotent** — calling it N times = same result as once (GET, PUT, DELETE)
- **Safe** — doesn't modify state (GET only)

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping                        // GET /api/products
    public List<Product> getAll() { }

    @GetMapping("/{id}")               // GET /api/products/5
    public Product getOne(@PathVariable Long id) { }

    @PostMapping                       // POST /api/products
    public Product create(@RequestBody Product p) { }

    @PutMapping("/{id}")               // PUT /api/products/5
    public Product update(@PathVariable Long id, @RequestBody Product p) { }

    @DeleteMapping("/{id}")            // DELETE /api/products/5
    public void delete(@PathVariable Long id) { }
}
```

---

## HTTP Status Codes (know these cold)

### 2xx Success
| Code | Meaning |
|------|---------|
| 200 OK | Request succeeded |
| 201 Created | Resource created (POST) |
| 202 Accepted | Accepted for async processing |
| 204 No Content | Success, no body (DELETE) |

In plain terms, 2xx codes tell the client the request worked, but the exact code carries extra meaning. Use `201` when you create something (return the new resource), `202` when you accept work to finish later (perfect for an async audit that gets queued to Kafka), and `204` when there's nothing to send back.

```java
// POST that creates a resource -> 201 Created + Location header
@PostMapping("/api/audits")
public ResponseEntity<Audit> create(@RequestBody AuditRequest req) {
    Audit saved = auditService.create(req);
    return ResponseEntity
        .created(URI.create("/api/audits/" + saved.getId())) // 201
        .body(saved);
}

// Async: work published to Kafka, processed later -> 202 Accepted
@PostMapping("/api/audits/async")
public ResponseEntity<Void> submitAsync(@RequestBody AuditRequest req) {
    kafkaTemplate.send("audit-requests", req);
    return ResponseEntity.accepted().build();   // 202
}
```

### 3xx Redirection
| Code | Meaning |
|------|---------|
| 301 Moved Permanently | Resource moved |
| 304 Not Modified | Use cached version |

These matter for caching. The client sends an `If-None-Match` header with the ETag it already has; if nothing changed, the server replies `304` with an empty body so the browser reuses its cached copy instead of re-downloading. This saves bandwidth on things like a rarely-changing audit-rules endpoint.

```
GET /api/audit-rules
If-None-Match: "v42"

HTTP/1.1 304 Not Modified      <-- no body sent; client uses its cache
```

### 4xx Client Errors
| Code | Meaning |
|------|---------|
| 400 Bad Request | Invalid input |
| 401 Unauthorized | Not authenticated |
| 403 Forbidden | Authenticated but not allowed |
| 404 Not Found | Resource doesn't exist |
| 409 Conflict | State conflict (duplicate) |
| 429 Too Many Requests | Rate limited |

4xx means "the caller made a mistake," so the fix is on the client side. The two most confused are 401 vs 403: `401` means "I don't know who you are" (no/invalid token), while `403` means "I know who you are, but you can't do this." A 409 typically fires when you try to create something that already exists.

```java
// 401 vs 403
if (token == null || !token.isValid())
    throw new UnauthorizedException();        // 401 - authenticate first
if (!user.hasRole("AUDITOR"))
    throw new ForbiddenException();           // 403 - logged in, still denied

// 409 Conflict - duplicate audit for the same contract
if (auditRepository.existsByContractId(req.getContractId()))
    throw new ConflictException("Audit already exists for this contract"); // 409
```

### 5xx Server Errors
| Code | Meaning |
|------|---------|
| 500 Internal Server Error | Generic server failure |
| 502 Bad Gateway | Upstream server error |
| 503 Service Unavailable | Server overloaded/down |
| 504 Gateway Timeout | Upstream timed out |

5xx means the server (not the caller) failed, so the client can safely retry later. The 502/503/504 family shows up in a microservices setup: if your WebClient call to a downstream service fails or times out, the gateway surfaces it as `502`/`504`. A common pattern is to catch that and return a clean error instead of leaking a raw stack trace.

```java
// WebClient call to a downstream service, mapping failures to sensible codes
webClient.get().uri("/contracts/{id}", id)
    .retrieve()
    .onStatus(HttpStatusCode::is5xxServerError,
        resp -> Mono.error(new ServiceUnavailableException("Contract service down"))) // 503
    .bodyToMono(Contract.class)
    .timeout(Duration.ofSeconds(3));  // if it hangs, surfaces as a 504 to the caller
```

---

## REST Best Practices

### 1. Use nouns, not verbs in URLs
```
Good:  GET /api/orders/5
Bad:   GET /api/getOrder?id=5
```

### 2. Use plurals for collections
```
/api/users
/api/users/123
/api/users/123/orders
```

### 3. Version your API
```
/api/v1/users
/api/v2/users
```

### 4. Use proper status codes
Don't return 200 for everything. Return 201 for created, 404 for not found, etc.

### 5. Consistent error responses
```json
{
  "timestamp": "2026-08-28T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product with id 5 not found",
  "path": "/api/products/5"
}
```

### 6. Support pagination, filtering, sorting
```
GET /api/products?page=0&size=20&sort=price,desc&category=electronics
```

---

## Exception Handling in REST (Spring)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse(404, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        return new ErrorResponse(400, "Validation failed");
    }
}
```
> **Your resume mentions** "Engineered global exception handlers to convert raw system errors into standardized, user-friendly format messages" — this is exactly `@RestControllerAdvice`.

---

## Input Validation

```java
public class UserRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email")
    private String email;

    @Min(18) @Max(100)
    private int age;
}

@PostMapping
public User create(@Valid @RequestBody UserRequest req) { ... }
```

---

## REST vs SOAP vs GraphQL

| | REST | SOAP | GraphQL |
|--|------|------|---------|
| Format | JSON (usually) | XML | JSON |
| Style | Resource-based | Protocol | Query language |
| Flexibility | Fixed endpoints | Rigid | Client picks fields |
| Over/under-fetching | Common issue | N/A | Solved |
| Use | Most web APIs | Enterprise, legacy | Complex data needs |

The key practical difference is "over-fetching." With REST, an endpoint returns a fixed shape, so if the UI only needs a contract's `id` and `status` it still receives the whole object. GraphQL lets the client ask for exactly those two fields in one query. For this audit service, REST is the right pick — the endpoints are simple and resource-based — but the contrast is worth knowing.

```graphql
# GraphQL: client requests only the fields it needs
query {
  audit(id: "5") {
    id
    status          # no wasted payload for description, timestamps, etc.
  }
}
```
```
REST equivalent: GET /api/audits/5  ->  returns the ENTIRE audit object
```

---

## Idempotency in APIs (advanced)

For payment/order APIs, use an **idempotency key** so retries don't duplicate.
```
POST /api/payments
Idempotency-Key: unique-request-id-123
```
Server checks if the key was seen; if yes, returns the previous result instead of processing again.

---

## Common REST Interview Questions

**Q: Is REST stateless? Why does it matter?**
> Yes. Each request carries all context (no server session). This enables horizontal scaling — any server can handle any request, and load balancing is simple.

**Q: PUT vs PATCH?**
> PUT replaces the entire resource (idempotent). PATCH applies a partial update (not necessarily idempotent).

**Q: POST vs PUT for creation?**
> POST when the server assigns the ID (not idempotent). PUT when the client specifies the ID/URL (idempotent).

**Q: How do you handle authentication in REST?**
> Stateless tokens — JWT in the Authorization header, or OAuth2. The server validates the token on each request without storing session state.

**Q: What is HATEOAS?**
> Hypermedia As The Engine Of Application State — responses include links to related actions. The highest level of REST maturity (Richardson Maturity Model level 3). Rarely fully implemented.

**Q: How do you handle rate limiting?**
> Token bucket or sliding window algorithm, often at the API Gateway. Return 429 with a `Retry-After` header. In my project I implemented it using Redis atomic increment with TTL.
