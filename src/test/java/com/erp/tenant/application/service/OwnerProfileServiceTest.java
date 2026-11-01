package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.CreateOwnerProfileCommand;
import com.erp.tenant.application.dto.command.UpdateOwnerProfileCommand;
import com.erp.tenant.application.dto.result.OwnerProfileResult;
import com.erp.tenant.domain.exception.CannotDeleteOwnerWithWorkshopsException;
import com.erp.tenant.domain.exception.DocumentAlreadyExistsException;
import com.erp.tenant.domain.exception.OwnerAlreadyExistsException;
import com.erp.tenant.domain.model.DocumentType;
import com.erp.tenant.domain.model.OwnerProfile;
import com.erp.tenant.domain.port.OwnerProfileRepositoryPort;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerProfileServiceTest {

    @Mock
    private OwnerProfileRepositoryPort ownerRepositoryPort;

    @Mock
    private TenantRepositoryPort tenantRepositoryPort;

    @Mock
    private UnitOfWork unitOfWork;

    private OwnerProfileService ownerService;

    @BeforeEach
    void setUp() {
        ownerService = new OwnerProfileService(ownerRepositoryPort, tenantRepositoryPort, unitOfWork);
    }

    @Test
    @DisplayName("Should successfully create owner profile")
    void shouldCreateOwnerProfile() {
        UUID userId = UUID.randomUUID();
        CreateOwnerProfileCommand command = new CreateOwnerProfileCommand(
                userId, "Raul", "Castro", "DNI", "45678901", "+51987112233"
        );

        when(ownerRepositoryPort.existsByUserId(userId)).thenReturn(false);
        when(ownerRepositoryPort.existsByDocumentNumber("45678901")).thenReturn(false);
        when(unitOfWork.callInTransaction(any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        when(ownerRepositoryPort.save(any(OwnerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        OwnerProfileResult result = ownerService.create(command);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.documentNumber()).isEqualTo("45678901");
    }

    @Test
    @DisplayName("Should reject owner creation if user already has an owner profile")
    void shouldRejectDuplicateOwnerForUser() {
        UUID userId = UUID.randomUUID();
        CreateOwnerProfileCommand command = new CreateOwnerProfileCommand(
                userId, "Raul", "Castro", "DNI", "45678901", "+51987112233"
        );

        when(ownerRepositoryPort.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> ownerService.create(command))
                .isInstanceOf(OwnerAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should reject owner creation when DNI is not found in RENIEC")
    void shouldRejectOwnerCreationWhenDniNotFound() {
        com.erp.tenant.domain.port.IdentityValidationPort identityPort = org.mockito.Mockito.mock(com.erp.tenant.domain.port.IdentityValidationPort.class);
        ownerService = new OwnerProfileService(ownerRepositoryPort, tenantRepositoryPort, unitOfWork, identityPort);

        UUID userId = UUID.randomUUID();
        CreateOwnerProfileCommand command = new CreateOwnerProfileCommand(
                userId, "Fake", "Name", "DNI", "00000000", "+51987112233"
        );

        when(ownerRepositoryPort.existsByUserId(userId)).thenReturn(false);
        when(ownerRepositoryPort.existsByDocumentNumber("00000000")).thenReturn(false);
        when(identityPort.validateDni("00000000")).thenReturn(com.erp.tenant.domain.port.DniValidationResult.invalid("00000000"));

        assertThatThrownBy(() -> ownerService.create(command))
                .isInstanceOf(com.erp.tenant.domain.exception.DocumentNotFoundException.class);
    }

    @Test
    @DisplayName("Should autofill official names when DNI is verified in RENIEC")
    void shouldAutofillOfficialNamesWhenDniVerified() {
        com.erp.tenant.domain.port.IdentityValidationPort identityPort = org.mockito.Mockito.mock(com.erp.tenant.domain.port.IdentityValidationPort.class);
        ownerService = new OwnerProfileService(ownerRepositoryPort, tenantRepositoryPort, unitOfWork, identityPort);

        UUID userId = UUID.randomUUID();
        CreateOwnerProfileCommand command = new CreateOwnerProfileCommand(
                userId, "Dummy", "User", "DNI", "70514079", "+51987112233"
        );

        when(ownerRepositoryPort.existsByUserId(userId)).thenReturn(false);
        when(ownerRepositoryPort.existsByDocumentNumber("70514079")).thenReturn(false);
        when(identityPort.validateDni("70514079")).thenReturn(new com.erp.tenant.domain.port.DniValidationResult(
                "70514079", "ALDO JEANFRANCO", "MACHACCA", "SOTO", "MACHACCA SOTO, ALDO JEANFRANCO", true
        ));
        when(unitOfWork.callInTransaction(any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        when(ownerRepositoryPort.save(any(OwnerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        OwnerProfileResult result = ownerService.create(command);

        assertThat(result.firstName()).isEqualTo("ALDO JEANFRANCO");
        assertThat(result.lastName()).isEqualTo("MACHACCA SOTO");
    }

    @Test
    @DisplayName("Should reject owner deletion if owner has associated workshops")
    void shouldRejectOwnerDeletionWithWorkshops() {
        UUID ownerId = UUID.randomUUID();
        OwnerProfile owner = OwnerProfile.create(
                UUID.randomUUID(), UUID.randomUUID(), "Raul", "Castro", DocumentType.DNI, "45678901", "+51987112233"
        );

        when(ownerRepositoryPort.findById(ownerId)).thenReturn(Optional.of(owner));
        when(tenantRepositoryPort.existsByOwnerId(ownerId)).thenReturn(true);

        assertThatThrownBy(() -> ownerService.delete(ownerId))
                .isInstanceOf(CannotDeleteOwnerWithWorkshopsException.class);
    }
}
