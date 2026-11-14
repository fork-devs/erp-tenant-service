package com.erp.tenant.infrastructure.persistence.rls;

import com.erp.tenant.infrastructure.security.TenantContextHolder;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * Enforces fail-closed PostgreSQL Row-Level Security (RLS) by binding {@code app.tenant_id}
 * to the active database transaction before executing tenant-scoped persistence adapters.
 */
@Aspect
@Component
@Order(1)
public class PostgresTenantRlsAspect {

    private final EntityManager entityManager;

    public PostgresTenantRlsAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before("execution(* com.erp.tenant.infrastructure.persistence.adapter.BranchPersistenceAdapter.*(..)) || "
            + "execution(* com.erp.tenant.infrastructure.persistence.adapter.BranchSubscriptionPersistenceAdapter.*(..)) || "
            + "execution(* com.erp.tenant.infrastructure.persistence.adapter.EmployeeProfilePersistenceAdapter.*(..)) || "
            + "execution(* com.erp.tenant.infrastructure.persistence.adapter.OwnerProfilePersistenceAdapter.*(..)) || "
            + "execution(* com.erp.tenant.infrastructure.persistence.adapter.SubscriptionPersistenceAdapter.*(..)) || "
            + "execution(* com.erp.tenant.infrastructure.persistence.adapter.WorkshopSpecialtyPersistenceAdapter.*(..)) || "
            + "execution(* com.erp.tenant.infrastructure.persistence.adapter.WorkshopSpecialtyRevisionPersistenceAdapter.*(..)) || "
            + "execution(* com.erp.tenant.infrastructure.security.MultiTenancySecurityService.isAuthorizedForBranch(..)) || "
            + "execution(* com.erp.tenant.infrastructure.security.MultiTenancySecurityService.isAuthorizedForUser(..))")
    public void applyTenantContext() {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("TenantContextHolder is empty: tenant-scoped persistence operation blocked (fail-closed)");
        }
        if (new UUID(0L, 0L).equals(tenantId)) {
            throw new IllegalStateException("Nil tenant UUID is not permitted for RLS context");
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            entityManager.createNativeQuery("SELECT set_config('app.tenant_id', :tenantId, true)")
                    .setParameter("tenantId", tenantId.toString())
                    .getSingleResult();
        }
    }
}
