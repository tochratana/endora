# Database Setup Guide for Endora API Engine

## 🗄️ PostgreSQL Cloud SQL Setup

Your PostgreSQL Cloud SQL instance is being created with these details:
- **Instance Name**: `endora-postgres-db`
- **Database**: `endora`
- **Region**: `us-central1`
- **Version**: PostgreSQL 15
- **Tier**: db-f1-micro (suitable for development/testing)
- **Root Password**: `EndoraSecure2024!`

### Connection Details:
```
Instance Connection Name: api-engine-backend-20250829:us-central1:endora-postgres-db
Database Name: endora
Username: postgres
Password: EndoraSecure2024!
```

## 🍃 MongoDB Atlas Setup

Since MongoDB Atlas requires manual setup through their web interface, follow these steps:

### Step 1: Create MongoDB Atlas Account
1. Go to [MongoDB Atlas](https://cloud.mongodb.com/)
2. Sign up or log in with your Google account
3. Create a new project called "Endora API Engine"

### Step 2: Create a Free Cluster
1. Click "Build a Database"
2. Choose the **FREE** M0 tier
3. Select **AWS** as cloud provider
4. Choose **US East (N. Virginia)** region
5. Name your cluster: `endora-cluster`

### Step 3: Configure Database Access
1. Go to "Database Access" in the left sidebar
2. Click "Add New Database User"
3. Choose "Password" authentication
4. Username: `endora_user`
5. Password: `EndoraSecure2024!`
6. Database User Privileges: "Read and write to any database"

### Step 4: Configure Network Access
1. Go to "Network Access" in the left sidebar
2. Click "Add IP Address"
3. Choose "Allow Access from Anywhere" (0.0.0.0/0)
4. Click "Confirm"

### Step 5: Get Connection String
1. Go to "Database" in the left sidebar
2. Click "Connect" on your cluster
3. Choose "Connect your application"
4. Driver: Node.js, Version: 4.1 or later
5. Copy the connection string (it will look like this):
   ```
   mongodb+srv://endora_user:<password>@endora-cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```
6. Replace `<password>` with `EndoraSecure2024!`
7. Add the database name by changing the URI to:
   ```
   mongodb+srv://endora_user:EndoraSecure2024!@endora-cluster.xxxxx.mongodb.net/endora?retryWrites=true&w=majority
   ```

## 🔧 Environment Variables for Cloud Run

Once both databases are ready, you'll need to set these environment variables in Cloud Run:

```bash
# PostgreSQL Configuration
DATABASE_URL=jdbc:postgresql://google/endora?cloudSqlInstance=api-engine-backend-20250829:us-central1:endora-postgres-db&socketFactory=com.google.cloud.sql.postgres.SocketFactory&useSSL=false
DB_USERNAME=postgres
DB_PASSWORD=EndoraSecure2024!
DB_DRIVER=org.postgresql.Driver
DDL_AUTO=update
DB_PLATFORM=org.hibernate.dialect.PostgreSQLDialect

# MongoDB Configuration
MONGODB_URI=mongodb+srv://endora_user:EndoraSecure2024!@endora-cluster.xxxxx.mongodb.net/endora?retryWrites=true&w=majority

# Email Configuration (if needed)
EMAIL_PASSWORD=your-gmail-app-password

# Instance Connection Name
INSTANCE_CONNECTION_NAME=api-engine-backend-20250829:us-central1:endora-postgres-db
```

## 🚀 Next Steps

1. Complete MongoDB Atlas setup (5-10 minutes)
2. Wait for PostgreSQL Cloud SQL to finish creating
3. Update Cloud Run environment variables
4. Redeploy the application
