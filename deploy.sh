#!/bin/bash

# Google Cloud Deployment Script for Spring Boot API Engine Backend
# This script automates the deployment process to Google Cloud Platform

set -e  # Exit on any error

echo "🚀 Starting deployment to Google Cloud Platform..."

# Configuration variables
PROJECT_ID="api-engine-backend-20250829"
REGION="us-central1"
SERVICE_NAME="api-engine-backend"
IMAGE_NAME="gcr.io/${PROJECT_ID}/${SERVICE_NAME}"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if required tools are installed
check_dependencies() {
    print_status "Checking dependencies..."

    if ! command -v gcloud &> /dev/null; then
        print_error "Google Cloud CLI is not installed. Please install it first:"
        echo "https://cloud.google.com/sdk/docs/install"
        exit 1
    fi

    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker Desktop first:"
        echo "https://www.docker.com/products/docker-desktop"
        exit 1
    fi

    print_success "All dependencies are installed"
}

# Get project ID from user
get_project_id() {
    if [ -z "$PROJECT_ID" ]; then
        echo ""
        print_status "Please enter your Google Cloud Project ID:"
        read -r PROJECT_ID

        if [ -z "$PROJECT_ID" ]; then
            print_error "Project ID cannot be empty"
            exit 1
        fi
    fi

    # Update the image name with the actual project ID
    IMAGE_NAME="gcr.io/${PROJECT_ID}/${SERVICE_NAME}"
    print_success "Using project: $PROJECT_ID"
}

# Setup Google Cloud
setup_gcloud() {
    print_status "Setting up Google Cloud..."

    # Set the project
    gcloud config set project "$PROJECT_ID"

    # Enable required APIs
    print_status "Enabling required Google Cloud APIs..."
    gcloud services enable cloudbuild.googleapis.com
    gcloud services enable run.googleapis.com
    gcloud services enable sqladmin.googleapis.com
    gcloud services enable containerregistry.googleapis.com

    # Configure Docker to use gcloud as a credential helper
    gcloud auth configure-docker

    print_success "Google Cloud setup complete"
}

# Build and push Docker image
build_and_push() {
    print_status "Building Docker image for linux/amd64 platform..."

    # First, let's test the build locally
    print_status "Testing local build first..."
    ./gradlew clean build --no-daemon

    if [ $? -ne 0 ]; then
        print_error "Local Gradle build failed. Please fix the build issues first."
        exit 1
    fi

    print_success "Local build successful"

    # Build the Docker image with explicit platform specification for Cloud Run
    docker buildx build --platform linux/amd64 -t "$IMAGE_NAME" . --push

    print_success "Docker image built and pushed successfully for linux/amd64"
}

# Deploy to Cloud Run
deploy_to_cloud_run() {
    print_status "Deploying to Cloud Run..."

    gcloud run deploy "$SERVICE_NAME" \
        --image "$IMAGE_NAME" \
        --platform managed \
        --region "$REGION" \
        --allow-unauthenticated \
        --set-env-vars "SPRING_PROFILES_ACTIVE=prod" \
        --set-env-vars "DATABASE_URL=jdbc:postgresql://google/endora?cloudSqlInstance=api-engine-backend-20250829:us-central1:endora-postgres-db&socketFactory=com.google.cloud.sql.postgres.SocketFactory&useSSL=false" \
        --set-env-vars "DB_USERNAME=postgres" \
        --set-env-vars "DB_PASSWORD=EndoraSecure2024!" \
        --set-env-vars "SPRING_DATA_MONGODB_URI=mongodb+srv://endora_user:EndoraSecure2024!@endora-cluster.p5pmd6m.mongodb.net/endora?retryWrites=true&w=majority" \
        --set-env-vars "EMAIL_USERNAME=endora.istad@gmail.com" \
        --set-env-vars "EMAIL_PASSWORD=utnu dtwk xyre ebkh" \
        --memory 1Gi \
        --cpu 1 \
        --max-instances 10 \
        --port 8080

    print_success "Deployment to Cloud Run complete!"

    # Get the service URL
    SERVICE_URL=$(gcloud run services describe "$SERVICE_NAME" --region="$REGION" --format="value(status.url)")
    print_success "Your application is available at: $SERVICE_URL"
}

# Main deployment function
main() {
    echo ""
    echo "🎯 Google Cloud Platform Deployment Script"
    echo "==========================================="
    echo ""

    check_dependencies
    get_project_id
    setup_gcloud
    build_and_push
    deploy_to_cloud_run

    echo ""
    print_success "🎉 Deployment completed successfully!"
    echo ""
    print_warning "Next steps:"
    echo "1. Set up Cloud SQL for PostgreSQL database"
    echo "2. Set up MongoDB Atlas or Cloud Firestore"
    echo "3. Configure environment variables in Cloud Run"
    echo "4. Set up your domain name (optional)"
    echo ""
}

# Run the main function
main
