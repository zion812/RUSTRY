@echo off
echo Testing Phase 3 Build...
echo.

echo Step 1: Clean build
call gradlew clean
if %ERRORLEVEL% neq 0 (
    echo Clean failed!
    exit /b 1
)

echo.
echo Step 2: Compile Kotlin
call gradlew compileDebugKotlin --no-daemon
if %ERRORLEVEL% neq 0 (
    echo Kotlin compilation failed!
    exit /b 1
)

echo.
echo Step 3: Run unit tests
call gradlew testDebugUnitTest --no-daemon
if %ERRORLEVEL% neq 0 (
    echo Unit tests failed!
    exit /b 1
)

echo.
echo Phase 3 build test completed successfully!
echo.
echo Next steps:
echo 1. Run: git push origin master
echo 2. GitHub Actions will automatically run the CI/CD pipeline
echo 3. Check the Actions tab for build status
echo 4. Use Fastlane for Play Store deployment when ready
echo.
pause