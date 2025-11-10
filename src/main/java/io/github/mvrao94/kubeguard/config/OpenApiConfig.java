package io.github.mvrao94.kubeguard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kubeguardOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KubeGuard API")
                        .description("Kubernetes Security Scanner - Analyze manifests and clusters for security misconfigurations")
                        .version("0.0.2-SNAPSHOT")
                        .contact(new Contact()
                                .name("KubeGuard Team")
                                .url("https://github.com/mvrao94/KubeGuard")
                                .email("venkateswararaom07@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://github.com/mvrao94/KubeGuard/blob/main/LICENSE")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development"),
                        new Server()
                                .url("https://api.kubeguard.io")
                                .description("Production")));
    }
}
