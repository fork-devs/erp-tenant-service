package com.erp.tenant.infrastructure.config;

import com.erp.tenant.application.service.OutboxProcessorService;
import com.erp.tenant.application.service.PlanQueryService;
import com.erp.tenant.application.service.TenantManagementService;
import com.erp.tenant.application.service.TenantProvisioningService;
import com.erp.tenant.domain.port.EventPublisherPort;
import com.erp.tenant.domain.port.PlanRepositoryPort;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ApplicationServiceConfig {

    @Bean
    public TenantProvisioningService tenantProvisioningService(
            TenantRepositoryPort tenantRepositoryPort,
            PlanRepositoryPort planRepositoryPort,
            TenantOutboxRepositoryPort outboxRepositoryPort) {
        return new TenantProvisioningService(tenantRepositoryPort, planRepositoryPort, outboxRepositoryPort);
    }

    @Bean
    public TenantManagementService tenantManagementService(
            TenantRepositoryPort tenantRepositoryPort,
            PlanRepositoryPort planRepositoryPort,
            TenantOutboxRepositoryPort outboxRepositoryPort) {
        return new TenantManagementService(tenantRepositoryPort, planRepositoryPort, outboxRepositoryPort);
    }

    @Bean
    public PlanQueryService planQueryService(PlanRepositoryPort planRepositoryPort) {
        return new PlanQueryService(planRepositoryPort);
    }

    @Bean
    public OutboxProcessorService outboxProcessorService(
            TenantOutboxRepositoryPort outboxRepositoryPort,
            EventPublisherPort eventPublisherPort) {
        return new OutboxProcessorService(outboxRepositoryPort, eventPublisherPort);
    }
}
