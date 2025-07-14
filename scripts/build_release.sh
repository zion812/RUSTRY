#!/bin/bash

# RUSTRY Release Build Script
# Automated build and deployment pipeline

set -e  # Exit on any error

echo "🚀 RUSTRY Release Build Pipeline Starting..."
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    print_error "Please run this script from the RUSTRY project root directory"
    exit 1
fi

print_status "Checking environment..."

# Check for required tools
command -v ./gradlew >/dev/null 2>&1 || { print_error "Gradle wrapper not found"; exit 1; }

print_success "Environment check passed"

# Clean previous builds
print_status "Cleaning previous builds..."
./gradlew clean
print_success "Clean completed"

# Run tests
print_status "Running unit tests..."
./gradlew testDebugUnitTest testReleaseUnitTest
if [ $? -eq 0 ]; then
    print_success "All tests passed ✅"
else
    print_error "Tests failed ❌"
    exit 1
fi

# Build release APK
print_status "Building release APK..."
./gradlew assembleRelease
if [ $? -eq 0 ]; then
    print_success "Release APK built successfully ✅"
else
    print_error "APK build failed ❌"
    exit 1
fi

# Build release AAB for Play Store
print_status "Building release AAB..."
./gradlew bundleRelease
if [ $? -eq 0 ]; then
    print_success "Release AAB built successfully ✅"
else
    print_error "AAB build failed ❌"
    exit 1
fi

echo ""
echo "🎉 RUSTRY Release Build Complete!"
echo "=================================="
echo ""
echo "🚀 Ready for deployment!"

exit 0