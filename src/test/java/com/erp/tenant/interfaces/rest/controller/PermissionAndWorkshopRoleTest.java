package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.service.WorkshopRoleService;
import com.erp.tenant.domain.model.PermissionCatalog;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopRoleAssignmentRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopRoleAuditLogRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PermissionAndWorkshopRoleTest {

    @Test
    void permissionCatalogExcludesPlatformAdmin() {
        PermissionController controller = new PermissionController();
        ResponseEntity<List<PermissionCatalog.PermissionItem>> res = controller.listAssignablePermissions();
        assertEquals(200, res.getStatusCode().value());
        assertFalse(res.getBody().stream().anyMatch(p -> "PLATFORM_ADMIN".equals(p.code())));
        assertEquals(21, res.getBody().size());
    }

    @Test
    void workshopRoleServiceRejectsPlatformAdminEscalation() {
        WorkshopRoleService service = new WorkshopRoleService(
                mock(SpringDataWorkshopRoleRepository.class),
                mock(SpringDataWorkshopRoleAssignmentRepository.class),
                mock(SpringDataWorkshopRoleAuditLogRepository.class)
        );
        UUID workshopId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        assertThrows(RuntimeException.class, () ->
                service.createRole(workshopId, actorId, "Super Hacker", "Invalid", Set.of("PLATFORM_ADMIN"))
        );
    }

    @Test
    void profileControllerResolvesAuthenticatedMeContext() {
        ProfileController controller = new ProfileController(null, null, null, null);
        UUID userId = UUID.randomUUID();
        var auth = new UsernamePasswordAuthenticationToken(
                userId.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_WORKSHOP_ADMIN"))
        );
        ResponseEntity<ProfileController.UserProfileContextResponse> res = controller.getMyProfileContext(auth);
        assertEquals(200, res.getStatusCode().value());
        assertEquals(userId, res.getBody().userId());
        assertEquals(List.of("ROLE_WORKSHOP_ADMIN"), res.getBody().roles());
    }
}
