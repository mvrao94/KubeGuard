# KubeGuard API Documentation

Complete REST API reference for KubeGuard - Kubernetes Security Scanner

**Base URL**: `http://localhost:8080`  
**Version**: 0.0.4-SNAPSHOT  
**Interactive Docs**: http://localhost:8080/swagger-ui.html

---

## Quick Start

```bash
# Start application
mvn spring-boot:run

# Test connection
curl http://localhost:8080/actuator/health

# Start a scan
curl -X POST http://localhost:8080/api/v1/scan/manifests \
  -H "Content-Type: application/json" \
  -d '{"path": "/path/to/manifests"}'
```

---

## Endpoints

### Security Scanning

#### 1. Scan Manifest Files

Scan Kubernetes YAML/YML files for security issues.

```http
POST /api/v1/scan/manifests
Content-Type: application/json

{
  "path": "/path/to/manifests",
  "description": "Optional description"
}
```

**Response (202 Accepted)**:
```json
{
  "scanId": "123e4567-e89b-12d3-a456-426614174000",
  "message": "Manifest scan started successfully",
  "status": "RUNNING"
}
```

#### 2. Scan Live Cluster

Scan resources in a Kubernetes namespace.

```http
GET /api/v1/scan/cluster/{namespace}
```

**Example**:
```bash
curl http://localhost:8080/api/v1/scan/cluster/production
```

#### 3. Get Scan Status

Check scan progress and results.

```http
GET /api/v1/scan/status/{scanId}
```

**Response (200 OK)**:
```json
{
  "id": 1,
  "scanId": "123e4567-e89b-12d3-a456-426614174000",
  "status": "COMPLETED",
  "totalResources": 25,
  "criticalIssues": 3,
  "highIssues": 8,
  "mediumIssues": 15,
  "lowIssues": 22
}
```

### Reports & Analytics

#### 4. List All Reports

Get paginated scan reports.

```http
GET /api/v1/reports?page=0&size=10&sortBy=timestamp&sortDir=desc
```

**Parameters**:
- `page` (int, default: 0) - Page number
- `size` (int, default: 10) - Items per page
- `sortBy` (string, default: timestamp) - Sort field
- `sortDir` (string, default: desc) - Sort direction

#### 5. Get High Priority Reports

Get reports with critical/high severity findings.

```http
GET /api/v1/reports/high-priority
```

#### 6. Get Report Findings

Get detailed findings for a specific report.

```http
GET /api/v1/reports/{reportId}/findings?page=0&size=20
```

**Response**:
```json
{
  "content": [
    {
      "resourceName": "nginx-deployment",
      "resourceType": "Deployment",
      "ruleId": "KSV001",
      "title": "Container running as root",
      "severity": "HIGH",
      "remediation": "Set securityContext.runAsNonRoot to true"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 48,
  "totalPages": 3
}
```

#### 7. Get Top Failing Rules

Get most frequently violated security rules.

```http
GET /api/v1/reports/analytics/top-failing-rules
```

**Response**:
```json
{
  "KSV001": 145,
  "KSV003": 98,
  "KSV012": 87
}
```

#### 8. Get Security Metrics

Get aggregated security statistics.

```http
GET /api/v1/reports/analytics/summary
```

**Response**:
```json
{
  "totalReports": 150,
  "completedReports": 142,
  "failedReports": 5,
  "runningReports": 3,
  "totalCriticalFindings": 23,
  "totalHighFindings": 67
}
```

---

## Data Models

### ScanRequest
```json
{
  "path": "string (required, 1-500 chars)",
  "description": "string (optional)"
}
```

### ScanResponse
```json
{
  "scanId": "string (UUID)",
  "message": "string",
  "status": "RUNNING | COMPLETED | FAILED"
}
```

### ScanReport
```json
{
  "id": "long",
  "scanId": "string (UUID)",
  "scanType": "MANIFEST | CLUSTER",
  "target": "string",
  "timestamp": "string (ISO 8601)",
  "status": "RUNNING | COMPLETED | FAILED",
  "totalResources": "integer",
  "criticalIssues": "integer",
  "highIssues": "integer",
  "mediumIssues": "integer",
  "lowIssues": "integer"
}
```

### SecurityFinding
```json
{
  "id": "long",
  "resourceName": "string",
  "resourceType": "string",
  "namespace": "string",
  "ruleId": "string",
  "title": "string",
  "severity": "CRITICAL | HIGH | MEDIUM | LOW | INFO",
  "description": "string",
  "remediation": "string",
  "location": "string"
}
```

---

## Common Workflows

### Complete Scan Workflow

```bash
#!/bin/bash

# 1. Start scan
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/scan/manifests \
  -H "Content-Type: application/json" \
  -d '{"path": "/path/to/manifests"}')

SCAN_ID=$(echo $RESPONSE | jq -r '.scanId')
echo "Scan started: $SCAN_ID"

# 2. Wait for completion
while true; do
  STATUS=$(curl -s http://localhost:8080/api/v1/scan/status/$SCAN_ID | jq -r '.status')
  echo "Status: $STATUS"
  [ "$STATUS" = "COMPLETED" ] && break
  sleep 5
done

# 3. Get results
REPORT=$(curl -s http://localhost:8080/api/v1/scan/status/$SCAN_ID)
echo "Critical: $(echo $REPORT | jq -r '.criticalIssues')"
echo "High: $(echo $REPORT | jq -r '.highIssues')"

# 4. Get findings
REPORT_ID=$(echo $REPORT | jq -r '.id')
curl -s "http://localhost:8080/api/v1/reports/$REPORT_ID/findings" | jq
```

### Python Client

```python
import requests
import time

class KubeGuardClient:
    def __init__(self, base_url="http://localhost:8080"):
        self.base_url = base_url
    
    def scan_manifests(self, path):
        response = requests.post(
            f"{self.base_url}/api/v1/scan/manifests",
            json={"path": path}
        )
        return response.json()
    
    def wait_for_scan(self, scan_id, timeout=300):
        start = time.time()
        while time.time() - start < timeout:
            result = requests.get(
                f"{self.base_url}/api/v1/scan/status/{scan_id}"
            ).json()
            if result['status'] in ['COMPLETED', 'FAILED']:
                return result
            time.sleep(5)
        raise TimeoutError("Scan timeout")
    
    def get_findings(self, report_id):
        response = requests.get(
            f"{self.base_url}/api/v1/reports/{report_id}/findings",
            params={"size": 100}
        )
        return response.json()

# Usage
client = KubeGuardClient()
scan = client.scan_manifests("/path/to/manifests")
result = client.wait_for_scan(scan['scanId'])
print(f"Found {result['criticalIssues']} critical issues")
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Action |
|------|---------|--------|
| 200 | OK | Success |
| 202 | Accepted | Scan started |
| 400 | Bad Request | Fix request parameters |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Retry or check logs |

### Error Response

```json
{
  "scanId": null,
  "message": "Error description",
  "status": "FAILED"
}
```

---

## Best Practices

### Polling
- Start with 2-5 second intervals
- Use exponential backoff
- Set reasonable timeout (5-10 minutes)

### Pagination
- Use appropriate page sizes (10-50)
- Check `hasNext` before requesting next page
- Sort by priority for critical issues

### Error Handling
- Always check HTTP status codes
- Implement retry logic for 500 errors
- Don't retry 400 errors

---

## Tools & Integration

### Postman Collection

Import `docs/POSTMAN_COLLECTION.json` for interactive testing.

### OpenAPI Spec

Download from: http://localhost:8080/api-docs

Generate clients:
```bash
openapi-generator-cli generate \
  -i http://localhost:8080/api-docs \
  -g python \
  -o ./client
```

### CI/CD Integration

```yaml
# GitHub Actions example
- name: Security Scan
  run: |
    SCAN=$(curl -X POST http://kubeguard:8080/api/v1/scan/manifests \
      -H "Content-Type: application/json" \
      -d '{"path": "./k8s"}')
    
    SCAN_ID=$(echo $SCAN | jq -r '.scanId')
    
    # Wait for completion
    while true; do
      RESULT=$(curl -s http://kubeguard:8080/api/v1/scan/status/$SCAN_ID)
      STATUS=$(echo $RESULT | jq -r '.status')
      [ "$STATUS" = "COMPLETED" ] && break
      sleep 5
    done
    
    # Fail if critical issues found
    CRITICAL=$(echo $RESULT | jq -r '.criticalIssues')
    if [ "$CRITICAL" -gt 0 ]; then
      echo "Found $CRITICAL critical issues!"
      exit 1
    fi
```

---

## Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus
- **GitHub**: https://github.com/mvrao94/KubeGuard

---

## Support

- **GitHub Issues**: https://github.com/mvrao94/KubeGuard/issues
- **Email**: venkateswararaom07@gmail.com
