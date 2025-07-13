#!/bin/bash

# Rooster Platform - Automated Distribution Script
# Usage: ./distribute-app.sh [version] [release-notes]

set -e

# Configuration
PROJECT_DIR="C:/RUSTRY"
APP_ID="1:YOUR_PROJECT_NUMBER:android:YOUR_APP_ID"  # Replace with actual App ID
APK_PATH="app/build/outputs/apk/release/app-release.apk"
GROUPS="beta-testers,internal-team"

# Get version and release notes
VERSION=${1:-"1.0.0"}
RELEASE_NOTES=${2:-"Latest release of Rooster Platform"}

echo "🚀 Starting Rooster Platform Distribution..."
echo "Version: $VERSION"
echo "Release Notes: $RELEASE_NOTES"

# Navigate to project directory
cd "$PROJECT_DIR"

# Clean and build release APK
echo "📦 Building release APK..."
./gradlew clean
./gradlew assembleRelease

# Verify APK exists
if [ ! -f "$APK_PATH" ]; then
    echo "❌ Error: APK not found at $APK_PATH"
    exit 1
fi

# Get APK size
APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
echo "📱 APK Size: $APK_SIZE"

# Upload to Firebase App Distribution
echo "🔥 Uploading to Firebase App Distribution..."
firebase appdistribution:distribute "$APK_PATH" \
    --app "$APP_ID" \
    --groups "$GROUPS" \
    --release-notes "$RELEASE_NOTES"

echo "✅ Distribution completed successfully!"
echo "📧 Testers will receive email notifications"
echo "🔗 Check Firebase Console for download links"

# Optional: Open Firebase Console
echo "🌐 Opening Firebase Console..."
# Uncomment the next line to auto-open browser
# start "https://console.firebase.google.com/project/YOUR_PROJECT_ID/appdistribution"

echo "🎉 Rooster Platform v$VERSION distributed successfully!"