@echo off
REM Convenience script for running the full stack locally on Windows

echo Starting KubeGuard with all dependencies...
docker-compose -f scripts/docker-compose.yml up -d

echo Waiting for services to start...
timeout /t 10 /nobreak > nul

echo Services started! Access points:
echo - KubeGuard API: http://localhost:8080
echo - Swagger UI: http://localhost:8080/swagger-ui.html
echo - Prometheus: http://localhost:9090
echo - Grafana: http://localhost:3000 (admin/admin)

echo.
echo To view logs: docker-compose -f scripts/docker-compose.yml logs -f
echo To stop: docker-compose -f scripts/docker-compose.yml down