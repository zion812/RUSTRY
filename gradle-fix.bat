@echo off
echo ================================================================================================
echo GRADLE BUILD FIX SCRIPT - ROOSTER PLATFORM
echo ================================================================================================

echo Step 1: Stopping all Gradle daemons...
call gradlew --stop

echo Step 2: Clearing Gradle caches...
if exist "%USERPROFILE%\.gradle" (
    echo Removing user Gradle directory...
    rmdir /s /q "%USERPROFILE%\.gradle"
)

if exist ".gradle" (
    echo Removing project Gradle directory...
    rmdir /s /q ".gradle"
)

if exist "build" (
    echo Removing build directory...
    rmdir /s /q "build"
)

if exist "app\build" (
    echo Removing app build directory...
    rmdir /s /q "app\build"
)

echo Step 3: Clearing temporary files...
if exist "%TEMP%\gradle*" (
    del /q /s "%TEMP%\gradle*" 2>nul
)

echo Step 4: Setting environment variables for better network connectivity...
set GRADLE_OPTS=-Dorg.gradle.daemon=true -Dorg.gradle.parallel=true -Dorg.gradle.caching=true
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en

echo Step 5: Testing network connectivity...
curl -I https://repo.maven.apache.org/maven2/ || echo "Maven Central connectivity issue detected"
curl -I https://maven.aliyun.com/repository/central/ || echo "Alibaba mirror connectivity issue detected"

echo Step 6: Attempting fresh build...
echo Running: gradlew clean --refresh-dependencies --no-daemon
call gradlew clean --refresh-dependencies --no-daemon

echo ================================================================================================
echo Build fix script completed. Check output above for any remaining issues.
echo ================================================================================================
pause