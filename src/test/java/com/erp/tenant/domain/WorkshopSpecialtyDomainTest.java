package com.erp.tenant.domain;

import com.erp.tenant.domain.model.WorkshopSpecialty;
import com.erp.tenant.domain.model.WorkshopSpecialtyRevision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorkshopSpecialtyDomainTest {

    @Test
    @DisplayName("Crea especialidad correctamente con código normalizado a UPPER_SNAKE_CASE")
    void shouldCreateSpecialtyWithNormalizedCode() {
        UUID tenantId = UUID.randomUUID();
        UUID workshopId = tenantId;

        WorkshopSpecialty specialty = WorkshopSpecialty.create(
                tenantId,
                workshopId,
                "Inyección Electrónica y Sensores",
                "inyeccion-electronica y sensores!",
                "Limpieza de inyectores"
        );

        assertNotNull(specialty.getId());
        assertEquals(tenantId, specialty.getTenantId());
        assertEquals(workshopId, specialty.getWorkshopId());
        assertEquals("Inyección Electrónica y Sensores", specialty.getName());
        assertEquals("INYECCION_ELECTRONICA_Y_SENSORES", specialty.getCode());
        assertEquals("Limpieza de inyectores", specialty.getDescription());
        assertTrue(specialty.isActive());
        assertNotNull(specialty.getCreatedAt());
        assertNotNull(specialty.getUpdatedAt());
    }

    @Test
    @DisplayName("Actualiza nombre y descripción correctamente")
    void shouldUpdateNameAndDescription() {
        UUID tenantId = UUID.randomUUID();
        WorkshopSpecialty specialty = WorkshopSpecialty.create(
                tenantId,
                tenantId,
                "Mecánica Básica",
                "MECANICA_BASICA",
                "Descripción inicial"
        );

        specialty.update("Mecánica Avanzada", "Nueva descripción");

        assertEquals("Mecánica Avanzada", specialty.getName());
        assertEquals("Nueva descripción", specialty.getDescription());
        assertEquals("MECANICA_BASICA", specialty.getCode()); // código inmutable
    }

    @Test
    @DisplayName("Desactiva la especialidad lógicamente")
    void shouldDeactivateSpecialty() {
        UUID tenantId = UUID.randomUUID();
        WorkshopSpecialty specialty = WorkshopSpecialty.create(
                tenantId,
                tenantId,
                "Alineación",
                "ALINEACION",
                null
        );

        assertTrue(specialty.isActive());
        specialty.deactivate();
        assertFalse(specialty.isActive());
    }

    @Test
    @DisplayName("Falla al crear o actualizar con campos obligatorios vacíos")
    void shouldFailOnInvalidFields() {
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                WorkshopSpecialty.create(null, id, "Nombre", "COD", "Desc"));

        assertThrows(IllegalArgumentException.class, () ->
                WorkshopSpecialty.create(id, id, "  ", "COD", "Desc"));

        assertThrows(IllegalArgumentException.class, () ->
                WorkshopSpecialty.create(id, id, "Nombre", "   ", "Desc"));

        WorkshopSpecialty specialty = WorkshopSpecialty.create(id, id, "Nombre", "COD", "Desc");
        assertThrows(IllegalArgumentException.class, () -> specialty.update("   ", "Desc"));
    }

    @Test
    @DisplayName("Registra revisión de auditoría con datos correctos")
    void shouldRecordRevisionCorrectly() {
        UUID tenantId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        WorkshopSpecialtyRevision rev = WorkshopSpecialtyRevision.recordChange(
                tenantId,
                specialtyId,
                "name",
                "Nombre Viejo",
                "Nombre Nuevo",
                "user-uuid-123"
        );

        assertNotNull(rev.getId());
        assertEquals(tenantId, rev.getTenantId());
        assertEquals(specialtyId, rev.getSpecialtyId());
        assertEquals("name", rev.getFieldName());
        assertEquals("Nombre Viejo", rev.getOldValue());
        assertEquals("Nombre Nuevo", rev.getNewValue());
        assertEquals("user-uuid-123", rev.getChangedBy());
        assertNotNull(rev.getChangedAt());
    }
}
