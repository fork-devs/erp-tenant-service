package com.erp.tenant.support;

import com.erp.tenant.domain.model.Plan;
import com.erp.tenant.domain.model.PlanCode;
import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.port.PlanRepositoryPort;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.domain.valueobject.BrandColor;
import com.erp.tenant.domain.valueobject.Ruc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Las operaciones de administracion de talleres declaran el rol que exigen.
 *
 * <p>Un endpoint sin rol declarado es un endpoint accesible a cualquier
 * principal autenticado. La declaracion explicita hace que el contrato sea
 * visible.
 *
 * <p>Ver docs/doc-estado-implementacion.md, hallazgo P1 6.19.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TenantRoleEnforcementTest extends AbstractPostgresIntegrationTest {

    private static final UUID TALLER = UUID.fromString("22223333-4444-5555-6666-777788889999");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepositoryPort tenantRepositoryPort;

    @Autowired
    private PlanRepositoryPort planRepositoryPort;

    @Autowired
    private UnitOfWork unitOfWork;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        Plan plan = planRepositoryPort.findByCode(PlanCode.BASICO)
                .orElseThrow(() -> new AssertionError("El plan BASICO deberia estar sembrado"));

        Tenant tenant = Tenant.create(
                "Taller de Prueba " + UUID.randomUUID().toString().substring(0, 6),
                Ruc.of("20" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1_000_000_000))),
                plan.getId(),
                BrandColor.of("#1A2B3C"));

        unitOfWork.runInTransaction(() -> tenantRepositoryPort.save(tenant));
        tenantId = tenant.getId().getValue();
    }

    @Test
    @DisplayName("El ASESOR recibe 403 al intentar suspender un taller")
    void elAsesorRecibe403AlSuspenderUnTaller() throws Exception {
        mockMvc.perform(patch("/api/v1/tenants/" + tenantId + "/status")
                        .header("Authorization", "Bearer " + tokenCon("ASESOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"SUSPEND\",\"reason\":\"prueba\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("El ADMIN_TALLER puede suspender el taller de su propio taller")
    void elAdminTallerPuedeSuspenderSuPropioTaller() throws Exception {
        mockMvc.perform(patch("/api/v1/tenants/" + tenantId + "/status")
                        .header("Authorization", "Bearer " + tokenCon("ADMIN_TALLER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"SUSPEND\",\"reason\":\"prueba\"}"))
                .andExpect(status().isOk());
    }

    private String tokenCon(String rol) {
        return TestJwksServer.signToken(tenantId, List.of(rol));
    }
}
