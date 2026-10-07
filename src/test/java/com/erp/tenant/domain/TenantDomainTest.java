package com.erp.tenant.domain;

import com.erp.tenant.domain.exception.TenantSuspendedException;
import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.model.TenantStatus;
import com.erp.tenant.domain.valueobject.BrandColor;
import com.erp.tenant.domain.valueobject.Ruc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantDomainTest {

    @Test
    @DisplayName("Should create tenant with ACTIVE status and valid defaults")
    void shouldCreateTenantSuccessfully() {
        Ruc ruc = Ruc.of("20601234567");
        UUID planId = UUID.randomUUID();
        Tenant tenant = Tenant.create("AutoTaller Lima Express", ruc, planId, BrandColor.of("#2563EB"));

        assertNotNull(tenant.getId());
        assertEquals("AutoTaller Lima Express", tenant.getCommercialName());
        assertEquals(ruc, tenant.getRuc());
        assertEquals(planId, tenant.getPlanId());
        assertEquals(TenantStatus.ACTIVE, tenant.getStatus());
        assertTrue(tenant.isActive());
        assertEquals("#2563EB", tenant.getBrandColor().getValue());
        assertNotNull(tenant.getCreatedAt());
    }

    @Test
    @DisplayName("Should update branding correctly")
    void shouldUpdateBranding() {
        Ruc ruc = Ruc.of("20601234567");
        UUID planId = UUID.randomUUID();
        Tenant tenant = Tenant.create("AutoTaller Lima", ruc, planId, null);

        assertEquals("#2563EB", tenant.getBrandColor().getValue()); // default color

        tenant.updateBranding("AutoTaller Lima Norte", BrandColor.of("#10B981"));
        assertEquals("AutoTaller Lima Norte", tenant.getCommercialName());
        assertEquals("#10B981", tenant.getBrandColor().getValue());
    }

    @Test
    @DisplayName("Should change plan when tenant is active")
    void shouldChangePlanWhenActive() {
        Ruc ruc = Ruc.of("20601234567");
        UUID initialPlanId = UUID.randomUUID();
        UUID newPlanId = UUID.randomUUID();

        Tenant tenant = Tenant.create("Taller Central", ruc, initialPlanId, null);
        tenant.changePlan(newPlanId);

        assertEquals(newPlanId, tenant.getPlanId());
    }

    @Test
    @DisplayName("Should forbid plan change when tenant is suspended")
    void shouldForbidPlanChangeWhenSuspended() {
        Ruc ruc = Ruc.of("20601234567");
        UUID initialPlanId = UUID.randomUUID();
        UUID newPlanId = UUID.randomUUID();

        Tenant tenant = Tenant.create("Taller Central", ruc, initialPlanId, null);
        tenant.suspend();

        assertFalse(tenant.isActive());
        assertEquals(TenantStatus.SUSPENDED, tenant.getStatus());

        assertThrows(TenantSuspendedException.class, () -> tenant.changePlan(newPlanId));
    }

    @Test
    @DisplayName("Should reactivate suspended tenant")
    void shouldReactivateSuspendedTenant() {
        Ruc ruc = Ruc.of("20601234567");
        UUID planId = UUID.randomUUID();

        Tenant tenant = Tenant.create("Taller Central", ruc, planId, null);
        tenant.suspend();
        assertEquals(TenantStatus.SUSPENDED, tenant.getStatus());

        tenant.reactivate();
        assertEquals(TenantStatus.ACTIVE, tenant.getStatus());
        assertTrue(tenant.isActive());
    }

    @Test
    @DisplayName("Should reject invalid construction arguments")
    void shouldRejectInvalidConstruction() {
        Ruc ruc = Ruc.of("20601234567");
        UUID planId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> Tenant.create("", ruc, planId, null));
        assertThrows(IllegalArgumentException.class, () -> Tenant.create(null, ruc, planId, null));
        assertThrows(IllegalArgumentException.class, () -> Tenant.create("Taller", null, planId, null));
        assertThrows(IllegalArgumentException.class, () -> Tenant.create("Taller", ruc, null, null));
    }
}
