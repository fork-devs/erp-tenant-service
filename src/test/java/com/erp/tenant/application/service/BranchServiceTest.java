package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.AssignBranchSubscriptionCommand;
import com.erp.tenant.application.dto.command.CreateBranchCommand;
import com.erp.tenant.application.dto.command.UpdateBranchCommand;
import com.erp.tenant.application.dto.result.BranchResult;
import com.erp.tenant.application.dto.result.BranchSubscriptionResult;
import com.erp.tenant.domain.exception.ActiveSubscriptionAlreadyExistsException;
import com.erp.tenant.domain.exception.BranchCodeAlreadyExistsException;
import com.erp.tenant.domain.exception.BranchNotFoundException;
import com.erp.tenant.domain.model.Branch;
import com.erp.tenant.domain.model.BranchSubscription;
import com.erp.tenant.domain.model.Plan;
import com.erp.tenant.domain.model.PlanCode;
import com.erp.tenant.domain.port.BranchRepositoryPort;
import com.erp.tenant.domain.port.BranchSubscriptionRepositoryPort;
import com.erp.tenant.domain.port.PlanRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepositoryPort branchRepositoryPort;

    @Mock
    private BranchSubscriptionRepositoryPort subscriptionRepositoryPort;

    @Mock
    private PlanRepositoryPort planRepositoryPort;

    @Mock
    private UnitOfWork unitOfWork;

    private BranchService branchService;

    @BeforeEach
    void setUp() {
        branchService = new BranchService(branchRepositoryPort, subscriptionRepositoryPort, planRepositoryPort, unitOfWork);
    }

    @Test
    @DisplayName("Should successfully create a branch")
    void shouldCreateBranch() {
        UUID workshopId = UUID.randomUUID();
        CreateBranchCommand command = new CreateBranchCommand(
                workshopId, "BR-SURCO-01", "Sede Surco", "Av. Primavera 123", "+51987654321"
        );

        when(branchRepositoryPort.existsByWorkshopIdAndCode(workshopId, "BR-SURCO-01")).thenReturn(false);
        when(unitOfWork.callInTransaction(any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        when(branchRepositoryPort.save(any(Branch.class))).thenAnswer(inv -> inv.getArgument(0));

        BranchResult result = branchService.create(command);

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("BR-SURCO-01");
        assertThat(result.name()).isEqualTo("Sede Surco");
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("Should reject branch creation if code already exists in workshop")
    void shouldRejectDuplicateCode() {
        UUID workshopId = UUID.randomUUID();
        CreateBranchCommand command = new CreateBranchCommand(
                workshopId, "BR-SURCO-01", "Sede Surco", "Av. Primavera 123", "+51987654321"
        );

        when(branchRepositoryPort.existsByWorkshopIdAndCode(workshopId, "BR-SURCO-01")).thenReturn(true);

        assertThatThrownBy(() -> branchService.create(command))
                .isInstanceOf(BranchCodeAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should update branch and deactivate")
    void shouldUpdateAndDeactivateBranch() {
        UUID branchId = UUID.randomUUID();
        UUID workshopId = UUID.randomUUID();
        Branch branch = Branch.create(workshopId, workshopId, "BR-01", "Old Name", "Old Addr", "+51999999999");

        when(branchRepositoryPort.findById(branchId)).thenReturn(Optional.of(branch));
        when(unitOfWork.callInTransaction(any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        when(branchRepositoryPort.save(any(Branch.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateBranchCommand updateCommand = new UpdateBranchCommand(branchId, "BR-01", "New Name", "New Addr", "+51988888888");
        BranchResult updated = branchService.update(updateCommand);

        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(updated.phone()).isEqualTo("+51988888888");

        branchService.deactivate(branchId);
        assertThat(branch.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should assign and cancel branch subscription")
    void shouldManageBranchSubscription() {
        UUID branchId = UUID.randomUUID();
        UUID workshopId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        Branch branch = Branch.create(workshopId, workshopId, "BR-01", "Sede Central", "Av. 1", "999");
        Plan plan = new Plan(planId, PlanCode.PRO, 10, 50, 1000, 100, 10, new BigDecimal("99.00"));

        when(branchRepositoryPort.findById(branchId)).thenReturn(Optional.of(branch));
        when(planRepositoryPort.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepositoryPort.findActiveByBranchId(branchId)).thenReturn(Optional.empty());
        when(unitOfWork.callInTransaction(any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        when(subscriptionRepositoryPort.save(any(BranchSubscription.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignBranchSubscriptionCommand command = new AssignBranchSubscriptionCommand(branchId, planId, "MONTHLY");
        BranchSubscriptionResult result = branchService.assignSubscription(command);

        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.billingCycle()).isEqualTo("MONTHLY");

        // Reject duplicate subscription
        BranchSubscription activeSub = BranchSubscription.create(workshopId, branchId, planId, "MONTHLY");
        when(subscriptionRepositoryPort.findActiveByBranchId(branchId)).thenReturn(Optional.of(activeSub));

        assertThatThrownBy(() -> branchService.assignSubscription(command))
                .isInstanceOf(ActiveSubscriptionAlreadyExistsException.class);
    }
}
