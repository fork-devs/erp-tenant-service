package com.erp.tenant.domain.exception;

import java.util.UUID;

public class BranchNotFoundException extends DomainException {
    public BranchNotFoundException(UUID branchId) {
        super("BRANCH_NOT_FOUND", "Branch not found with ID: " + branchId);
    }
}
