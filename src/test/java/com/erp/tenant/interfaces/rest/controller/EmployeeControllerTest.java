package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.command.CreateEmployeeProfileCommand;
import com.erp.tenant.application.dto.result.EmployeeProfileResult;
import com.erp.tenant.application.usecase.GetEmployeeProfilesUseCase;
import com.erp.tenant.application.usecase.ManageEmployeeProfileUseCase;
import com.erp.tenant.domain.exception.DocumentNotFoundException;
import com.erp.tenant.domain.model.DocumentType;
import com.erp.tenant.infrastructure.security.MultiTenancySecurityService;
import com.erp.tenant.interfaces.rest.advice.GlobalRestExceptionHandler;
import com.erp.tenant.interfaces.rest.dto.request.CreateEmployeeProfileRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeControllerTest {

    private MockMvc mockMvc;
    private ManageEmployeeProfileUseCase manageEmployeeUseCase;
    private GetEmployeeProfilesUseCase getEmployeeUseCase;
    private MultiTenancySecurityService securityService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        manageEmployeeUseCase = mock(ManageEmployeeProfileUseCase.class);
        getEmployeeUseCase = mock(GetEmployeeProfilesUseCase.class);
        securityService = mock(MultiTenancySecurityService.class);
        objectMapper = new ObjectMapper();

        EmployeeController controller = new EmployeeController(manageEmployeeUseCase, getEmployeeUseCase, securityService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should create employee profile with HTTP 201 Created")
    void shouldCreateEmployeeProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();
        CreateEmployeeProfileRequest request = new CreateEmployeeProfileRequest(
                userId, "Reynaldo", "Machacca", "DNI", "40364594", "+51987111222"
        );

        EmployeeProfileResult result = new EmployeeProfileResult(
                empId, userId, "REYNALDO", "MACHACCA GONZALES", "DNI", "40364594", "+51987111222", Instant.now(), Instant.now()
        );

        when(manageEmployeeUseCase.create(any(CreateEmployeeProfileCommand.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(empId.toString()))
                .andExpect(jsonPath("$.firstName").value("REYNALDO"))
                .andExpect(jsonPath("$.lastName").value("MACHACCA GONZALES"))
                .andExpect(jsonPath("$.documentNumber").value("40364594"));
    }

    @Test
    @DisplayName("Should return HTTP 422 when employee DNI fails RENIEC validation")
    void shouldReturn422WhenDniNotFoundInReniec() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateEmployeeProfileRequest request = new CreateEmployeeProfileRequest(
                userId, "Fake", "Person", "DNI", "00000000", "+51987111222"
        );

        when(manageEmployeeUseCase.create(any(CreateEmployeeProfileCommand.class)))
                .thenThrow(new DocumentNotFoundException("00000000"));

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("DOCUMENT_NOT_FOUND_RENIEC"))
                .andExpect(jsonPath("$.status").value(422));
    }
}
