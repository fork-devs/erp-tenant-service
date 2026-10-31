package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.CreateOwnerProfileCommand;
import com.erp.tenant.application.dto.command.UpdateOwnerProfileCommand;
import com.erp.tenant.application.dto.result.OwnerProfileResult;
import com.erp.tenant.application.usecase.GetOwnerProfilesUseCase;
import com.erp.tenant.application.usecase.ManageOwnerProfileUseCase;
import com.erp.tenant.domain.exception.CannotDeleteOwnerWithWorkshopsException;
import com.erp.tenant.domain.exception.DocumentAlreadyExistsException;
import com.erp.tenant.domain.exception.OwnerAlreadyExistsException;
import com.erp.tenant.domain.exception.OwnerNotFoundException;
import com.erp.tenant.domain.model.DocumentType;
import com.erp.tenant.domain.model.OwnerProfile;
import com.erp.tenant.domain.port.OwnerProfileRepositoryPort;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.infrastructure.security.TenantContextHolder;

import java.util.UUID;

public class OwnerProfileService implements ManageOwnerProfileUseCase, GetOwnerProfilesUseCase {

    private final OwnerProfileRepositoryPort ownerRepositoryPort;
    private final TenantRepositoryPort tenantRepositoryPort;
    private final UnitOfWork unitOfWork;

    public OwnerProfileService(OwnerProfileRepositoryPort ownerRepositoryPort,
                               TenantRepositoryPort tenantRepositoryPort,
                               UnitOfWork unitOfWork) {
        this.ownerRepositoryPort = ownerRepositoryPort;
        this.tenantRepositoryPort = tenantRepositoryPort;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public OwnerProfileResult create(CreateOwnerProfileCommand command) {
        if (command == null || command.userId() == null) {
            throw new IllegalArgumentException("User ID is required to create owner profile");
        }
        if (ownerRepositoryPort.existsByUserId(command.userId())) {
            throw new OwnerAlreadyExistsException(command.userId());
        }
        if (ownerRepositoryPort.existsByDocumentNumber(command.documentNumber())) {
            throw new DocumentAlreadyExistsException(command.documentNumber());
        }

        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            tenantId = command.userId();
        }

        DocumentType docType = DocumentType.fromString(command.documentType());
        OwnerProfile owner = OwnerProfile.create(
                tenantId,
                command.userId(),
                command.firstName(),
                command.lastName(),
                docType,
                command.documentNumber(),
                command.phone()
        );

        OwnerProfile saved = unitOfWork.callInTransaction(() -> ownerRepositoryPort.save(owner));
        return OwnerProfileResult.from(saved);
    }

    @Override
    public OwnerProfileResult update(UpdateOwnerProfileCommand command) {
        if (command == null || command.ownerId() == null) {
            throw new IllegalArgumentException("Owner ID is required for update");
        }
        OwnerProfile owner = ownerRepositoryPort.findById(command.ownerId())
                .orElseThrow(() -> new OwnerNotFoundException(command.ownerId()));

        if (command.documentNumber() != null && !command.documentNumber().trim().equalsIgnoreCase(owner.getDocumentNumber())) {
            if (ownerRepositoryPort.existsByDocumentNumber(command.documentNumber())) {
                throw new DocumentAlreadyExistsException(command.documentNumber());
            }
        }

        DocumentType docType = command.documentType() != null ? DocumentType.fromString(command.documentType()) : null;
        owner.update(command.firstName(), command.lastName(), docType, command.documentNumber(), command.phone());

        OwnerProfile saved = unitOfWork.callInTransaction(() -> ownerRepositoryPort.save(owner));
        return OwnerProfileResult.from(saved);
    }

    @Override
    public void delete(UUID ownerId) {
        OwnerProfile owner = ownerRepositoryPort.findById(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));

        if (tenantRepositoryPort.existsByOwnerId(ownerId)) {
            throw new CannotDeleteOwnerWithWorkshopsException(ownerId);
        }

        unitOfWork.runInTransaction(() -> ownerRepositoryPort.delete(owner.getId()));
    }

    @Override
    public OwnerProfileResult getById(UUID ownerId) {
        return ownerRepositoryPort.findById(ownerId)
                .map(OwnerProfileResult::from)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));
    }

    @Override
    public OwnerProfileResult getByUserId(UUID userId) {
        return ownerRepositoryPort.findByUserId(userId)
                .map(OwnerProfileResult::from)
                .orElseThrow(() -> new OwnerNotFoundException("Owner profile not found for user: " + userId));
    }
}
