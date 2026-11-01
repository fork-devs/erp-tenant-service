package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.usecase.ManageWorkshopUseCase;
import com.erp.tenant.application.usecase.ProvisionTenantUseCase;
import com.erp.tenant.domain.port.IdentityValidationPort;
import com.erp.tenant.domain.port.RucValidationResult;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.interfaces.rest.advice.GlobalRestExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkshopControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageWorkshopUseCase manageWorkshopUseCase;

    @Mock
    private ProvisionTenantUseCase provisionTenantUseCase;

    @Mock
    private TenantRepositoryPort tenantRepositoryPort;

    @Mock
    private IdentityValidationPort identityValidationPort;

    @BeforeEach
    void setUp() {
        WorkshopController controller = new WorkshopController(
                manageWorkshopUseCase,
                provisionTenantUseCase,
                tenantRepositoryPort,
                identityValidationPort
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/workshops with valid, active and habido RUC should return 201 Created")
    void createWorkshop_WhenValidRuc_ShouldReturn201() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID workshopId = UUID.randomUUID();

        RucValidationResult rucResult = new RucValidationResult(
                "20100070970",
                "SUPERMERCADOS PERUANOS S.A.",
                "PLAZA VEA",
                "ACTIVO",
                "HABIDO",
                "CAL. MORELLI NRO. 181",
                true
        );
        when(identityValidationPort.validateRuc("20100070970")).thenReturn(rucResult);
        when(tenantRepositoryPort.findByRuc(any())).thenReturn(Optional.empty());
        when(provisionTenantUseCase.provisionTenant(any())).thenReturn(new TenantResult(
                workshopId, "SUPERMERCADOS PERUANOS S.A.", "20100070970", UUID.randomUUID(), "ENTERPRISE", "ACTIVE", "#000000", null
        ));
        when(tenantRepositoryPort.findById(any())).thenReturn(Optional.empty());

        String json = """
                {
                    "ownerId": "%s",
                    "businessName": "My Shop",
                    "brandName": "My Brand",
                    "taxId": "20100070970",
                    "mileageIntervalConfig": 5000
                }
                """.formatted(ownerId);

        mockMvc.perform(post("/api/v1/workshops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(workshopId.toString()))
                .andExpect(jsonPath("$.businessName").value("SUPERMERCADOS PERUANOS S.A."))
                .andExpect(jsonPath("$.taxId").value("20100070970"));
    }

    @Test
    @DisplayName("POST /api/v1/workshops with invalid or non-existent RUC in SUNAT should return 422 Unprocessable Entity")
    void createWorkshop_WhenRucNotFoundInSunat_ShouldReturn422() throws Exception {
        UUID ownerId = UUID.randomUUID();
        when(identityValidationPort.validateRuc("20705140791")).thenReturn(RucValidationResult.invalid("20705140791"));

        String json = """
                {
                    "ownerId": "%s",
                    "businessName": "Fictitious Shop",
                    "brandName": "Fake Motors",
                    "taxId": "20705140791",
                    "mileageIntervalConfig": 5000
                }
                """.formatted(ownerId);

        mockMvc.perform(post("/api/v1/workshops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("RUC_NOT_FOUND_OR_INACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/workshops with inactive or no-habido RUC should return 422 Unprocessable Entity")
    void createWorkshop_WhenRucInactiveInSunat_ShouldReturn422() throws Exception {
        UUID ownerId = UUID.randomUUID();
        RucValidationResult inactiveRuc = new RucValidationResult(
                "20123456789",
                "INACTIVE COMPANY SAC",
                "INACTIVE",
                "SUSPENDIDO",
                "NO HABIDO",
                "Av. Principal 123",
                true
        );
        when(identityValidationPort.validateRuc("20123456789")).thenReturn(inactiveRuc);

        String json = """
                {
                    "ownerId": "%s",
                    "businessName": "Inactive Shop",
                    "brandName": "Inactive Motors",
                    "taxId": "20123456789",
                    "mileageIntervalConfig": 5000
                }
                """.formatted(ownerId);

        mockMvc.perform(post("/api/v1/workshops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("RUC_NOT_FOUND_OR_INACTIVE"));
    }
}
