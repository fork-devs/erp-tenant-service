package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.TenantOutboxJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTenantOutboxRepository extends JpaRepository<TenantOutboxJpaEntity, UUID> {

    @Query("SELECT o FROM TenantOutboxJpaEntity o WHERE o.published = false ORDER BY o.createdAt ASC")
    List<TenantOutboxJpaEntity> findUnpublished(Pageable pageable);

    @Modifying
    @Query("UPDATE TenantOutboxJpaEntity o SET o.published = true WHERE o.id = :id")
    void markAsPublished(@Param("id") UUID id);
}
