package com.formation.usermanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        // 1. INFORMATIONS DE L'API
        Info info = new Info()
                .title("User Management API")
                .description("""
                        API de gestion des utilisateurs, rôles et permissions.

                        🔐 Authentification JWT

                        1. Utilisez /api/auth/register pour créer un compte
                        2. Utilisez /api/auth/login pour obtenir un token
                        3. Cliquez sur le bouton Authorize ci-dessous
                        4. Entrez votre token JWT
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Support")
                        .email("support@example.com")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );

        // 2. CONFIGURATION JWT
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Entrez votre token JWT.");

        // 3. AJOUT DU SCHEMA DE SECURITE
        Components components = new Components()
                .addSecuritySchemes("bearer-jwt", securityScheme);

        // 4. JWT OBLIGATOIRE PAR DEFAUT
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearer-jwt");

        // 5. CREATION DE L'OPENAPI
        return new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}