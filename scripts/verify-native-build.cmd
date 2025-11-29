@echo off
REM Verification script for Native Image build on Windows
REM This script proves that Java can achieve Go-like performance

echo.
echo 🚀 KubeGuard Native Image Build Verification
echo ==============================================
echo.

REM Check prerequisites
echo 📋 Checking Prerequisites...

where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Java not found
    exit /b 1
)

java -version 2>&1 | findstr /C:"GraalVM" >nul
if %ERRORLEVEL% NEQ 0 (
    echo ⚠️  Warning: Not using GraalVM. Native Image build may fail.
    echo    Install GraalVM: sdk install java 21.0.1-graal
)

where native-image >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ native-image tool not found
    echo    Install: gu install native-image
    exit /b 1
)

echo ✅ Prerequisites check passed
echo.

REM Generate API key
echo 🔐 Generating API Key...
for /f %%i in ('powershell -Command "[System.BitConverter]::ToString([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32)) -replace '-'"') do set KUBEGUARD_API_KEY=%%i
echo ✅ API Key generated
echo.

REM Build JVM version
echo 🏗️  Building JVM Version...
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ❌ JVM build failed
    exit /b 1
)
echo ✅ JVM build completed
echo.

REM Build Native Image
echo 🚀 Building Native Image (this takes 5-10 minutes)...
call mvnw.cmd -Pnative native:compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Native Image build failed
    exit /b 1
)
echo ✅ Native Image build completed
echo.

echo 📊 Build completed successfully!
echo.
echo Next steps:
echo 1. Test startup: target\kubeguard.exe
echo 2. Build Docker: docker build -f Dockerfile.native -t kubeguard:native .
echo 3. Update PERFORMANCE.md with actual measurements
echo.

exit /b 0
