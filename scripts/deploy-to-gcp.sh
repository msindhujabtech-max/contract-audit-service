#!/bin/bash
# ============================================================
# Deploy contract-audit-service to GCP VM
# VM: contract-analyzer-vm | Zone: us-central1-a
# ============================================================

set -e

echo "=========================================="
echo "  Deploying contract-audit-service to GCP"
echo "=========================================="

# --- Step 1: Open firewall for port 8082 (run this from your local machine) ---
echo ""
echo "[Step 1] Creating firewall rule for port 8082..."
gcloud compute firewall-rules create allow-audit-service \
  --allow tcp:8082 \
  --direction=INGRESS \
  --target-tags=http-server \
  --source-ranges=0.0.0.0/0 \
  --description="Allow traffic to contract-audit-service on port 8082" \
  --project=$(gcloud config get-value project) 2>/dev/null || echo "Firewall rule may already exist, skipping..."

# --- Step 2: SSH into VM and deploy ---
echo ""
echo "[Step 2] Connecting to contract-analyzer-vm and deploying..."

gcloud compute ssh contract-analyzer-vm \
  --zone=us-central1-a \
  --command='
    set -e
    echo ""
    echo ">>> Connected to VM. Starting deployment..."

    # Create shared Docker network if it does not exist
    echo "[2.1] Ensuring contract-network exists..."
    docker network create contract-network 2>/dev/null || echo "Network already exists."

    # Clone or pull the audit service repo
    echo "[2.2] Fetching contract-audit-service..."
    if [ -d "$HOME/contract-audit-service" ]; then
      cd $HOME/contract-audit-service
      git pull origin main
    else
      cd $HOME
      git clone https://github.com/msindhujabtech-max/contract-audit-service.git
      cd $HOME/contract-audit-service
    fi

    # Build and run with Docker Compose
    echo "[2.3] Building and starting container..."
    docker-compose down 2>/dev/null || true
    docker-compose up -d --build

    # Wait for container to be healthy
    echo "[2.4] Waiting for service to start..."
    sleep 10

    # Verify the service is running
    echo "[2.5] Verifying deployment..."
    curl -s -X POST http://localhost:8082/api/audit/log \
      -H "Content-Type: application/json" \
      -d "{\"contractName\":\"deploy-test\",\"status\":\"DEPLOYED\",\"wordCount\":42}" && echo ""

    echo ""
    echo ">>> contract-audit-service is running!"

    # --- Step 3: Reconnect contract-analyser to the same network ---
    echo ""
    echo "[3.1] Updating contract-analyser-spring-ai to use shared network..."
    if [ -d "$HOME/contract-analyser-spring-ai" ]; then
      cd $HOME/contract-analyser-spring-ai
      git pull origin main
      docker-compose down
      docker-compose up -d --build
      echo "[3.2] contract-analyser restarted on shared network."
    else
      echo "[3.2] WARN: contract-analyser-spring-ai not found at ~/contract-analyser-spring-ai"
      echo "       You may need to redeploy it manually after merging the integration branch."
    fi

    echo ""
    echo "=========================================="
    echo "  DEPLOYMENT COMPLETE"
    echo "=========================================="
    echo ""
    echo "  Audit Service:    http://34.70.230.73:8082/api/audit/log"
    echo "  Analyser Service: http://34.70.230.73:8000"
    echo ""
    echo "  Internal (Docker): http://contract-audit-service:8082"
    echo "=========================================="
  '
