@echo off
echo Testing minimal Gradle configuration...

echo Setting minimal environment...
set GRADLE_OPTS=-Dorg.gradle.daemon=false -Dorg.gradle.caching=false -Dorg.gradle.parallel=false

echo Testing basic Gradle functionality...
timeout 30 gradlew.bat --version --no-daemon --console=plain

echo.
echo Testing project configuration...
timeout 60 gradlew.bat projects --no-daemon --console=plain

echo.
echo Test completed.
pause