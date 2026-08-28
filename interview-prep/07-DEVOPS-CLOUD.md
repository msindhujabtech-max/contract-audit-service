# DevOps, Cloud & Middleware — Interview Preparation

## Docker

### What is Docker?
A containerization platform. Packages an app with all its dependencies into a portable "container" that runs identically anywhere.

**Container vs VM:**
| Container | VM |
|-----------|-----|
| Shares host OS kernel | Full guest OS |
| Lightweight (MBs) | Heavy (GBs) |
| Starts in seconds | Starts in minutes |
| Less isolation | Full isolation |

### Key concepts
- **Image** — read-only template (blueprint)
- **Container** — running instance of an image
- **Dockerfile** — instructions to build an image
- **Registry** — stores images (Docker Hub, ECR)
- **Volume** — persistent storage outside the container

### Dockerfile (multi-stage, you used this)
```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run (smaller final image)
FROM eclipse-temurin:21-jre
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```
> **Why multi-stage?** Build stage has Maven+JDK (~800MB). Final stage has only JRE (~200MB). Smaller, more secure images.

### Common Docker commands
```bash
docker build -t myapp .
docker run -d -p 8080:8080 myapp
docker ps                    # running containers
docker logs <container>
docker exec -it <container> bash
docker-compose up -d --build
docker-compose down
```

### Docker Compose
Defines multi-container apps in one YAML file (what you used for your whole stack — db, redis, kafka, backend, etc.)

---

## Kubernetes (K8s)

### What is Kubernetes?
Container orchestration — automates deployment, scaling, and management of containers across a cluster of machines.

**Docker Compose vs Kubernetes:**
- Compose: single machine, dev/simple
- K8s: multi-machine cluster, production, auto-scaling, self-healing

### Core objects

| Object | Purpose |
|--------|---------|
| **Pod** | Smallest unit — one or more containers |
| **Deployment** | Manages pod replicas, rolling updates |
| **Service** | Stable network endpoint + load balancing for pods |
| **ConfigMap** | Non-secret configuration |
| **Secret** | Sensitive data (passwords, keys) |
| **Ingress** | External HTTP routing into the cluster |
| **Namespace** | Logical isolation of resources |
| **StatefulSet** | For stateful apps (databases) |
| **HPA** | Horizontal Pod Autoscaler — scale by CPU/memory |

### Deployment example
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
spec:
  replicas: 3               # 3 pods
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
        - name: backend
          image: backend:latest
          ports:
            - containerPort: 8080
          livenessProbe:      # restarts if unhealthy
            httpGet:
              path: /actuator/health
              port: 8080
          readinessProbe:     # removes from load balancer if not ready
            httpGet:
              path: /actuator/health
              port: 8080
```

### Self-healing & scaling
- **Self-healing:** If a pod crashes, K8s restarts it automatically
- **Rolling updates:** Deploy new version with zero downtime
- **HPA:** Automatically adds pods when CPU exceeds threshold

### Liveness vs Readiness probes
- **Liveness** — is the app alive? If not, restart it
- **Readiness** — is the app ready for traffic? If not, remove from load balancer

---

## Apache Kafka

### What is Kafka?
A distributed event-streaming platform / message broker. Producers publish to topics; consumers subscribe.

### Core concepts
| Term | Meaning |
|------|---------|
| **Topic** | Named channel for messages |
| **Partition** | Topic split for parallelism |
| **Producer** | Publishes messages |
| **Consumer** | Reads messages |
| **Consumer Group** | Consumers sharing work (each message → one consumer in group) |
| **Offset** | Position of a message in a partition |
| **Broker** | Kafka server |
| **Zookeeper** | Coordinates brokers (being replaced by KRaft) |

### Why Kafka over traditional queues (RabbitMQ)?
- Messages persist (can replay)
- High throughput (millions/sec)
- Horizontal scaling via partitions
- Multiple consumer groups read the same topic independently

### Producer/Consumer (you implemented this)
```java
// Producer
kafkaTemplate.send("audit-topic", key, message);

// Consumer
@KafkaListener(topics = "audit-topic", groupId = "audit-group")
public void consume(String message) { process(message); }
```

### Delivery guarantees
- **At-most-once** — may lose messages, no duplicates
- **At-least-once** — no loss, possible duplicates (most common)
- **Exactly-once** — no loss, no duplicates (harder, needs idempotency)

---

## CI/CD & Jenkins

### CI/CD explained
- **CI (Continuous Integration)** — merge code frequently, auto-build and test
- **CD (Continuous Delivery/Deployment)** — auto-release to staging/production

### Jenkins Pipeline
```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps { sh 'mvn clean package' }
        }
        stage('Test') {
            steps { sh 'mvn test' }
        }
        stage('Docker Build') {
            steps { sh 'docker build -t myapp .' }
        }
        stage('Deploy') {
            steps { sh 'kubectl apply -f k8s/' }
        }
    }
}
```

---

## Maven vs Gradle (both on your resume)

| | Maven | Gradle |
|--|-------|--------|
| Config | XML (pom.xml) | Groovy/Kotlin DSL |
| Speed | Slower | Faster (incremental, caching) |
| Flexibility | Convention-based | Highly customizable |

### Maven lifecycle phases
```
validate → compile → test → package → verify → install → deploy
```
```bash
mvn clean install        # clean, build, run tests, install to local repo
mvn package -DskipTests  # build jar without tests
```

### Maven scopes
- `compile` (default) — available everywhere
- `provided` — provided by runtime (e.g., servlet API)
- `runtime` — needed at runtime, not compile (e.g., JDBC driver)
- `test` — only for testing (JUnit)

---

## Cloud Platforms

### General cloud concepts (apply to AWS/Azure/GCP)
| Concept | AWS | Azure | GCP |
|---------|-----|-------|-----|
| Compute (VM) | EC2 | Virtual Machines | Compute Engine |
| Containers | ECS/EKS | AKS | GKE |
| Serverless | Lambda | Functions | Cloud Functions |
| Object Storage | S3 | Blob Storage | Cloud Storage |
| Managed DB | RDS | SQL Database | Cloud SQL |
| Secrets | Secrets Manager | Key Vault | Secret Manager |

### GCP (you deployed here)
- **Compute Engine** — VMs (your `contract-analyzer-vm`)
- **Secret Manager** — store credentials (your resume mentions Key Vault/Secret Manager)
- **Firewall rules** — control traffic
- **GKE** — managed Kubernetes

---

## Terraform (Infrastructure as Code)

### What is Terraform?
Define cloud infrastructure as code. Run `terraform apply` to create it reproducibly.

### Core concepts
- **Provider** — cloud plugin (google, aws, azure)
- **Resource** — infrastructure piece (VM, firewall)
- **Variable** — configurable input
- **Output** — values shown after apply
- **State** — tracks created resources (`terraform.tfstate`)

### Workflow
```bash
terraform init      # download providers
terraform plan      # preview changes
terraform apply     # create/update
terraform destroy   # tear down
```

### Example (you built this)
```hcl
resource "google_compute_instance" "vm" {
  name         = "contract-analyzer-vm"
  machine_type = "e2-standard-4"
  boot_disk {
    initialize_params { image = "debian-cloud/debian-12" }
  }
}
```

**Terraform vs Ansible:** Terraform provisions infrastructure (declarative). Ansible configures servers (procedural). Often used together.

---

## Middleware: JBoss & WebLogic (your resume)

Application servers that host Java EE apps.
- **JBoss (WildFly)** — open-source Java EE app server
- **WebLogic** — Oracle's enterprise app server (used in your ATG projects)

They provide: servlet container, EJB, JMS, connection pooling, clustering, transaction management.

> Note: Spring Boot uses **embedded** servers (Tomcat/Netty), removing the need for external app servers — a key modernization talking point.

---

## Monitoring: Grafana, SonarQube, Zipkin (your resume)

- **Grafana** — visualization dashboards (metrics from Prometheus)
- **SonarQube** — code quality & security scanning (bugs, code smells, coverage, vulnerabilities)
- **Zipkin** — distributed tracing visualization

---

## Common DevOps Interview Questions

**Q: What happens when you run `docker run`?**
> Docker pulls the image (if not local), creates a container layer, allocates a filesystem, sets up networking, and runs the entrypoint command.

**Q: How does Kubernetes achieve high availability?**
> Multiple replicas across nodes, self-healing (restarts failed pods), rolling updates (zero downtime), and load balancing via Services.

**Q: Blue-Green vs Canary deployment?**
> Blue-Green: two identical environments, switch traffic all at once. Canary: gradually route a small % of traffic to the new version, increase if healthy.

**Q: How do you handle secrets in containers?**
> Never hardcode. Use environment variables from K8s Secrets, or a vault (HashiCorp Vault, Azure Key Vault, GCP Secret Manager). Inject at runtime.

**Q: What is idempotency in Terraform?**
> Running `apply` multiple times produces the same result. Terraform compares desired state (code) with actual state and only changes the difference.
