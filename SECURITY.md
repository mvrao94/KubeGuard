# Security Policy

## Supported Versions

As a solo maintainer, I focus on maintaining the latest stable version of KubeGuard. Security updates will be primarily released for the most recent version.

| Version | Supported |
| ------- | --------- |
| latest  | ✓         |
| < latest| ✗         |

## Reporting a Vulnerability

I take security vulnerabilities seriously. If you discover a security issue, please follow these steps:

1. **DO NOT** disclose the vulnerability publicly in GitHub Issues or Discussions
2. Send an email to [venkateswararaom07@gmail.com](mailto:venkateswararaom07@gmail.com) with:
   - A detailed description of the vulnerability
   - Steps to reproduce (if possible)
   - Affected versions
   - Any potential mitigations you've identified

### What to Expect

As a solo maintainer, here's what you can expect after reporting:

- **Initial Response**: I aim to acknowledge your report within 48 hours
- **Updates**: I will provide updates on the progress at least once every 72 hours
- **Timeline**: I will work to release a fix within 7-14 days, depending on the severity and complexity
- **Disclosure**: Once a fix is ready, we will coordinate the disclosure timeline

### Process

1. Your report is received and acknowledged
2. I will investigate and validate the issue
3. A fix will be developed and tested
4. A security advisory will be created (if applicable)
5. The fix will be released, and the advisory will be published

## Security Best Practices

When using KubeGuard:

1. Always use the latest version
2. Follow the principle of least privilege when configuring access
3. Regularly review your configuration files
4. Keep your Kubernetes clusters updated
5. Monitor logs for suspicious activities

## GPG Key

For secure communication, you can use my GPG key:
```
[TODO]
```

## Acknowledgments

I appreciate the security researchers and community members who responsibly disclose vulnerabilities. Contributors who report valid security issues will be acknowledged in the security advisories (unless they wish to remain anonymous).

## Additional Resources

- [KubeGuard Documentation](https://github.com/mvrao94/kubeguard#readme)
- [Kubernetes Security Best Practices](https://kubernetes.io/docs/concepts/security/)