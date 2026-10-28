package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.usecase.ChangeTenantPlanUseCase;
import com.erp.tenant.application.usecase.GetTenantDetailsUseCase;
import com.erp.tenant.application.usecase.ProvisionTenantUseCase;
import com.erp.tenant.application.usecase.ReactivateTenantUseCase;
import com.erp.tenant.application.usecase.SuspendTenantUseCase;
import com.erp.tenant.application.usecase.UpdateBrandingUseCase;
import com.erp.tenant.infrastructure.config.I18nConfig;
import com.erp.tenant.interfaces.rest.advice.GlobalRestExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class I18nValidationTest {

    private MockMvc mockMvc;

    @Mock
    private ProvisionTenantUseCase provisionTenantUseCase;
    @Mock
    private ChangeTenantPlanUseCase changeTenantPlanUseCase;
    @Mock
    private SuspendTenantUseCase suspendTenantUseCase;
    @Mock
    private ReactivateTenantUseCase reactivateTenantUseCase;
    @Mock
    private UpdateBrandingUseCase updateBrandingUseCase;
    @Mock
    private GetTenantDetailsUseCase getTenantDetailsUseCase;

    @BeforeEach
    void setUp() {
        I18nConfig config = new I18nConfig();
        MessageSource messageSource = config.messageSource();
        LocalValidatorFactoryBean validator = config.getValidator(messageSource);
        validator.afterPropertiesSet();

        TenantController controller = new TenantController(
                provisionTenantUseCase,
                changeTenantPlanUseCase,
                suspendTenantUseCase,
                reactivateTenantUseCase,
                updateBrandingUseCase,
                getTenantDetailsUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setLocaleResolver(config.localeResolver())
                .setControllerAdvice(new GlobalRestExceptionHandler(messageSource))
                .build();
    }

    @Test
    @DisplayName("Accept-Language: es debe retornar mensajes de validación en español")
    void shouldReturnValidationErrorsInSpanish() throws Exception {
        mockMvc.perform(post("/api/v1/tenants")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validación Fallida"))
                .andExpect(jsonPath("$.invalidParams['commercialName']").value("El nombre comercial del taller es obligatorio."))
                .andExpect(jsonPath("$.invalidParams['ruc']").value("El RUC del taller es obligatorio."));
    }

    @Test
    @DisplayName("Accept-Language: en debe retornar mensajes de validación en inglés")
    void shouldReturnValidationErrorsInEnglish() throws Exception {
        mockMvc.perform(post("/api/v1/tenants")
                        .header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.invalidParams['commercialName']").value("The workshop commercial name is required."))
                .andExpect(jsonPath("$.invalidParams['ruc']").value("The workshop RUC is required."));
    }

    @Test
    @DisplayName("Sin header Accept-Language debe usar locale por defecto (es)")
    void shouldFallbackToDefaultLocaleWhenHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validación Fallida"))
                .andExpect(jsonPath("$.invalidParams['commercialName']").value("El nombre comercial del taller es obligatorio."));
    }
}
