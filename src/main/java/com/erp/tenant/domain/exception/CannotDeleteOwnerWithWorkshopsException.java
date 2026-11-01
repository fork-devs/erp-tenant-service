package com.erp.tenant.domain.exception;

import java.util.UUID;

public class CannotDeleteOwnerWithWorkshopsException extends DomainException {
    public CannotDeleteOwnerWithWorkshopsException(UUID ownerId) {
        super("CANNOT_DELETE_OWNER_WITH_WORKSHOPS", "Cannot delete owner profile because they have associated workshops: " + ownerId);
    }
}
