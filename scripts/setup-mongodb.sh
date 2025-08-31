#!/bin/bash

# MongoDB Atlas Setup Helper Script
# This script will guide you through setting up MongoDB Atlas

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "$1"
    echo "=================================================="
    echo -e "${NC}"
}

print_step() {
    echo -e "${GREEN}$1${NC}"
}

print_note() {
    echo -e "${YELLOW}💡 $1${NC}"
}

print_important() {
    echo -e "${RED}⚠️  $1${NC}"
}

wait_for_user() {
    echo ""
    read -p "Press Enter when you've completed this step..."
    echo ""
}

print_header "🍃 MongoDB Atlas Setup Guide"

echo "This script will guide you through setting up MongoDB Atlas for your Endora API."
echo "MongoDB Atlas is a cloud-hosted MongoDB service with a free tier perfect for development."
echo ""

print_step "Step 1: Create MongoDB Atlas Account"
echo "1. Open your browser and go to: https://cloud.mongodb.com/"
echo "2. Click 'Try Free' or 'Sign Up'"
echo "3. You can sign up with:"
echo "   - Your Google account (recommended)"
echo "   - Your GitHub account"
echo "   - Or create a new account with email"
echo ""
print_note "Using Google account is fastest since you're already signed in!"
wait_for_user

print_step "Step 2: Create a New Project"
echo "1. After signing in, you'll see the MongoDB Atlas dashboard"
echo "2. Click 'New Project' or 'Create Project'"
echo "3. Name your project: 'Endora API Engine'"
echo "4. Click 'Next' then 'Create Project'"
echo ""
wait_for_user

print_step "Step 3: Build Your First Database"
echo "1. Click 'Build a Database' (big green button)"
echo "2. Choose 'M0 FREE' tier (it's completely free!)"
echo "3. Cloud Provider: Select 'AWS'"
echo "4. Region: Choose 'US East (N. Virginia) - us-east-1'"
echo "5. Cluster Name: Change to 'endora-cluster'"
echo "6. Click 'Create' (this will take 2-3 minutes)"
echo ""
print_note "The free M0 tier gives you 512MB storage - perfect for development!"
wait_for_user

print_step "Step 4: Configure Database Access (Create User)"
echo "1. You'll see a 'Security Quickstart' popup"
echo "2. Choose 'Username and Password'"
echo "3. Username: endora_user"
echo "4. Password: EndoraSecure2024!"
echo "5. Click 'Create User'"
echo ""
print_important "Write down these credentials - you'll need them!"
echo "Username: endora_user"
echo "Password: EndoraSecure2024!"
wait_for_user

print_step "Step 5: Configure Network Access"
echo "1. Still in the Security Quickstart popup"
echo "2. Under 'Where would you like to connect from?'"
echo "3. Click 'My Local Environment'"
echo "4. Click 'Add My Current IP Address'"
echo "5. THEN click 'Add a Different IP Address'"
echo "6. Enter: 0.0.0.0/0 (this allows access from anywhere - needed for Cloud Run)"
echo "7. Description: 'Allow all IPs for Cloud Run'"
echo "8. Click 'Add Entry'"
echo "9. Click 'Finish and Close'"
echo ""
print_note "We need 0.0.0.0/0 because Cloud Run uses dynamic IP addresses"
wait_for_user

print_step "Step 6: Get Your Connection String"
echo "1. Your cluster should now be ready (green status)"
echo "2. Click 'Connect' button on your 'endora-cluster'"
echo "3. Choose 'Drivers'"
echo "4. Driver: Select 'Node.js'"
echo "5. Version: Keep the default (latest)"
echo "6. Copy the connection string - it looks like:"
echo "   mongodb+srv://endora_user:<password>@endora-cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority"
echo ""
print_important "COPY THIS CONNECTION STRING NOW!"
echo ""

# Get the connection string from user
echo "Please paste your MongoDB connection string here:"
read -p "Connection String: " MONGODB_CONNECTION

if [ -z "$MONGODB_CONNECTION" ]; then
    print_important "No connection string provided. You can add it later."
    MONGODB_CONNECTION="mongodb+srv://endora_user:EndoraSecure2024!@endora-cluster.xxxxx.mongodb.net/endora?retryWrites=true&w=majority"
else
    # Replace <password> with actual password and add database name
    MONGODB_CONNECTION=$(echo "$MONGODB_CONNECTION" | sed 's/<password>/EndoraSecure2024!/g')

    # Add database name if not present
    if [[ ! "$MONGODB_CONNECTION" == *"/endora?"* ]]; then
        MONGODB_CONNECTION=$(echo "$MONGODB_CONNECTION" | sed 's/?retryWrites/\/endora?retryWrites/g')
    fi
fi

echo ""
print_step "Step 7: Your Final MongoDB Connection Details"
echo "Connection String: $MONGODB_CONNECTION"
echo "Database Name: endora"
echo "Username: endora_user"
echo "Password: EndoraSecure2024!"
echo ""

# Save connection string to environment file
echo "# MongoDB Atlas Configuration" > .env.mongodb
echo "MONGODB_URI=$MONGODB_CONNECTION" >> .env.mongodb
echo "MONGODB_DATABASE=endora" >> .env.mongodb
echo "MONGODB_USERNAME=endora_user" >> .env.mongodb
echo "MONGODB_PASSWORD=EndoraSecure2024!" >> .env.mongodb

print_step "✅ MongoDB Atlas Setup Complete!"
echo ""
echo "Your MongoDB connection details have been saved to '.env.mongodb'"
echo "Next: We'll update your Cloud Run deployment with these settings."
echo ""

# Ask if user wants to update the deployment now
read -p "Would you like to update your Cloud Run deployment now? (y/n): " UPDATE_DEPLOYMENT

if [[ $UPDATE_DEPLOYMENT == "y" || $UPDATE_DEPLOYMENT == "Y" ]]; then
    echo ""
    print_header "🚀 Updating Cloud Run Deployment"

    # Check if PostgreSQL is ready
    print_step "Checking PostgreSQL status..."
    export PATH=$PATH:~/google-cloud-sdk/bin

    POSTGRES_STATE=$(gcloud sql instances describe endora-postgres-db --format="value(state)" 2>/dev/null || echo "NOT_FOUND")

    if [ "$POSTGRES_STATE" = "RUNNABLE" ]; then
        echo "✅ PostgreSQL is ready!"

        # Get instance connection name
        INSTANCE_CONNECTION_NAME=$(gcloud sql instances describe endora-postgres-db --format="value(connectionName)")

        # Create database if it doesn't exist
        if ! gcloud sql databases describe endora --instance=endora-postgres-db &>/dev/null; then
            echo "Creating database 'endora'..."
            gcloud sql databases create endora --instance=endora-postgres-db
        fi

        echo ""
        print_step "Updating Cloud Run environment variables..."

        # Update Cloud Run with both PostgreSQL and MongoDB settings
        gcloud run services update api-engine-backend \
            --region=us-central1 \
            --set-env-vars="SPRING_PROFILES_ACTIVE=prod,INSTANCE_CONNECTION_NAME=$INSTANCE_CONNECTION_NAME,DATABASE_URL=jdbc:postgresql://google/endora?cloudSqlInstance=$INSTANCE_CONNECTION_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory&useSSL=false,DB_USERNAME=postgres,DB_PASSWORD=EndoraSecure2024!,DB_DRIVER=org.postgresql.Driver,DDL_AUTO=update,DB_PLATFORM=org.hibernate.dialect.PostgreSQLDialect,MONGODB_URI=$MONGODB_CONNECTION"

        echo ""
        print_step "🎉 Deployment Updated Successfully!"

        # Get service URL
        SERVICE_URL=$(gcloud run services describe api-engine-backend --region=us-central1 --format="value(status.url)")
        echo ""
        echo "🌐 Your application is now running at: $SERVICE_URL"
        echo ""
        echo "✅ Both databases are now configured:"
        echo "   • PostgreSQL Cloud SQL: endora-postgres-db"
        echo "   • MongoDB Atlas: endora-cluster"

    else
        print_important "PostgreSQL is not ready yet (Status: $POSTGRES_STATE)"
        echo "Please wait a few more minutes and run the deployment update manually."
    fi
else
    echo ""
    echo "MongoDB setup is complete! You can update your deployment later using:"
    echo "gcloud run services update api-engine-backend --region=us-central1 --set-env-vars=\"MONGODB_URI=$MONGODB_CONNECTION\""
fi

echo ""
print_header "🎯 Next Steps"
echo "1. Test your application endpoints"
echo "2. Check the Cloud Run logs for any issues"
echo "3. Your databases are ready for development!"
echo ""
echo "MongoDB Atlas Dashboard: https://cloud.mongodb.com/"
echo "Google Cloud Console: https://console.cloud.google.com/"
