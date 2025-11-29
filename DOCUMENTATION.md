# KubeGuard Documentation Guide

## 📁 Clean Documentation Structure

```
KubeGuard/
├── README.md                          # Main project overview
├── CONTRIBUTING.md                    # How to contribute
├── CODE_OF_CONDUCT.md                # Community guidelines
├── SECURITY.md                        # Security policy
├── DOCUMENTATION.md                   # This file
│
└── docs/
    ├── README.md                      # Documentation index
    ├── API.md                         # REST API reference
    ├── RULE_ENGINE_ARCHITECTURE.md    # Rule engine design
    ├── SECURITY_INTEGRATIONS.md       # NVD & MITRE integration
    ├── OBSERVABILITY.md               # Monitoring & metrics
    ├── SECURITY_RULES_REFERENCE.md    # Security rules catalog
    ├── PROJECT_STRUCTURE.md           # Code organization
    └── POSTMAN_COLLECTION.json        # API testing collection
```

---

## 📚 Documentation Files

### Root Level (4 files)

| File | Purpose | Audience |
|------|---------|----------|
| **README.md** | Project overview, quick start, features | Everyone |
| **CONTRIBUTING.md** | Contribution guidelines | Contributors |
| **CODE_OF_CONDUCT.md** | Community standards | Everyone |
| **SECURITY.md** | Security policy, vulnerability reporting | Security researchers |

### docs/ Directory (8 files)

| File | Purpose | Audience |
|------|---------|----------|
| **README.md** | Documentation index and navigation | Everyone |
| **API.md** | Complete REST API reference | Developers, DevOps |
| **RULE_ENGINE_ARCHITECTURE.md** | Rule engine design and extensibility | Developers, Architects |
| **SECURITY_INTEGRATIONS.md** | NIST NVD & MITRE ATT&CK integration | Security teams, Developers |
| **OBSERVABILITY.md** | Monitoring, metrics, health checks | DevOps, SRE |
| **SECURITY_RULES_REFERENCE.md** | Complete security rules catalog | Security teams |
| **PROJECT_STRUCTURE.md** | Codebase organization | Contributors |
| **POSTMAN_COLLECTION.json** | API testing collection | Developers, QA |

---

## 🎯 Quick Navigation

### I want to...

#### Get Started
→ [README.md](README.md)

#### Use the API
→ [docs/API.md](docs/API.md)  
→ http://localhost:8080/swagger-ui.html

#### Understand the Rule Engine
→ [docs/RULE_ENGINE_ARCHITECTURE.md](docs/RULE_ENGINE_ARCHITECTURE.md)

#### Integrate with Security Frameworks
→ [docs/SECURITY_INTEGRATIONS.md](docs/SECURITY_INTEGRATIONS.md)

#### Monitor the Application
→ [docs/OBSERVABILITY.md](docs/OBSERVABILITY.md)

#### Understand Security Rules
→ [docs/SECURITY_RULES_REFERENCE.md](docs/SECURITY_RULES_REFERENCE.md)

#### Contribute Code
→ [CONTRIBUTING.md](CONTRIBUTING.md)  
→ [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md)

#### Report Security Issue
→ [SECURITY.md](SECURITY.md)

---

## 📊 Documentation Statistics

```
Total Files: 12
├── Root: 4 files
└── docs/: 8 files

Total Pages: ~100 pages
Total Words: ~30,000 words
Code Examples: 50+
API Endpoints: 8 documented
```

---

## 🔗 External Links

### Application
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus

### Repository
- **GitHub**: https://github.com/mvrao94/KubeGuard
- **Issues**: https://github.com/mvrao94/KubeGuard/issues
- **Docker Hub**: https://hub.docker.com/r/mvrao94/kubeguard

### Standards & References
- **CIS Benchmarks**: https://www.cisecurity.org/benchmark/kubernetes
- **NSA/CISA Guide**: https://media.defense.gov/2022/Aug/29/2003066362/-1/-1/0/CTR_KUBERNETES_HARDENING_GUIDANCE_1.2_20220829.PDF
- **MITRE ATT&CK**: https://attack.mitre.org/matrices/enterprise/containers/
- **NIST NVD**: https://nvd.nist.gov/
- **OWASP K8s Top 10**: https://owasp.org/www-project-kubernetes-top-ten/

---

## ✨ What's Documented

### Features
- ✅ 50+ security rules with compliance mappings
- ✅ NIST NVD integration for CVE data
- ✅ MITRE ATT&CK integration (20+ techniques)
- ✅ Extensible rule engine architecture
- ✅ REST API with 8 endpoints
- ✅ Prometheus metrics and monitoring
- ✅ CI/CD integration examples
- ✅ Postman collection for testing

### Architecture
- ✅ Rule engine design patterns
- ✅ Integration architecture
- ✅ Performance characteristics
- ✅ Scalability analysis
- ✅ Security considerations

### Operations
- ✅ Installation and setup
- ✅ Configuration options
- ✅ Monitoring and observability
- ✅ Troubleshooting guides
- ✅ Best practices

---

## 📝 Documentation Standards

### File Naming
- Use descriptive names
- Use UPPERCASE for root-level docs
- Use Title Case for docs/ directory
- Keep names concise

### Content Structure
- Start with clear purpose
- Include table of contents for long docs
- Use examples liberally
- Keep language clear and concise
- Link between related docs

### Maintenance
- Update when features change
- Keep examples current
- Review quarterly
- Validate links
- Update version numbers

---

## 🎉 Clean Structure Benefits

### Before Cleanup
- 15+ scattered documentation files
- Redundant content
- Confusing navigation
- Multiple summaries

### After Cleanup
- 12 focused documentation files
- Single source of truth
- Clear navigation
- No redundancy

---

## 📞 Support

### Documentation Issues
- Check [docs/README.md](docs/README.md) for navigation
- Browse Swagger UI for API help
- Search this guide for keywords

### Technical Support
- **GitHub Issues**: https://github.com/mvrao94/KubeGuard/issues
- **Email**: venkateswararaom07@gmail.com

---

**Documentation Version**: 2.0.0  
**Last Updated**: 2025-11-30  
**Status**: ✅ Clean and Organized
