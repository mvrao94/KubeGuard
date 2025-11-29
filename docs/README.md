# KubeGuard Documentation

Complete documentation for KubeGuard - Kubernetes Security Scanner

---

## 📚 Documentation

### Getting Started
- **[Main README](../README.md)** - Project overview, installation, and quick start
- **[API Documentation](./API.md)** - Complete REST API reference

### Guides
- **[Observability](./OBSERVABILITY.md)** - Monitoring and metrics
- **[Security Rules](./SECURITY_RULES_REFERENCE.md)** - Security checks reference
- **[Project Structure](./PROJECT_STRUCTURE.md)** - Codebase organization

### Additional Resources
- **[Postman Collection](./POSTMAN_COLLECTION.json)** - Import for API testing
- **[Swagger UI](http://localhost:8080/swagger-ui.html)** - Interactive API docs
- **[OpenAPI Spec](http://localhost:8080/api-docs)** - Machine-readable API spec

---

## 🚀 Quick Links

| Resource | URL | Purpose |
|----------|-----|---------|
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Interactive API testing |
| **Health Check** | http://localhost:8080/actuator/health | Application status |
| **Metrics** | http://localhost:8080/actuator/prometheus | Prometheus metrics |
| **GitHub** | https://github.com/mvrao94/KubeGuard | Source code |

---

## 📖 Quick Start

### 1. Start Application
```bash
mvn spring-boot:run
```

### 2. Test API
```bash
# Health check
curl http://localhost:8080/actuator/health

# Start a scan
curl -X POST http://localhost:8080/api/v1/scan/manifests \
  -H "Content-Type: application/json" \
  -d '{"path": "/path/to/manifests"}'
```

### 3. View Documentation
- Open http://localhost:8080/swagger-ui.html
- Read [API.md](./API.md) for complete reference

---

## 🎯 Use Cases

### CI/CD Integration
See [API.md - CI/CD Integration](./API.md#cicd-integration)

### Security Monitoring
See [OBSERVABILITY.md](./OBSERVABILITY.md)

### Compliance Reporting
See [API.md - Reports & Analytics](./API.md#reports--analytics)

---

## 📞 Support

- **GitHub Issues**: https://github.com/mvrao94/KubeGuard/issues
- **Email**: venkateswararaom07@gmail.com
- **Contributing**: See [CONTRIBUTING.md](../CONTRIBUTING.md)

---

**Version**: 0.0.4-SNAPSHOT  
**Last Updated**: 2025-11-30
