package com.erp.tenant.support;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.service.TenantProvisioningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Aislamiento entre talleres en los endpoints de administracion.
 *
 * <p>El atacante es el administrador del taller A y la victima es el taller B.
 *
 * <p>Ver docs/doc-estado-implementacion.md, hallazgo P0 5.4.
 */
@AutoConfigureMockMvc
class CrossTenantAccessTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantProvisioningService tenantProvisioningService;

    @Test
    void elAdministradorDeUnTallerNoPuedeConsultarOtro() throws Exception {
        TenantResult atacante = crearTaller("Taller Atacante", "20601111111");
        TenantResult victima = crearTaller("Taller Victima", "20602222222");

        mockMvc.perform(get("/api/v1/tenants/" + victima.id())
                        .header("Authorization", "Bearer " + tokenDe(atacante.id(), "ADMIN_TALLER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdministradorDeUnTallerNoPuedeSuspenderOtro() throws Exception {
        TenantResult atacante = crearTaller("Taller Atacante 2", "20603333333");
        TenantResult victima = crearTaller("Taller Victima 2", "20604444444");

        mockMvc.perform(patch("/api/v1/tenants/" + victima.id() + "/status")
                        .header("Authorization", "Bearer " + tokenDe(atacante.id(), "ADMIN_TALLER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"SUSPEND\",\"reason\":\"intento\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdministradorDeUnTallerNoPuedeCambiarElBrandingDeOtro() throws Exception {
        TenantResult atacante = crearTaller("Taller Atacante 3", "20605555555");
        TenantResult victima = crearTaller("Taller Victima 3", "20606666666");

        mockMvc.perform(patch("/api/v1/tenants/" + victima.id() + "/branding")
                        .header("Authorization", "Bearer " + tokenDe(atacante.id(), "ADMIN_TALLER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commercialName\":\"Secuestrado\",\"brandColor\":\"#000000\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenLosEndpointsDeTallerSeRechazan() throws Exception {
        TenantResult taller = crearTaller("Taller Publico", "20607777777");

        mockMvc.perform(get("/api/v1/tenants/" + taller.id()))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/v1/internal/tenants/" + taller.id() + "/limits"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void elSuperadministradorOperaSobreCualquierTaller() throws Exception {
        TenantResult victima = crearTaller("Taller Sumo", "20608888888");

        mockMvc.perform(get("/api/v1/tenants/" + victima.id())
                        .header("Authorization", "Bearer " + tokenDe(victima.id(), "SUPERADMIN")))
                .andExpect(status().isOk());
    }

    private TenantResult crearTaller(String nombre, String ruc) {
        return tenantProvisioningService.provisionTenant(
                new ProvisionTenantCommand(nombre, ruc, "BASICO", null, "#1A2B3C"));
    }

    private String tokenDe(UUID tenantId, String rol) {
        return TestJwksServer.signToken(tenantId, List.of(rol));
    }
}
