@echo off
echo ================================================================================================
echo EMERGENCY BUILD SCRIPT - RUSTRY PROJECT
echo ================================================================================================

echo Setting environment variables...
set GRADLE_OPTS=-Dorg.gradle.daemon=false -Dorg.gradle.caching=false -Dorg.gradle.parallel=false
set JAVA_OPTS=-Xmx4g -Dfile.encoding=UTF-8

echo Attempting emergency build with minimal configuration...
echo.

echo Step 1: Testing basic Gradle functionality...
gradlew.bat help --no-daemon --stacktrace

echo.
echo Step 2: Attempting clean...
gradlew.bat clean --no-daemon --stacktrace

echo.
echo Step 3: Attempting build...
gradlew.bat build --no-daemon --stacktrace --refresh-dependencies

echo ================================================================================================
echo Emergency build completed. Check output above for results.
echo ================================================================================================
pause