package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.service.OperatingHoursService;
import com.erp.tenant.domain.model.OperatingHourEntry;
import com.erp.tenant.domain.model.OperatingHourException;
import com.erp.tenant.domain.model.OperatingHoursResolutionStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Operating Hours", description = "Workshop and branch weekly operating schedules, holiday exceptions, and availability")
public class OperatingHoursController {

    public record UpsertOperatingHoursRequest(@NotEmpty List<@Valid OperatingHourEntry> schedule) {}

    public record CreateOperatingHourExceptionRequest(
            @NotNull LocalDate date,
            boolean closed,
            LocalTime openTime,
            LocalTime closeTime,
            String reason
    ) {}

    private final OperatingHoursService operatingHoursService;

    public OperatingHoursController(OperatingHoursService operatingHoursService) {
        this.operatingHoursService = operatingHoursService;
    }

    @GetMapping("/workshops/{workshopId}/operating-hours")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get workshop default operating hours")
    public ResponseEntity<List<OperatingHourEntry>> getWorkshopHours(@PathVariable UUID workshopId) {
        return ResponseEntity.ok(operatingHoursService.getWorkshopHours(workshopId));
    }

    @PutMapping("/workshops/{workshopId}/operating-hours")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    @Operation(summary = "Replace workshop default operating hours")
    public ResponseEntity<List<OperatingHourEntry>> putWorkshopHours(
            @PathVariable UUID workshopId,
            @Valid @RequestBody UpsertOperatingHoursRequest request) {
        return ResponseEntity.ok(operatingHoursService.putWorkshopHours(workshopId, request.schedule()));
    }

    @GetMapping("/branches/{branchId}/operating-hours")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get effective branch operating hours (falls back to workshop schedule)")
    public ResponseEntity<List<OperatingHourEntry>> getBranchHours(@PathVariable UUID branchId) {
        return ResponseEntity.ok(operatingHoursService.getBranchHours(branchId));
    }

    @PutMapping("/branches/{branchId}/operating-hours")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    @Operation(summary = "Override branch weekly operating hours")
    public ResponseEntity<List<OperatingHourEntry>> putBranchHours(
            @PathVariable UUID branchId,
            @Valid @RequestBody UpsertOperatingHoursRequest request) {
        return ResponseEntity.ok(operatingHoursService.putBranchHours(branchId, request.schedule()));
    }

    @DeleteMapping("/branches/{branchId}/operating-hours")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    @Operation(summary = "Reset branch operating hours to inherit workshop default")
    public ResponseEntity<Void> resetBranchHours(@PathVariable UUID branchId) {
        operatingHoursService.resetBranchHours(branchId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/workshops/{workshopId}/operating-hours/exceptions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List workshop operating hour exceptions")
    public ResponseEntity<List<OperatingHourException>> listWorkshopExceptions(@PathVariable UUID workshopId) {
        return ResponseEntity.ok(operatingHoursService.listWorkshopExceptions(workshopId));
    }

    @PostMapping("/workshops/{workshopId}/operating-hours/exceptions")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    @Operation(summary = "Add workshop operating hour exception")
    public ResponseEntity<OperatingHourException> addWorkshopException(
            @PathVariable UUID workshopId,
            @Valid @RequestBody CreateOperatingHourExceptionRequest request) {
        OperatingHourException created = operatingHoursService.addWorkshopException(
                workshopId, request.date(), request.closed(), request.openTime(), request.closeTime(), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/workshops/{workshopId}/operating-hours/exceptions/{exceptionId}")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    @Operation(summary = "Delete workshop operating hour exception")
    public ResponseEntity<Void> deleteWorkshopException(
            @PathVariable UUID workshopId,
            @PathVariable UUID exceptionId) {
        operatingHoursService.deleteWorkshopException(workshopId, exceptionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/branches/{branchId}/operating-hours/exceptions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List branch operating hour exceptions")
    public ResponseEntity<List<OperatingHourException>> listBranchExceptions(@PathVariable UUID branchId) {
        return ResponseEntity.ok(operatingHoursService.listBranchExceptions(branchId));
    }

    @PostMapping("/branches/{branchId}/operating-hours/exceptions")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    @Operation(summary = "Add branch operating hour exception")
    public ResponseEntity<OperatingHourException> addBranchException(
            @PathVariable UUID branchId,
            @Valid @RequestBody CreateOperatingHourExceptionRequest request) {
        OperatingHourException created = operatingHoursService.addBranchException(
                branchId, request.date(), request.closed(), request.openTime(), request.closeTime(), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/branches/{branchId}/operating-hours/exceptions/{exceptionId}")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    @Operation(summary = "Delete branch operating hour exception")
    public ResponseEntity<Void> deleteBranchException(
            @PathVariable UUID branchId,
            @PathVariable UUID exceptionId) {
        operatingHoursService.deleteBranchException(branchId, exceptionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/branches/{branchId}/availability")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Resolve effective branch availability for a target date")
    public ResponseEntity<OperatingHoursResolutionStrategy.EffectiveAvailability> getBranchAvailability(
            @PathVariable UUID branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(operatingHoursService.getBranchAvailability(branchId, date));
    }
}
