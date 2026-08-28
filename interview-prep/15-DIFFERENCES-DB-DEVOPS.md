# Differences (X vs Y) — Databases, DevOps, Cloud

Each has a quick table, followed by a plain-English explanation for when the table alone isn't enough.

# Database Differences

## SQL vs NoSQL
| SQL | NoSQL |
|-----|-------|
| Relational, tables | Document/key-value/graph/column |
| Fixed schema | Flexible schema |
| ACID | BASE (eventual) |
| Vertical scaling | Horizontal scaling |
| Complex joins | Denormalized |
| Oracle, MySQL | MongoDB, Cassandra, Redis |

**Explanation:** **SQL** databases store data in structured tables with a fixed schema and relationships, guaranteeing strong consistency (ACID) — great for structured, relational data (orders, accounts). **NoSQL** databases are flexible — documents, key-value, graph, or wide-column — with loose schemas that scale horizontally across many servers, favoring availability over strict consistency (BASE). Use SQL for structured, transactional data; NoSQL for huge scale, flexible/changing data, or specific access patterns (caching, documents).

## Oracle vs MySQL
| Oracle | MySQL |
|--------|-------|
| Enterprise, paid | Open-source (Oracle-owned) |
| PL/SQL | Stored procs (less rich) |
| Sequences | AUTO_INCREMENT |
| ROWNUM / FETCH FIRST | LIMIT |
| More features/scale | Lightweight, web apps |

**Explanation:** Both are relational SQL databases. **Oracle** is the heavyweight enterprise database — licensed/paid, extremely feature-rich (advanced PL/SQL, partitioning, RAC clustering), used for large mission-critical systems (like your ATG commerce projects). **MySQL** is a lighter, free/open-source database popular for web applications. Syntax differs: Oracle uses sequences and `ROWNUM`/`FETCH FIRST`, MySQL uses `AUTO_INCREMENT` and `LIMIT`.

## DELETE vs TRUNCATE vs DROP
| DELETE | TRUNCATE | DROP |
|--------|----------|------|
| Rows (WHERE) | All rows | Entire table |
| DML | DDL | DDL |
| Rollback-able | Usually not | No |
| Slow, logged | Fast | Removes structure |
| Triggers fire | No triggers | N/A |

**Explanation:** All remove data but at different levels. **DELETE** removes specific rows (you can use WHERE), logs each row, can be rolled back, and fires triggers — but is slower. **TRUNCATE** wipes ALL rows instantly with minimal logging (can't filter, usually can't roll back, no triggers) — fast reset of a table. **DROP** removes the entire table STRUCTURE itself (columns, indexes, data — everything gone). Order of destructiveness: DELETE (some rows) < TRUNCATE (all rows) < DROP (whole table).

## WHERE vs HAVING
| WHERE | HAVING |
|-------|--------|
| Filters rows | Filters groups |
| Before GROUP BY | After GROUP BY |
| No aggregates | Aggregates allowed |

**Explanation:** Both filter, but at different stages. **WHERE** filters individual ROWS before they're grouped, and can't use aggregate functions like COUNT/SUM. **HAVING** filters the GROUPS after `GROUP BY` has aggregated them, and CAN use aggregates.
```sql
SELECT dept, COUNT(*) FROM employees
WHERE salary > 50000        -- filter rows first
GROUP BY dept
HAVING COUNT(*) > 5;        -- then filter groups
```

## INNER JOIN vs OUTER JOIN
| INNER | OUTER (LEFT/RIGHT/FULL) |
|-------|-------------------------|
| Only matching rows | Includes non-matching (nulls) |
| Intersection | Union-ish |

**Explanation:** A **JOIN** combines rows from two tables. **INNER JOIN** returns only rows that have a match in BOTH tables (the intersection). **OUTER JOIN** also includes non-matching rows, filling the missing side with NULLs — **LEFT** keeps all rows from the left table, **RIGHT** keeps all from the right, **FULL** keeps all from both. Use INNER when you only want matched data; OUTER when you also need the unmatched rows (e.g., customers with no orders).

## Primary Key vs Unique Key
| Primary Key | Unique Key |
|-------------|------------|
| One per table | Many allowed |
| Not null | One null allowed (most DBs) |
| Clustered index (usually) | Non-clustered |

**Explanation:** Both enforce uniqueness. A **Primary Key** uniquely identifies each row — there's exactly ONE per table, it can't be null, and it's typically the clustered index. A **Unique Key** also enforces no duplicates but you can have MANY per table and it usually allows one null. Use the primary key as the row's main identifier; unique keys for other columns that must be unique (like email).

## Primary Key vs Foreign Key
| Primary Key | Foreign Key |
|-------------|-------------|
| Identifies row uniquely | References another table's PK |
| One per table | Many allowed |
| Not null | Can be null |

**Explanation:** A **Primary Key** uniquely identifies a row within its own table. A **Foreign Key** is a column that POINTS to the primary key of ANOTHER table, creating a relationship (e.g., an `orders` table has a `customer_id` foreign key referencing the `customers` table's primary key). Foreign keys enforce referential integrity — you can't reference a customer that doesn't exist.

## Clustered vs Non-clustered Index
| Clustered | Non-clustered |
|-----------|---------------|
| Physical row order | Separate structure + pointers |
| One per table | Many per table |
| Faster range queries | Extra lookup |

**Explanation:** A **clustered index** determines the PHYSICAL order in which rows are stored on disk (like a phone book sorted by last name) — there's only one per table because data can only be physically sorted one way, and range queries are fast. A **non-clustered index** is a SEPARATE structure holding sorted keys with pointers back to the rows (like a book's index pointing to page numbers) — you can have many, but looking up data needs an extra hop to fetch the actual row.

## char vs varchar
| char | varchar |
|------|---------|
| Fixed length | Variable length |
| Pads with spaces | Uses actual length |
| Faster fixed data | Space-efficient |

**Explanation:** **char(10)** always reserves 10 characters, padding short values with spaces — good for fixed-length data like country codes ("US "). **varchar(10)** stores only the actual length used, up to 10 — space-efficient for variable data like names. Use char when values are truly fixed-length; varchar for everything else (the common choice).

## UNION vs UNION ALL
| UNION | UNION ALL |
|-------|-----------|
| Removes duplicates | Keeps duplicates |
| Slower (sort) | Faster |

**Explanation:** Both combine the results of two queries into one. **UNION** removes duplicate rows, which requires an extra sorting/comparison step (slower). **UNION ALL** keeps everything including duplicates (faster, no dedup work). If you know there are no duplicates or don't care, use UNION ALL for performance.

## Stored Procedure vs Function
| Stored Procedure | Function |
|------------------|----------|
| May return 0/many values | Must return one value |
| Can modify data | Usually read-only |
| Called with CALL/EXEC | Used in SQL expressions |

**Explanation:** Both are reusable blocks of database logic. A **stored procedure** performs actions (insert/update/delete), may return zero or many results, and is called explicitly with CALL/EXEC. A **function** computes and RETURNS a single value, is usually read-only, and can be used directly inside SQL expressions (like `SELECT calculate_tax(price)`). Procedures = do something; functions = calculate and return.

## OLTP vs OLAP
| OLTP | OLAP |
|------|------|
| Transactional | Analytical |
| Many small writes | Complex reads |
| Normalized | Denormalized (star schema) |
| E-commerce orders | Reporting/BI |

**Explanation:** **OLTP** (Online Transaction Processing) handles day-to-day operations — lots of small, fast reads/writes (placing an order, updating a profile) on a normalized schema. **OLAP** (Online Analytical Processing) handles reporting/analytics — complex queries scanning huge amounts of data (sales trends over years) on a denormalized/star schema optimized for reads. OLTP runs the business; OLAP analyzes it.

## Liquibase vs Flyway
| Liquibase | Flyway |
|-----------|--------|
| XML/YAML/JSON/SQL | SQL-first |
| Rollback support | Limited rollback |
| DB-agnostic abstraction | Simpler |

**Explanation:** Both are database migration tools that version-control your schema changes like code. **Liquibase** (which you use) supports multiple formats (XML/YAML/JSON/SQL), abstracts away DB-specific syntax, and has strong rollback support — more powerful and flexible. **Flyway** is SQL-first and simpler — you write plain SQL migration scripts. Both track applied changes so each runs exactly once. Liquibase for flexibility/rollback; Flyway for simplicity.

## Optimistic vs Pessimistic Locking
| Optimistic | Pessimistic |
|------------|-------------|
| Version check on update | Lock row upfront |
| No locks, retry on conflict | Blocks others |
| High concurrency, rare conflicts | Frequent conflicts |

**Explanation:** Two strategies for handling concurrent updates to the same row. **Optimistic locking** assumes conflicts are RARE — it doesn't lock; instead it checks a version number at update time and fails if someone else changed the row (then you retry). Great for high concurrency. **Pessimistic locking** assumes conflicts are LIKELY — it locks the row upfront so others must wait. Safer under heavy contention but reduces concurrency. Optimistic for mostly-reads; pessimistic for hot, frequently-contended rows.

---

# DevOps Differences

## Container vs Virtual Machine
| Container | VM |
|-----------|-----|
| Shares host kernel | Full guest OS |
| MBs, seconds to start | GBs, minutes |
| Less isolation | Full isolation |
| Docker | VMware, VirtualBox |

**Explanation:** A **VM** virtualizes an entire computer — it runs a full guest operating system on top of a hypervisor, so it's heavy (GBs) and slow to boot (minutes) but fully isolated. A **container** shares the host's OS kernel and only packages the app + its dependencies — so it's tiny (MBs), starts in seconds, and is far more efficient, with slightly less isolation. Analogy: VMs are separate houses (each with its own plumbing); containers are apartments sharing the building's infrastructure.

## Docker Image vs Container
| Image | Container |
|-------|-----------|
| Read-only template | Running instance |
| Blueprint | Live process |
| Stored in registry | Runtime |

**Explanation:** A Docker **image** is a read-only blueprint — the packaged app, dependencies, and instructions (built from a Dockerfile). A **container** is a running INSTANCE of that image — a live process. Analogy: the image is a class, the container is an object; or the image is a recipe, the container is the cooked dish. You can run many containers from one image.

## Docker Compose vs Kubernetes
| Docker Compose | Kubernetes |
|----------------|------------|
| Single host | Multi-host cluster |
| Dev/simple | Production/scale |
| No auto-healing | Self-healing |
| No auto-scaling | HPA auto-scaling |
| Manual updates | Rolling updates |

**Explanation:** **Docker Compose** runs multiple containers on ONE machine using a simple YAML file — perfect for development and small deployments (like your GCP VM). **Kubernetes** orchestrates containers across a CLUSTER of many machines and adds production features: self-healing (restarts crashed containers), auto-scaling (adds pods under load), rolling updates (zero-downtime deploys), and load balancing. Compose for dev/single host; Kubernetes for production scale.

## Kubernetes Pod vs Deployment vs Service
| Pod | Deployment | Service |
|-----|------------|---------|
| Smallest unit (containers) | Manages pod replicas | Network endpoint + LB |
| Ephemeral | Rolling updates, scaling | Stable DNS |

**Explanation:** A **Pod** is the smallest deployable unit — one or more containers running together; pods are disposable and can die anytime. A **Deployment** manages pods — it ensures the desired number of replicas are running, handles rolling updates, and recreates crashed pods. A **Service** gives a stable network address and load-balances traffic across the pods (since pods come and go with changing IPs). Deployment keeps pods alive; Service gives them a stable front door.

## Liveness vs Readiness Probe
| Liveness | Readiness |
|----------|-----------|
| Is app alive? | Ready for traffic? |
| Restarts pod if fails | Removes from LB if fails |

**Explanation:** Kubernetes health checks. The **liveness probe** asks "is this app still alive?" — if it fails, Kubernetes RESTARTS the pod (e.g., app is deadlocked). The **readiness probe** asks "is this app ready to serve requests?" — if it fails, Kubernetes stops sending it traffic (removes it from the load balancer) but does NOT restart it (e.g., app is still warming up or a dependency is temporarily down). Liveness = restart if broken; readiness = pause traffic until ready.

## ConfigMap vs Secret
| ConfigMap | Secret |
|-----------|--------|
| Non-sensitive config | Sensitive data |
| Plain text | Base64/encrypted |

**Explanation:** Both inject configuration into Kubernetes pods. A **ConfigMap** holds non-sensitive settings (URLs, feature flags, log levels) in plain text. A **Secret** holds sensitive data (passwords, API keys, tokens), stored base64-encoded and can be encrypted at rest with tighter access control. Same idea, but use Secret for anything confidential.

## StatefulSet vs Deployment
| StatefulSet | Deployment |
|-------------|------------|
| Stable identity/storage | Interchangeable pods |
| Databases | Stateless apps |
| Ordered scaling | Any order |

**Explanation:** A **Deployment** manages stateless pods that are interchangeable — any pod can handle any request, and they can be created/destroyed in any order (web apps, APIs). A **StatefulSet** manages pods that need a STABLE identity and their own persistent storage — each pod has a fixed name and disk that survives restarts, and they scale in order (databases, Kafka, Zookeeper). Use Deployment for stateless services; StatefulSet for stateful ones.

## Maven vs Gradle
| Maven | Gradle |
|-------|--------|
| XML (pom.xml) | Groovy/Kotlin DSL |
| Convention | Flexible |
| Slower | Faster (incremental) |

**Explanation:** Both are Java build tools that manage dependencies and build your project. **Maven** uses an XML file (`pom.xml`) and a fixed convention-based lifecycle — predictable and widely used. **Gradle** uses a Groovy/Kotlin script — more flexible and faster thanks to incremental builds and caching. Maven for simplicity and convention; Gradle for flexibility and speed on large builds.

## Maven compile vs provided vs runtime vs test scope
| Scope | Available at |
|-------|--------------|
| compile | Everywhere (default) |
| provided | Compile, not runtime (servlet API) |
| runtime | Runtime only (JDBC driver) |
| test | Testing only (JUnit) |

**Explanation:** A dependency's **scope** controls when it's on the classpath. **compile** (default) — needed everywhere (compiling, running, testing). **provided** — needed to compile but supplied by the runtime environment, so not bundled (e.g., the Servlet API provided by the app server). **runtime** — not needed to compile but needed to run (e.g., a JDBC driver loaded dynamically). **test** — only for tests, never shipped (e.g., JUnit). Scopes keep your final artifact lean.

## CI vs CD
| CI | CD |
|----|-----|
| Continuous Integration | Continuous Delivery/Deployment |
| Auto build + test on merge | Auto release to environments |

**Explanation:** **CI (Continuous Integration)** — developers merge code frequently, and each merge automatically triggers a build and test run to catch problems early. **CD (Continuous Delivery/Deployment)** — the next step: automatically releasing that tested code to staging or production. Delivery = ready to deploy at the click of a button; Deployment = fully automatic to production. Together (CI/CD) they automate the path from code to production.

## Blue-Green vs Canary Deployment
| Blue-Green | Canary |
|------------|--------|
| Two full environments, switch | Gradual % rollout |
| Instant switch/rollback | Progressive |
| Double resources | Less risk |

**Explanation:** Two zero-downtime deployment strategies. **Blue-Green** runs two identical environments — "blue" (current) and "green" (new); you deploy to green, test it, then switch ALL traffic at once (instant rollback by switching back). Needs double resources. **Canary** releases the new version to a SMALL percentage of users first, monitors it, then gradually increases if healthy — lower risk, catches issues early with limited blast radius.

## Jenkins vs GitHub Actions
| Jenkins | GitHub Actions |
|---------|----------------|
| Self-hosted server | Cloud-native (GitHub) |
| Plugins | YAML workflows |
| Flexible | Tight GitHub integration |

**Explanation:** Both automate CI/CD pipelines. **Jenkins** is a self-hosted automation server you install and maintain, with a huge plugin ecosystem and maximum flexibility (but you manage the infrastructure). **GitHub Actions** is built into GitHub — you define workflows in YAML right in your repo, with nothing to host — very convenient if your code is already on GitHub. Jenkins for flexibility/self-hosting; Actions for seamless GitHub integration.

## Kafka Delivery: At-most vs At-least vs Exactly-once
| At-most-once | At-least-once | Exactly-once |
|--------------|---------------|--------------|
| May lose | No loss, may duplicate | No loss, no duplicate |
| Fire-forget | Common default | Needs idempotency |

**Explanation:** Message delivery guarantees. **At-most-once** — a message is delivered zero or one time; it may be LOST but never duplicated (fastest, least safe). **At-least-once** — a message is never lost but may be delivered more than once on retries (the common default; consumers must handle duplicates). **Exactly-once** — delivered precisely one time, no loss or duplicates — the strongest guarantee but the hardest, requiring idempotent processing and transactions.

## Terraform vs Ansible
| Terraform | Ansible |
|-----------|---------|
| Provisioning (infra) | Configuration mgmt |
| Declarative | Procedural |
| State-based | Agentless |
| Create VMs/networks | Configure servers |

**Explanation:** Both are Infrastructure-as-Code tools but focus differently. **Terraform** PROVISIONS infrastructure — it creates cloud resources like VMs, networks, and firewalls (what you used on GCP), tracking their state declaratively. **Ansible** CONFIGURES existing servers — installing software, editing config files, running commands, step by step (procedural). They're often used together: Terraform builds the servers, Ansible configures them.

## Terraform vs CloudFormation
| Terraform | CloudFormation |
|-----------|----------------|
| Multi-cloud | AWS only |
| HCL | JSON/YAML |
| Third-party | AWS native |

**Explanation:** Both provision cloud infrastructure as code. **Terraform** (by HashiCorp) is multi-cloud — the same tool works with AWS, Azure, GCP, and more, using its own HCL language. **CloudFormation** is AWS's native tool — only works with AWS, using JSON/YAML. Terraform if you're multi-cloud or prefer one tool everywhere; CloudFormation if you're all-in on AWS and want the native, tightly-integrated option.

---

# Cloud Differences

## AWS vs Azure vs GCP
| Feature | AWS | Azure | GCP |
|---------|-----|-------|-----|
| Market share | Largest | Enterprise/Microsoft | Data/AI/K8s |
| VM | EC2 | Virtual Machines | Compute Engine |
| Kubernetes | EKS | AKS | GKE (best K8s) |
| Serverless | Lambda | Functions | Cloud Functions |
| Storage | S3 | Blob | Cloud Storage |
| Database | RDS | SQL Database | Cloud SQL |
| Secrets | Secrets Manager | Key Vault | Secret Manager |
| Strength | Broadest services | MS ecosystem | AI/ML, Kubernetes origin |

**Explanation:** The three big cloud providers offer similar building blocks under different names. **AWS** is the market leader with the broadest range of services. **Azure** is favored by enterprises already in the Microsoft ecosystem (Windows, Office, Active Directory). **GCP** is strong in data, AI/ML, and Kubernetes (Google invented Kubernetes). The concepts map 1:1 — a VM is EC2/Virtual Machines/Compute Engine; managed Kubernetes is EKS/AKS/GKE — so skills transfer across all three. You deployed on GCP Compute Engine.

## IaaS vs PaaS vs SaaS
| IaaS | PaaS | SaaS |
|------|------|------|
| Infrastructure (VMs) | Platform (runtime) | Software (app) |
| EC2, Compute Engine | App Engine, Heroku | Gmail, Salesforce |
| You manage OS+app | You manage app | Use only |
| Most control | Balanced | Least effort |

**Explanation:** Three levels of cloud service, by how much YOU manage. **IaaS** (Infrastructure as a Service) — you get raw VMs and manage the OS, runtime, and app yourself (most control, most work) — e.g., your GCP VM. **PaaS** (Platform as a Service) — the provider manages the OS/runtime; you just deploy your app code (balanced). **SaaS** (Software as a Service) — a ready-to-use application; you just use it (least effort) — e.g., Gmail. Pizza analogy: IaaS = you cook using their kitchen; PaaS = they cook, you add toppings; SaaS = pizza delivered.

## Public vs Private vs Hybrid Cloud
| Public | Private | Hybrid |
|--------|---------|--------|
| Shared (AWS) | Dedicated (on-prem) | Mix |
| Cost-effective | More control/security | Flexible |

**Explanation:** **Public cloud** — shared infrastructure rented from providers like AWS/GCP; cost-effective and scalable, no hardware to own. **Private cloud** — infrastructure dedicated to one organization (on-premises or hosted); more control and security, often for compliance/sensitive data. **Hybrid** — a mix, keeping sensitive workloads private while using public cloud for scale/bursting. Choose based on cost, control, and regulatory needs.

## Horizontal Pod Autoscaler vs Vertical Pod Autoscaler
| HPA | VPA |
|-----|-----|
| More pods | Bigger pods (CPU/mem) |
| Scale out | Scale up |

**Explanation:** Two Kubernetes autoscalers. **HPA (Horizontal Pod Autoscaler)** adds MORE pod replicas when load rises (scale out) — the common choice for handling more traffic. **VPA (Vertical Pod Autoscaler)** gives existing pods MORE CPU/memory (scale up) — useful when a pod needs more resources rather than more copies. HPA = more workers; VPA = stronger workers.

---

# Testing Differences

## Unit vs Integration vs E2E Test
| Unit | Integration | E2E |
|------|-------------|-----|
| Single class | Multiple components | Whole system |
| Mocks | Real dependencies | Real environment |
| Fast, many | Medium | Slow, few |

**Explanation:** Three levels of testing. **Unit tests** check a single class in isolation, mocking its dependencies — fast, and you write many. **Integration tests** check that multiple components work together (e.g., service + real database) — medium speed. **E2E (End-to-End) tests** exercise the whole system as a user would (UI → backend → DB) — slow and brittle, so you write few. The testing pyramid: many unit, some integration, few E2E.

## @Mock vs @MockBean vs @Spy
| @Mock | @MockBean | @Spy |
|-------|-----------|------|
| Mockito unit | Spring context bean | Partial mock |
| Fake object | Replaces bean | Real unless stubbed |

**Explanation:** **@Mock** (Mockito) creates a plain fake object for a unit test. **@MockBean** (Spring) replaces a real bean in the Spring application context with a mock — used in Spring integration tests. **@Spy** creates a PARTIAL mock — it wraps a real object and runs its real methods UNLESS you specifically stub one. Use @Mock for pure unit tests, @MockBean when the Spring context is loaded, @Spy when you want mostly-real behavior with a few overrides.

## Mockito vs PowerMock
| Mockito | PowerMock |
|---------|-----------|
| Standard mocking | Static/private/final/constructor |
| Modern (3.4+ can mock static) | Legacy need |

**Explanation:** **Mockito** is the standard mocking framework for most needs — mocking interfaces and regular classes. **PowerMock** extends Mockito to mock the hard cases: static methods, private methods, final classes, and constructors. Historically you needed PowerMock for those, but modern Mockito (3.4+) can now mock static methods itself, reducing the need for PowerMock. Mention this evolution in interviews — it shows you're current.

## Stub vs Mock
| Stub | Mock |
|------|------|
| Returns canned data | Verifies interactions |
| State verification | Behavior verification |

**Explanation:** Both are test doubles, but with different intent. A **stub** just returns predefined (canned) data so the test can run — you check the RESULT (state). A **mock** additionally VERIFIES that certain methods were called with certain arguments — you check the BEHAVIOR/interaction. Stub = "give me fake data to proceed"; mock = "confirm my code called this dependency correctly".

## TDD vs BDD
| TDD | BDD |
|-----|-----|
| Test-Driven | Behavior-Driven |
| Developer tests | Given-When-Then, business language |
| JUnit | Cucumber |

**Explanation:** **TDD (Test-Driven Development)** — write a failing test first, then code to pass it, then refactor (Red-Green-Refactor); focused on developer-level correctness. **BDD (Behavior-Driven Development)** — describe behavior in plain business language (Given-When-Then scenarios) that non-developers can read, using tools like Cucumber; focused on shared understanding of requirements. TDD tests code units; BDD tests behavior in business terms.
