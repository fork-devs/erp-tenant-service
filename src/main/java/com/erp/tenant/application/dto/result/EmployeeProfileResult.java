package com.erp.tenant.application.dto.result;

import com.erp.tenant.domain.model.EmployeeProfile;

import java.time.Instant;
import java.util.UUID;

public record EmployeeProfileResult(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {
    public static EmployeeProfileResult from(EmployeeProfile emp) {
        if (emp == null) return null;
        return new EmployeeProfileResult(
                emp.getId(),
                emp.getUserId(),
                emp.getFirstName(),
                emp.getLastName(),
                emp.getDocumentType().name(),
                emp.getDocumentNumber(),
                emp.getPhone(),
                emp.getCreatedAt(),
                emp.getUpdatedAt()
        );
    }
}
