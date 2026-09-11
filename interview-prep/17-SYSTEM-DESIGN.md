# System Design — Interview Preparation

At 12.5 years, expect at least one open-ended design question ("Design X"). This doc gives you a repeatable framework, the core building blocks, and worked examples.

## The Framework (use this structure every time)

```
1. Clarify requirements   — functional + non-functional, scope it down
2. Estimate scale         — users, QPS, data size (back-of-envelope)
3. Define APIs            — key endpoints
4. High-level design      — draw boxes: client, LB, services, DB, cache, queue
5. Deep dive              — pick 1-2 components, discuss trade-offs
6. Address bottlenecks    — scaling, caching, sharding, failure handling
```

> Always start by ASKING clarifying questions. Interviewers grade how you think, not a memorized answer.

## Functional vs Non-Functional Requirements

- **Functional** — what it does ("users can shorten a URL and get redirected")
- **Non-functional** — how well ("99.9% uptime, <100ms latency, 100M URLs, read-heavy")

## Back-of-the-Envelope Estimation

Learn these numbers:
```
1 day       = 86,400 seconds (~10^5)
1 million writes/day ≈ 12 writes/second
1 KB per record × 1M records = 1 GB
Read:Write ratio — most systems are read-heavy (e.g., 100:1)
```
Example: "100M URLs, 500 bytes each = 50 GB storage. 100:1 read:write, 1000 writes/sec → 100,000 reads/sec → need caching + read replicas."

## Core Building Blocks

### Load Balancer
Distributes traffic across servers. Algorithms: round-robin, least-connections, IP-hash. Provides high availability (routes around dead servers).

### Caching
Store frequently-accessed data in memory (Redis) to reduce DB load and latency.
- **Cache-aside** (lazy) — app checks cache, on miss loads from DB and populates. (You used this pattern in your RAG cache.)
- **Write-through** — write to cache and DB together.
- **Write-behind** — write to cache, async to DB.
- **Eviction:** LRU (least recently used), LFU, TTL.

```
Read: check Redis → hit? return : miss? → DB → populate cache → return
```

### Database Scaling
- **Replication** — primary handles writes, replicas handle reads (scales reads).
- **Sharding/Partitioning** — split data across DBs by a key (scales writes). E.g., shard by user_id.
- **Vertical partitioning** — split columns/tables by feature.

### CAP Theorem (must know)
In a distributed system, you can only guarantee 2 of 3:
- **C**onsistency — every read sees the latest write
- **A**vailability — every request gets a response
- **P**artition tolerance — works despite network splits

Since network partitions are unavoidable, you really choose **CP** (consistency, e.g., banking) or **AP** (availability, e.g., social feed). SQL leans CP; many NoSQL lean AP.

### Message Queue (Kafka/RabbitMQ)
Decouples producers from consumers, smooths traffic spikes, enables async processing. (Your audit service uses Kafka for this.)

### CDN (Content Delivery Network)
Caches static content (images, JS) at edge locations near users for low latency.

### API Gateway
Single entry point — routing, auth, rate limiting, aggregation.

## Scaling Strategies

| Strategy | What |
|----------|------|
| Vertical scaling | Bigger server (limited) |
| Horizontal scaling | More servers (preferred) |
| Stateless services | Any server handles any request (enables horizontal) |
| Caching | Reduce repeated work |
| DB replication | Scale reads |
| DB sharding | Scale writes |
| Async processing | Queue heavy work |
| CDN | Offload static content |

## Worked Example 1: Design a URL Shortener

**Requirements:** shorten long URL → short code; redirect; 100M URLs; read-heavy.

**API:**
```
POST /shorten { longUrl }  → { shortUrl }
GET  /{code}               → 301 redirect to longUrl
```

**Design:**
```
Client → LB → App Servers → Cache (Redis) → DB
                                 ↑
              write: generate unique code, store code→url
              read:  check cache, else DB, then cache it
```

**Key decisions:**
- **Code generation:** base62 encode an auto-increment ID (7 chars = 62^7 ≈ 3.5 trillion), or hash + collision check.
- **Storage:** key-value fits well (code → URL). Read-heavy → heavy caching + read replicas.
- **Redirect:** 301 (permanent, cached by browser) vs 302 (temporary, lets you track clicks).

## Worked Example 2: Design a Rate Limiter

**Algorithms:**
- **Token Bucket** — tokens refill at a fixed rate; each request consumes one; empty = reject. (Most common.)
- **Sliding Window** — count requests in the rolling time window.
- **Fixed Window** — count per fixed interval (simpler, edge bursts).

**Implementation (you did this with Redis):**
```java
// Redis atomic increment with TTL
Long count = redis.increment("rate:" + userId);
if (count == 1) redis.expire(key, Duration.ofMinutes(1));
return count <= 20;  // 20 requests/minute
```
Distributed rate limiting needs a shared store (Redis) so all servers agree on the count.

## Worked Example 3: Design a Notification System (like your email feature)

```
Event source → Kafka topic → Consumer(s) → [Email / SMS / Push] providers
                                    ↓
                          template + user preferences
```
- **Async via Kafka** — decouples event from delivery (your audit → email flow).
- **Fan-out** — one event, multiple channels.
- **Retry + Dead Letter Queue** — failed sends retried, then parked.
- **Idempotency** — dedupe so users don't get double notifications.

## Common System Design Questions

**Q: How do you scale a read-heavy system?**
> Add caching (Redis), read replicas, and a CDN for static content. Keep services stateless so you can scale horizontally behind a load balancer.

**Q: How do you handle a sudden traffic spike?**
> Auto-scaling (HPA), a message queue to buffer/smooth writes, caching to absorb reads, rate limiting to protect downstream, and circuit breakers to fail fast.

**Q: SQL or NoSQL for this design?**
> SQL for structured, relational, transactional data with strong consistency. NoSQL for massive scale, flexible schema, or specific access patterns. Often both (polyglot persistence).

**Q: How do you ensure high availability?**
> Redundancy (multiple instances/AZs), load balancing, health checks, failover replicas, no single point of failure, and graceful degradation.

**Q: How do you keep two services' data consistent?**
> Prefer eventual consistency via events (Kafka). For multi-step operations, the Saga pattern with compensating transactions. Avoid distributed 2PC.
