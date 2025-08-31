#!/bin/bash

# Simple Local Development Helper
# Just manages databases - you run your app normally with ./gradlew bootRun

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

show_help() {
    echo "🐳 Simple Local Development Helper"
    echo "======================================"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  start     - Start all databases"
    echo "  stop      - Stop all databases"
    echo "  status     - Show status of all services"
    echo "  help      - Show this help message"
    echo ""
    echo "Database URLs:"
    echo "  PostgreSQL:  localhost:5432"
    echo "  MongoDB:     localhost:27017"
    echo ""
}

case "${1:-help}" in
    start)
        print_status "Starting databases..."
        docker-compose up -d
        print_success "Databases ready!"
        echo ""
        echo "Now run your app: ./gradlew bootRun"
        echo "  PostgreSQL: localhost:5432"
        echo "  MongoDB: localhost:27017"
        ;;
    stop)
        print_status "Stopping databases..."
        docker-compose down
        print_success "Databases stopped!"
        ;;
    status)
        docker-compose ps
        ;;
    help|*)
        show_help
        ;;
esac
