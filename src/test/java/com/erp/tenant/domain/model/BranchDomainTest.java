package com.erp.tenant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BranchDomainTest {

    @Test
    @DisplayName("Should create branch with normalized uppercase code")
    void shouldCreateBranch() {
        UUID tenantId = UUID.randomUUID();
        UUID workshopId = UUID.randomUUID();

        Branch branch = Branch.create(
                tenantId,
                workshopId,
                "br-surco-01",
                "Sede Surco",
                "Av. Primavera 123",
                "+51987654321"
        );

        assertThat(branch.getId()).isNotNull();
        assertThat(branch.getCode()).isEqualTo("BR-SURCO-01");
        assertThat(branch.getName()).isEqualTo("Sede Surco");
        assertThat(branch.getAddress()).isEqualTo("Av. Primavera 123");
        assertThat(branch.getPhone()).isEqualTo("+51987654321");
        assertThat(branch.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should reject branch with empty required fields")
    void shouldRejectEmptyFields() {
        UUID tenantId = UUID.randomUUID();
        UUID workshopId = UUID.randomUUID();

        assertThatThrownBy(() -> Branch.create(null, workshopId, "CODE", "Name", "Addr", "Phone"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Branch.create(tenantId, null, "CODE", "Name", "Addr", "Phone"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Branch.create(tenantId, workshopId, "", "Name", "Addr", "Phone"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should create and cancel branch subscription")
    void shouldManageBranchSubscription() {
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        BranchSubscription sub = BranchSubscription.create(tenantId, branchId, planId, "MONTHLY");

        assertThat(sub.getId()).isNotNull();
        assertThat(sub.getStatus()).isEqualTo("ACTIVE");
        assertThat(sub.isActive()).isTrue();

        sub.cancel();
        assertThat(sub.getStatus()).isEqualTo("CANCELED");
        assertThat(sub.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should validate employee and owner document number")
    void shouldValidateDocumentNumber() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EmployeeProfile employee = EmployeeProfile.create(
                tenantId,
                userId,
                "Luis",
                "Mendoza",
                DocumentType.DNI,
                "71234567",
                "+51912345678"
        );
        assertThat(employee.getDocumentNumber()).isEqualTo("71234567");

        assertThatThrownBy(() -> EmployeeProfile.create(
                tenantId,
                userId,
                "Luis",
                "Mendoza",
                DocumentType.DNI,
                "123",
                "+51912345678"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8 digits");

        OwnerProfile owner = OwnerProfile.create(
                tenantId,
                userId,
                "Raul",
                "Castro",
                DocumentType.DNI,
                "45678901",
                "+51987112233"
        );
        assertThat(owner.getDocumentNumber()).isEqualTo("45678901");
    }
}
