@echo off
echo Testing Gradle with minimal configuration...

echo Setting environment variables...
set GRADLE_OPTS=-Dorg.gradle.daemon=false -Dorg.gradle.caching=false -Dorg.gradle.parallel=false -Dorg.gradle.configureondemand=false
set JAVA_OPTS=-Xmx2g

echo Testing Gradle version...
gradlew.bat --version --no-daemon --console=plain

echo.
echo Testing basic help command...
timeout 60 gradlew.bat help --no-daemon --console=plain

echo.
echo Test completed.
pause