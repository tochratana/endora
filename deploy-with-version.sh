#!/bin/bash

# Enhanced deployment script with versioned images
set -e

echo "🚀 Starting versioned deployment..."

# Generate timestamp for unique image tag
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
PROJECT_ID="api-engine-backend-20250829"
SERVICE_NAME="api-engine-backend"
REGION="us-central1"

echo "📦 Building with version tag: $TIMESTAMP"

# Step 1: Build the project
echo "🔨 Building project..."
./gradlew clean build --no-daemon

# Step 2: Build and push Docker image with timestamp tag
echo "🐳 Building and pushing Docker image..."
docker buildx build --platform linux/amd64 \
  -t gcr.io/$PROJECT_ID/$SERVICE_NAME:$TIMESTAMP \
  -t gcr.io/$PROJECT_ID/$SERVICE_NAME:latest \
  . --push

# Step 3: Deploy to Cloud Run with the new image
echo "☁️ Deploying to Cloud Run..."
gcloud run deploy $SERVICE_NAME \
  --image gcr.io/$PROJECT_ID/$SERVICE_NAME:$TIMESTAMP \
  --platform managed \
  --region $REGION \
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

echo "✅ Deployment completed successfully!"
echo "🌐 Your application is available at: https://api.api-ngin.oudom.dev"
echo "📊 Cloud Run URL: https://api-engine-backend-308354822720.us-central1.run.app"
echo "📝 Swagger UI: https://api.api-ngin.oudom.dev/swagger-ui/index.html"
