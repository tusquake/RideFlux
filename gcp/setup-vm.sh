#!/bin/bash

# ==========================================
# 🚀 GCP VM Setup Script for Ride Sharing App
# ==========================================

USER_EMAIL=$(gcloud config get-value account)
echo "✅ Using GCP Account: $USER_EMAIL"

PROJECT_ID=$(gcloud config get-value project)
echo "✅ Using Project ID: $PROJECT_ID"

# 1. Create the VM
echo "--- Creating VM 'ridesharing-vm' ---"
gcloud compute instances create ridesharing-vm \
  --project=$PROJECT_ID \
  --zone=asia-south1-a \
  --machine-type=e2-medium \
  --image-family=ubuntu-2204-lts \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=30GB \
  --tags=ridesharing-server,http-server,https-server \
  --scopes=https://www.googleapis.com/auth/cloud-platform

# 2. Configure Firewall Rules
echo "--- configuring Firewall Rules ---"

# Allow API Gateway (8080)
gcloud compute firewall-rules create allow-gateway \
  --allow=tcp:8080 \
  --target-tags=ridesharing-server \
  --description="Allow API Gateway access"

# Allow RabbitMQ Dashboard (15672) & Eureka (8761) - Optional, for debugging
gcloud compute firewall-rules create allow-dashboards \
  --allow=tcp:8761,tcp:15672 \
  --target-tags=ridesharing-server \
  --description="Allow Eureka and RabbitMQ Dashboards"

# 3. Get VM IP
VM_IP=$(gcloud compute instances describe ridesharing-vm --zone=asia-south1-a --format='get(networkInterfaces[0].accessConfigs[0].natIP)')

echo ""
echo "🎉 VM Created Successfully!"
echo "➡️  VM IP Address: $VM_IP"
echo ""
echo "⚠️  NEXT STEPS:"
echo "1. Go to GitHub Repo -> Settings -> Secrets -> Actions"
echo "2. Add these secrets:"
echo "   - GCP_VM_IP: $VM_IP"
echo "   - GCP_VM_USER: $(whoami)  (or your preferred SSH user)"
echo "   - GCP_VM_SSH_KEY: (Paste your private SSH key content)"
echo ""
