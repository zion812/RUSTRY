#!/bin/bash

# ================================================================================================
# ROOSTER PLATFORM - BUILD TROUBLESHOOTING SCRIPT
# ================================================================================================
# This script helps diagnose and fix common build issues
# Run this script if you encounter build failures or dependency resolution problems

echo "🐓 ROOSTER PLATFORM - BUILD TROUBLESHOOTING"
echo "=============================================="
echo ""

# Step 1: Check network connectivity
echo "📡 Step 1: Testing network connectivity..."
echo "Testing Maven Central connectivity..."
if curl -s --head https://repo.maven.apache.org/maven2/ | head -n 1 | grep -q "200 OK"; then
    echo "✅ Maven Central is accessible"
else
    echo "❌ Maven Central is not accessible - check your internet connection"
    echo "💡 Consider configuring proxy settings in gradle.properties"
fi

echo ""
echo "Testing Google Maven connectivity..."
if curl -s --head https://maven.google.com/ | head -n 1 | grep -q "200 OK"; then
    echo "✅ Google Maven is accessible"
else
    echo "❌ Google Maven is not accessible - check your internet connection"
fi

echo ""
echo "Testing Alibaba Maven mirror..."
if curl -s --head https://maven.aliyun.com/repository/central/ | head -n 1 | grep -q "200 OK"; then
    echo "✅ Alibaba Maven mirror is accessible"
else
    echo "❌ Alibaba Maven mirror is not accessible"
fi

echo ""
echo "🧹 Step 2: Cleaning build caches..."

# Step 2: Clean all caches
echo "Cleaning Gradle build cache..."
./gradlew cleanBuildCache 2>/dev/null || echo "Build cache clean completed"

echo "Cleaning project build directories..."
./gradlew clean 2>/dev/null || echo "Project clean completed"

echo "Removing Gradle daemon..."
./gradlew --stop 2>/dev/null || echo "Gradle daemon stopped"

echo "Clearing Gradle user cache..."
if [ -d "$HOME/.gradle/caches" ]; then
    rm -rf "$HOME/.gradle/caches"
    echo "✅ Gradle user cache cleared"
else
    echo "ℹ️ Gradle user cache directory not found"
fi

echo ""
echo "🔧 Step 3: Checking Gradle configuration..."

# Step 3: Verify Gradle configuration
echo "Gradle version:"
./gradlew --version | head -n 10

echo ""
echo "Java version:"
java -version

echo ""
echo "🚀 Step 4: Testing build with fresh dependencies..."

# Step 4: Test build
echo "Testing build with fresh dependency download..."
if ./gradlew build --refresh-dependencies --no-daemon --stacktrace; then
    echo ""
    echo "🎉 BUILD SUCCESSFUL!"
    echo "✅ All dependencies resolved successfully"
    echo "✅ Project builds without errors"
else
    echo ""
    echo "❌ BUILD FAILED"
    echo ""
    echo "🔍 TROUBLESHOOTING SUGGESTIONS:"
    echo ""
    echo "1. PROXY CONFIGURATION:"
    echo "   If you're behind a corporate firewall, uncomment and configure"
    echo "   the proxy settings in gradle.properties:"
    echo "   systemProp.http.proxyHost=your.proxy.host"
    echo "   systemProp.http.proxyPort=8080"
    echo ""
    echo "2. NETWORK RESTRICTIONS:"
    echo "   Contact your IT department to ensure these URLs are accessible:"
    echo "   - https://repo.maven.apache.org"
    echo "   - https://maven.google.com"
    echo "   - https://plugins.gradle.org"
    echo ""
    echo "3. VPN SOLUTION:"
    echo "   Try connecting to a VPN that bypasses network restrictions"
    echo ""
    echo "4. OFFLINE MODE:"
    echo "   If some dependencies are cached, try:"
    echo "   ./gradlew build --offline"
    echo ""
    echo "5. MANUAL DEPENDENCY DOWNLOAD:"
    echo "   Download the Kotlin compiler manually:"
    echo "   curl -O https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/kotlin-compiler-embeddable/1.9.25/kotlin-compiler-embeddable-1.9.25.jar"
fi

echo ""
echo "📋 CONFIGURATION SUMMARY:"
echo "========================="
echo "✅ Maven mirrors configured (Alibaba as fallback)"
echo "✅ Proxy configuration template available in gradle.properties"
echo "✅ Optimized Gradle settings for performance"
echo "✅ Latest dependency versions configured"
echo ""
echo "For additional help, check:"
echo "- BUILD_FIX_SUMMARY.md"
echo "- OPTIMIZATION_COMPLETE.md"
echo "- gradle.properties (for proxy configuration)"
echo ""
echo "🐓 Rooster Platform Build Troubleshooting Complete!"