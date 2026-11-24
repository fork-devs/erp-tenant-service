package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.command.AssignBranchSubscriptionCommand;
import com.erp.tenant.application.dto.command.CreateBranchCommand;
import com.erp.tenant.application.dto.command.UpdateBranchCommand;
import com.erp.tenant.application.dto.result.BranchResult;
import com.erp.tenant.application.dto.result.BranchSubscriptionResult;
import com.erp.tenant.application.usecase.GetBranchesUseCase;
import com.erp.tenant.application.usecase.ManageBranchSubscriptionUseCase;
import com.erp.tenant.application.usecase.ManageBranchUseCase;
import com.erp.tenant.domain.exception.BranchNotFoundException;
import com.erp.tenant.infrastructure.adapter.external.MediaUploadClient;
import com.erp.tenant.infrastructure.persistence.entity.BranchJpaEntity;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataBranchRepository;
import com.erp.tenant.interfaces.rest.dto.request.AssignBranchSubscriptionRequest;
import com.erp.tenant.interfaces.rest.dto.request.CreateBranchRequest;
import com.erp.tenant.interfaces.rest.dto.request.UpdateBranchRequest;
import com.erp.tenant.interfaces.rest.dto.response.BranchResponse;
import com.erp.tenant.interfaces.rest.dto.response.BranchSubscriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/branches")
@Tag(name = "Branches", description = "Endpoints para gestión de sedes de taller y suscripciones de sede")
public class BranchController {

    private final ManageBranchUseCase manageBranchUseCase;
    private final GetBranchesUseCase getBranchesUseCase;
    private final ManageBranchSubscriptionUseCase subscriptionUseCase;

    @Autowired(required = false)
    private MediaUploadClient mediaUploadClient;

    @Autowired(required = false)
    private SpringDataBranchRepository branchRepository;

    public BranchController(ManageBranchUseCase manageBranchUseCase,
                            GetBranchesUseCase getBranchesUseCase,
                            ManageBranchSubscriptionUseCase subscriptionUseCase) {
        this.manageBranchUseCase = manageBranchUseCase;
        this.getBranchesUseCase = getBranchesUseCase;
        this.subscriptionUseCase = subscriptionUseCase;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#request.workshopId())")
    @Operation(summary = "Crear Sede para un Taller", description = "Crea una nueva sede (branch) asociada a un taller")
    public ResponseEntity<BranchResponse> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        CreateBranchCommand command = new CreateBranchCommand(
                request.workshopId(),
                request.code(),
                request.name(),
                request.address(),
                request.phone()
        );
        BranchResult result = manageBranchUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(BranchResponse.from(result));
    }

    @PutMapping("/{branchId}")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    @Operation(summary = "Actualizar Sede", description = "Actualiza datos de la sede")
    public ResponseEntity<BranchResponse> updateBranch(@PathVariable UUID branchId,
                                                      @RequestBody UpdateBranchRequest request) {
        UpdateBranchCommand command = new UpdateBranchCommand(
                branchId,
                request.code(),
                request.name(),
                request.address(),
                request.phone()
        );
        BranchResult result = manageBranchUseCase.update(command);
        return ResponseEntity.ok(BranchResponse.from(result));
    }

    @GetMapping("/{branchId}")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    @Operation(summary = "Obtener Sede por ID", description = "Retorna detalle de la sede")
    public ResponseEntity<BranchResponse> getBranchById(@PathVariable UUID branchId) {
        BranchResult result = getBranchesUseCase.getById(branchId);
        return ResponseEntity.ok(BranchResponse.from(result));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    @Operation(summary = "Listar Sedes de un Taller", description = "Lista sedes del taller, filtrando por acceso del caller")
    public ResponseEntity<List<BranchResponse>> listBranches(@RequestParam(name = "workshopId") UUID workshopId) {
        List<BranchResponse> responses = getBranchesUseCase.listByWorkshop(workshopId).stream()
                .map(BranchResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{branchId}/subscriptions")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    @Operation(summary = "Asignar Suscripción a Sede", description = "Asigna plan de suscripción a la sede")
    public ResponseEntity<BranchSubscriptionResponse> assignSubscription(
            @PathVariable UUID branchId,
            @Valid @RequestBody AssignBranchSubscriptionRequest request) {
        AssignBranchSubscriptionCommand command = new AssignBranchSubscriptionCommand(
                branchId,
                request.planId(),
                request.billingCycle()
        );
        BranchSubscriptionResult result = subscriptionUseCase.assignSubscription(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(BranchSubscriptionResponse.from(result));
    }

    @PutMapping(value = "/{branchId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    @Transactional
    @Operation(summary = "Subir Banner de Sede", description = "Sube una imagen de banner a Cloudflare R2 y la asocia a la sede")
    public ResponseEntity<BranchResponse> uploadBanner(
            @PathVariable UUID branchId,
            @RequestParam("file") MultipartFile file) {
        if (mediaUploadClient == null || branchRepository == null) {
            throw new IllegalStateException("Media upload dependencies are not configured");
        }
        BranchJpaEntity entity = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException(branchId));
        MediaUploadClient.UploadedMedia uploaded = mediaUploadClient.upload(file, "branches/" + branchId);
        entity.setBannerImageUrl(uploaded.url());
        entity.setUpdatedAt(java.time.Instant.now());
        branchRepository.save(entity);
        return ResponseEntity.ok(new BranchResponse(
                entity.getId(),
                entity.getWorkshopId(),
                entity.getCode(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getBannerImageUrl()
        ));
    }

    @DeleteMapping("/{branchId}/subscription")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    @Operation(summary = "Cancelar Suscripción de Sede", description = "Cancela la suscripción activa de la sede")
    public ResponseEntity<Void> cancelSubscription(@PathVariable UUID branchId) {
        subscriptionUseCase.cancelSubscription(branchId);
        return ResponseEntity.noContent().build();
    }
}
