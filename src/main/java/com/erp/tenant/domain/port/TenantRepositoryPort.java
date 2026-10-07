package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.valueobject.Ruc;
import com.erp.tenant.domain.valueobject.TenantId;

import java.util.List;
import java.util.Optional;

public interface TenantRepositoryPort {
    Tenant save(Tenant tenant);
    Optional<Tenant> findById(TenantId id);
    Optional<Tenant> findByRuc(Ruc ruc);
    boolean existsByRuc(Ruc ruc);
    List<Tenant> findAll();
}
