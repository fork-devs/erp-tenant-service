package com.erp.tenant;

import com.erp.tenant.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Verifies application context startup against embedded PostgreSQL with Flyway
 * migrations applied and schema validated by Hibernate.
 */
class ErpTenantServiceApplicationTests extends AbstractPostgresIntegrationTest {

    @Test
    void contextLoads() {
    }

}
