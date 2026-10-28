package com.erp.tenant.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base integration test class for tenant-service.
 *
 * <p>Starts embedded PostgreSQL and configures DataSource connection with runtime credentials.
 */
@SpringBootTest
public abstract class AbstractPostgresIntegrationTest {

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        EmbeddedPostgresSupport.start();

        registry.add("spring.datasource.url", EmbeddedPostgresSupport::jdbcUrl);
        registry.add("spring.datasource.username", EmbeddedPostgresSupport::appRole);
        registry.add("spring.datasource.password", EmbeddedPostgresSupport::appPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.flyway.url", EmbeddedPostgresSupport::jdbcUrl);
        registry.add("spring.flyway.user", EmbeddedPostgresSupport::ownerUser);
        registry.add("spring.flyway.password", EmbeddedPostgresSupport::ownerPassword);
        registry.add("spring.flyway.placeholders.appRolePassword", EmbeddedPostgresSupport::appPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");

        // The service validates tokens against the issuer JWKS endpoint.
        // Tests run an embedded JWKS server with signed tokens.
        registry.add("erp.security.jwks-url", TestJwksServer::jwksUrl);
    }
}
