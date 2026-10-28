package com.erp.tenant.support;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.service.TenantProvisioningService;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that outbox messages carry their originating tenant identifier.
 */
class OutboxTenantIsolationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TenantProvisioningService tenantProvisioningService;

    @Autowired
    private TenantOutboxRepositoryPort outboxRepositoryPort;

    @Autowired
    private UnitOfWork unitOfWork;

    @Test
    void outboxMessageCarriesTenantIdOfCreatedTenant() {
        TenantResult result = tenantProvisioningService.provisionTenant(new ProvisionTenantCommand(
                "Taller de Prueba",
                "20601234567",
                "BASICO",
                null,
                "#1A2B3C"));

        // Row claiming executes within an active writable transaction boundary.
        List<TenantOutboxMessage> pending = unitOfWork.callInTransaction(
                () -> outboxRepositoryPort.findPending(java.time.Instant.now(), 50));

        assertTrue(pending.stream().anyMatch(m ->
                        m.getTenantId().equals(result.id())
                                && m.getAggregateId().equals(result.id())),
                "Domain event outbox message must carry the tenant identifier");
    }

    @Test
    void migrationAppliesTenantColumnOnOutboxTable() throws Exception {
        try (var connection = EmbeddedPostgresSupport.openOwnerConnection();
             var statement = connection.createStatement();
             var rs = statement.executeQuery(
                     "SELECT count(*) FROM information_schema.columns "
                     + "WHERE table_schema = 'public' AND table_name = 'tenant_outbox' AND column_name = 'tenant_id'")) {
            rs.next();
            assertEquals(1, rs.getInt(1), "tenant_outbox debe tener columna tenant_id");
        }

        try (var connection = EmbeddedPostgresSupport.openOwnerConnection();
             var statement = connection.createStatement();
             var rs = statement.executeQuery(
                     "SELECT is_nullable FROM information_schema.columns "
                     + "WHERE table_schema = 'public' AND table_name = 'tenant_outbox' AND column_name = 'tenant_id'")) {
            rs.next();
            assertEquals("NO", rs.getString(1), "tenant_id no puede admitir nulos");
        }
    }
}
