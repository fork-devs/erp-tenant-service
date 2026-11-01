package com.erp.tenant.domain.exception;

import java.util.UUID;

public class BranchCodeAlreadyExistsException extends DomainException {
    public BranchCodeAlreadyExistsException(UUID workshopId, String code) {
        super("BRANCH_CODE_ALREADY_EXISTS", "A branch with code '" + code + "' already exists for workshop: " + workshopId);
    }
}
