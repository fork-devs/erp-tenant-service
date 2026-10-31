package com.erp.tenant.application;

import com.erp.tenant.application.dto.command.CreateSpecialtyCommand;
import com.erp.tenant.application.dto.command.UpdateSpecialtyCommand;
import com.erp.tenant.application.dto.result.SpecialtyResult;
import com.erp.tenant.application.dto.result.SpecialtyRevisionResult;
import com.erp.tenant.application.service.WorkshopSpecialtyService;
import com.erp.tenant.domain.exception.SpecialtyCodeAlreadyExistsException;
import com.erp.tenant.domain.exception.SpecialtyNotFoundException;
import com.erp.tenant.domain.model.WorkshopSpecialty;
import com.erp.tenant.domain.model.WorkshopSpecialtyRevision;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.domain.port.WorkshopSpecialtyRepositoryPort;
import com.erp.tenant.domain.port.WorkshopSpecialtyRevisionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkshopSpecialtyServiceTest {

    @Mock
    private WorkshopSpecialtyRepositoryPort specialtyRepository;

    @Mock
    private WorkshopSpecialtyRevisionRepositoryPort revisionRepository;

    private static final UnitOfWork DIRECT_UOW = new UnitOfWork() {
        @Override
        public void runInTransaction(Runnable work) {
            work.run();
        }

        @Override
        public <T> T callInTransaction(java.util.function.Supplier<T> work) {
            return work.get();
        }
    };

    private WorkshopSpecialtyService service;

    @BeforeEach
    void setUp() {
        service = new WorkshopSpecialtyService(specialtyRepository, revisionRepository, DIRECT_UOW);
    }

    @Test
    @DisplayName("Crea una nueva especialidad y registra la revisión inicial")
    void shouldCreateSpecialtyAndRecordInitialRevision() {
        UUID tenantId = UUID.randomUUID();
        UUID workshopId = tenantId;

        CreateSpecialtyCommand command = new CreateSpecialtyCommand(
                tenantId,
                workshopId,
                "Inyección Electrónica",
                "INYECCION_ELECTRONICA",
                "Diagnóstico y calibración",
                "admin-user-1"
        );

        when(specialtyRepository.existsByCode(tenantId, workshopId, "INYECCION_ELECTRONICA")).thenReturn(false);
        when(specialtyRepository.save(any(WorkshopSpecialty.class))).thenAnswer(inv -> inv.getArgument(0));

        SpecialtyResult result = service.create(command);

        assertNotNull(result);
        assertEquals("Inyección Electrónica", result.name());
        assertEquals("INYECCION_ELECTRONICA", result.code());
        assertTrue(result.active());

        verify(revisionRepository).save(any(WorkshopSpecialtyRevision.class));
    }

    @Test
    @DisplayName("Rechaza la creación si el código ya existe en el taller")
    void shouldRejectWhenCodeAlreadyExists() {
        UUID tenantId = UUID.randomUUID();
        CreateSpecialtyCommand command = new CreateSpecialtyCommand(
                tenantId,
                tenantId,
                "Mecánica",
                "GENERAL_MECHANIC",
                "Desc",
                "admin-user"
        );

        when(specialtyRepository.existsByCode(tenantId, tenantId, "GENERAL_MECHANIC")).thenReturn(true);

        assertThrows(SpecialtyCodeAlreadyExistsException.class, () -> service.create(command));
        verify(specialtyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualiza especialidad y registra revisiones de campos modificados")
    void shouldUpdateSpecialtyAndRecordRevisions() {
        UUID tenantId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        WorkshopSpecialty existing = new WorkshopSpecialty(
                specialtyId,
                tenantId,
                tenantId,
                "Nombre Viejo",
                "CODIGO",
                "Desc Vieja",
                true,
                null,
                null
        );

        when(specialtyRepository.findById(tenantId, specialtyId)).thenReturn(Optional.of(existing));
        when(specialtyRepository.save(any(WorkshopSpecialty.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateSpecialtyCommand command = new UpdateSpecialtyCommand(
                tenantId,
                tenantId,
                specialtyId,
                "Nombre Nuevo",
                "Desc Nueva",
                "admin-user"
        );

        SpecialtyResult result = service.update(command);

        assertEquals("Nombre Nuevo", result.name());
        assertEquals("Desc Nueva", result.description());
        verify(revisionRepository).saveAll(argThat(list -> list.size() == 2));
    }

    @Test
    @DisplayName("Desactiva especialidad y registra revisión de estado")
    void shouldDeactivateSpecialty() {
        UUID tenantId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        WorkshopSpecialty existing = new WorkshopSpecialty(
                specialtyId,
                tenantId,
                tenantId,
                "Nombre",
                "CODIGO",
                "Desc",
                true,
                null,
                null
        );

        when(specialtyRepository.findById(tenantId, specialtyId)).thenReturn(Optional.of(existing));

        service.deactivate(tenantId, tenantId, specialtyId, "admin-user");

        assertFalse(existing.isActive());
        verify(specialtyRepository).save(existing);
        verify(revisionRepository).save(any(WorkshopSpecialtyRevision.class));
    }

    @Test
    @DisplayName("Siembra las 5 especialidades base automáticamente e idempotentemente")
    void shouldSeedBaseSpecialtiesIdempotently() {
        UUID tenantId = UUID.randomUUID();
        UUID workshopId = tenantId;

        // Simulamos que no existe ninguna
        when(specialtyRepository.existsByCode(eq(tenantId), eq(workshopId), anyString())).thenReturn(false);

        service.seedBaseSpecialties(tenantId, workshopId);

        verify(specialtyRepository).saveAll(argThat(list -> list.size() == 5));
    }

    @Test
    @DisplayName("Lanza SpecialtyNotFoundException cuando la especialidad no existe")
    void shouldThrowNotFoundWhenSpecialtyDoesNotExist() {
        UUID tenantId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        when(specialtyRepository.findById(tenantId, specialtyId)).thenReturn(Optional.empty());

        assertThrows(SpecialtyNotFoundException.class, () ->
                service.getById(tenantId, tenantId, specialtyId));
    }
}
