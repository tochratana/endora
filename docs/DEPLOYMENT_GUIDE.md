# 🚀 Google Cloud Platform Deployment Guide

This guide will help you deploy your Spring Boot API Engine Backend to Google Cloud Platform (GCP) step by step.

## 📋 Prerequisites

Before we start, you'll need:

1. **Google Cloud Account**: Create one at [cloud.google.com](https://cloud.google.com)
2. **Google Cloud CLI**: Install from [cloud.google.com/sdk](https://cloud.google.com/sdk/docs/install)
3. **Docker Desktop**: Install from [docker.com](https://www.docker.com/products/docker-desktop)
4. **A Google Cloud Project**: We'll create this together

## 🏗️ What We'll Build

We're deploying your application using these Google Cloud services:

- **Cloud Run**: Serverless container hosting (auto-scales, pay-per-use)
- **Cloud SQL**: Managed PostgreSQL database
- **MongoDB Atlas**: Cloud MongoDB (we'll use Atlas as it's easier than Google's MongoDB options)
- **Container Registry**: To store your Docker images

## 📁 Files Created for Deployment

I've created these files in your project:

- `Dockerfile`: Instructions to containerize your Spring Boot app
- `application-prod.yml`: Production configuration with environment variables
- `.dockerignore`: Excludes unnecessary files from Docker builds
- `cloud-run-service.yaml`: Cloud Run service configuration
- `deploy.sh`: Automated deployment script

## 🚀 Step-by-Step Deployment

### Step 1: Set Up Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select an existing one
3. Note your Project ID (you'll need this)

### Step 2: Install and Configure Google Cloud CLI

```bash
# Install Google Cloud CLI (if not already installed)
# Follow instructions at: https://cloud.google.com/sdk/docs/install

# Login to your Google account
gcloud auth login

# Set your project (replace YOUR_PROJECT_ID with your actual project ID)
gcloud config set project YOUR_PROJECT_ID
```

### Step 3: Set Up Databases

#### PostgreSQL (Cloud SQL)
1. Go to Cloud SQL in Google Cloud Console
2. Create a new PostgreSQL instance:
   - Instance ID: `endora-postgres`
   - Password: Set a strong password
   - Region: Choose closest to your users
   - Machine type: `db-f1-micro` (for testing) or `db-n1-standard-1` (for production)

#### MongoDB (MongoDB Atlas - Recommended)
1. Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create a free cluster
3. Create a database user
4. Get your connection string (it looks like: `mongodb+srv://username:password@cluster.mongodb.net/endora`)

### Step 4: Run the Deployment

```bash
# Navigate to your project directory
cd /path/to/your/api-engine-backend

# Run the deployment script
./deploy.sh
```

The script will:
1. Check if you have all required tools installed
2. Ask for your Google Cloud Project ID
3. Enable necessary Google Cloud APIs
4. Build your Docker container
5. Push it to Google Container Registry
6. Deploy to Cloud Run

### Step 5: Configure Environment Variables

After deployment, you need to set up your database connections:

```bash
# Set up secrets for database credentials
gcloud secrets create db-password --data-file=- <<< "YOUR_POSTGRES_PASSWORD"
gcloud secrets create mongodb-uri --data-file=- <<< "YOUR_MONGODB_CONNECTION_STRING"

# Update Cloud Run service with environment variables
gcloud run services update api-engine-backend \
  --region=us-central1 \
  --set-env-vars="DATABASE_URL=jdbc:postgresql://CLOUD_SQL_CONNECTION_NAME/endora" \
  --set-secrets="DB_PASSWORD=db-password:latest" \
  --set-secrets="MONGODB_URI=mongodb-uri:latest"
```

### Step 6: Connect Cloud SQL

```bash
# Add Cloud SQL connection to your Cloud Run service
gcloud run services update api-engine-backend \
  --region=us-central1 \
  --add-cloudsql-instances=YOUR_PROJECT_ID:YOUR_REGION:endora-postgres
```

## 🔧 Configuration Details

### Environment Variables in Production

Your `application-prod.yml` uses these environment variables:

- `DATABASE_URL`: PostgreSQL connection string
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password (stored as secret)
- `MONGODB_URI`: MongoDB connection string (stored as secret)
- `PORT`: Server port (automatically set by Cloud Run)

### Security Best Practices

1. **Secrets Management**: Database passwords are stored in Google Secret Manager
2. **IAM Permissions**: Only your Cloud Run service can access the secrets
3. **Network Security**: Cloud SQL is accessible only from your Cloud Run service
4. **HTTPS**: Cloud Run automatically provides HTTPS endpoints

## 🌐 Domain Setup (Optional)

To use your own domain:

1. Go to Cloud Run console
2. Select "Manage Custom Domains"
3. Add your domain and follow verification steps
4. Update your DNS records as instructed

## 💰 Cost Estimation

For a small to medium application:

- **Cloud Run**: ~$0-10/month (pay per request)
- **Cloud SQL**: ~$7-25/month (depending on instance size)
- **MongoDB Atlas**: Free tier available, ~$9/month for paid tier
- **Container Registry**: ~$0.10/GB/month

## 🐛 Troubleshooting

### Common Issues:

1. **Build Fails**: Check Dockerfile and ensure all dependencies are in build.gradle
2. **Database Connection**: Verify connection strings and credentials
3. **Memory Issues**: Increase memory allocation in Cloud Run
4. **Timeout**: Increase request timeout in Cloud Run settings

### Useful Commands:

```bash
# View logs
gcloud run services logs tail api-engine-backend --region=us-central1

# Check service status
gcloud run services describe api-engine-backend --region=us-central1

# Update service
gcloud run services update api-engine-backend --region=us-central1 [options]
```

## 📞 Next Steps

After successful deployment:

1. Test your API endpoints
2. Set up monitoring and alerts
3. Configure CI/CD for automatic deployments
4. Set up backup strategies for your databases
5. Consider setting up a staging environment

## 🎯 Quick Deployment Checklist

- [ ] Google Cloud project created
- [ ] Google Cloud CLI installed and configured
- [ ] Docker Desktop installed
- [ ] PostgreSQL instance created in Cloud SQL
- [ ] MongoDB Atlas cluster created
- [ ] Run `./deploy.sh`
- [ ] Configure environment variables
- [ ] Test the deployed application

---

**Need Help?** 
- Google Cloud Documentation: https://cloud.google.com/docs
- Cloud Run Documentation: https://cloud.google.com/run/docs
- Spring Boot on Google Cloud: https://cloud.google.com/java/docs/setup
