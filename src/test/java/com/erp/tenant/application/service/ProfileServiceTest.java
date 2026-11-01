package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.result.ProfileUnifiedResult;
import com.erp.tenant.domain.exception.ProfileNotFoundException;
import com.erp.tenant.domain.model.DocumentType;
import com.erp.tenant.domain.model.EmployeeProfile;
import com.erp.tenant.domain.model.OwnerProfile;
import com.erp.tenant.domain.port.EmployeeProfileRepositoryPort;
import com.erp.tenant.domain.port.OwnerProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private OwnerProfileRepositoryPort ownerRepositoryPort;

    @Mock
    private EmployeeProfileRepositoryPort employeeRepositoryPort;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(ownerRepositoryPort, employeeRepositoryPort);
    }

    @Test
    @DisplayName("Should return active roles for a user possessing owner and employee profiles")
    void shouldReturnActiveRoles() {
        UUID userId = UUID.randomUUID();
        OwnerProfile owner = OwnerProfile.create(
                UUID.randomUUID(), userId, "Raul", "Castro", DocumentType.DNI, "45678901", "+51987112233"
        );
        EmployeeProfile employee = EmployeeProfile.create(
                UUID.randomUUID(), userId, "Raul", "Castro", DocumentType.DNI, "45678901", "+51987112233"
        );

        when(ownerRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(owner));
        when(employeeRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(employee));

        List<String> roles = profileService.getRolesByUserId(userId);
        assertThat(roles).containsExactlyInAnyOrder("ROLE_OWNER", "ROLE_EMPLOYEE");
    }

    @Test
    @DisplayName("Should lookup unified profile by document number")
    void shouldLookupByDocumentNumber() {
        UUID userId = UUID.randomUUID();
        EmployeeProfile employee = EmployeeProfile.create(
                UUID.randomUUID(), userId, "Luis", "Mendoza", DocumentType.DNI, "71234567", "+51912345678"
        );

        when(ownerRepositoryPort.findByDocumentNumber("71234567")).thenReturn(Optional.empty());
        when(employeeRepositoryPort.findByDocumentNumber("71234567")).thenReturn(Optional.of(employee));

        ProfileUnifiedResult result = profileService.getByDocumentNumber("71234567");
        assertThat(result.profileType()).isEqualTo("EMPLOYEE");
        assertThat(result.firstName()).isEqualTo("Luis");
        assertThat(result.roles()).contains("ROLE_EMPLOYEE");
    }

    @Test
    @DisplayName("Should throw ProfileNotFoundException when no profile matches document")
    void shouldThrowNotFoundOnUnknownDocument() {
        when(ownerRepositoryPort.findByDocumentNumber("99999999")).thenReturn(Optional.empty());
        when(employeeRepositoryPort.findByDocumentNumber("99999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getByDocumentNumber("99999999"))
                .isInstanceOf(ProfileNotFoundException.class);
    }
}
