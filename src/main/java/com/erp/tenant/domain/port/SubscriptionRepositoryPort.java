package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.Subscription;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepositoryPort {
    Subscription save(Subscription subscription);
    Optional<Subscription> findByTenantId(UUID tenantId);
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
}
