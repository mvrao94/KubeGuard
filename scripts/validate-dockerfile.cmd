@echo off
REM Validate Dockerfile syntax without building

echo Validating Dockerfile syntax...

REM Check if docker is available
docker --version >nul 2>&1
if errorlevel 1 (
    echo Error: Docker is not installed or not in PATH
    exit /b 1
)

REM Check if Dockerfile exists
if not exist "scripts\Dockerfile" (
    echo Error: Dockerfile not found at scripts\Dockerfile
    exit /b 1
)

echo Checking basic Dockerfile syntax...

REM Check for common issues using findstr
findstr /C:"COPY.*2>/dev/null" scripts\Dockerfile >nul 2>&1
if not errorlevel 1 (
    echo Error: Found shell redirection in COPY command (not supported)
    exit /b 1
)

findstr /C:"COPY.*||" scripts\Dockerfile >nul 2>&1
if not errorlevel 1 (
    echo Error: Found shell operator in COPY command (not supported)
    exit /b 1
)

REM Check for required stages
findstr /C:"FROM.*AS builder" scripts\Dockerfile >nul 2>&1
if errorlevel 1 (
    echo Error: Builder stage not found
    exit /b 1
)

findstr /C:"FROM.*AS runtime" scripts\Dockerfile >nul 2>&1
if errorlevel 1 (
    echo Error: Runtime stage not found
    exit /b 1
)

REM Check for required instructions
findstr /C:"WORKDIR" scripts\Dockerfile >nul 2>&1
if errorlevel 1 (
    echo Error: WORKDIR instruction not found
    exit /b 1
)

findstr /C:"EXPOSE" scripts\Dockerfile >nul 2>&1
if errorlevel 1 (
    echo Error: EXPOSE instruction not found
    exit /b 1
)

findstr /C:"HEALTHCHECK" scripts\Dockerfile >nul 2>&1
if errorlevel 1 (
    echo Warning: HEALTHCHECK instruction not found
)

echo.
echo Dockerfile validation passed!
echo.
echo To build the image:
echo   Local mode:  scripts\build-docker.cmd
echo   CI mode:     mvn clean package ^&^& scripts\build-docker.cmd --mode ci
