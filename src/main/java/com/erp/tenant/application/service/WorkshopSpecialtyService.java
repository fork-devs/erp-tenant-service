package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.CreateSpecialtyCommand;
import com.erp.tenant.application.dto.command.UpdateSpecialtyCommand;
import com.erp.tenant.application.dto.result.SpecialtyResult;
import com.erp.tenant.application.dto.result.SpecialtyRevisionResult;
import com.erp.tenant.application.logging.SecurityEventLogger;
import com.erp.tenant.application.usecase.GetWorkshopSpecialtiesUseCase;
import com.erp.tenant.application.usecase.ManageWorkshopSpecialtyUseCase;
import com.erp.tenant.domain.exception.SpecialtyCodeAlreadyExistsException;
import com.erp.tenant.domain.exception.SpecialtyNotFoundException;
import com.erp.tenant.domain.model.WorkshopSpecialty;
import com.erp.tenant.domain.model.WorkshopSpecialtyRevision;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.domain.port.WorkshopSpecialtyRepositoryPort;
import com.erp.tenant.domain.port.WorkshopSpecialtyRevisionRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class WorkshopSpecialtyService implements ManageWorkshopSpecialtyUseCase, GetWorkshopSpecialtiesUseCase {

    public record BaseSpecialtyDefinition(String code, String name, String description) {}

    public static final List<BaseSpecialtyDefinition> BASE_SPECIALTIES = List.of(
            new BaseSpecialtyDefinition("GENERAL_MECHANIC", "Mecánica General",
                    "Mantenimiento preventivo, correctivo y reparación de motores"),
            new BaseSpecialtyDefinition("ELECTRICIAN", "Electricidad y Electrónica Automotriz",
                    "Diagnóstico eléctrico, alternadores, arrancadores y cableado"),
            new BaseSpecialtyDefinition("BODYWORK_PAINT", "Planchado y Pintura",
                    "Enderezado de chasis, reparación de abolladuras y pintura al horno"),
            new BaseSpecialtyDefinition("DIAGNOSTIC", "Diagnóstico Computarizado y Scanner",
                    "Lectura de códigos OBD-II, reprogramación de ECUs y sensores"),
            new BaseSpecialtyDefinition("TIRE_ALIGNMENT", "Alineación y Tramado",
                    "Servicios de suspensión, dirección, balanceo y enllante")
    );

    private final WorkshopSpecialtyRepositoryPort specialtyRepositoryPort;
    private final WorkshopSpecialtyRevisionRepositoryPort revisionRepositoryPort;
    private final UnitOfWork unitOfWork;

    public WorkshopSpecialtyService(WorkshopSpecialtyRepositoryPort specialtyRepositoryPort,
                                    WorkshopSpecialtyRevisionRepositoryPort revisionRepositoryPort,
                                    UnitOfWork unitOfWork) {
        this.specialtyRepositoryPort = specialtyRepositoryPort;
        this.revisionRepositoryPort = revisionRepositoryPort;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public SpecialtyResult create(CreateSpecialtyCommand command) {
        String normalizedCode = WorkshopSpecialty.normalizeCode(command.code());

        return unitOfWork.callInTransaction(() -> {
            if (specialtyRepositoryPort.existsByCode(command.tenantId(), command.workshopId(), normalizedCode)) {
                throw new SpecialtyCodeAlreadyExistsException(normalizedCode);
            }

            WorkshopSpecialty specialty = WorkshopSpecialty.create(
                    command.tenantId(),
                    command.workshopId(),
                    command.name(),
                    normalizedCode,
                    command.description()
            );

            WorkshopSpecialty saved = specialtyRepositoryPort.save(specialty);

            WorkshopSpecialtyRevision initialRevision = WorkshopSpecialtyRevision.recordChange(
                    command.tenantId(),
                    saved.getId(),
                    "status",
                    null,
                    "CREATED",
                    command.actorId()
            );
            revisionRepositoryPort.save(initialRevision);

            SecurityEventLogger.registrar("especialidad_creada", command.tenantId(), null, null, "ok",
                    java.util.Map.of("specialty_id", saved.getId().toString(), "code", saved.getCode()));

            return SpecialtyResult.from(saved);
        });
    }

    @Override
    public SpecialtyResult update(UpdateSpecialtyCommand command) {
        return unitOfWork.callInTransaction(() -> {
            WorkshopSpecialty specialty = specialtyRepositoryPort.findById(command.tenantId(), command.specialtyId())
                    .orElseThrow(() -> new SpecialtyNotFoundException(command.specialtyId()));

            List<WorkshopSpecialtyRevision> revisions = new ArrayList<>();

            if (!Objects.equals(specialty.getName(), command.name().trim())) {
                revisions.add(WorkshopSpecialtyRevision.recordChange(
                        command.tenantId(),
                        specialty.getId(),
                        "name",
                        specialty.getName(),
                        command.name().trim(),
                        command.actorId()
                ));
            }

            String newDesc = command.description() != null ? command.description().trim() : null;
            if (!Objects.equals(specialty.getDescription(), newDesc)) {
                revisions.add(WorkshopSpecialtyRevision.recordChange(
                        command.tenantId(),
                        specialty.getId(),
                        "description",
                        specialty.getDescription(),
                        newDesc,
                        command.actorId()
                ));
            }

            specialty.update(command.name(), command.description());
            WorkshopSpecialty updated = specialtyRepositoryPort.save(specialty);

            if (!revisions.isEmpty()) {
                revisionRepositoryPort.saveAll(revisions);
            }

            SecurityEventLogger.registrar("especialidad_actualizada", command.tenantId(), null, null, "ok",
                    java.util.Map.of("specialty_id", updated.getId().toString(), "code", updated.getCode()));

            return SpecialtyResult.from(updated);
        });
    }

    @Override
    public void deactivate(UUID tenantId, UUID workshopId, UUID specialtyId, String actorId) {
        unitOfWork.runInTransaction(() -> {
            WorkshopSpecialty specialty = specialtyRepositoryPort.findById(tenantId, specialtyId)
                    .orElseThrow(() -> new SpecialtyNotFoundException(specialtyId));

            if (specialty.isActive()) {
                specialty.deactivate();
                specialtyRepositoryPort.save(specialty);

                WorkshopSpecialtyRevision revision = WorkshopSpecialtyRevision.recordChange(
                        tenantId,
                        specialty.getId(),
                        "active",
                        "true",
                        "false",
                        actorId
                );
                revisionRepositoryPort.save(revision);

                SecurityEventLogger.registrar("especialidad_desactivada", tenantId, null, null, "ok",
                        java.util.Map.of("specialty_id", specialty.getId().toString(), "code", specialty.getCode()));
            }
        });
    }

    @Override
    public void seedBaseSpecialties(UUID tenantId, UUID workshopId) {
        unitOfWork.runInTransaction(() -> {
            List<WorkshopSpecialty> toSave = new ArrayList<>();
            for (BaseSpecialtyDefinition def : BASE_SPECIALTIES) {
                if (!specialtyRepositoryPort.existsByCode(tenantId, workshopId, def.code())) {
                    toSave.add(WorkshopSpecialty.create(
                            tenantId,
                            workshopId,
                            def.name(),
                            def.code(),
                            def.description()
                    ));
                }
            }
            if (!toSave.isEmpty()) {
                specialtyRepositoryPort.saveAll(toSave);
                SecurityEventLogger.registrar("especialidades_base_sembradas", tenantId, null, null, "ok",
                        java.util.Map.of("workshop_id", workshopId.toString(), "total", String.valueOf(toSave.size())));
            }
        });
    }

    @Override
    public List<SpecialtyResult> list(UUID tenantId, UUID workshopId, boolean activeOnly) {
        return specialtyRepositoryPort.findByWorkshopId(tenantId, workshopId, activeOnly).stream()
                .map(SpecialtyResult::from)
                .toList();
    }

    @Override
    public SpecialtyResult getById(UUID tenantId, UUID workshopId, UUID specialtyId) {
        return specialtyRepositoryPort.findById(tenantId, specialtyId)
                .map(SpecialtyResult::from)
                .orElseThrow(() -> new SpecialtyNotFoundException(specialtyId));
    }

    @Override
    public List<SpecialtyRevisionResult> getHistory(UUID tenantId, UUID workshopId, UUID specialtyId) {
        // Ensure specialty exists first
        specialtyRepositoryPort.findById(tenantId, specialtyId)
                .orElseThrow(() -> new SpecialtyNotFoundException(specialtyId));

        return revisionRepositoryPort.findBySpecialtyId(tenantId, specialtyId).stream()
                .map(SpecialtyRevisionResult::from)
                .toList();
    }
}
