package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.result.ProfileUnifiedResult;
import com.erp.tenant.application.usecase.GetProfilesUseCase;
import com.erp.tenant.domain.exception.ProfileNotFoundException;
import com.erp.tenant.domain.model.EmployeeProfile;
import com.erp.tenant.domain.model.OwnerProfile;
import com.erp.tenant.domain.port.EmployeeProfileRepositoryPort;
import com.erp.tenant.domain.port.OwnerProfileRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProfileService implements GetProfilesUseCase {

    private final OwnerProfileRepositoryPort ownerRepositoryPort;
    private final EmployeeProfileRepositoryPort employeeRepositoryPort;

    public ProfileService(OwnerProfileRepositoryPort ownerRepositoryPort,
                          EmployeeProfileRepositoryPort employeeRepositoryPort) {
        this.ownerRepositoryPort = ownerRepositoryPort;
        this.employeeRepositoryPort = employeeRepositoryPort;
    }

    @Override
    public List<String> getRolesByUserId(UUID userId) {
        List<String> roles = new ArrayList<>();
        if (ownerRepositoryPort.findByUserId(userId).isPresent()) {
            roles.add("ROLE_OWNER");
        }
        if (employeeRepositoryPort.findByUserId(userId).isPresent()) {
            roles.add("ROLE_EMPLOYEE");
        }
        if (roles.isEmpty()) {
            throw new ProfileNotFoundException("User has no active business profiles: " + userId);
        }
        return roles;
    }

    @Override
    public ProfileUnifiedResult getByDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Document number is required");
        }
        String doc = documentNumber.trim();

        Optional<OwnerProfile> ownerOpt = ownerRepositoryPort.findByDocumentNumber(doc);
        if (ownerOpt.isPresent()) {
            OwnerProfile o = ownerOpt.get();
            return new ProfileUnifiedResult(
                    "OWNER",
                    o.getUserId(),
                    o.getFirstName(),
                    o.getLastName(),
                    o.getDocumentType().name(),
                    o.getDocumentNumber(),
                    o.getPhone(),
                    null,
                    o.getId(),
                    List.of("ROLE_OWNER")
            );
        }

        Optional<EmployeeProfile> empOpt = employeeRepositoryPort.findByDocumentNumber(doc);
        if (empOpt.isPresent()) {
            EmployeeProfile e = empOpt.get();
            return new ProfileUnifiedResult(
                    "EMPLOYEE",
                    e.getUserId(),
                    e.getFirstName(),
                    e.getLastName(),
                    e.getDocumentType().name(),
                    e.getDocumentNumber(),
                    e.getPhone(),
                    e.getId(),
                    null,
                    List.of("ROLE_EMPLOYEE")
            );
        }

        throw new ProfileNotFoundException("No profile found for document: " + doc);
    }
}
