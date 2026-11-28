package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.result.ProfileUnifiedResult;
import com.erp.tenant.application.usecase.GetProfilesUseCase;
import com.erp.tenant.domain.exception.ProfileNotFoundException;
import com.erp.tenant.domain.exception.TenantAccessDeniedException;
import com.erp.tenant.domain.port.CustomerProfileQueryPort;
import com.erp.tenant.infrastructure.adapter.external.MediaUploadClient;
import com.erp.tenant.infrastructure.persistence.entity.EmployeeProfileJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.OwnerProfileJpaEntity;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataEmployeeProfileRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataOwnerProfileRepository;
import com.erp.tenant.infrastructure.security.MultiTenancySecurityService;
import com.erp.tenant.interfaces.rest.dto.response.ProfileUnifiedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profiles", description = "Cross-cutting business profile and user role queries")
public class ProfileController {

    public record UpdateProfileImageRequest(@NotBlank String profileImageUrl) {}

    public record OwnerSummaryView(
            UUID ownerId,
            String firstName,
            String lastName,
            String documentType,
            String documentNumber,
            String phone,
            String profileImageUrl
    ) {}

    public record EmployeeSummaryView(
            UUID employeeId,
            UUID workshopId,
            String firstName,
            String lastName,
            String documentType,
            String documentNumber,
            String phone,
            String profileImageUrl
    ) {}

    public record UserProfileContextResponse(
            UUID userId,
            List<String> roles,
            OwnerSummaryView owner,
            EmployeeSummaryView employee,
            Object customer
    ) {}

    private final GetProfilesUseCase getProfilesUseCase;
    private final MultiTenancySecurityService securityService;
    private final SpringDataOwnerProfileRepository ownerProfileRepository;
    private final SpringDataEmployeeProfileRepository employeeProfileRepository;

    public ProfileController(GetProfilesUseCase getProfilesUseCase,
                             MultiTenancySecurityService securityService) {
        this(getProfilesUseCase, securityService, null, null);
    }

    @Autowired
    public ProfileController(GetProfilesUseCase getProfilesUseCase,
                             MultiTenancySecurityService securityService,
                             SpringDataOwnerProfileRepository ownerProfileRepository,
                             SpringDataEmployeeProfileRepository employeeProfileRepository) {
        this.getProfilesUseCase = getProfilesUseCase;
        this.securityService = securityService;
        this.ownerProfileRepository = ownerProfileRepository;
        this.employeeProfileRepository = employeeProfileRepository;
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

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get authenticated user profile context")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileContextResponse> getMyProfileContext(Authentication auth) {
        UUID userId = resolveAuthenticatedUserId(auth);
        return ResponseEntity.ok(buildContextResponse(userId, auth));
    }

    @Autowired(required = false)
    private MediaUploadClient mediaUploadClient;

    @Autowired(required = false)
    private CustomerProfileQueryPort customerProfileQueryPort;

    @PutMapping("/me/image")
    @PatchMapping("/me/image")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update authenticated user profile image URL")
    @Transactional
    public ResponseEntity<UserProfileContextResponse> updateMyProfileImage(
            @Valid @RequestBody UpdateProfileImageRequest request,
            Authentication auth) {
        UUID userId = resolveAuthenticatedUserId(auth);
        Optional<OwnerProfileJpaEntity> ownerOpt = ownerProfileRepository != null
                ? ownerProfileRepository.findByUserId(userId) : Optional.empty();
        Optional<EmployeeProfileJpaEntity> empOpt = employeeProfileRepository != null
                ? employeeProfileRepository.findByUserId(userId) : Optional.empty();

        boolean customerUpdated = false;
        if (ownerOpt.isEmpty() && empOpt.isEmpty()) {
            if (customerProfileQueryPort != null
                    && customerProfileQueryPort.updateProfileImage(userId, request.profileImageUrl().trim()).isPresent()) {
                customerUpdated = true;
            }
            if (!customerUpdated) {
                throw new ProfileNotFoundException("No Core profiles associated with user: " + userId);
            }
        }

        Instant now = Instant.now();
        ownerOpt.ifPresent(o -> {
            o.setProfileImageUrl(request.profileImageUrl().trim());
            o.setUpdatedAt(now);
            ownerProfileRepository.save(o);
        });
        empOpt.ifPresent(e -> {
            e.setProfileImageUrl(request.profileImageUrl().trim());
            e.setUpdatedAt(now);
            employeeProfileRepository.save(e);
        });

        return ResponseEntity.ok(buildContextResponse(userId, auth));
    }

    @PutMapping(value = "/me/image/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload authenticated user profile image file")
    @Transactional
    public ResponseEntity<UserProfileContextResponse> uploadMyProfileImage(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            Authentication auth) {
        if (mediaUploadClient == null) {
            throw new IllegalStateException("Media upload client is not configured");
        }
        MediaUploadClient.UploadedMedia uploaded = mediaUploadClient.upload(file, "profiles");
        return updateMyProfileImage(new UpdateProfileImageRequest(uploaded.url()), auth);
    }

    private UserProfileContextResponse buildContextResponse(UUID userId, Authentication auth) {
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        OwnerSummaryView ownerView = ownerProfileRepository != null
                ? ownerProfileRepository.findByUserId(userId)
                .map(o -> new OwnerSummaryView(
                        o.getId(), o.getFirstName(), o.getLastName(),
                        o.getDocumentType(), o.getDocumentNumber(), o.getPhone(), o.getProfileImageUrl()))
                .orElse(null)
                : null;
        EmployeeSummaryView empView = employeeProfileRepository != null
                ? employeeProfileRepository.findByUserId(userId)
                .map(e -> new EmployeeSummaryView(
                        e.getId(), e.getTenantId(), e.getFirstName(), e.getLastName(),
                        e.getDocumentType(), e.getDocumentNumber(), e.getPhone(), e.getProfileImageUrl()))
                .orElse(null)
                : null;
        Object customerView = customerProfileQueryPort != null
                ? customerProfileQueryPort.findByUserId(userId).orElse(null)
                : null;
        return new UserProfileContextResponse(userId, roles, ownerView, empView, customerView);
    }

    private UUID resolveAuthenticatedUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new TenantAccessDeniedException("Authenticated user ID is missing");
        }
        try {
            return UUID.fromString(auth.getName().trim());
        } catch (IllegalArgumentException ex) {
            throw new TenantAccessDeniedException("Invalid authenticated user ID");
        }
    }
}
