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

    @Autowired
    private UnitOfWork unitOfWork;

    @Test
    void siFallaLaEscrituraDelEventoNoQuedaNingunTallerCreado() {
        int talleresAntes = tenantRepositoryPort.findAll().size();

        doThrow(new RuntimeException("el evento no se puede escribir"))
                .when(outboxRepositoryPort).save(any());

        assertThrows(RuntimeException.class, () -> tenantProvisioningService.provisionTenant(
                new ProvisionTenantCommand("Taller Atomico", "20609999999", "BASICO", null, "#1A2B3C")));

        // El taller NO debe existir: la escritura del evento fallo y la
        // transaccion se revirtio por completo. El conteo es relativo, y no
        // absoluto, porque la base de datos es compartida entre tests.
        assertEquals(talleresAntes, tenantRepositoryPort.findAll().size(),
                "No debe quedar ningun taller creado cuando falla la escritura de su evento");
    }

    /**
     * RUC de 11 digitos, unico por test: el RUC es unico en la base de
     * datos, que es compartida entre tests.
     */
    private String rucUnico() {
        return "2060" + String.format("%07d", Math.abs(UUID.randomUUID().hashCode() % 10_000_000));
    }

    @Test
    void siTodoVaBienElTallerYSuEventoQuedanConfirmados() {
        // Cada test usa un RUC propio: el RUC es unico, y la base de datos es
        // compartida entre tests.
        TenantResult result = tenantProvisioningService.provisionTenant(
                new ProvisionTenantCommand("Taller Correcto", rucUnico(), "BASICO", null, "#1A2B3C"));

        // Los conteos son relativos: la base de datos es compartida entre
        // tests, de modo que el conteo absoluto dependeria del orden de
        // ejecucion.
        List<TenantOutboxMessage> pendientes = unitOfWork.callInTransaction(
                () -> outboxRepositoryPort.findPending(java.time.Instant.now().plusSeconds(60), 200));

        assertEquals(1, pendientes.stream()
                        .filter(m -> m.getTenantId().equals(result.id()))
                        .count(),
                "El evento del taller debe quedar pendiente de publicar");
    }
}
