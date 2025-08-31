#!/bin/bash

# Quick deployment verification script
# Run this after installing Docker Desktop

echo "🔍 Checking deployment readiness..."

# Check Docker
if command -v docker &> /dev/null; then
    echo "✅ Docker is installed"
    if docker info &> /dev/null; then
        echo "✅ Docker daemon is running"
    else
        echo "❌ Docker daemon is not running. Please start Docker Desktop."
        exit 1
    fi
else
    echo "❌ Docker is not installed. Please install Docker Desktop first."
    exit 1
fi

# Check Google Cloud CLI
if command -v gcloud &> /dev/null; then
    echo "✅ Google Cloud CLI is installed"

    # Check if logged in
    if gcloud auth list --filter=status:ACTIVE --format="value(account)" | grep -q "@"; then
        echo "✅ Logged into Google Cloud"

        # Check project
        PROJECT=$(gcloud config get-value project)
        if [ -n "$PROJECT" ]; then
            echo "✅ Project set: $PROJECT"
        else
            echo "❌ No project set"
            exit 1
        fi
    else
        echo "❌ Not logged into Google Cloud"
        exit 1
    fi
else
    echo "❌ Google Cloud CLI is not installed"
    exit 1
fi

# Check PostgreSQL instance
echo "🔍 Checking PostgreSQL database status..."
gcloud sql instances describe endora-postgres --format="value(state)" 2>/dev/null | grep -q "RUNNABLE" && echo "✅ PostgreSQL database is ready" || echo "⏳ PostgreSQL database is still being created..."

echo ""
echo "🚀 Ready to deploy! Run ./deploy.sh to start the deployment process."
