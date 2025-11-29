package io.github.mvrao94.kubeguard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Value("${spring.application.name}")
  private String applicationName;

  @Value("${project.version:0.0.1}")
  private String version;

  @Value("${project.description:Kubernetes Security Scanner}")
  private String description;

  @Bean
  public OpenAPI kubeguardOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title(applicationName + " API")
                .description(
                    description
                        + "\n\n"
                        + "## Overview\n"
                        + "KubeGuard is a comprehensive Kubernetes security scanner that helps identify "
                        + "security misconfigurations, compliance violations, and best practice deviations "
                        + "in both Kubernetes manifests and live clusters.\n\n"
                        + "## Features\n"
                        + "- **Manifest Scanning**: Analyze YAML/YML files before deployment\n"
                        + "- **Cluster Scanning**: Scan live Kubernetes resources in real-time\n"
                        + "- **Comprehensive Rules**: 100+ security rules covering CIS benchmarks, NSA/CISA guidelines\n"
                        + "- **Severity Classification**: Findings categorized as CRITICAL, HIGH, MEDIUM, LOW, or INFO\n"
                        + "- **Analytics & Reporting**: Historical trends, top failing rules, and compliance reports\n\n"
                        + "## Getting Started\n"
                        + "1. Use `/api/v1/scan/manifests` to scan manifest files\n"
                        + "2. Use `/api/v1/scan/cluster/{namespace}` to scan live clusters\n"
                        + "3. Poll `/api/v1/scan/status/{scanId}` to check scan progress\n"
                        + "4. Use `/api/v1/reports` endpoints for analytics and historical data\n\n"
                        + "## Authentication\n"
                        + "Currently, all endpoints are open for demonstration purposes. "
                        + "In production, implement proper authentication and authorization.")
                .version(version)
                .termsOfService("https://github.com/mvrao94/KubeGuard/blob/main/CODE_OF_CONDUCT.md")
                .contact(
                    new Contact()
                        .name("KubeGuard Team")
                        .url("https://github.com/mvrao94/KubeGuard")
                        .email("venkateswararaom07@gmail.com"))
                .license(
                    new License()
                        .name("MIT License")
                        .url("https://github.com/mvrao94/KubeGuard/blob/main/LICENSE")))
        .externalDocs(
            new ExternalDocumentation()
                .description("KubeGuard Documentation")
                .url("https://github.com/mvrao94/KubeGuard/blob/main/README.md"))
        .servers(
            List.of(
                new Server().url("http://localhost:8080").description("Local Development Server"),
                new Server().url("https://api.kubeguard.io").description("Production Server")))
        .tags(
            List.of(
                new Tag()
                    .name("Security Scanning")
                    .description(
                        "Endpoints for initiating and monitoring security scans on Kubernetes resources. "
                            + "Scans are asynchronous and return immediately with a scan ID."),
                new Tag()
                    .name("Reports & Analytics")
                    .description(
                        "Endpoints for retrieving historical scan reports, security findings, and aggregated analytics. "
                            + "Use these for compliance reporting and trend analysis.")))
        .components(
            new Components()
                .addResponses(
                    "BadRequest",
                    new ApiResponse()
                        .description("Bad Request - Invalid input parameters")
                        .content(
                            new Content()
                                .addMediaType(
                                    "application/json",
                                    new MediaType()
                                        .schema(
                                            new Schema<>()
                                                .$ref("#/components/schemas/ScanResponse")))))
                .addResponses(
                    "NotFound",
                    new ApiResponse()
                        .description("Not Found - Resource does not exist")
                        .content(
                            new Content()
                                .addMediaType(
                                    "application/json",
                                    new MediaType()
                                        .schema(
                                            new Schema<>()
                                                .$ref("#/components/schemas/ScanResponse")))))
                .addResponses(
                    "InternalServerError",
                    new ApiResponse()
                        .description("Internal Server Error - Unexpected error occurred")
                        .content(
                            new Content()
                                .addMediaType(
                                    "application/json",
                                    new MediaType()
                                        .schema(
                                            new Schema<>()
                                                .$ref("#/components/schemas/ScanResponse"))))));
  }
}
