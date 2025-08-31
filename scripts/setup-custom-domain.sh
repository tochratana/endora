#!/bin/bash

# Custom Domain Setup Script for Cloud Run
# This script helps you map a custom domain to your Cloud Run service

set -e

echo "🌐 Custom Domain Setup for Endora API Engine Backend"
echo "=================================================="

# Configuration
PROJECT_ID="api-engine-backend-20250829"
SERVICE_NAME="api-engine-backend"
REGION="us-central1"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

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

# Get domain from user
get_domain() {
    echo ""
    print_status "Please enter your custom domain name:"
    echo "Examples:"
    echo "  - api.yourdomain.com"
    echo "  - endora-api.yourdomain.com"
    echo "  - yourdomain.com"
    echo ""
    read -p "Domain: " DOMAIN_NAME

    if [ -z "$DOMAIN_NAME" ]; then
        print_error "Domain name cannot be empty"
        exit 1
    fi

    print_success "Using domain: $DOMAIN_NAME"
}

# Verify domain ownership
verify_domain() {
    print_status "Verifying domain ownership..."

    echo ""
    print_warning "IMPORTANT: Before proceeding, you need to verify domain ownership."
    echo ""
    echo "1. Go to Google Search Console: https://search.google.com/search-console"
    echo "2. Add and verify ownership of: $DOMAIN_NAME"
    echo "3. Make sure you can manage DNS records for this domain"
    echo ""
    read -p "Have you verified domain ownership in Google Search Console? (y/n): " verified

    if [ "$verified" != "y" ] && [ "$verified" != "Y" ]; then
        print_error "Please verify domain ownership first, then run this script again."
        exit 1
    fi
}

# Create domain mapping
create_domain_mapping() {
    print_status "Creating domain mapping..."

    gcloud beta run domain-mappings create \
        --service="$SERVICE_NAME" \
        --domain="$DOMAIN_NAME" \
        --region="$REGION" \
        --project="$PROJECT_ID"

    if [ $? -eq 0 ]; then
        print_success "Domain mapping created successfully!"
    else
        print_error "Failed to create domain mapping"
        exit 1
    fi
}

# Get DNS records to configure
get_dns_records() {
    print_status "Getting DNS records you need to configure..."

    echo ""
    echo "📋 DNS Configuration Required:"
    echo "=============================="

    # Get the domain mapping details
    MAPPING_INFO=$(gcloud run domain-mappings describe "$DOMAIN_NAME" \
        --region="$REGION" \
        --project="$PROJECT_ID" \
        --format="value(status.resourceRecords[].name,status.resourceRecords[].rrdata)")

    if [ -n "$MAPPING_INFO" ]; then
        echo "Please add the following DNS records to your domain provider:"
        echo ""

        # Parse and display the records
        gcloud run domain-mappings describe "$DOMAIN_NAME" \
            --region="$REGION" \
            --project="$PROJECT_ID" \
            --format="table(status.resourceRecords[].name,status.resourceRecords[].type,status.resourceRecords[].rrdata)"

        echo ""
        print_warning "Add these DNS records to your domain provider's DNS settings."
        print_warning "It may take 24-48 hours for DNS changes to propagate worldwide."
    else
        print_error "Could not retrieve DNS records. Please check the domain mapping status."
    fi
}

# Check domain mapping status
check_status() {
    print_status "Checking domain mapping status..."

    gcloud run domain-mappings describe "$DOMAIN_NAME" \
        --region="$REGION" \
        --project="$PROJECT_ID" \
        --format="table(metadata.name,status.conditions[].type,status.conditions[].status)"
}

# Main function
main() {
    echo ""
    print_status "This script will help you set up a custom domain for your API."
    echo ""

    get_domain
    verify_domain
    create_domain_mapping
    get_dns_records
    check_status

    echo ""
    print_success "🎉 Domain setup initiated!"
    echo ""
    print_warning "Next steps:"
    echo "1. Add the DNS records shown above to your domain provider"
    echo "2. Wait for DNS propagation (24-48 hours)"
    echo "3. Test your domain: https://$DOMAIN_NAME"
    echo ""
    echo "You can check status anytime with:"
    echo "gcloud run domain-mappings describe $DOMAIN_NAME --region=$REGION --project=$PROJECT_ID"
    echo ""
}

# Run the main function
main
