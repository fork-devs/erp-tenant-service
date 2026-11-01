package com.erp.tenant.domain.exception;

import java.util.UUID;

public class EmployeeNotFoundException extends DomainException {
    public EmployeeNotFoundException(UUID employeeId) {
        super("EMPLOYEE_NOT_FOUND", "Employee profile not found with ID: " + employeeId);
    }

    public EmployeeNotFoundException(String message) {
        super("EMPLOYEE_NOT_FOUND", message);
    }
}
