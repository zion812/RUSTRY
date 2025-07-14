@echo off
echo ========================================
echo RUSTRY Simple Deployment Script
echo ========================================

echo.
echo Step 1: Building debug APK...
call gradlew assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Step 2: APK built successfully!
echo Location: app\build\outputs\apk\debug\app-debug.apk

echo.
echo Step 3: Firebase App Distribution (Manual)
echo Please run this command with your Firebase project details:
echo.
echo firebase appdistribution:distribute app\build\outputs\apk\debug\app-debug.apk ^
echo   --app YOUR_FIREBASE_APP_ID ^
echo   --groups "beta-farmers" ^
echo   --release-notes "RUSTRY v1.0 - Simple marketplace demo"

echo.
echo Step 4: Alternative - Share APK directly
echo You can share the APK file directly with farmers for testing
echo File size: ~8-12 MB

echo.
echo ========================================
echo Deployment Ready!
echo ========================================
echo.
echo Next steps:
echo 1. Configure Firebase App Distribution
echo 2. Upload APK to Firebase Console
echo 3. Invite 5 farmers to test
echo 4. Collect feedback for next iteration
echo.
pause