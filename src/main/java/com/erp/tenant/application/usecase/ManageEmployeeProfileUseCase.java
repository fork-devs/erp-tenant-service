package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.command.CreateEmployeeProfileCommand;
import com.erp.tenant.application.dto.command.UpdateEmployeeProfileCommand;
import com.erp.tenant.application.dto.result.EmployeeProfileResult;

import java.util.UUID;

public interface ManageEmployeeProfileUseCase {
    EmployeeProfileResult create(CreateEmployeeProfileCommand command);
    EmployeeProfileResult update(UpdateEmployeeProfileCommand command);
    void delete(UUID employeeId);
}
