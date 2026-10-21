package com.erp.tenant.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base de los tests de integracion del servicio de tenants.
 *
 * <p>Arranca PostgreSQL real en proceso y conecta la aplicacion con el rol de
 * runtime {@code erp_app}. Las migraciones de Flyway las ejecuta el
 * propietario.
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

        // El servicio valida el token contra el JWKS del emisor de
        // credenciales. Los tests levantan su propio emisor, de modo que los
        // tokens que se usan estan realmente firmados.
        registry.add("erp.security.jwks-url", TestJwksServer::jwksUrl);
    }
}
