# kubeguard Helm Chart

## Overview

The kubeguard Helm chart provides a way to deploy the KubeGuard application on a Kubernetes cluster. KubeGuard is a security scanner that helps in identifying vulnerabilities in your container images and running applications.

## Prerequisites

- Kubernetes 1.12+
- Helm 3.x

## Installation

To install the kubeguard Helm chart, use the following command:

```bash
helm install kubeguard ./kubeguard-helm
```

This command will deploy the KubeGuard application in the default namespace. To specify a different namespace, use the `--namespace` flag:

```bash
helm install kubeguard ./kubeguard-helm --namespace your-namespace
```

## Configuration

The default configuration values can be found in the `values.yaml` file. You can override these values by providing your own `values.yaml` file or by using the `--set` flag during installation.

### Example

To set a custom database password during installation, you can use:

```bash
helm install kubeguard ./kubeguard-helm --set secret.DB_PASSWORD=yourpassword
```

## Uninstallation

To uninstall the kubeguard release, run:

```bash
helm uninstall kubeguard
```

## Notes

- Ensure that you have the necessary permissions to create resources in the specified namespace.
- Review the `templates` directory for detailed configurations of each Kubernetes resource.

## License

This project is licensed under the MIT License. See the LICENSE file for details.