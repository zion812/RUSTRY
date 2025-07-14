@echo off
echo ========================================
echo RUSTRY Firebase Deployment Script
echo ========================================

echo.
echo Step 1: Deploying Firestore rules...
firebase deploy --only firestore:rules

echo.
echo Step 2: Deploying Storage rules...
firebase deploy --only storage

echo.
echo Step 3: Deploying Privacy Policy to Hosting...
firebase deploy --only hosting

echo.
echo Step 4: Setting up Remote Config...
echo Please manually configure Remote Config in Firebase Console:
echo - min_version_code: 3
echo - force_update_enabled: false
echo - maintenance_mode: false

echo.
echo Step 5: App Distribution setup...
echo Please run this command after successful APK build:
echo firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk --app YOUR_APP_ID --groups "rusty-closed"

echo.
echo ========================================
echo Firebase deployment completed
echo Next: Build APK and distribute to testers
echo ========================================
pause