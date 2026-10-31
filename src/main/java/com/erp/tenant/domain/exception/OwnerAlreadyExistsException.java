package com.erp.tenant.domain.exception;

import java.util.UUID;

public class OwnerAlreadyExistsException extends DomainException {
    public OwnerAlreadyExistsException(UUID userId) {
        super("OWNER_ALREADY_EXISTS", "User already has an owner profile: " + userId);
    }
}
