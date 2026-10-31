package com.erp.tenant.domain.exception;

import java.util.UUID;

public class ActiveSubscriptionAlreadyExistsException extends DomainException {
    public ActiveSubscriptionAlreadyExistsException(UUID branchId) {
        super("ACTIVE_SUBSCRIPTION_ALREADY_EXISTS", "Branch already has an active subscription: " + branchId);
    }
}
