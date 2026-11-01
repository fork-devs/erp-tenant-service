package com.erp.tenant.domain.exception;

import java.util.UUID;

public class OwnerNotFoundException extends DomainException {
    public OwnerNotFoundException(UUID ownerId) {
        super("OWNER_NOT_FOUND", "Owner profile not found with ID: " + ownerId);
    }

    public OwnerNotFoundException(String message) {
        super("OWNER_NOT_FOUND", message);
    }
}
