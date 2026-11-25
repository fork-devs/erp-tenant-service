package com.erp.tenant.application.service;

import com.erp.tenant.infrastructure.persistence.entity.WorkshopRoleAssignmentJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.WorkshopRoleAuditLogJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.WorkshopRoleJpaEntity;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopRoleAssignmentRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopRoleAuditLogRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkshopRoleTemplatesTest {

    @Mock
    private SpringDataWorkshopRoleRepository roleRepository;

    @Mock
    private SpringDataWorkshopRoleAssignmentRepository assignmentRepository;

    @Mock
    private SpringDataWorkshopRoleAuditLogRepository auditLogRepository;

    @InjectMocks
    private WorkshopRoleService service;

    @Test
    void listTemplatesShouldReflectActivationStatus() {
        UUID workshopId = UUID.randomUUID();
        when(roleRepository.existsByWorkshopIdAndTemplateCodeIgnoreCase(workshopId, "BRANCH_MANAGER"))
                .thenReturn(true);
        when(roleRepository.existsByWorkshopIdAndTemplateCodeIgnoreCase(workshopId, "SECRETARY"))
                .thenReturn(false);

        List<WorkshopRoleService.RoleTemplateView> templates = service.listTemplates(workshopId);

        assertEquals(2, templates.size());
        WorkshopRoleService.RoleTemplateView branchManager = templates.stream()
                .filter(t -> "BRANCH_MANAGER".equals(t.code()))
                .findFirst()
                .orElseThrow();
        WorkshopRoleService.RoleTemplateView secretary = templates.stream()
                .filter(t -> "SECRETARY".equals(t.code()))
                .findFirst()
                .orElseThrow();

        assertTrue(branchManager.activated());
        assertFalse(branchManager.permissions().contains("BRANCH_MANAGE"));
        assertFalse(branchManager.permissions().contains("ROLE_MANAGE"));
        assertFalse(secretary.activated());
        assertTrue(secretary.permissions().contains("APPOINTMENT_MANAGE"));
        assertTrue(secretary.permissions().contains("CHECKOUT_PROCESS"));
    }

    @Test
    void activateTemplateShouldCreateRoleAndAuditLog() {
        UUID workshopId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(roleRepository.existsByWorkshopIdAndTemplateCodeIgnoreCase(workshopId, "BRANCH_MANAGER"))
                .thenReturn(false);
        when(roleRepository.existsByWorkshopIdAndNameIgnoreCase(workshopId, "Jefe de Sede"))
                .thenReturn(false);
        when(roleRepository.save(any(WorkshopRoleJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditLogRepository.save(any(WorkshopRoleAuditLogJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkshopRoleService.WorkshopRoleView created = service.activateTemplate(workshopId, actorId, "branch_manager");

        assertEquals("Jefe de Sede", created.name());
        assertTrue(created.active());
        assertFalse(created.system());
        assertFalse(created.permissions().contains("BRANCH_MANAGE"));
        assertFalse(created.permissions().contains("ROLE_MANAGE"));

        ArgumentCaptor<WorkshopRoleJpaEntity> roleCaptor = ArgumentCaptor.forClass(WorkshopRoleJpaEntity.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertEquals("BRANCH_MANAGER", roleCaptor.getValue().getTemplateCode());

        ArgumentCaptor<WorkshopRoleAuditLogJpaEntity> auditCaptor = ArgumentCaptor.forClass(WorkshopRoleAuditLogJpaEntity.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("TEMPLATE_ACTIVATED", auditCaptor.getValue().getActionType());
    }

    @Test
    void activateTemplateShouldRejectDuplicateOrUnknownTemplate() {
        UUID workshopId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> service.activateTemplate(workshopId, actorId, "NON_EXISTENT"));

        when(roleRepository.existsByWorkshopIdAndTemplateCodeIgnoreCase(workshopId, "SECRETARY"))
                .thenReturn(true);
        assertThrows(IllegalStateException.class,
                () -> service.activateTemplate(workshopId, actorId, "SECRETARY"));
    }

    @Test
    void assignRoleShouldSupportBranchScopedAndGlobalAssignments() {
        UUID workshopId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        WorkshopRoleJpaEntity role = new WorkshopRoleJpaEntity(
                roleId, workshopId, workshopId, "Jefe de Sede", "Branch manager",
                false, true, "WORKORDER_READ,STAFF_READ", Instant.now(), Instant.now()
        );
        when(roleRepository.findByIdAndWorkshopId(roleId, workshopId)).thenReturn(Optional.of(role));
        when(assignmentRepository.existsByWorkshopIdAndWorkshopRoleIdAndUserIdAndBranchId(
                workshopId, roleId, targetUserId, branchId)).thenReturn(false);
        when(assignmentRepository.existsByWorkshopIdAndWorkshopRoleIdAndUserIdAndBranchIdIsNull(
                workshopId, roleId, targetUserId)).thenReturn(false);

        service.assignRole(workshopId, roleId, actorId, targetUserId, branchId);
        service.assignRole(workshopId, roleId, actorId, targetUserId, null);

        ArgumentCaptor<WorkshopRoleAssignmentJpaEntity> captor = ArgumentCaptor.forClass(WorkshopRoleAssignmentJpaEntity.class);
        verify(assignmentRepository, times(2)).save(captor.capture());
        List<WorkshopRoleAssignmentJpaEntity> saved = captor.getAllValues();
        assertEquals(branchId, saved.get(0).getBranchId());
        assertNull(saved.get(1).getBranchId());
    }
}
