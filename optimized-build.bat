@echo off
echo ================================================================================================
echo RUSTRY BUILD OPTIMIZATION SCRIPT
echo ================================================================================================

echo Step 1: Setting optimized environment variables...
set GRADLE_OPTS=-Dorg.gradle.daemon=true -Dorg.gradle.caching=true -Dorg.gradle.parallel=true -Dorg.gradle.configureondemand=true
set JAVA_OPTS=-Xmx6g -XX:+UseG1GC -XX:+UseStringDeduplication

echo Step 2: Stopping any existing daemons...
call gradlew --stop

echo Step 3: Testing optimized configuration...
echo Running: gradlew help --console=plain
call gradlew help --console=plain

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Basic help command failed. Falling back to conservative settings...
    set GRADLE_OPTS=-Dorg.gradle.daemon=false -Dorg.gradle.caching=false
    call gradlew help --no-daemon --console=plain
)

echo.
echo Step 4: Attempting clean build with optimizations...
echo Running: gradlew clean --console=plain
call gradlew clean --console=plain

echo.
echo Step 5: Building with dependency refresh...
echo Running: gradlew build --refresh-dependencies --console=plain
call gradlew build --refresh-dependencies --console=plain

echo.
echo Step 6: Generating build scan for analysis...
echo Running: gradlew build --scan
call gradlew build --scan

echo ================================================================================================
echo Build optimization completed. Check output above for results.
echo If successful, subsequent builds can use: gradlew build --offline
echo ================================================================================================
pause