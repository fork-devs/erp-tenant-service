package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.CreateEmployeeProfileCommand;
import com.erp.tenant.application.dto.command.UpdateEmployeeProfileCommand;
import com.erp.tenant.application.dto.result.EmployeeProfileResult;
import com.erp.tenant.domain.exception.DocumentAlreadyExistsException;
import com.erp.tenant.domain.exception.EmployeeNotFoundException;
import com.erp.tenant.domain.model.DocumentType;
import com.erp.tenant.domain.model.EmployeeProfile;
import com.erp.tenant.domain.port.EmployeeProfileRepositoryPort;
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
class EmployeeProfileServiceTest {

    @Mock
    private EmployeeProfileRepositoryPort employeeRepositoryPort;

    @Mock
    private UnitOfWork unitOfWork;

    @Mock
    private com.erp.tenant.domain.port.IdentityValidationPort identityValidationPort;

    private EmployeeProfileService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeProfileService(employeeRepositoryPort, unitOfWork, identityValidationPort);
    }

    @Test
    @DisplayName("Should successfully create employee profile")
    void shouldCreateEmployeeProfile() {
        UUID userId = UUID.randomUUID();
        CreateEmployeeProfileCommand command = new CreateEmployeeProfileCommand(
                userId, "Luis", "Mendoza", "DNI", "71234567", "+51912345678"
        );

        when(employeeRepositoryPort.existsByDocumentNumber("71234567")).thenReturn(false);
        when(identityValidationPort.validateDni("71234567")).thenReturn(
                new com.erp.tenant.domain.port.DniValidationResult("71234567", "LUIS", "MENDOZA", "CASTRO", "MENDOZA CASTRO, LUIS", true)
        );
        when(unitOfWork.callInTransaction(any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        when(employeeRepositoryPort.save(any(EmployeeProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeProfileResult result = employeeService.create(command);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.documentNumber()).isEqualTo("71234567");
        assertThat(result.firstName()).isEqualTo("LUIS");
        assertThat(result.lastName()).isEqualTo("MENDOZA CASTRO");
    }

    @Test
    @DisplayName("Should reject employee creation when DNI is not found or invalid in RENIEC")
    void shouldRejectWhenDniNotFoundInReniec() {
        UUID userId = UUID.randomUUID();
        CreateEmployeeProfileCommand command = new CreateEmployeeProfileCommand(
                userId, "Falso", "Inexistente", "DNI", "00000000", "+51912345678"
        );

        when(employeeRepositoryPort.existsByDocumentNumber("00000000")).thenReturn(false);
        when(identityValidationPort.validateDni("00000000")).thenReturn(
                com.erp.tenant.domain.port.DniValidationResult.invalid("00000000")
        );

        assertThatThrownBy(() -> employeeService.create(command))
                .isInstanceOf(com.erp.tenant.domain.exception.DocumentNotFoundException.class);
    }

    @Test
    @DisplayName("Should reject employee creation on duplicate document number")
    void shouldRejectDuplicateDocument() {
        UUID userId = UUID.randomUUID();
        CreateEmployeeProfileCommand command = new CreateEmployeeProfileCommand(
                userId, "Luis", "Mendoza", "DNI", "71234567", "+51912345678"
        );

        when(employeeRepositoryPort.existsByDocumentNumber("71234567")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(command))
                .isInstanceOf(DocumentAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should update and delete employee profile")
    void shouldUpdateAndDeleteEmployee() {
        UUID empId = UUID.randomUUID();
        EmployeeProfile emp = EmployeeProfile.create(
                UUID.randomUUID(), UUID.randomUUID(), "Luis", "Mendoza", DocumentType.DNI, "71234567", "+51912345678"
        );

        when(employeeRepositoryPort.findById(empId)).thenReturn(Optional.of(emp));
        when(unitOfWork.callInTransaction(any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        when(employeeRepositoryPort.save(any(EmployeeProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateEmployeeProfileCommand updateCommand = new UpdateEmployeeProfileCommand(
                empId, "Luis Alberto", "Mendoza", "DNI", "71234567", "+51999999999"
        );
        EmployeeProfileResult updated = employeeService.update(updateCommand);

        assertThat(updated.firstName()).isEqualTo("Luis Alberto");
        assertThat(updated.phone()).isEqualTo("+51999999999");

        employeeService.delete(empId);
        verify(unitOfWork).runInTransaction(any());
    }
}
