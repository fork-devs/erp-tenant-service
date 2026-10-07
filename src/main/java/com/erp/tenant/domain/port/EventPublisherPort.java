package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.TenantOutboxMessage;

public interface EventPublisherPort {
    void publish(TenantOutboxMessage message);
}
