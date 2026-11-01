package com.erp.tenant.support;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.service.TenantProvisioningService;
import com.erp.tenant.domain.port.DniValidationResult;
import com.erp.tenant.domain.port.IdentityValidationPort;
import com.erp.tenant.domain.port.RucValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class CoreBranchesAndProfilesIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantProvisioningService tenantProvisioningService;

    @MockitoBean
    private IdentityValidationPort identityValidationPort;

    @BeforeEach
    void setUpIdentityValidation() {
        when(identityValidationPort.validateDni(anyString()))
                .thenReturn(new DniValidationResult("45678901", "Raul", "Castro", "Perez", "Raul Castro Perez", true));
        when(identityValidationPort.validateRuc(anyString()))
                .thenReturn(new RucValidationResult("20601111101", "Taller Sede Test SAC", "Taller Test", "ACTIVO", "HABIDO", "Av. Test 123", true));
    }

    @Test
    @DisplayName("Should test full lifecycle of branches and branch subscriptions")
    void shouldTestBranchLifecycle() throws Exception {
        TenantResult taller = crearTaller("Taller Sede Test", "20601111101");
        String token = tokenDe(taller.id(), "WORKSHOP_ADMIN");

        // 1. Create branch
        String createBranchJson = """
                {
                    "workshopId": "%s",
                    "code": "BR-SURCO-01",
                    "name": "Sede Principal Surco",
                    "address": "Av. Primavera 123",
                    "phone": "+51987654321"
                }
                """.formatted(taller.id());

        String branchResponse = mockMvc.perform(post("/api/v1/branches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBranchJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("BR-SURCO-01"))
                .andExpect(jsonPath("$.name").value("Sede Principal Surco"))
                .andReturn().getResponse().getContentAsString();

        String branchId = extraerId(branchResponse);

        // 2. Get branch by ID
        mockMvc.perform(get("/api/v1/branches/" + branchId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(branchId))
                .andExpect(jsonPath("$.code").value("BR-SURCO-01"));

        // 3. List branches of workshop
        mockMvc.perform(get("/api/v1/branches?workshopId=" + taller.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].code").value("BR-SURCO-01"));

        // 4. Update branch
        String updateBranchJson = """
                {
                    "code": "BR-SURCO-01",
                    "name": "Sede Surco Premium",
                    "address": "Av. Primavera 1250",
                    "phone": "+51999888777"
                }
                """;

        mockMvc.perform(put("/api/v1/branches/" + branchId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBranchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sede Surco Premium"));

        // 5. Assign branch subscription
        String assignSubJson = """
                {
                    "planId": "%s",
                    "billingCycle": "MONTHLY"
                }
                """.formatted(taller.planId());

        mockMvc.perform(post("/api/v1/branches/" + branchId + "/subscriptions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignSubJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.billingCycle").value("MONTHLY"));

        // 6. Cancel branch subscription
        mockMvc.perform(delete("/api/v1/branches/" + branchId + "/subscription")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should test full lifecycle of employee profiles")
    void shouldTestEmployeeProfileLifecycle() throws Exception {
        TenantResult taller = crearTaller("Taller Empleados Test", "20601111102");
        UUID employeeUserId = UUID.randomUUID();
        String adminToken = tokenDe(taller.id(), "WORKSHOP_ADMIN");
        String employeeSelfToken = tokenDe(taller.id(), employeeUserId, "EMPLOYEE");

        // 1. Create employee profile
        String createEmpJson = """
                {
                    "userId": "%s",
                    "firstName": "Luis",
                    "lastName": "Mendoza",
                    "documentType": "DNI",
                    "documentNumber": "71234567",
                    "phone": "+51912345678"
                }
                """.formatted(employeeUserId);

        String empResponse = mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEmpJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Luis"))
                .andExpect(jsonPath("$.documentNumber").value("71234567"))
                .andReturn().getResponse().getContentAsString();

        String employeeId = extraerId(empResponse);

        // 2. Query employee by ID
        mockMvc.perform(get("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId));

        // 3. Query employee by userId
        mockMvc.perform(get("/api/v1/employees?userId=" + employeeUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(employeeUserId.toString()));

        // 4. Query employee by documentNumber
        mockMvc.perform(get("/api/v1/employees?documentNumber=71234567")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value("71234567"));

        // 5. Update employee (self token)
        String updateEmpJson = """
                {
                    "firstName": "Luis Alberto",
                    "lastName": "Mendoza",
                    "documentType": "DNI",
                    "documentNumber": "71234567",
                    "phone": "+51999888111"
                }
                """;

        mockMvc.perform(put("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + employeeSelfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateEmpJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Luis Alberto"));

        // 6. Delete employee (self token)
        mockMvc.perform(delete("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + employeeSelfToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should test full lifecycle of owner profiles and unified profile lookups")
    void shouldTestOwnerProfileAndUnifiedProfileLifecycle() throws Exception {
        UUID ownerUserId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String ownerToken = tokenDe(tenantId, ownerUserId, "OWNER");

        // 1. Create owner profile
        String createOwnerJson = """
                {
                    "userId": "%s",
                    "firstName": "Raul",
                    "lastName": "Castro",
                    "documentType": "DNI",
                    "documentNumber": "45678901",
                    "phone": "+51987112233"
                }
                """.formatted(ownerUserId);

        String ownerResponse = mockMvc.perform(post("/api/v1/owners")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOwnerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Raul"))
                .andExpect(jsonPath("$.documentNumber").value("45678901"))
                .andReturn().getResponse().getContentAsString();

        String ownerId = extraerId(ownerResponse);

        // 2. Query owner by ID
        mockMvc.perform(get("/api/v1/owners/" + ownerId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ownerId));

        // 3. Query owner by userId
        mockMvc.perform(get("/api/v1/owners?userId=" + ownerUserId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(ownerUserId.toString()));

        // 4. Query active roles in Profiles controller
        mockMvc.perform(get("/api/v1/profiles/roles?userId=" + ownerUserId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("ROLE_OWNER")));

        // 5. Query unified profile by documentNumber
        mockMvc.perform(get("/api/v1/profiles?documentNumber=45678901")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileType").value("OWNER"))
                .andExpect(jsonPath("$.documentNumber").value("45678901"));

        // 6. Delete owner profile
        mockMvc.perform(delete("/api/v1/owners/" + ownerId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should update workshop and list workshops by owner")
    void shouldManageWorkshops() throws Exception {
        TenantResult taller = crearTaller("Taller Castro Original", "20601111103");
        String token = tokenDe(taller.id(), "WORKSHOP_ADMIN");

        // 1. Update workshop
        String updateWorkshopJson = """
                {
                    "businessName": "Castro Motors Premium S.A.C.",
                    "brandName": "Castro Motors Pro",
                    "taxId": "20601111103",
                    "mileageIntervalConfig": 7500
                }
                """;

        mockMvc.perform(put("/api/v1/workshops/" + taller.id())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateWorkshopJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Castro Motors Pro"))
                .andExpect(jsonPath("$.mileageIntervalConfig").value(7500));

        // 2. Query workshop by ID
        mockMvc.perform(get("/api/v1/workshops/" + taller.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taller.id().toString()))
                .andExpect(jsonPath("$.businessName").value("Castro Motors Pro"));
    }

    private TenantResult crearTaller(String nombre, String ruc) {
        return tenantProvisioningService.provisionTenant(
                new ProvisionTenantCommand(nombre, ruc, "BASICO", null, "#1A2B3C"));
    }

    private String tokenDe(UUID tenantId, String rol) {
        return TestJwksServer.signToken(tenantId, List.of(rol));
    }

    private String tokenDe(UUID tenantId, UUID userId, String rol) {
        return TestJwksServer.signToken(tenantId, userId, List.of(rol));
    }

    private String extraerId(String json) {
        int idx = json.indexOf("\"id\":\"");
        if (idx == -1) return null;
        int start = idx + 6;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
