package com.erp.tenant.support;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.service.TenantProvisioningService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class WorkshopSpecialtyIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantProvisioningService tenantProvisioningService;

    @Test
    @DisplayName("Al crear un taller se siembran automáticamente las 5 especialidades base")
    void shouldSeedBaseSpecialtiesOnWorkshopCreation() throws Exception {
        TenantResult taller = crearTaller("Taller Especialidades", "20609999901");

        mockMvc.perform(get("/api/v1/workshops/" + taller.id() + "/specialties")
                        .header("Authorization", "Bearer " + tokenDe(taller.id(), "WORKSHOP_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].code", hasItems(
                        "GENERAL_MECHANIC",
                        "ELECTRICIAN",
                        "BODYWORK_PAINT",
                        "DIAGNOSTIC",
                        "TIRE_ALIGNMENT"
                )));
    }

    @Test
    @DisplayName("El WORKSHOP_ADMIN puede crear, consultar, actualizar y desactivar una especialidad personalizada")
    void shouldPerformFullSpecialtyLifecycle() throws Exception {
        TenantResult taller = crearTaller("Taller Ciclo Completo", "20609999902");
        String token = tokenDe(taller.id(), "WORKSHOP_ADMIN");

        // 1. Crear especialidad personalizada
        String createJson = """
                {
                    "name": "Inyección Electrónica y Calibración",
                    "code": "INYECCION_ELECTRONICA",
                    "description": "Limpieza y diagnóstico de inyectores"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/v1/workshops/" + taller.id() + "/specialties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.code").value("INYECCION_ELECTRONICA"))
                .andExpect(jsonPath("$.name").value("Inyección Electrónica y Calibración"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();

        // Extraer id generado
        String specialtyId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        // 2. Intentar crear duplicado con el mismo código -> 400
        mockMvc.perform(post("/api/v1/workshops/" + taller.id() + "/specialties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        // 3. Consultar detalle
        mockMvc.perform(get("/api/v1/workshops/" + taller.id() + "/specialties/" + specialtyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(specialtyId))
                .andExpect(jsonPath("$.name").value("Inyección Electrónica y Calibración"));

        // 4. Actualizar nombre y descripción
        String updateJson = """
                {
                    "name": "Inyección Electrónica y Telemetría",
                    "description": "Nueva descripción avanzada"
                }
                """;

        mockMvc.perform(put("/api/v1/workshops/" + taller.id() + "/specialties/" + specialtyId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Inyección Electrónica y Telemetría"))
                .andExpect(jsonPath("$.description").value("Nueva descripción avanzada"));

        // 5. Desactivar (soft-delete)
        mockMvc.perform(delete("/api/v1/workshops/" + taller.id() + "/specialties/" + specialtyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 6. Consultar con activeOnly=true no debe incluirla
        mockMvc.perform(get("/api/v1/workshops/" + taller.id() + "/specialties?activeOnly=true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(specialtyId))));

        // 7. Historial de revisiones
        mockMvc.perform(get("/api/v1/workshops/" + taller.id() + "/specialties/" + specialtyId + "/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].fieldName", hasItems("status", "name", "description", "active")));
    }

    @Test
    @DisplayName("Aislamiento multi-tenant: Un administrador de otro taller recibe 403 al intentar acceder a las especialidades")
    void shouldDenyCrossTenantAccess() throws Exception {
        TenantResult atacante = crearTaller("Taller Atacante Specialties", "20609999903");
        TenantResult victima = crearTaller("Taller Victima Specialties", "20609999904");

        String tokenAtacante = tokenDe(atacante.id(), "WORKSHOP_ADMIN");

        // Intentar listar especialidades del taller victima
        mockMvc.perform(get("/api/v1/workshops/" + victima.id() + "/specialties")
                        .header("Authorization", "Bearer " + tokenAtacante))
                .andExpect(status().isForbidden());

        // Intentar crear especialidad en el taller victima
        String createJson = """
                {
                    "name": "Especialidad Maliciosa",
                    "code": "MALICIOSA",
                    "description": "Hack"
                }
                """;

        mockMvc.perform(post("/api/v1/workshops/" + victima.id() + "/specialties")
                        .header("Authorization", "Bearer " + tokenAtacante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isForbidden());
    }

    private TenantResult crearTaller(String nombre, String ruc) {
        return tenantProvisioningService.provisionTenant(
                new ProvisionTenantCommand(nombre, ruc, "BASICO", null, "#1A2B3C"));
    }

    private String tokenDe(UUID tenantId, String rol) {
        return TestJwksServer.signToken(tenantId, List.of(rol));
    }
}
