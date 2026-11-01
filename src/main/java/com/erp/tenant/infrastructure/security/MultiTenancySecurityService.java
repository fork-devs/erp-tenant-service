package com.erp.tenant.infrastructure.security;

import com.erp.tenant.infrastructure.persistence.entity.BranchJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.OwnerProfileJpaEntity;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataBranchRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataOwnerProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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

    public MultiTenancySecurityService(SpringDataBranchRepository branchRepository,
                                       SpringDataOwnerProfileRepository ownerProfileRepository) {
        this.branchRepository = branchRepository;
        this.ownerProfileRepository = ownerProfileRepository;
    }

    /**
     * Checks if the caller is authorized for the given workshop.
     */
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
        return contextTenant != null && contextTenant.equals(workshopId);
    }

    /**
     * Checks if the caller is authorized for the given branch.
     */
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
     * Allows SUPERADMIN, OWNER (global bypass for reading profiles), or self.
     */
    public boolean isAuthorizedForUser(UUID userId) {
        if (userId == null) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (hasRole(auth, "SUPERADMIN") || hasRole(auth, "OWNER") || hasRole(auth, "WORKSHOP_ADMIN") || hasRole(auth, "ADMIN_TALLER")) {
            return true;
        }
        return isSelf(auth, userId);
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
