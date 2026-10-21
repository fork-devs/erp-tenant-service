package com.erp.tenant.support;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.service.TenantProvisioningService;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * El taller y su evento de dominio se confirman juntos.
 *
 * <p>Con el limite transaccional en la capa de aplicacion, un fallo al
 * escribir el evento revierte tambien la escritura del taller. Sin el, cada
 * llamada a un repositorio abria su propia transaccion y un fallo intermedio
 * dejaba un taller sin su evento: un estado que nadie puede recuperar y que
 * el sistema afirma haber producido.
 */
class TenantProvisioningAtomicityTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TenantProvisioningService tenantProvisioningService;

    @Autowired
    private TenantRepositoryPort tenantRepositoryPort;

    @MockitoSpyBean
    private TenantOutboxRepositoryPort outboxRepositoryPort;

    @Test
    void siFallaLaEscrituraDelEventoNoQuedaNingunTallerCreado() {
        doThrow(new RuntimeException("el evento no se puede escribir"))
                .when(outboxRepositoryPort).save(any());

        assertThrows(RuntimeException.class, () -> tenantProvisioningService.provisionTenant(
                new ProvisionTenantCommand("Taller Atomico", "20609999999", "BASICO", null, "#1A2B3C")));

        // El taller NO debe existir: la escritura del evento fallo y la
        // transaccion se revirtio por completo.
        assertEquals(0, tenantRepositoryPort.findAll().size(),
                "No debe quedar ningun taller creado cuando falla la escritura de su evento");
    }

    @Test
    void siTodoVaBienElTallerYSuEventoQuedanConfirmados() {
        TenantResult result = tenantProvisioningService.provisionTenant(
                new ProvisionTenantCommand("Taller Correcto", "20608888888", "BASICO", null, "#1A2B3C"));

        assertEquals(1, tenantRepositoryPort.findAll().size());
        assertEquals(1, outboxRepositoryPort.findPending(java.time.Instant.now().plusSeconds(60), 50).size(),
                "El evento del taller debe quedar pendiente de publicar");
    }
}
