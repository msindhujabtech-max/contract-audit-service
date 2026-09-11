# Interview Preparation Master Guide — M. Sindhuja (12.5 yrs)

This guide covers every technology in your resume with concepts, versions, features, purpose, and examples.

## How to Use This Guide

Read in order, but focus extra time on **Java, Spring Boot, Microservices, and ATG** — these are your core strengths and most likely deep-dive areas.

## Document Index

| # | File | Topics Covered |
|---|------|----------------|
| 1 | `01-JAVA-CORE.md` | Java versions (8-21), OOP, Collections, Streams, Functional Interfaces, Concurrency |
| 2 | `02-JAVA-ADVANCED.md` | Multithreading, JVM, Memory, Garbage Collection, Exception Handling, Generics |
| 3 | `03-SPRING-BOOT.md` | Spring Core, Boot, DI, Beans, AOP, Annotations, Auto-config, Profiles |
| 4 | `04-MICROSERVICES.md` | Patterns, Communication, Resilience, Service Discovery, API Gateway, Saga, CQRS |
| 5 | `05-REST-API.md` | REST principles, HTTP methods, status codes, versioning, best practices |
| 6 | `06-DATABASES.md` | Oracle, MySQL, SQL, Liquibase, indexing, transactions, joins, PostgreSQL/pgvector |
| 7 | `07-DEVOPS-CLOUD.md` | Docker, Kubernetes, Kafka, Jenkins, Maven/Gradle, AWS/Azure/GCP, Terraform |
| 8 | `08-TESTING.md` | JUnit, Mockito, PowerMock, test strategies |
| 9 | `09-ATG-COMMERCE.md` | Oracle ATG (DCS, DPS, ACC, BCC), repositories, droplets, form handlers, pipelines |
| 10 | `10-GENAI-RAG.md` | RAG, Vector DBs, LangChain, LangGraph, LlamaIndex, embeddings, LLMs |
| 11 | `11-BEHAVIORAL-LEADERSHIP.md` | Leadership, Agile, project scenarios based on your experience |
| 12 | `12-QUICK-REVISION.md` | One-page cheat sheet for last-minute review |
| 13 | `13-DIFFERENCES-JAVA.md` | All "X vs Y" for Java (JDK/JRE/JVM, HashMap/Hashtable, etc.) |
| 14 | `14-DIFFERENCES-SPRING-MICROSERVICES.md` | All "X vs Y" for Spring, Microservices, REST (Kafka vs WebClient, etc.) |
| 15 | `15-DIFFERENCES-DB-DEVOPS.md` | All "X vs Y" for DB, DevOps, Cloud (AWS vs Azure vs GCP, etc.) |
| 16 | `16-DIFFERENCES-ATG-GENAI.md` | All "X vs Y" for ATG & Gen AI (RAG vs Fine-tuning, LangChain vs LangGraph, etc.) |

## Difference Documents (13-16) — Highly Requested in Interviews

Interviewers frequently ask "what is the difference between X and Y". Documents 13-16 collect **every applicable comparison** across all your technologies in quick-reference tables. Review these thoroughly — they're among the most predictable questions.

## Advanced / Gap-Filling Documents (17-21)

| # | File | Topics Covered |
|---|------|----------------|
| 17 | `17-SYSTEM-DESIGN.md` | Design framework, load balancing, caching, sharding, CAP, worked examples (URL shortener, rate limiter, notifications) |
| 18 | `18-SECURITY.md` | OWASP Top 10, SQL injection, XSS, CSRF, JWT/OAuth2, Spring Security, password hashing |
| 19 | `19-CONCURRENCY-DEEPDIVE.md` | CountDownLatch, Semaphore, BlockingQueue, thread pools, JMM/happens-before, locks |
| 20 | `20-KAFKA-DEEPDIVE.md` | Partitions, consumer groups, offsets, delivery semantics, replication, idempotency |
| 21 | `21-DSA.md` | Big-O, data structures, patterns (two-pointer, sliding window), classic problems |

These fill the gaps most likely probed at a senior/lead level. **System Design (17)** is the highest priority — expect an open-ended design question. **Security (18)** is expected for a lead. **Concurrency (19)** and **Kafka (20)** go deeper on things you claim on your resume/project.

## Advanced / Senior-Level Documents (17-21)

These fill gaps critical for a 12.5-year senior/lead role:

| # | File | Topics Covered |
|---|------|----------------|
| 17 | `17-SYSTEM-DESIGN.md` | HLD framework, load balancing, caching, CAP, capacity estimation, worked designs (URL shortener, rate limiter, checkout) |
| 18 | `18-SECURITY.md` | OWASP Top 10, SQL injection, XSS/CSRF, JWT, OAuth2, Spring Security, password hashing, secrets |
| 19 | `19-CONCURRENCY-DEEPDIVE.md` | JMM/happens-before, CountDownLatch, Semaphore, BlockingQueue, ThreadLocal, CompletableFuture, locks |
| 20 | `20-KAFKA-DEEPDIVE.md` | Partitions, consumer groups, rebalancing, offsets, delivery guarantees, DLQ, replication |
| 21 | `21-DSA.md` | Big-O, data structures, key patterns (two pointers, sliding window, BFS/DFS), common problems, DP, sorting |

**Priority for 3-day prep:** System Design (17) and Security (18) are the most likely to be probed at your level. Concurrency (19) and Kafka (20) back up your resume claims. DSA (21) if the company has a coding round.

## Your Resume Technology Map

```
CORE (deep-dive expected)
├── Java (8, 11, 17, 21)
├── Spring Boot + Microservices
├── REST API
└── Oracle ATG Web Commerce

SUPPORTING (solid understanding expected)
├── Databases: Oracle, MySQL, Liquibase, SQL
├── DevOps: Docker, Kubernetes, Kafka, Jenkins, Maven, Gradle
├── Cloud: AWS, Azure, GCP
├── Testing: JUnit, Mockito, PowerMock
└── Middleware: JBoss, WebLogic

EMERGING (your upskilling story)
├── Python, LangGraph, LangChain, LlamaIndex
├── RAG, Vector Databases
├── Spring AI, WebFlux, Ollama
└── Terraform, Zipkin, Circuit Breaker, CQRS
```

## Interview Strategy for 12.5 Years Experience

At your level, interviewers focus on:
1. **Depth** — not "what is X" but "why X over Y", "how does X work internally"
2. **Design decisions** — architecture, trade-offs, scalability
3. **Leadership** — mentoring, decision-making, handling failures
4. **Real examples** — from Boeing, Rogers/Vodafone, Nike projects

Always answer with: **Concept → Why it matters → Real example from your work**
