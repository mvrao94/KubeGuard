# SBOM and Provenance Documentation

## Overview

KubeGuard Docker images include both **SBOM (Software Bill of Materials)** and **Provenance** attestations for enhanced security and transparency.

---

## What Are These?

### 🔍 SBOM (Software Bill of Materials)
A complete inventory of all software components, libraries, and dependencies in the Docker image.

**Benefits:**
- Identify vulnerable components
- Track software supply chain
- Comply with security requirements
- Enable vulnerability scanning

### 📜 Provenance (SLSA Attestation)
A signed record of how the image was built, including:
- Source repository and commit
- Build platform and environment
- Build parameters and configuration
- Timestamp and builder identity

**Benefits:**
- Verify image authenticity
- Prevent supply chain attacks
- Ensure reproducible builds
- Meet compliance requirements

---

## How It Works

### In CI/CD Pipeline

Our GitHub Actions workflow automatically generates and attaches both attestations:

```yaml
- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    provenance: mode=max    # Generate SLSA provenance
    sbom: true              # Generate SBOM
```

**What happens:**
1. Image is built from source
2. SBOM is generated (lists all components)
3. Provenance is generated (records build details)
4. Both are cryptographically signed
5. Attestations are attached to the image
6. Image + attestations pushed to Docker Hub

---

## Viewing Attestations

### Using Docker CLI

#### View Image Attestations
```bash
# Inspect image with attestations
docker buildx imagetools inspect mvrao94/kubeguard:latest

# View SBOM
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .SBOM }}'

# View Provenance
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .Provenance }}'
```

#### View Detailed SBOM
```bash
# Get SBOM in SPDX format
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .SBOM }}' | jq -r '.SPDX'

# List all packages
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .SBOM }}' | jq -r '.SPDX.packages[].name'
```

#### View Detailed Provenance
```bash
# Get full provenance
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .Provenance }}' | jq

# View build source
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .Provenance }}' | jq -r '.predicate.materials'

# View builder info
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .Provenance }}' | jq -r '.predicate.builder'
```

### Using Docker Scout (Recommended)

```bash
# Analyze image with Scout
docker scout cves mvrao94/kubeguard:latest

# View SBOM with Scout
docker scout sbom mvrao94/kubeguard:latest

# Compare with base image
docker scout compare --to mvrao94/kubeguard:latest eclipse-temurin:25-jre-alpine
```

### Using Cosign (for verification)

```bash
# Install cosign
# https://docs.sigstore.dev/cosign/installation/

# Verify attestations
cosign verify-attestation mvrao94/kubeguard:latest

# Download and view SBOM
cosign download sbom mvrao94/kubeguard:latest

# Download and view provenance
cosign download attestation mvrao94/kubeguard:latest
```

---

## What's Included in SBOM

### Package Information
- Package name and version
- License information
- Package source and origin
- Dependencies and relationships

### Example SBOM Content
```json
{
  "SPDX": {
    "spdxVersion": "SPDX-2.3",
    "packages": [
      {
        "name": "eclipse-temurin",
        "versionInfo": "25-jre-alpine",
        "licenseConcluded": "GPL-2.0-with-classpath-exception"
      },
      {
        "name": "kubeguard",
        "versionInfo": "1.0.0",
        "licenseConcluded": "MIT"
      },
      {
        "name": "spring-boot-starter-web",
        "versionInfo": "3.x.x",
        "licenseConcluded": "Apache-2.0"
      }
      // ... all dependencies
    ]
  }
}
```

---

## What's Included in Provenance

### Build Information
- **Source**: Git repository URL and commit SHA
- **Builder**: GitHub Actions runner details
- **Build Time**: Timestamp of build
- **Build Args**: All build arguments used
- **Materials**: Input files and dependencies

### Example Provenance Content
```json
{
  "predicate": {
    "builder": {
      "id": "https://github.com/mvrao94/KubeGuard/actions/runs/..."
    },
    "buildType": "https://slsa.dev/provenance/v1",
    "invocation": {
      "configSource": {
        "uri": "git+https://github.com/mvrao94/KubeGuard",
        "digest": {
          "sha1": "abc123..."
        }
      }
    },
    "metadata": {
      "buildStartedOn": "2024-01-01T00:00:00Z",
      "buildFinishedOn": "2024-01-01T00:05:00Z"
    },
    "materials": [
      {
        "uri": "git+https://github.com/mvrao94/KubeGuard",
        "digest": {
          "sha1": "abc123..."
        }
      }
    ]
  }
}
```

---

## Security Benefits

### Supply Chain Security
✅ **Verify Image Origin**: Confirm image was built from official source
✅ **Detect Tampering**: Cryptographic signatures prevent modification
✅ **Track Dependencies**: Know exactly what's in your image
✅ **Vulnerability Management**: Quickly identify affected components

### Compliance
✅ **SLSA Level 3**: Meets SLSA (Supply-chain Levels for Software Artifacts) requirements
✅ **SBOM Standards**: Complies with SPDX and CycloneDX formats
✅ **Audit Trail**: Complete record of build process
✅ **Regulatory Compliance**: Meets NIST, EO 14028, and other requirements

---

## Verification Workflow

### 1. Pull Image
```bash
docker pull mvrao94/kubeguard:latest
```

### 2. Verify Attestations Exist
```bash
docker buildx imagetools inspect mvrao94/kubeguard:latest
```

### 3. Check SBOM for Vulnerabilities
```bash
# Using Docker Scout
docker scout cves mvrao94/kubeguard:latest

# Using Grype
grype mvrao94/kubeguard:latest

# Using Trivy
trivy image mvrao94/kubeguard:latest
```

### 4. Verify Provenance
```bash
# Check build source matches expected repo
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .Provenance }}' | \
  jq -r '.predicate.materials[0].uri'

# Should output: git+https://github.com/mvrao94/KubeGuard
```

### 5. Verify Build Environment
```bash
# Check builder is GitHub Actions
docker buildx imagetools inspect mvrao94/kubeguard:latest \
  --format '{{ json .Provenance }}' | \
  jq -r '.predicate.builder.id'

# Should contain: github.com/mvrao94/KubeGuard/actions
```

---

## Integration with Security Tools

### Vulnerability Scanners

#### Docker Scout
```bash
# Enable Docker Scout
docker scout enroll

# Scan image
docker scout cves mvrao94/kubeguard:latest

# Get recommendations
docker scout recommendations mvrao94/kubeguard:latest
```

#### Trivy
```bash
# Scan with SBOM
trivy image --sbom mvrao94/kubeguard:latest

# Generate report
trivy image --format json mvrao94/kubeguard:latest > scan-results.json
```

#### Grype
```bash
# Scan using SBOM
grype sbom:./sbom.json

# Scan image directly
grype mvrao94/kubeguard:latest
```

### Policy Enforcement

#### OPA (Open Policy Agent)
```bash
# Verify image meets policy requirements
opa eval --data policy.rego --input provenance.json "data.policy.allow"
```

#### Kyverno (Kubernetes)
```yaml
apiVersion: kyverno.io/v1
kind: ClusterPolicy
metadata:
  name: verify-image-attestations
spec:
  validationFailureAction: enforce
  rules:
  - name: verify-sbom
    match:
      resources:
        kinds:
        - Pod
    verifyImages:
    - imageReferences:
      - "mvrao94/kubeguard:*"
      attestations:
      - type: sbom
        conditions:
        - all:
          - key: "{{ packages }}"
            operator: NotEquals
            value: []
```

---

## CI/CD Configuration

### Current Setup

Our `.github/workflows/ci.yml` includes:

```yaml
- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    provenance: mode=max  # Maximum provenance detail
    sbom: true            # Generate SBOM
    outputs: type=image,name=target,annotation-index.org.opencontainers.image.description=...
```

### What Gets Generated

1. **SBOM Attestation**
   - Format: SPDX 2.3
   - Includes: All packages, licenses, dependencies
   - Signed: Yes (using GitHub's signing key)

2. **Provenance Attestation**
   - Format: SLSA Provenance v1
   - Level: SLSA Level 3
   - Includes: Source, builder, materials, metadata
   - Signed: Yes (using GitHub's signing key)

---

## Best Practices

### For Developers

✅ **Always verify attestations** before deploying
✅ **Scan SBOM regularly** for new vulnerabilities
✅ **Check provenance** matches expected source
✅ **Use specific tags** (not just `latest`)
✅ **Automate verification** in CI/CD

### For Security Teams

✅ **Enforce attestation policies** in Kubernetes
✅ **Monitor SBOM** for license compliance
✅ **Track provenance** for audit trails
✅ **Integrate with security tools** (Scout, Trivy, etc.)
✅ **Regular vulnerability scanning** using SBOM

### For Operations

✅ **Verify images** before deployment
✅ **Use admission controllers** to enforce policies
✅ **Monitor for vulnerabilities** in production
✅ **Maintain audit logs** of image deployments
✅ **Automate compliance checks**

---

## Troubleshooting

### Attestations Not Found

**Problem**: `docker buildx imagetools inspect` shows no attestations

**Solutions:**
1. Ensure image was built with BuildKit
2. Check Docker version (need v20.10+)
3. Verify buildx is installed: `docker buildx version`
4. Pull image with attestations: `docker pull --platform linux/amd64 mvrao94/kubeguard:latest`

### Cannot View SBOM

**Problem**: SBOM format is not readable

**Solutions:**
1. Use `jq` to format JSON: `... | jq`
2. Use Docker Scout: `docker scout sbom mvrao94/kubeguard:latest`
3. Export to file: `... > sbom.json`
4. Use SBOM viewer tools

### Verification Fails

**Problem**: Cosign verification fails

**Solutions:**
1. Check image digest matches
2. Verify network connectivity
3. Ensure cosign is up to date
4. Check signature key is correct

---

## Additional Resources

### Documentation
- **SLSA**: https://slsa.dev/
- **SPDX**: https://spdx.dev/
- **Docker BuildKit**: https://docs.docker.com/build/attestations/
- **Cosign**: https://docs.sigstore.dev/cosign/overview/

### Tools
- **Docker Scout**: https://docs.docker.com/scout/
- **Trivy**: https://aquasecurity.github.io/trivy/
- **Grype**: https://github.com/anchore/grype
- **Syft**: https://github.com/anchore/syft

### Standards
- **SLSA Framework**: Supply-chain Levels for Software Artifacts
- **SPDX**: Software Package Data Exchange
- **CycloneDX**: OWASP SBOM standard
- **in-toto**: Supply chain security framework

---

## Summary

✅ **All KubeGuard images include SBOM and Provenance**
✅ **Automatically generated in CI/CD**
✅ **Cryptographically signed**
✅ **Verifiable by anyone**
✅ **Compliant with industry standards**

**Your images are secure, transparent, and auditable!**

---

**Last Updated**: 2024
**SLSA Level**: 3
**SBOM Format**: SPDX 2.3
**Provenance Format**: SLSA Provenance v1
