@echo off
REM Convenience script for building the project on Windows
REM This maintains backward compatibility while using the organized structure

echo Building KubeGuard...
call build-tools\mvnw.cmd clean package %*