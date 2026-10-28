#!/bin/bash
set -e

echo "🚀 Pulling latest code from GitHub..."
git pull origin main

echo "🔨 Rebuilding and restarting Docker containers..."
docker compose up -d --build

echo "🧹 Cleaning up unused Docker images..."
docker image prune -f

echo "✅ Banking System Backend Deployment Finished Successfully!"
