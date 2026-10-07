package com.erp.tenant.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ERP Tenant Management Service API")
                        .version("1.0.0")
                        .description("Microservicio de Gestión de Talleres y Suscripciones Multi-inquilino (Perú Automotive ERP).")
                        .contact(new Contact()
                                .name("ERP Engineering Team")
                                .email("dev@erp-automotive.pe"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://erp-automotive.pe")));
    }
}
