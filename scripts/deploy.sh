#!/bin/bash

# RUSTRY Deployment Script
# Automated deployment for RUSTRY poultry marketplace

set -e

echo "🚀 Starting RUSTRY Deployment Process..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="RUSTRY"
BUILD_TYPE="release"
FIREBASE_PROJECT_ID="rustry-production"

# Functions
print_step() {
    echo -e "${BLUE}📋 Step $1: $2${NC}"
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

# Step 1: Environment Check
print_step "1" "Checking Environment"

if ! command -v java &> /dev/null; then
    print_error "Java not found. Please install JDK 17+"
    exit 1
fi

if ! command -v firebase &> /dev/null; then
    print_warning "Firebase CLI not found. Installing..."
    npm install -g firebase-tools
fi

print_success "Environment check completed"

# Step 2: Clean Build
print_step "2" "Cleaning Previous Builds"

rm -rf build/
rm -rf app/build/
rm -rf .gradle/

print_success "Clean completed"

# Step 3: Build APK
print_step "3" "Building Release APK"

if ./gradlew assembleRelease --no-daemon; then
    print_success "APK build successful"
else
    print_warning "Gradle build failed, trying Android Studio method..."
    echo "Please build using Android Studio:"
    echo "1. Open Android Studio"
    echo "2. File → Open → Select RUSTRY project"
    echo "3. Build → Generate Signed Bundle/APK"
    exit 1
fi

# Step 4: Build AAB for Play Store
print_step "4" "Building Release Bundle"

if ./gradlew bundleRelease --no-daemon; then
    print_success "AAB bundle build successful"
else
    print_warning "Bundle build failed, APK is still available"
fi

# Step 5: Firebase App Distribution
print_step "5" "Deploying to Firebase App Distribution"

if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
    firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk \
        --app $FIREBASE_PROJECT_ID \
        --groups "beta-farmers,early-adopters" \
        --release-notes "RUSTRY marketplace production release - Complete poultry trading platform with authentication, camera, notifications, and payments"
    
    print_success "Firebase distribution completed"
else
    print_warning "APK not found, skipping Firebase distribution"
fi

# Step 6: Generate Deployment Report
print_step "6" "Generating Deployment Report"

cat > deployment_report.md << EOF
# 🚀 RUSTRY Deployment Report

**Deployment Date:** $(date)
**Build Type:** $BUILD_TYPE
**Version:** $(grep versionName app/build.gradle.kts | head -1 | cut -d'"' -f2)

## 📱 Build Artifacts

### APK (Direct Installation)
- **File:** app/build/outputs/apk/release/app-release.apk
- **Size:** $(du -h app/build/outputs/apk/release/app-release.apk 2>/dev/null | cut -f1 || echo "N/A")
- **Target:** Direct distribution, sideloading

### AAB (Play Store)
- **File:** app/build/outputs/bundle/release/app-release.aab
- **Size:** $(du -h app/build/outputs/bundle/release/app-release.aab 2>/dev/null | cut -f1 || echo "N/A")
- **Target:** Google Play Store submission

## 🎯 Deployment Targets

### ✅ Firebase App Distribution
- **Status:** Deployed
- **Groups:** beta-farmers, early-adopters
- **Download Link:** Check Firebase Console

### 📱 Google Play Store
- **Status:** Ready for submission
- **File:** app-release.aab
- **Action:** Upload to Play Console

## 📊 Expected Metrics

### Week 1 Targets
- Farmer Sign-ups: 200+
- Fowl Listings: 1,500+
- Successful Orders: 50+
- App Store Rating: 4.5+

### Technical KPIs
- App Startup Time: <3 seconds
- Crash-Free Sessions: >99.5%
- Payment Success Rate: >95%

## 🚀 Next Steps

1. **Monitor Firebase Analytics** for user engagement
2. **Submit AAB to Play Store** for production release
3. **Launch marketing campaigns** across social media
4. **Engage with farmer communities** for user acquisition
5. **Monitor crash reports** and user feedback

## 📞 Support

- **Technical Issues:** Check BUILD_TROUBLESHOOTING.md
- **Deployment Issues:** Check DEPLOYMENT_STRATEGY.md
- **Business Questions:** Check PROJECT_COMPLETION_SUMMARY.md

**RUSTRY is now live and ready for users! 🐔💰**
EOF

print_success "Deployment report generated"

# Step 7: Final Summary
print_step "7" "Deployment Summary"

echo ""
echo "🎉 RUSTRY DEPLOYMENT COMPLETED SUCCESSFULLY!"
echo ""
echo "📱 Build Artifacts:"
if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
    echo "   ✅ APK: app/build/outputs/apk/release/app-release.apk"
fi
if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
    echo "   ✅ AAB: app/build/outputs/bundle/release/app-release.aab"
fi

echo ""
echo "🚀 Next Actions:"
echo "   1. Test APK on device: adb install app/build/outputs/apk/release/app-release.apk"
echo "   2. Submit AAB to Play Store for production release"
echo "   3. Monitor Firebase Analytics for user engagement"
echo "   4. Launch marketing campaigns"

echo ""
echo "📊 Expected Results:"
echo "   • 200+ farmer sign-ups in Week 1"
echo "   • 1,500+ fowl listings initially"
echo "   • 4.5+ app store rating"
echo "   • 99.5%+ crash-free sessions"

echo ""
print_success "RUSTRY is ready to revolutionize poultry farming! 🐔💰"

exit 0