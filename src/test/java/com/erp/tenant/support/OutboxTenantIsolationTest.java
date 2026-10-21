package com.erp.tenant.support;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.service.TenantProvisioningService;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El mensaje de la bandeja de salida lleva el taller al que pertenece.
 *
 * <p>Sin esa columna, la cola de eventos no podia filtrarse por taller, y el
 * payload contiene el RUC y la razon social del taller. Ver
 * docs/doc-estado-implementacion.md, hallazgo P1 6.14.
 */
class OutboxTenantIsolationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TenantProvisioningService tenantProvisioningService;

    @Autowired
    private TenantOutboxRepositoryPort outboxRepositoryPort;

    @Test
    void elMensajeDelOutboxLlevaElTallerDelTenantCreado() {
        TenantResult result = tenantProvisioningService.provisionTenant(new ProvisionTenantCommand(
                "Taller de Prueba",
                "20601234567",
                "BASICO",
                null,
                "#1A2B3C"));

        List<TenantOutboxMessage> pendientes = outboxRepositoryPort.findUnpublished(50);

        assertTrue(pendientes.stream().anyMatch(m ->
                        m.getTenantId().equals(result.id())
                                && m.getAggregateId().equals(result.id())),
                "El mensaje del evento de dominio debe llevar el identificador del taller");
    }

    @Test
    void laMigracionAplicaLaColumnaDeTenantEnLaBandejaDeSalida() throws Exception {
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
