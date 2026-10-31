package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.command.CreateEmployeeProfileCommand;
import com.erp.tenant.application.dto.command.UpdateEmployeeProfileCommand;
import com.erp.tenant.application.dto.result.EmployeeProfileResult;
import com.erp.tenant.application.usecase.GetEmployeeProfilesUseCase;
import com.erp.tenant.application.usecase.ManageEmployeeProfileUseCase;
import com.erp.tenant.domain.exception.TenantAccessDeniedException;
import com.erp.tenant.infrastructure.security.MultiTenancySecurityService;
import com.erp.tenant.interfaces.rest.dto.request.CreateEmployeeProfileRequest;
import com.erp.tenant.interfaces.rest.dto.request.UpdateEmployeeProfileRequest;
import com.erp.tenant.interfaces.rest.dto.response.EmployeeProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Profiles", description = "Endpoints para la gestión de perfiles de negocio de empleados")
public class EmployeeController {

    private final ManageEmployeeProfileUseCase manageEmployeeUseCase;
    private final GetEmployeeProfilesUseCase getEmployeeUseCase;
    private final MultiTenancySecurityService securityService;

    public EmployeeController(ManageEmployeeProfileUseCase manageEmployeeUseCase,
                              GetEmployeeProfilesUseCase getEmployeeUseCase,
                              MultiTenancySecurityService securityService) {
        this.manageEmployeeUseCase = manageEmployeeUseCase;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.securityService = securityService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForUser(#request.userId())")
    @Operation(summary = "Crear Registro de Empleado", description = "Crea perfil de empleado vinculado a User (IAM)")
    public ResponseEntity<EmployeeProfileResponse> createEmployee(@Valid @RequestBody CreateEmployeeProfileRequest request) {
        CreateEmployeeProfileCommand command = new CreateEmployeeProfileCommand(
                request.userId(),
                request.firstName(),
                request.lastName(),
                request.documentType(),
                request.documentNumber(),
                request.phone()
        );
        EmployeeProfileResult result = manageEmployeeUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeProfileResponse.from(result));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener Empleado por ID", description = "Retorna perfil de empleado validando acceso")
    public ResponseEntity<EmployeeProfileResponse> getEmployeeById(@PathVariable UUID employeeId) {
        EmployeeProfileResult result = getEmployeeUseCase.getById(employeeId);
        if (!securityService.isAuthorizedForUser(result.userId())) {
            throw new TenantAccessDeniedException("Access denied for user profile: " + result.userId());
        }
        return ResponseEntity.ok(EmployeeProfileResponse.from(result));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar Empleado por User ID o Documento", description = "Busca empleado por query param userId o documentNumber")
    public ResponseEntity<EmployeeProfileResponse> searchEmployee(
            @RequestParam(name = "userId", required = false) UUID userId,
            @RequestParam(name = "documentNumber", required = false) String documentNumber) {
        if (userId != null) {
            if (!securityService.isAuthorizedForUser(userId)) {
                throw new TenantAccessDeniedException("Access denied for user profile: " + userId);
            }
            EmployeeProfileResult result = getEmployeeUseCase.getByUserId(userId);
            return ResponseEntity.ok(EmployeeProfileResponse.from(result));
        }
        if (documentNumber != null && !documentNumber.trim().isEmpty()) {
            EmployeeProfileResult result = getEmployeeUseCase.getByDocumentNumber(documentNumber.trim());
            if (!securityService.isAuthorizedForUser(result.userId())) {
                throw new TenantAccessDeniedException("Access denied for employee profile with document: " + documentNumber);
            }
            return ResponseEntity.ok(EmployeeProfileResponse.from(result));
        }
        throw new IllegalArgumentException("Either userId or documentNumber must be provided as query parameter");
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar Empleado", description = "Actualiza datos de empleado bajo política de auto-gestión estricta (sin bypass de OWNER)")
    public ResponseEntity<EmployeeProfileResponse> updateEmployee(
            @PathVariable UUID employeeId,
            @RequestBody UpdateEmployeeProfileRequest request) {
        EmployeeProfileResult existing = getEmployeeUseCase.getById(employeeId);
        if (!securityService.isAuthorizedForSelf(existing.userId())) {
            throw new TenantAccessDeniedException("Self-management access required to update employee profile: " + existing.userId());
        }

        UpdateEmployeeProfileCommand command = new UpdateEmployeeProfileCommand(
                employeeId,
                request.firstName(),
                request.lastName(),
                request.documentType(),
                request.documentNumber(),
                request.phone()
        );
        EmployeeProfileResult updated = manageEmployeeUseCase.update(command);
        return ResponseEntity.ok(EmployeeProfileResponse.from(updated));
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Eliminar Empleado", description = "Elimina perfil de empleado bajo política de auto-gestión estricta (sin bypass de OWNER)")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID employeeId) {
        EmployeeProfileResult existing = getEmployeeUseCase.getById(employeeId);
        if (!securityService.isAuthorizedForSelf(existing.userId())) {
            throw new TenantAccessDeniedException("Self-management access required to delete employee profile: " + existing.userId());
        }

        manageEmployeeUseCase.delete(employeeId);
        return ResponseEntity.noContent().build();
    }
}
