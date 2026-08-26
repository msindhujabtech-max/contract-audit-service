# Deployment Guide — AI Contract Analyzer + Audit Service

This guide lets anyone deploy the complete project on Google Cloud in under 15 minutes.
No Java, no IDE, no manual GCP Console clicking — just Terraform + Docker.

---

## What Gets Deployed

```
┌─────────────────────────── GCP VM (contract-analyzer-vm) ───────────────────────────┐
│                                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐  ┌──────────────────┐  │
│  │   Frontend   │  │   Backend    │  │  Audit Service     │  │     Ollama       │  │
│  │  React UI    │  │  Spring Boot │  │  Spring Boot       │  │  LLM + Embed     │  │
│  │  Port: 3000  │  │  Port: 8000  │  │  Port: 8082        │  │  Port: 11434     │  │
│  └──────────────┘  └──────────────┘  └────────────────────┘  └──────────────────┘  │
│                                                                                      │
│  ┌──────────────┐  ┌──────────────┐                                                 │
│  │  PostgreSQL  │  │    Redis     │          All on: contract-network               │
│  │  + pgvector  │  │    Cache     │                                                 │
│  │  Port: 5432  │  │  Port: 6379  │                                                 │
│  └──────────────┘  └──────────────┘                                                 │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Prerequisites (One-Time Setup)

Your laptop/PC needs only 3 things installed:

| Tool | What it does | Install link |
|------|-------------|--------------|
| **Google Cloud SDK** (`gcloud`) | Authenticates with GCP | https://cloud.google.com/sdk/docs/install |
| **Terraform** | Creates cloud infrastructure from code | https://developer.hashicorp.com/terraform/install |
| **Git** | Clones the project repos | https://git-scm.com/downloads |

### GCP Account Requirements
- A Google Cloud account with **billing enabled**
- A GCP project (create one at https://console.cloud.google.com)
- Compute Engine API enabled (Terraform will prompt you if it's not)

---

## Deployment Steps

### Step 1: Authenticate with Google Cloud

Open your terminal and run:

```bash
gcloud auth login
gcloud auth application-default login
gcloud config set project YOUR-GCP-PROJECT-ID
```

Replace `YOUR-GCP-PROJECT-ID` with your actual project ID (e.g., `my-project-12345`).

---

### Step 2: Clone the audit service repo (contains Terraform files)

```bash
git clone https://github.com/msindhujabtech-max/contract-audit-service.git
cd contract-audit-service/terraform
```

---

### Step 3: Configure your project ID

```bash
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` and replace `YOUR-GCP-PROJECT-ID-HERE` with your actual GCP project ID:

```hcl
project_id = "my-actual-project-id"
```

---

### Step 4: Deploy everything with Terraform

```bash
terraform init
terraform plan
terraform apply
```

- `terraform init` — Downloads the Google Cloud provider plugin
- `terraform plan` — Shows you what will be created (review it)
- `terraform apply` — Type `yes` to confirm and create everything

**Wait ~10-15 minutes.** Terraform creates the VM, then the startup script installs Docker, clones both repos, and starts all containers.

---

### Step 5: Get your public IP

After `terraform apply` completes, it prints:

```
vm_public_ip    = "XX.XX.XX.XX"
frontend_url    = "http://XX.XX.XX.XX:3000"
backend_url     = "http://XX.XX.XX.XX:8000"
audit_service_url = "http://XX.XX.XX.XX:8082"
ssh_command     = "gcloud compute ssh contract-analyzer-vm --zone=us-central1-a"
```

---

### Step 6: Wait for services to be ready

The VM startup script takes a few minutes to download Docker images and build containers. Check progress by SSH-ing in:

```bash
gcloud compute ssh contract-analyzer-vm --zone=us-central1-a
```

Once inside the VM:

```bash
# Check if containers are running
docker ps

# Watch the startup script progress (if still running)
sudo journalctl -u google-startup-scripts -f
```

You should see **6 containers** running:
- `contract-frontend`
- `contract-backend`
- `contract-audit-service`
- `contract-db`
- `contract-redis`
- `contract-ollama`

---

### Step 7: Test in browser

Open `http://YOUR-IP:3000` in your browser. Upload a PDF and ask questions!

---

## Manual Build & Deployment (Without Terraform)

If you already have a GCP VM and just want to deploy the code:

### SSH into your VM

```bash
gcloud compute ssh contract-analyzer-vm --zone=us-central1-a
```

### Deploy audit service (port 8082)

```bash
cd ~/contract-audit-service
git pull origin main
docker compose down
docker compose up -d --build
```

### Deploy analyser service (port 8000 + 3000)

```bash
cd ~/contract-analyser-spring-ai
git pull origin main
docker compose down
docker compose up -d --build
```

### Verify

```bash
docker ps
```

All 6 containers should be running.

---

## Updating After Code Changes

After pushing new code to GitHub, redeploy on the VM:

```bash
gcloud compute ssh contract-analyzer-vm --zone=us-central1-a
```

```bash
# Rebuild audit service
cd ~/contract-audit-service && git pull origin main && docker compose down && docker compose up -d --build

# Rebuild analyser
cd ~/contract-analyser-spring-ai && git pull origin main && docker compose down && docker compose up -d --build
```

---

## Tearing Down (Cleanup)

To destroy all GCP resources and stop billing:

```bash
cd contract-audit-service/terraform
terraform destroy
```

Type `yes` to confirm. This deletes the VM, IP, and firewall rules. Your code on GitHub is untouched.

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `terraform apply` fails with "API not enabled" | Run: `gcloud services enable compute.googleapis.com` |
| Containers not running after 15 min | SSH in, run: `sudo journalctl -u google-startup-scripts` to see errors |
| Port not accessible from browser | Check firewall: `gcloud compute firewall-rules list` |
| `docker compose` not found on VM | VM startup script may still be running — wait a few minutes |
| Ollama models not loaded | SSH in, run: `docker logs contract-ollama` — first pull takes ~5 min |
| Frontend shows but upload fails | Backend may still be starting — check: `docker logs contract-backend --tail 20` |

---

## Project Repositories

| Repo | Purpose |
|------|---------|
| https://github.com/msindhujabtech-max/contract-analyser-spring-ai | Main app: Frontend + Backend + AI/RAG |
| https://github.com/msindhujabtech-max/contract-audit-service | Audit microservice + Terraform configs |

---

## Architecture & Ports

| Service | Internal URL (Docker) | External URL |
|---------|----------------------|--------------|
| Frontend | http://contract-frontend:3000 | http://YOUR-IP:3000 |
| Backend | http://contract-backend:8080 | http://YOUR-IP:8000 |
| Audit Service | http://contract-audit-service:8082 | http://YOUR-IP:8082 |
| PostgreSQL | postgresql://db:5432 | Not exposed externally |
| Redis | redis://redis:6379 | Not exposed externally |
| Ollama | http://ollama:11434 | Not exposed externally |

---

## Cost Estimate

Running on `e2-standard-4` in `us-central1`:
- **~$0.13/hour** (~$97/month if running 24/7)
- Remember to `terraform destroy` when not using it to stop charges!
