package com.erp.tenant.infrastructure.config;

import com.erp.tenant.application.service.OutboxDeadLetterService;
import com.erp.tenant.application.service.OutboxProcessorService;
import com.erp.tenant.application.service.PlanQueryService;
import com.erp.tenant.application.service.TenantManagementService;
import com.erp.tenant.application.service.TenantProvisioningService;
import com.erp.tenant.domain.port.EventPublisherPort;
import com.erp.tenant.domain.port.PlanRepositoryPort;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.infrastructure.messaging.DomainEventSerializer;
import org.springframework.beans.factory.annotation.Value;
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
            TenantOutboxRepositoryPort outboxRepositoryPort,
            UnitOfWork unitOfWork,
            DomainEventSerializer eventSerializer) {
        return new TenantProvisioningService(tenantRepositoryPort, planRepositoryPort, outboxRepositoryPort,
                unitOfWork, eventSerializer);
    }

    @Bean
    public TenantManagementService tenantManagementService(
            TenantRepositoryPort tenantRepositoryPort,
            PlanRepositoryPort planRepositoryPort,
            TenantOutboxRepositoryPort outboxRepositoryPort,
            UnitOfWork unitOfWork,
            DomainEventSerializer eventSerializer) {
        return new TenantManagementService(tenantRepositoryPort, planRepositoryPort, outboxRepositoryPort,
                unitOfWork, eventSerializer);
    }

    @Bean
    public PlanQueryService planQueryService(PlanRepositoryPort planRepositoryPort) {
        return new PlanQueryService(planRepositoryPort);
    }

    @Bean
    public OutboxProcessorService outboxProcessorService(
            TenantOutboxRepositoryPort outboxRepositoryPort,
            EventPublisherPort eventPublisherPort,
            UnitOfWork unitOfWork,
            @Value("${erp.tenant.outbox.max-attempts:7}") int maxAttempts,
            @Value("${erp.tenant.outbox.max-delay:PT24H}") java.time.Duration maxDelay) {
        return new OutboxProcessorService(outboxRepositoryPort, eventPublisherPort, unitOfWork, maxAttempts, maxDelay);
    }

    @Bean
    public OutboxDeadLetterService outboxDeadLetterService(
            TenantOutboxRepositoryPort outboxRepositoryPort,
            UnitOfWork unitOfWork) {
        return new OutboxDeadLetterService(outboxRepositoryPort, unitOfWork);
    }
}
