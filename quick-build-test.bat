@echo off
echo Starting quick build test...
cd /d C:\RUSTRY
echo Running kapt generation...
call gradlew :app:kaptGenerateStubsDebugKotlin --no-daemon --quiet
if %ERRORLEVEL% neq 0 (
    echo KAPT generation failed
    exit /b 1
)
echo KAPT generation successful
echo Running Kotlin compilation...
call gradlew :app:compileDebugKotlin --no-daemon --quiet
if %ERRORLEVEL% neq 0 (
    echo Kotlin compilation failed
    exit /b 1
)
echo Kotlin compilation successful
echo All checks passed!