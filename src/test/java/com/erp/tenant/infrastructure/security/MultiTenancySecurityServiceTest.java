package com.erp.tenant.infrastructure.security;

import com.erp.tenant.infrastructure.persistence.entity.BranchJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.OwnerProfileJpaEntity;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataBranchRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataOwnerProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiTenancySecurityServiceTest {

    @Mock
    private SpringDataBranchRepository branchRepository;

    @Mock
    private SpringDataOwnerProfileRepository ownerProfileRepository;

    @InjectMocks
    private MultiTenancySecurityService securityService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("Superadmin should be authorized for any workshop and branch")
    void superadminShouldBeAuthorizedEverywhere() {
        UUID workshopId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityService.isAuthorizedForWorkshop(workshopId)).isTrue();
        assertThat(securityService.isAuthorizedForBranch(branchId)).isTrue();
        assertThat(securityService.isAuthorizedForUser(UUID.randomUUID())).isTrue();
        assertThat(securityService.isAuthorizedForSelf(UUID.randomUUID())).isTrue();
    }

    @Test
    @DisplayName("Workshop member should be authorized for their own workshop and branches")
    void workshopMemberAuthorization() {
        UUID tenantId = UUID.randomUUID();
        UUID callerUserId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        TenantContextHolder.setTenantId(tenantId);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                callerUserId.toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_WORKSHOP_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        BranchJpaEntity branch = new BranchJpaEntity(
                branchId,
                tenantId,
                tenantId,
                "BR-01",
                "Sede Central",
                "Av. Principal 100",
                "+51999999999",
                true,
                Instant.now(),
                Instant.now()
        );
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));

        assertThat(securityService.isAuthorizedForWorkshop(tenantId)).isTrue();
        assertThat(securityService.isAuthorizedForWorkshop(UUID.randomUUID())).isFalse();
        assertThat(securityService.isAuthorizedForBranch(branchId)).isTrue();
    }

    @Test
    @DisplayName("Owner should have bypass on isAuthorizedForUser but NOT on isAuthorizedForSelf")
    void ownerBypassDistinction() {
        UUID callerUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                callerUserId.toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_OWNER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Owner can view other user profiles
        assertThat(securityService.isAuthorizedForUser(otherUserId)).isTrue();

        // But Owner CANNOT mutate or delete other user profiles (strictly self)
        assertThat(securityService.isAuthorizedForSelf(otherUserId)).isFalse();
        assertThat(securityService.isAuthorizedForSelf(callerUserId)).isTrue();
    }

    @Test
    @DisplayName("Should verify owner profile authorization against the owner userId")
    void ownerProfileAuthorization() {
        UUID callerUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                callerUserId.toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        OwnerProfileJpaEntity ownerEntity = new OwnerProfileJpaEntity(
                ownerId,
                UUID.randomUUID(),
                callerUserId,
                "Carlos",
                "Gomez",
                "DNI",
                "12345678",
                "+51987654321",
                Instant.now(),
                Instant.now()
        );
        when(ownerProfileRepository.findById(ownerId)).thenReturn(Optional.of(ownerEntity));

        assertThat(securityService.isAuthorizedForOwnerProfile(ownerId)).isTrue();
    }
}
