package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.CreateEmployeeProfileCommand;
import com.erp.tenant.application.dto.command.UpdateEmployeeProfileCommand;
import com.erp.tenant.application.dto.result.EmployeeProfileResult;
import com.erp.tenant.application.usecase.GetEmployeeProfilesUseCase;
import com.erp.tenant.application.usecase.ManageEmployeeProfileUseCase;
import com.erp.tenant.domain.exception.DocumentAlreadyExistsException;
import com.erp.tenant.domain.exception.EmployeeNotFoundException;
import com.erp.tenant.domain.model.DocumentType;
import com.erp.tenant.domain.model.EmployeeProfile;
import com.erp.tenant.domain.port.EmployeeProfileRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.infrastructure.security.TenantContextHolder;

import java.util.UUID;

public class EmployeeProfileService implements ManageEmployeeProfileUseCase, GetEmployeeProfilesUseCase {

    private final EmployeeProfileRepositoryPort employeeRepositoryPort;
    private final UnitOfWork unitOfWork;

    public EmployeeProfileService(EmployeeProfileRepositoryPort employeeRepositoryPort,
                                  UnitOfWork unitOfWork) {
        this.employeeRepositoryPort = employeeRepositoryPort;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public EmployeeProfileResult create(CreateEmployeeProfileCommand command) {
        if (command == null || command.userId() == null) {
            throw new IllegalArgumentException("User ID is required to create employee profile");
        }
        if (employeeRepositoryPort.existsByDocumentNumber(command.documentNumber())) {
            throw new DocumentAlreadyExistsException(command.documentNumber());
        }

        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            tenantId = command.userId();
        }

        DocumentType docType = DocumentType.fromString(command.documentType());
        EmployeeProfile employee = EmployeeProfile.create(
                tenantId,
                command.userId(),
                command.firstName(),
                command.lastName(),
                docType,
                command.documentNumber(),
                command.phone()
        );

        EmployeeProfile saved = unitOfWork.callInTransaction(() -> employeeRepositoryPort.save(employee));
        return EmployeeProfileResult.from(saved);
    }

    @Override
    public EmployeeProfileResult update(UpdateEmployeeProfileCommand command) {
        if (command == null || command.employeeId() == null) {
            throw new IllegalArgumentException("Employee ID is required for update");
        }
        EmployeeProfile employee = employeeRepositoryPort.findById(command.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));

        if (command.documentNumber() != null && !command.documentNumber().trim().equalsIgnoreCase(employee.getDocumentNumber())) {
            if (employeeRepositoryPort.existsByDocumentNumber(command.documentNumber())) {
                throw new DocumentAlreadyExistsException(command.documentNumber());
            }
        }

        DocumentType docType = command.documentType() != null ? DocumentType.fromString(command.documentType()) : null;
        employee.update(command.firstName(), command.lastName(), docType, command.documentNumber(), command.phone());

        EmployeeProfile saved = unitOfWork.callInTransaction(() -> employeeRepositoryPort.save(employee));
        return EmployeeProfileResult.from(saved);
    }

    @Override
    public void delete(UUID employeeId) {
        EmployeeProfile employee = employeeRepositoryPort.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        unitOfWork.runInTransaction(() -> employeeRepositoryPort.delete(employee.getId()));
    }

    @Override
    public EmployeeProfileResult getById(UUID employeeId) {
        return employeeRepositoryPort.findById(employeeId)
                .map(EmployeeProfileResult::from)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    @Override
    public EmployeeProfileResult getByUserId(UUID userId) {
        return employeeRepositoryPort.findByUserId(userId)
                .map(EmployeeProfileResult::from)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee profile not found for user: " + userId));
    }

    @Override
    public EmployeeProfileResult getByDocumentNumber(String documentNumber) {
        return employeeRepositoryPort.findByDocumentNumber(documentNumber)
                .map(EmployeeProfileResult::from)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee profile not found for document: " + documentNumber));
    }
}
