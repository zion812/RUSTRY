@echo off
echo ========================================
echo RUSTRY Build Fix Script
echo ========================================

echo.
echo Step 1: Cleaning project...
call gradlew clean

echo.
echo Step 2: Testing debug build...
call gradlew assembleDebug

echo.
echo Step 3: Checking for Hilt issues...
findstr /r /s "@HiltAndroidApp @AndroidEntryPoint" app\src\main\java\*.kt

echo.
echo Step 4: Attempting release build...
call gradlew assembleRelease --stacktrace

echo.
echo ========================================
echo Build fix script completed
echo Check output above for errors
echo ========================================
pause