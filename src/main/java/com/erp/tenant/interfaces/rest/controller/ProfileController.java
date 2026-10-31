package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.result.ProfileUnifiedResult;
import com.erp.tenant.application.usecase.GetProfilesUseCase;
import com.erp.tenant.domain.exception.TenantAccessDeniedException;
import com.erp.tenant.infrastructure.security.MultiTenancySecurityService;
import com.erp.tenant.interfaces.rest.dto.response.ProfileUnifiedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profiles", description = "Consultas transversales de perfiles de negocio y roles de usuario")
public class ProfileController {

    private final GetProfilesUseCase getProfilesUseCase;
    private final MultiTenancySecurityService securityService;

    public ProfileController(GetProfilesUseCase getProfilesUseCase,
                             MultiTenancySecurityService securityService) {
        this.getProfilesUseCase = getProfilesUseCase;
        this.securityService = securityService;
    }

    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener Roles Activos del Usuario", description = "Retorna lista de roles de perfiles de negocio para redirección post-login")
    public ResponseEntity<List<String>> getRolesByUserId(@RequestParam(name = "userId") UUID userId) {
        if (!securityService.isAuthorizedForUser(userId)) {
            throw new TenantAccessDeniedException("Access denied for user profile: " + userId);
        }
        List<String> roles = getProfilesUseCase.getRolesByUserId(userId);
        return ResponseEntity.ok(roles);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar Perfil por Documento", description = "Búsqueda unificada por número de documento (DNI/RUC) across perfiles")
    public ResponseEntity<ProfileUnifiedResponse> getProfileByDocumentNumber(
            @RequestParam(name = "documentNumber") String documentNumber) {
        ProfileUnifiedResult result = getProfilesUseCase.getByDocumentNumber(documentNumber);
        if (!securityService.isAuthorizedForUser(result.userId())) {
            throw new TenantAccessDeniedException("Access denied for profile document: " + documentNumber);
        }
        return ResponseEntity.ok(ProfileUnifiedResponse.from(result));
    }
}
