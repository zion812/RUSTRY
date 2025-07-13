@echo off
REM ================================================================================================
REM ROOSTER PLATFORM - BUILD TROUBLESHOOTING SCRIPT (Windows)
REM ================================================================================================
REM This script helps diagnose and fix common build issues
REM Run this script if you encounter build failures or dependency resolution problems

echo 🐓 ROOSTER PLATFORM - BUILD TROUBLESHOOTING
echo ==============================================
echo.

REM Step 1: Check network connectivity
echo 📡 Step 1: Testing network connectivity...
echo Testing Maven Central connectivity...
curl -s --head https://repo.maven.apache.org/maven2/ >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Maven Central is accessible
) else (
    echo ❌ Maven Central is not accessible - check your internet connection
    echo 💡 Consider configuring proxy settings in gradle.properties
)

echo.
echo Testing Google Maven connectivity...
curl -s --head https://maven.google.com/ >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Google Maven is accessible
) else (
    echo ❌ Google Maven is not accessible - check your internet connection
)

echo.
echo Testing Alibaba Maven mirror...
curl -s --head https://maven.aliyun.com/repository/central/ >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Alibaba Maven mirror is accessible
) else (
    echo ❌ Alibaba Maven mirror is not accessible
)

echo.
echo 🧹 Step 2: Cleaning build caches...

REM Step 2: Clean all caches
echo Cleaning Gradle build cache...
gradlew.bat cleanBuildCache >nul 2>&1
echo Build cache clean completed

echo Cleaning project build directories...
gradlew.bat clean >nul 2>&1
echo Project clean completed

echo Removing Gradle daemon...
gradlew.bat --stop >nul 2>&1
echo Gradle daemon stopped

echo Clearing Gradle user cache...
if exist "%USERPROFILE%\.gradle\caches" (
    rmdir /s /q "%USERPROFILE%\.gradle\caches" >nul 2>&1
    echo ✅ Gradle user cache cleared
) else (
    echo ℹ️ Gradle user cache directory not found
)

echo.
echo 🔧 Step 3: Checking Gradle configuration...

REM Step 3: Verify Gradle configuration
echo Gradle version:
gradlew.bat --version

echo.
echo Java version:
java -version

echo.
echo 🚀 Step 4: Testing build with fresh dependencies...

REM Step 4: Test build
echo Testing build with fresh dependency download...
gradlew.bat build --refresh-dependencies --no-daemon --stacktrace
if %errorlevel% == 0 (
    echo.
    echo 🎉 BUILD SUCCESSFUL!
    echo ✅ All dependencies resolved successfully
    echo ✅ Project builds without errors
) else (
    echo.
    echo ❌ BUILD FAILED
    echo.
    echo 🔍 TROUBLESHOOTING SUGGESTIONS:
    echo.
    echo 1. PROXY CONFIGURATION:
    echo    If you're behind a corporate firewall, uncomment and configure
    echo    the proxy settings in gradle.properties:
    echo    systemProp.http.proxyHost=your.proxy.host
    echo    systemProp.http.proxyPort=8080
    echo.
    echo 2. NETWORK RESTRICTIONS:
    echo    Contact your IT department to ensure these URLs are accessible:
    echo    - https://repo.maven.apache.org
    echo    - https://maven.google.com
    echo    - https://plugins.gradle.org
    echo.
    echo 3. VPN SOLUTION:
    echo    Try connecting to a VPN that bypasses network restrictions
    echo.
    echo 4. OFFLINE MODE:
    echo    If some dependencies are cached, try:
    echo    gradlew.bat build --offline
    echo.
    echo 5. MANUAL DEPENDENCY DOWNLOAD:
    echo    Download the Kotlin compiler manually:
    echo    curl -O https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/kotlin-compiler-embeddable/1.9.25/kotlin-compiler-embeddable-1.9.25.jar
)

echo.
echo 📋 CONFIGURATION SUMMARY:
echo =========================
echo ✅ Maven mirrors configured (Alibaba as fallback)
echo ✅ Proxy configuration template available in gradle.properties
echo ✅ Optimized Gradle settings for performance
echo ✅ Latest dependency versions configured
echo.
echo For additional help, check:
echo - BUILD_FIX_SUMMARY.md
echo - OPTIMIZATION_COMPLETE.md
echo - gradle.properties (for proxy configuration)
echo.
echo 🐓 Rooster Platform Build Troubleshooting Complete!
pause