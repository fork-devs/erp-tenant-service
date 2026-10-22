package com.erp.tenant;

import com.erp.tenant.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * El contexto de la aplicacion arranca contra PostgreSQL real embebido, con
 * las migraciones de Flyway aplicadas y el esquema validado por Hibernate.
 */
class ErpTenantServiceApplicationTests extends AbstractPostgresIntegrationTest {

    @Test
    void contextLoads() {
    }

}
