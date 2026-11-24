package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.domain.model.PermissionCatalog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permissions", description = "Catalog of atomic workshop-assignable permissions")
public class PermissionController {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List assignable permissions", description = "Returns the catalog of atomic permissions excluding PLATFORM_ADMIN")
    public ResponseEntity<List<PermissionCatalog.PermissionItem>> listAssignablePermissions() {
        return ResponseEntity.ok(PermissionCatalog.listAssignable());
    }
}
