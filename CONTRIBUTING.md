# Contributing to KubeGuard

Thank you for your interest in contributing to KubeGuard! We welcome contributions from the community and appreciate your help in making Kubernetes security more accessible and robust.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Documentation](#documentation)
- [Issue Reporting](#issue-reporting)
- [Feature Requests](#feature-requests)

## Code of Conduct

By participating in this project, you agree to abide by our Code of Conduct. Please read [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) to understand what behavior will and will not be tolerated.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/KubeGuard.git
   cd KubeGuard
   ```
3. Add the upstream repository as a remote:
   ```bash
   git remote add upstream https://github.com/mvrao94/KubeGuard.git
   ```

## Development Setup

### Prerequisites
- Java 25
- Maven 3.9+
- Docker (for building and testing)
- Kubernetes cluster (local or remote) for testing
- PostgreSQL (for local development)

### Local Environment Setup
1. Install dependencies:
   ```bash
   ./mvnw clean install
   ```

2. Set up the local database:
   ```bash
   docker-compose up -d postgres
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Access the application:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/api-docs

## Making Changes

1. Create a new branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes following our coding standards:
   - Use consistent code formatting
   - Follow Java naming conventions
   - Write descriptive commit messages
   - Add/update tests as needed
   - Update documentation if required

3. Commit your changes:
   ```bash
   git add .
   git commit -m "feat: describe your changes here"
   ```

### Commit Message Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification. Each commit message should be structured as follows:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation only changes
- style: Changes that do not affect the meaning of the code
- refactor: Code change that neither fixes a bug nor adds a feature
- perf: Code change that improves performance
- test: Adding missing tests or correcting existing tests
- chore: Changes to the build process or auxiliary tools

## Testing

Before submitting your changes, ensure all tests pass:

1. Run unit tests:
   ```bash
   ./mvnw test
   ```

2. Run integration tests:
   ```bash
   ./mvnw verify
   ```

3. Run security scans:
   ```bash
   ./mvnw verify -Psecurity
   ```

## Submitting Changes

1. Push your changes to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

2. Create a Pull Request (PR) on GitHub:
   - Write a clear PR description
   - Link any related issues
   - Fill out the PR template completely
   - Ensure CI checks pass

### PR Review Process

1. Wait for maintainers to review your PR
2. Address any requested changes
3. Once approved, your PR will be merged

## Documentation

- Update documentation in the `docs/` directory
- Keep README.md up to date
- Add inline code comments for complex logic
- Update API documentation in Swagger annotations

## Issue Reporting

Before creating an issue:
1. Search existing issues
2. Use the issue template
3. Include:
   - KubeGuard version
   - Environment details
   - Steps to reproduce
   - Expected vs actual behavior
   - Relevant logs/screenshots

## Feature Requests

For feature requests:
1. Use the feature request template
2. Explain the use case
3. Describe expected behavior
4. Provide example scenarios

## Security Issues

For security vulnerabilities:
1. Do NOT open public issues
2. Follow our [Security Policy](SECURITY.md)
3. Email venkateswararaom07@gmail.com

## License

By contributing to KubeGuard, you agree that your contributions will be licensed under its MIT License.
Thank you for helping to improve KubeGuard! We look forward to your contributions.
