package com.boazhousing.qrcodegen.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Boaz Housing QR Code Generator API")
                        .description("API pour générer des QR codes avec le style unique Boaz-Housing. " +
                                    "Cette API permet de générer des QR codes stylisés avec des points bleus, " +
                                    "des coins orange dégradé et le logo central Boaz-Housing.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Boaz Housing")
                                .email("support@boaz-housing.com")
                                .url("https://housing.boaz-study.tech"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Serveur de développement local"),
                        new Server()
                                .url("https://qr-generator.boaz-housing.com")
                                .description("Serveur de production")
                ));
    }
}