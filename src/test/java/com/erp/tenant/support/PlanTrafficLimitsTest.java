package com.erp.tenant.support;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.service.TenantProvisioningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los limites de TRAFICO del plan (peticiones por minuto y conexiones
 * concurrentes) los posee el tenant-service, que es el dueño del plan.
 *
 * <p>El gateway los aplica como cuota, de modo que si aqui no estan, el
 * gateway tiene que inventarlos: y entonces la promesa comercial del plan
 * tiene dos dueños y deriva sin que nadie lo note (ADR-007, hallazgo 6.9).
 *
 * <p>Ver docs/adr/ADR-007-quien-posee-los-limites-del-plan.md.
 */
@AutoConfigureMockMvc
class PlanTrafficLimitsTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantProvisioningService tenantProvisioningService;

    @Test
    void elPlanBasicoExponeSusLimitesDeTrafico() throws Exception {
        TenantResult taller = crearTaller("Taller Basico", "20612345678", "BASICO");

        mockMvc.perform(get("/api/v1/internal/tenants/" + taller.id() + "/limits")
                        .header("Authorization", "Bearer " + tokenDe(taller.id(), "ADMIN_TALLER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planCode").value("BASICO"))
                .andExpect(jsonPath("$.maxRequestsPerMinute").value(100))
                .andExpect(jsonPath("$.maxConcurrentSlots").value(15));
    }

    @Test
    void elPlanProExponeSusLimitesDeTrafico() throws Exception {
        TenantResult taller = crearTaller("Taller Pro", "20612345679", "PRO");

        mockMvc.perform(get("/api/v1/internal/tenants/" + taller.id() + "/limits")
                        .header("Authorization", "Bearer " + tokenDe(taller.id(), "ADMIN_TALLER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planCode").value("PRO"))
                .andExpect(jsonPath("$.maxRequestsPerMinute").value(500))
                .andExpect(jsonPath("$.maxConcurrentSlots").value(40));
    }

    @Test
    void elPlanEnterpriseExponeSusLimitesDeTrafico() throws Exception {
        TenantResult taller = crearTaller("Taller Enterprise", "20612345680", "ENTERPRISE");

        mockMvc.perform(get("/api/v1/internal/tenants/" + taller.id() + "/limits")
                        .header("Authorization", "Bearer " + tokenDe(taller.id(), "ADMIN_TALLER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planCode").value("ENTERPRISE"))
                .andExpect(jsonPath("$.maxRequestsPerMinute").value(2000))
                .andExpect(jsonPath("$.maxConcurrentSlots").value(100));
    }

    @Test
    void losLimitesDeNegocioDelPlanSiguenExponiendose() throws Exception {
        TenantResult taller = crearTaller("Taller Negocio", "20612345681", "PRO");

        mockMvc.perform(get("/api/v1/internal/tenants/" + taller.id() + "/limits")
                        .header("Authorization", "Bearer " + tokenDe(taller.id(), "ADMIN_TALLER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxUsers").exists())
                .andExpect(jsonPath("$.maxVehicles").exists())
                .andExpect(jsonPath("$.maxIotMsgPerDay").exists());
    }

    private TenantResult crearTaller(String nombre, String ruc, String plan) {
        return tenantProvisioningService.provisionTenant(
                new ProvisionTenantCommand(nombre, ruc, plan, null, "#1A2B3C"));
    }

    private String tokenDe(UUID tenantId, String rol) {
        return TestJwksServer.signToken(tenantId, List.of(rol));
    }
}
