# KubeGuard: Lightweight Kubernetes Security Scanner

KubeGuard is a lightweight, self-hosted security scanner for Kubernetes, built with Java and Spring Boot. It provides developers and DevOps engineers with quick insights into common security misconfigurations, helping to harden applications before and after deployment.

---

## Table of Contents
- [About The Project](#about-the-project)
- [Key Features](#key-features)
- [How It Works](#how-it-works)
- [Built With](#built-with)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation & Local Run](#installation--local-run)
- [API Usage](#api-usage)
  - [Static Manifest Scan](#static-manifest-scan)
  - [Live Cluster Scan](#live-cluster-scan)
- [Deployment to Kubernetes](#deployment-to-kubernetes)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

---

## About The Project

Drawing from my experience in building enterprise-grade applications and migrating critical services to cloud-native environments like AWS and GCP, I recognized a common challenge: maintaining security posture in Kubernetes can be complex. While powerful, enterprise security tools are often too heavy for development workflows.

KubeGuard was born out of the need for a developer-first tool that makes security accessible and automated. It's inspired by direct experience in:
- Implementing security hardening measures like read-only filesystems and non-root containers.
- Architecting and developing fully containerized applications with Docker and Kubernetes.
- Building robust CI/CD pipelines with tools like Jenkins and GitLab to automate quality gates.

KubeGuard is not meant to replace comprehensive security platforms but to act as a first line of defense, empowering teams to shift security left and catch issues early in the development lifecycle.

---

## Key Features

- **Static Manifest Scanning:** Scan Kubernetes YAML files for security issues before they are deployed.
- **Live Cluster Scanning:** Scan running resources within a Kubernetes cluster for active vulnerabilities.
- **Simple REST API:** An intuitive API to trigger scans and retrieve results in JSON format.
- **CI/CD Friendly:** Packaged as a lightweight container, it's designed to be a seamless step in any CI/CD pipeline.

---

## How It Works

KubeGuard uses two primary methods for security analysis:

- **Static Scanning:** Parses `.yaml` or `.yml` files, analyzing the structure of Kubernetes objects like Deployments and Pods to find common security anti-patterns (e.g., privileged containers, missing resource limits).
- **Live Scanning:** Uses the official Kubernetes Java client to connect to the Kubernetes API server and inspect the state of running resources in real-time, providing continuous visibility.

---

## Built With

This project leverages a modern, robust tech stack based on extensive professional experience:
- Java
- Spring Boot
- Hibernate (for potential future stateful features)
- Docker
- Kubernetes

---

## Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites
- Java 11 or higher
- Apache Maven 3.6+
- Docker
- `kubectl` configured to point to a Kubernetes cluster (e.g., Minikube, kind, GKE, EKS)

### Installation & Local Run

```sh
# Clone the repo
git clone https://github.com/mvrao94/kubeguard.git
cd kubeguard

# Build the project using Maven
mvn clean install

# Run the application
java -jar target/kubeguard-0.0.1-SNAPSHOT.jar
```

The application will start on port 8080.

---

## API Usage

### 1. Static Manifest Scan

This endpoint scans a local directory (on the server where KubeGuard is running) for Kubernetes manifests and analyzes them.

- **Endpoint:** `POST /api/scan/manifests`
- **Content-Type:** `application/json`
- **Body:**
  ```json
  {
    "path": "/path/to/your/manifests"
  }
  ```

**Example cURL Request:**

```sh
curl -X POST http://localhost:8080/api/scan/manifests \
  -H "Content-Type: application/json" \
  -d '{"path": "./sample-manifests"}'
```

### 2. Live Cluster Scan

This endpoint scans a specific namespace within the Kubernetes cluster that `kubectl` is configured to access.

- **Endpoint:** `GET /api/scan/cluster/{namespace}`

**Example cURL Request:**

```sh
curl http://localhost:8080/api/scan/cluster/default
```

---

## Deployment to Kubernetes

To run KubeGuard inside your cluster, you need to build and push its Docker image, then apply the provided manifests.

### Build and Push the Docker Image

```sh
# Build the image
docker build -t your-dockerhub-username/kubeguard:latest .

# Push to your registry
docker push your-dockerhub-username/kubeguard:latest
```

### Deploy to Kubernetes

- Update the image name in `k8s/deployment.yaml` to point to your image.

**Apply the manifests:**

```sh
# First, apply the RBAC rules to give KubeGuard read access
kubectl apply -f k8s/rbac.yaml

# Then, deploy the application
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

---

## Roadmap

- [ ] Web UI: Develop a simple frontend dashboard for visualizing scan results.
- [ ] Expanded Security Checks: Add more security checks based on the CIS Kubernetes Benchmark.
- [ ] Alerting Integration: Push notifications for critical vulnerabilities to Slack or email.
- [ ] Cost Optimization Suggestions: Analyze resource requests vs. actual usage to suggest cost-saving opportunities.

---

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are greatly appreciated.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

Distributed under the MIT License. See `LICENSE` file for more information.

---

## Contact

Project Link: [https://github.com/mvrao94/kubeguard](https://github.com/mvrao94/kubeguard)
