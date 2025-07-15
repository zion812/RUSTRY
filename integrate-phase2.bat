@echo off
echo ========================================
echo RUSTRY Phase 2 Integration Script
echo ========================================
echo.

echo [1/6] Creating backup of current app structure...
if not exist "backup\pre-phase2" mkdir "backup\pre-phase2"
xcopy "app\src" "backup\pre-phase2\src" /E /I /Y > nul 2>&1

echo [2/6] Integrating Phase 2 main source files...
xcopy "generated\phase2\app\src\main" "app\src\main" /E /I /Y > nul 2>&1

echo [3/6] Integrating Phase 2 test files...
xcopy "generated\phase2\app\src\test" "app\src\test" /E /I /Y > nul 2>&1
xcopy "generated\phase2\app\src\androidTest" "app\src\androidTest" /E /I /Y > nul 2>&1

echo [4/6] Setting up Cloud Functions...
if not exist "functions" mkdir "functions"
copy "generated\phase2\functions\enhancedDeleteUserData.js" "functions\" > nul 2>&1

echo [5/6] Setting up CI/CD pipeline...
if not exist ".github\workflows" mkdir ".github\workflows"
copy "generated\phase2\ci\phase2.yml" ".github\workflows\" > nul 2>&1

echo [6/6] Copying documentation...
copy "generated\phase2\docs\phase2.md" "docs\" > nul 2>&1

echo.
echo ========================================
echo ✅ Phase 2 Integration Complete!
echo ========================================
echo.
echo Next Steps:
echo 1. Review integrated files in app/src/
echo 2. Update build.gradle.kts with new dependencies
echo 3. Run: gradlew clean build
echo 4. Run tests: gradlew test
echo 5. Deploy: Use .github/workflows/phase2.yml
echo.
echo 📁 Backup created in: backup/pre-phase2/
echo 📚 Documentation: docs/phase2.md
echo 🚀 CI/CD Pipeline: .github/workflows/phase2.yml
echo.
pause