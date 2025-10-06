#!/bin/bash

# Deployment script for Docker Swarm service
# Usage: ./deploy.sh <image_name> <service_name>

set -e  # Exit on any error
set -a  # Export all variables

# Check if required arguments are provided
if [ $# -ne 2 ]; then
    echo "Usage: $0 <image_name> <service_name>"
    echo "Example: $0 myapp:latest my-service"
    exit 1
fi

IMAGE_NAME="$1"
SERVICE_NAME="$2"

echo "Starting deployment..."
echo "Image: $IMAGE_NAME"
echo "Service: $SERVICE_NAME"

cd /home/administrator/starline

echo "Run compose"
pwd
docker-compose --env-file .env.local up -d "$SERVICE_NAME"