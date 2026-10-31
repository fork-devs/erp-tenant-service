package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.result.EmployeeProfileResult;

import java.util.UUID;

public interface GetEmployeeProfilesUseCase {
    EmployeeProfileResult getById(UUID employeeId);
    EmployeeProfileResult getByUserId(UUID userId);
    EmployeeProfileResult getByDocumentNumber(String documentNumber);
}
