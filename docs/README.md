# KubeGuard Documentation

Complete documentation for KubeGuard - Kubernetes Security Scanner

---

## 📚 Documentation

### Core Documentation
| Document | Description |
|----------|-------------|
| **[Main README](../README.md)** | Project overview, installation, quick start |
| **[API Reference](./API.md)** | Complete REST API documentation |
| **[Rule Engine](./RULE_ENGINE_ARCHITECTURE.md)** | Extensible rule engine with 50+ rules |
| **[Security Integrations](./SECURITY_INTEGRATIONS.md)** | NIST NVD & MITRE ATT&CK integration |
| **[Observability](./OBSERVABILITY.md)** | Monitoring, metrics, and health checks |
| **[Security Rules](./SECURITY_RULES_REFERENCE.md)** | Complete security rules reference |
| **[Project Structure](./PROJECT_STRUCTURE.md)** | Codebase organization |

### Tools & Resources
| Resource | Description |
|----------|-------------|
| **[Postman Collection](./POSTMAN_COLLECTION.json)** | Import for API testing |
| **[Swagger UI](http://localhost:8080/swagger-ui.html)** | Interactive API documentation |
| **[OpenAPI Spec](http://localhost:8080/api-docs)** | Machine-readable API specification |

---

## 🚀 Quick Start

```bash
# 1. Start application
mvn spring-boot:run

# 2. Test API
curl http://localhost:8080/actuator/health

# 3. View documentation
open http://localhost:8080/swagger-ui.html
```

---

## 🎯 By Use Case

| I want to... | Read this |
|--------------|-----------|
| **Use the API** | [API.md](./API.md) |
| **Understand the rule engine** | [RULE_ENGINE_ARCHITECTURE.md](./RULE_ENGINE_ARCHITECTURE.md) |
| **Integrate with NVD/MITRE** | [SECURITY_INTEGRATIONS.md](./SECURITY_INTEGRATIONS.md) |
| **Monitor the application** | [OBSERVABILITY.md](./OBSERVABILITY.md) |
| **Understand security rules** | [SECURITY_RULES_REFERENCE.md](./SECURITY_RULES_REFERENCE.md) |
| **Contribute code** | [../CONTRIBUTING.md](../CONTRIBUTING.md) |
| **Report security issue** | [../SECURITY.md](../SECURITY.md) |

---

## 📊 Key Features

- **50+ Security Rules** mapped to CIS, NSA/CISA, MITRE ATT&CK, OWASP
- **NIST NVD Integration** for CVE vulnerability data
- **MITRE ATT&CK Integration** with 20+ Kubernetes attack techniques
- **Extensible Architecture** supporting hundreds of rules
- **REST API** with Swagger/OpenAPI documentation
- **Prometheus Metrics** for monitoring

---

## 📞 Support

- **GitHub**: https://github.com/mvrao94/KubeGuard
- **Issues**: https://github.com/mvrao94/KubeGuard/issues
- **Email**: venkateswararaom07@gmail.com

---

**Version**: 0.0.4-SNAPSHOT  
**Last Updated**: 2025-11-30
