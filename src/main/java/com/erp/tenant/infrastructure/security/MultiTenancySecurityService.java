package com.erp.tenant.infrastructure.security;

import com.erp.tenant.infrastructure.persistence.entity.BranchJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.EmployeeProfileJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.OwnerProfileJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataBranchRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataEmployeeProfileRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataOwnerProfileRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataTenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Evaluates security access control rules for workshops, branches, and user profiles.
 * Used in SpEL expressions such as:
 * {@code @PreAuthorize("@multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")}
 */
@Component("multiTenancySecurityService")
public class MultiTenancySecurityService {

    private final SpringDataBranchRepository branchRepository;
    private final SpringDataOwnerProfileRepository ownerProfileRepository;
    private final SpringDataEmployeeProfileRepository employeeProfileRepository;
    private final SpringDataTenantRepository tenantRepository;

    public MultiTenancySecurityService(SpringDataBranchRepository branchRepository,
                                       SpringDataOwnerProfileRepository ownerProfileRepository) {
        this(branchRepository, ownerProfileRepository, null, null);
    }

    public MultiTenancySecurityService(SpringDataBranchRepository branchRepository,
                                       SpringDataOwnerProfileRepository ownerProfileRepository,
                                       SpringDataEmployeeProfileRepository employeeProfileRepository) {
        this(branchRepository, ownerProfileRepository, employeeProfileRepository, null);
    }

    @Autowired
    public MultiTenancySecurityService(SpringDataBranchRepository branchRepository,
                                       SpringDataOwnerProfileRepository ownerProfileRepository,
                                       SpringDataEmployeeProfileRepository employeeProfileRepository,
                                       @Autowired(required = false) SpringDataTenantRepository tenantRepository) {
        this.branchRepository = branchRepository;
        this.ownerProfileRepository = ownerProfileRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Checks if the caller is authorized for the given workshop.
     */
    @Transactional(readOnly = true)
    public boolean isAuthorizedForWorkshop(UUID workshopId) {
        if (workshopId == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (hasRole(auth, "SUPERADMIN")) {
            return true;
        }
        UUID contextTenant = TenantContextHolder.getTenantId();
        if (contextTenant != null && contextTenant.equals(workshopId)) {
            return true;
        }
        if (tenantRepository != null && ownerProfileRepository != null && auth.getName() != null) {
            try {
                UUID userId = UUID.fromString(auth.getName());
                Optional<OwnerProfileJpaEntity> ownerOpt = ownerProfileRepository.findByUserId(userId);
                if (ownerOpt.isPresent()) {
                    Optional<TenantJpaEntity> workshopOpt = tenantRepository.findById(workshopId);
                    if (workshopOpt.isPresent()
                            && ownerOpt.get().getId().equals(workshopOpt.get().getOwnerId())) {
                        return true;
                    }
                }
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }
        return false;
    }

    /**
     * Checks if the caller holds a given permission within the specified workshop.
     */
    public boolean hasWorkshopPermission(UUID workshopId, String permission) {
        if (!isAuthorizedForWorkshop(workshopId)) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return hasRole(auth, "SUPERADMIN")
                || hasRole(auth, "OWNER")
                || hasRole(auth, "WORKSHOP_ADMIN")
                || hasRole(auth, "ADMIN_TALLER")
                || hasRole(auth, "ADMIN");
    }

    /**
     * Checks if the caller is authorized for the given branch.
     */
    @Transactional(readOnly = true)
    public boolean isAuthorizedForBranch(UUID branchId) {
        if (branchId == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (hasRole(auth, "SUPERADMIN")) {
            return true;
        }
        Optional<BranchJpaEntity> branchOpt = branchRepository.findById(branchId);
        if (branchOpt.isEmpty()) {
            return false;
        }
        BranchJpaEntity branch = branchOpt.get();
        return isAuthorizedForWorkshop(branch.getWorkshopId());
    }

    /**
     * Checks if the caller is authorized to view/access the given user ID.
     * Allows SUPERADMIN globally, tenant admins/owners strictly within their own tenant, or self.
     */
    @Transactional(readOnly = true)
    public boolean isAuthorizedForUser(UUID userId) {
        if (userId == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (hasRole(auth, "SUPERADMIN")) {
            return true;
        }
        if (isSelf(auth, userId)) {
            return true;
        }
        if (hasRole(auth, "OWNER") || hasRole(auth, "WORKSHOP_ADMIN") || hasRole(auth, "ADMIN_TALLER")) {
            UUID currentTenant = TenantContextHolder.getTenantId();
            if (currentTenant == null) {
                return false;
            }
            if (employeeProfileRepository != null) {
                Optional<EmployeeProfileJpaEntity> emp = employeeProfileRepository.findByUserId(userId);
                if (emp.isPresent()) {
                    return currentTenant.equals(emp.get().getTenantId());
                }
            }
            if (ownerProfileRepository != null) {
                Optional<OwnerProfileJpaEntity> owner = ownerProfileRepository.findByUserId(userId);
                if (owner.isPresent()) {
                    return currentTenant.equals(owner.get().getTenantId());
                }
            }
            // User does not yet have a profile in any tenant (e.g. initial profile creation scoped to currentTenant)
            return true;
        }
        return false;
    }

    /**
     * Strictly verifies self-access: only SUPERADMIN or the exact authenticated user.
     * Crucial: ROLE_OWNER has NO bypass here.
     */
    public boolean isAuthorizedForSelf(UUID userId) {
        if (userId == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (hasRole(auth, "SUPERADMIN")) {
            return true;
        }
        return isSelf(auth, userId);
    }

    /**
     * Checks if caller is authorized for the given owner profile ID.
     */
    public boolean isAuthorizedForOwnerProfile(UUID ownerId) {
        if (ownerId == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (hasRole(auth, "SUPERADMIN")) {
            return true;
        }
        Optional<OwnerProfileJpaEntity> ownerOpt = ownerProfileRepository.findById(ownerId);
        if (ownerOpt.isEmpty()) {
            return false;
        }
        return isAuthorizedForSelf(ownerOpt.get().getUserId());
    }

    private boolean isSelf(Authentication auth, UUID targetUserId) {
        String principalName = auth.getName();
        if (principalName == null || principalName.trim().isEmpty()) {
            return false;
        }
        try {
            UUID callerId = UUID.fromString(principalName.trim());
            return callerId.equals(targetUserId);
        } catch (IllegalArgumentException e) {
            return principalName.trim().equalsIgnoreCase(targetUserId.toString());
        }
    }

    private boolean hasRole(Authentication auth, String roleName) {
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        String expectedRole = "ROLE_" + roleName;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (expectedRole.equalsIgnoreCase(ga.getAuthority()) || roleName.equalsIgnoreCase(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
