# Differences (X vs Y) — Databases, DevOps, Cloud

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

## Oracle vs MySQL
| Oracle | MySQL |
|--------|-------|
| Enterprise, paid | Open-source (Oracle-owned) |
| PL/SQL | Stored procs (less rich) |
| Sequences | AUTO_INCREMENT |
| ROWNUM / FETCH FIRST | LIMIT |
| More features/scale | Lightweight, web apps |

## DELETE vs TRUNCATE vs DROP
| DELETE | TRUNCATE | DROP |
|--------|----------|------|
| Rows (WHERE) | All rows | Entire table |
| DML | DDL | DDL |
| Rollback-able | Usually not | No |
| Slow, logged | Fast | Removes structure |
| Triggers fire | No triggers | N/A |

## WHERE vs HAVING
| WHERE | HAVING |
|-------|--------|
| Filters rows | Filters groups |
| Before GROUP BY | After GROUP BY |
| No aggregates | Aggregates allowed |

## INNER JOIN vs OUTER JOIN
| INNER | OUTER (LEFT/RIGHT/FULL) |
|-------|-------------------------|
| Only matching rows | Includes non-matching (nulls) |
| Intersection | Union-ish |

## Primary Key vs Unique Key
| Primary Key | Unique Key |
|-------------|------------|
| One per table | Many allowed |
| Not null | One null allowed (most DBs) |
| Clustered index (usually) | Non-clustered |

## Primary Key vs Foreign Key
| Primary Key | Foreign Key |
|-------------|-------------|
| Identifies row uniquely | References another table's PK |
| One per table | Many allowed |
| Not null | Can be null |

## Clustered vs Non-clustered Index
| Clustered | Non-clustered |
|-----------|---------------|
| Physical row order | Separate structure + pointers |
| One per table | Many per table |
| Faster range queries | Extra lookup |

## char vs varchar
| char | varchar |
|------|---------|
| Fixed length | Variable length |
| Pads with spaces | Uses actual length |
| Faster fixed data | Space-efficient |

## UNION vs UNION ALL
| UNION | UNION ALL |
|-------|-----------|
| Removes duplicates | Keeps duplicates |
| Slower (sort) | Faster |

## Stored Procedure vs Function
| Stored Procedure | Function |
|------------------|----------|
| May return 0/many values | Must return one value |
| Can modify data | Usually read-only |
| Called with CALL/EXEC | Used in SQL expressions |

## OLTP vs OLAP
| OLTP | OLAP |
|------|------|
| Transactional | Analytical |
| Many small writes | Complex reads |
| Normalized | Denormalized (star schema) |
| E-commerce orders | Reporting/BI |

## Liquibase vs Flyway
| Liquibase | Flyway |
|-----------|--------|
| XML/YAML/JSON/SQL | SQL-first |
| Rollback support | Limited rollback |
| DB-agnostic abstraction | Simpler |

## Optimistic vs Pessimistic Locking
| Optimistic | Pessimistic |
|------------|-------------|
| Version check on update | Lock row upfront |
| No locks, retry on conflict | Blocks others |
| High concurrency, rare conflicts | Frequent conflicts |

---

# DevOps Differences

## Container vs Virtual Machine
| Container | VM |
|-----------|-----|
| Shares host kernel | Full guest OS |
| MBs, seconds to start | GBs, minutes |
| Less isolation | Full isolation |
| Docker | VMware, VirtualBox |

## Docker Image vs Container
| Image | Container |
|-------|-----------|
| Read-only template | Running instance |
| Blueprint | Live process |
| Stored in registry | Runtime |

## Docker Compose vs Kubernetes
| Docker Compose | Kubernetes |
|----------------|------------|
| Single host | Multi-host cluster |
| Dev/simple | Production/scale |
| No auto-healing | Self-healing |
| No auto-scaling | HPA auto-scaling |
| Manual updates | Rolling updates |

## Kubernetes Pod vs Deployment vs Service
| Pod | Deployment | Service |
|-----|------------|---------|
| Smallest unit (containers) | Manages pod replicas | Network endpoint + LB |
| Ephemeral | Rolling updates, scaling | Stable DNS |

## Liveness vs Readiness Probe
| Liveness | Readiness |
|----------|-----------|
| Is app alive? | Ready for traffic? |
| Restarts pod if fails | Removes from LB if fails |

## ConfigMap vs Secret
| ConfigMap | Secret |
|-----------|--------|
| Non-sensitive config | Sensitive data |
| Plain text | Base64/encrypted |

## StatefulSet vs Deployment
| StatefulSet | Deployment |
|-------------|------------|
| Stable identity/storage | Interchangeable pods |
| Databases | Stateless apps |
| Ordered scaling | Any order |

## Maven vs Gradle
| Maven | Gradle |
|-------|--------|
| XML (pom.xml) | Groovy/Kotlin DSL |
| Convention | Flexible |
| Slower | Faster (incremental) |

## Maven compile vs provided vs runtime vs test scope
| Scope | Available at |
|-------|--------------|
| compile | Everywhere (default) |
| provided | Compile, not runtime (servlet API) |
| runtime | Runtime only (JDBC driver) |
| test | Testing only (JUnit) |

## CI vs CD
| CI | CD |
|----|-----|
| Continuous Integration | Continuous Delivery/Deployment |
| Auto build + test on merge | Auto release to environments |

## Blue-Green vs Canary Deployment
| Blue-Green | Canary |
|------------|--------|
| Two full environments, switch | Gradual % rollout |
| Instant switch/rollback | Progressive |
| Double resources | Less risk |

## Jenkins vs GitHub Actions
| Jenkins | GitHub Actions |
|---------|----------------|
| Self-hosted server | Cloud-native (GitHub) |
| Plugins | YAML workflows |
| Flexible | Tight GitHub integration |

## Kafka Delivery: At-most vs At-least vs Exactly-once
| At-most-once | At-least-once | Exactly-once |
|--------------|---------------|--------------|
| May lose | No loss, may duplicate | No loss, no duplicate |
| Fire-forget | Common default | Needs idempotency |

## Terraform vs Ansible
| Terraform | Ansible |
|-----------|---------|
| Provisioning (infra) | Configuration mgmt |
| Declarative | Procedural |
| State-based | Agentless |
| Create VMs/networks | Configure servers |

## Terraform vs CloudFormation
| Terraform | CloudFormation |
|-----------|----------------|
| Multi-cloud | AWS only |
| HCL | JSON/YAML |
| Third-party | AWS native |

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

## IaaS vs PaaS vs SaaS
| IaaS | PaaS | SaaS |
|------|------|------|
| Infrastructure (VMs) | Platform (runtime) | Software (app) |
| EC2, Compute Engine | App Engine, Heroku | Gmail, Salesforce |
| You manage OS+app | You manage app | Use only |
| Most control | Balanced | Least effort |

## Public vs Private vs Hybrid Cloud
| Public | Private | Hybrid |
|--------|---------|--------|
| Shared (AWS) | Dedicated (on-prem) | Mix |
| Cost-effective | More control/security | Flexible |

## Horizontal Pod Autoscaler vs Vertical Pod Autoscaler
| HPA | VPA |
|-----|-----|
| More pods | Bigger pods (CPU/mem) |
| Scale out | Scale up |

---

# Testing Differences

## Unit vs Integration vs E2E Test
| Unit | Integration | E2E |
|------|-------------|-----|
| Single class | Multiple components | Whole system |
| Mocks | Real dependencies | Real environment |
| Fast, many | Medium | Slow, few |

## @Mock vs @MockBean vs @Spy
| @Mock | @MockBean | @Spy |
|-------|-----------|------|
| Mockito unit | Spring context bean | Partial mock |
| Fake object | Replaces bean | Real unless stubbed |

## Mockito vs PowerMock
| Mockito | PowerMock |
|---------|-----------|
| Standard mocking | Static/private/final/constructor |
| Modern (3.4+ can mock static) | Legacy need |

## Stub vs Mock
| Stub | Mock |
|------|------|
| Returns canned data | Verifies interactions |
| State verification | Behavior verification |

## TDD vs BDD
| TDD | BDD |
|-----|-----|
| Test-Driven | Behavior-Driven |
| Developer tests | Given-When-Then, business language |
| JUnit | Cucumber |
