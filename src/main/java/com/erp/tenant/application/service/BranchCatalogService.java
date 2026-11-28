package com.erp.tenant.application.service;

import com.erp.tenant.infrastructure.persistence.entity.BranchJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataBranchRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataTenantRepository;
import com.erp.tenant.infrastructure.security.TenantContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-only query service for the B2C branch catalog (SRP).
 */
@Service
public class BranchCatalogService {

    public record BranchCatalogItem(
            UUID id,
            UUID workshopId,
            String code,
            String name,
            String address,
            String phone,
            Double latitude,
            Double longitude,
            String bannerImageUrl,
            String coverImageUrl,
            String workshopBrandName,
            String workshopBusinessName,
            Double averageRating,
            Integer reviewCount,
            Integer activeBaysCount,
            Integer freeBaysToday
    ) {}

    private final SpringDataBranchRepository branchRepository;
    private final SpringDataTenantRepository tenantRepository;

    public BranchCatalogService(SpringDataBranchRepository branchRepository,
                                SpringDataTenantRepository tenantRepository) {
        this.branchRepository = branchRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public List<BranchCatalogItem> listCatalog(UUID workshopId) {
        UUID effectiveWorkshopId = workshopId != null ? workshopId : TenantContextHolder.getTenantId();
        List<BranchJpaEntity> branches = effectiveWorkshopId != null
                ? branchRepository.findByWorkshopId(effectiveWorkshopId)
                : branchRepository.findAll();

        List<BranchJpaEntity> activeBranches = branches.stream()
                .filter(BranchJpaEntity::isActive)
                .toList();

        Map<UUID, TenantJpaEntity> tenantsById = tenantRepository.findAllById(
                activeBranches.stream().map(BranchJpaEntity::getWorkshopId).distinct().toList()
        ).stream().collect(Collectors.toMap(TenantJpaEntity::getId, Function.identity()));

        return activeBranches.stream()
                .map(b -> {
                    TenantJpaEntity t = tenantsById.get(b.getWorkshopId());
                    String brandName = t != null ? t.getCommercialName() : null;
                    return new BranchCatalogItem(
                            b.getId(),
                            b.getWorkshopId(),
                            b.getCode(),
                            b.getName(),
                            b.getAddress(),
                            b.getPhone(),
                            b.getLatitude(),
                            b.getLongitude(),
                            b.getBannerImageUrl(),
                            b.getBannerImageUrl(),
                            brandName,
                            brandName,
                            0.0,
                            0,
                            0,
                            0
                    );
                })
                .toList();
    }
}
