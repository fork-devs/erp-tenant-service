package com.erp.tenant.support;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.service.TenantProvisioningService;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Verifies transactional atomicity during tenant provisioning: event persistence failure rolls back tenant creation.
 */
class TenantProvisioningAtomicityTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TenantProvisioningService tenantProvisioningService;

    @Autowired
    private TenantRepositoryPort tenantRepositoryPort;

    @MockitoSpyBean
    private TenantOutboxRepositoryPort outboxRepositoryPort;

    @Autowired
    private UnitOfWork unitOfWork;

    @Test
    void ifEventPersistenceFailsNoTenantIsCreated() {
        int tenantsBefore = tenantRepositoryPort.findAll().size();

        doThrow(new RuntimeException("Outbox event write simulated failure"))
                .when(outboxRepositoryPort).save(any());

        assertThrows(RuntimeException.class, () -> tenantProvisioningService.provisionTenant(
                new ProvisionTenantCommand("Taller Atomico", "20609999999", "BASICO", null, "#1A2B3C")));

        // Tenant must NOT exist: outbox failure rolled back entire transaction.
        assertEquals(tenantsBefore, tenantRepositoryPort.findAll().size(),
                "No tenant must remain created when its domain event persistence fails");
    }

    /**
     * Generates unique 11-digit RUC number per test to avoid unique constraint collisions.
     */
    private String uniqueRuc() {
        return "2060" + String.format("%07d", Math.abs(UUID.randomUUID().hashCode() % 10_000_000));
    }

    @Test
    void whenProvisioningSucceedsTenantAndEventAreCommittedTogether() {
        // Unique RUC per test execution.
        TenantResult result = tenantProvisioningService.provisionTenant(
                new ProvisionTenantCommand("Taller Correcto", uniqueRuc(), "BASICO", null, "#1A2B3C"));

        // Relative counts ensure independence from test execution order.
        List<TenantOutboxMessage> pending = unitOfWork.callInTransaction(
                () -> outboxRepositoryPort.findPending(java.time.Instant.now().plusSeconds(60), 200));

        assertEquals(1, pending.stream()
                        .filter(m -> m.getTenantId().equals(result.id()))
                        .count(),
                "Tenant domain event must remain pending for publication in the outbox");
    }
}
