@echo off
REM Docker build script for Windows

setlocal enabledelayedexpansion

REM Default values
set MODE=local
set PUSH=false
set PLATFORM=linux/amd64
set TAG=latest
set REGISTRY=docker.io

REM Parse arguments
:parse_args
if "%~1"=="" goto end_parse
if "%~1"=="--mode" (
    set MODE=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="--tag" (
    set TAG=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="--push" (
    set PUSH=true
    shift
    goto parse_args
)
if "%~1"=="--multi-arch" (
    set PLATFORM=linux/amd64,linux/arm64
    shift
    goto parse_args
)
if "%~1"=="--registry" (
    set REGISTRY=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="--help" (
    echo Usage: %~nx0 [OPTIONS]
    echo.
    echo Options:
    echo   --mode MODE        Build mode: local (default) or ci
    echo   --tag TAG          Image tag (default: latest)
    echo   --push             Push image to registry
    echo   --multi-arch       Build for multiple architectures
    echo   --registry REG     Registry URL (default: docker.io)
    echo   --help             Show this help message
    echo.
    echo Examples:
    echo   %~nx0 --mode local --tag dev
    echo   %~nx0 --mode ci --tag 1.0.0 --push --multi-arch
    exit /b 0
)
shift
goto parse_args

:end_parse

REM Get version from pom.xml
for /f "tokens=*" %%i in ('mvn help:evaluate -Dexpression^=project.version -q -DforceStdout 2^>nul') do set VERSION=%%i
if "%VERSION%"=="" set VERSION=unknown

REM Get git commit
for /f "tokens=*" %%i in ('git rev-parse --short HEAD 2^>nul') do set VCS_REF=%%i
if "%VCS_REF%"=="" set VCS_REF=unknown

REM Get current date in ISO format
for /f "tokens=*" %%i in ('powershell -Command "Get-Date -Format 'yyyy-MM-ddTHH:mm:ssZ' -AsUTC"') do set BUILD_DATE=%%i

set IMAGE_NAME=%REGISTRY%/kubeguard
set FULL_TAG=%IMAGE_NAME%:%TAG%

echo Building KubeGuard Docker Image
echo ================================
echo Mode:         %MODE%
echo Version:      %VERSION%
echo Tag:          %TAG%
echo Platform:     %PLATFORM%
echo Push:         %PUSH%
echo Build Date:   %BUILD_DATE%
echo VCS Ref:      %VCS_REF%
echo.

REM Change to repo root
cd /d "%~dp0\.."

if "%MODE%"=="ci" (
    echo Building in CI mode (using pre-built JAR)...
    
    if not exist "target\kubeguard-*.jar" (
        echo Error: JAR file not found in target\. Build with Maven first.
        exit /b 1
    )
    
    set BUILD_ARGS=--build-arg SKIP_BUILD=true
) else (
    echo Building in local mode (building from source)...
    set BUILD_ARGS=--build-arg SKIP_BUILD=false
)

set PUSH_ARG=--load
if "%PUSH%"=="true" set PUSH_ARG=--push

docker buildx build ^
    --platform %PLATFORM% ^
    %BUILD_ARGS% ^
    --build-arg APP_VERSION=%VERSION% ^
    --build-arg BUILD_DATE=%BUILD_DATE% ^
    --build-arg VCS_REF=%VCS_REF% ^
    --tag %FULL_TAG% ^
    --file scripts/Dockerfile ^
    %PUSH_ARG% ^
    .

if %ERRORLEVEL% equ 0 (
    echo.
    echo Build successful!
    echo.
    echo Image: %FULL_TAG%
    echo.
    echo Run with:
    echo   docker run -p 8080:8080 %FULL_TAG%
    echo.
    echo Or use docker-compose:
    echo   docker-compose -f scripts/docker-compose.yml up
) else (
    echo Build failed
    exit /b 1
)
