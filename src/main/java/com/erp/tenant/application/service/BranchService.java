package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.AssignBranchSubscriptionCommand;
import com.erp.tenant.application.dto.command.CreateBranchCommand;
import com.erp.tenant.application.dto.command.UpdateBranchCommand;
import com.erp.tenant.application.dto.result.BranchResult;
import com.erp.tenant.application.dto.result.BranchSubscriptionResult;
import com.erp.tenant.application.usecase.GetBranchesUseCase;
import com.erp.tenant.application.usecase.ManageBranchSubscriptionUseCase;
import com.erp.tenant.application.usecase.ManageBranchUseCase;
import com.erp.tenant.domain.exception.ActiveSubscriptionAlreadyExistsException;
import com.erp.tenant.domain.exception.BranchCodeAlreadyExistsException;
import com.erp.tenant.domain.exception.BranchNotFoundException;
import com.erp.tenant.domain.exception.PlanNotFoundException;
import com.erp.tenant.domain.model.Branch;
import com.erp.tenant.domain.model.BranchSubscription;
import com.erp.tenant.domain.port.BranchRepositoryPort;
import com.erp.tenant.domain.port.BranchSubscriptionRepositoryPort;
import com.erp.tenant.domain.port.PlanRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.infrastructure.security.TenantContextHolder;

import java.util.List;
import java.util.UUID;

public class BranchService implements ManageBranchUseCase, GetBranchesUseCase, ManageBranchSubscriptionUseCase {

    private final BranchRepositoryPort branchRepositoryPort;
    private final BranchSubscriptionRepositoryPort subscriptionRepositoryPort;
    private final PlanRepositoryPort planRepositoryPort;
    private final UnitOfWork unitOfWork;

    public BranchService(BranchRepositoryPort branchRepositoryPort,
                         BranchSubscriptionRepositoryPort subscriptionRepositoryPort,
                         PlanRepositoryPort planRepositoryPort,
                         UnitOfWork unitOfWork) {
        this.branchRepositoryPort = branchRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.planRepositoryPort = planRepositoryPort;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public BranchResult create(CreateBranchCommand command) {
        if (command == null || command.workshopId() == null) {
            throw new IllegalArgumentException("Workshop ID is required to create a branch");
        }
        String normalizedCode = Branch.normalizeCode(command.code());
        if (branchRepositoryPort.existsByWorkshopIdAndCode(command.workshopId(), normalizedCode)) {
            throw new BranchCodeAlreadyExistsException(command.workshopId(), normalizedCode);
        }

        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            tenantId = command.workshopId();
        }

        Branch branch = Branch.create(
                tenantId,
                command.workshopId(),
                command.code(),
                command.name(),
                command.address(),
                command.phone()
        );

        Branch saved = unitOfWork.callInTransaction(() -> branchRepositoryPort.save(branch));
        return BranchResult.from(saved);
    }

    @Override
    public BranchResult update(UpdateBranchCommand command) {
        if (command == null || command.branchId() == null) {
            throw new IllegalArgumentException("Branch ID is required for update");
        }
        Branch branch = branchRepositoryPort.findById(command.branchId())
                .orElseThrow(() -> new BranchNotFoundException(command.branchId()));

        if (command.code() != null && !command.code().trim().isEmpty()) {
            String normalizedCode = Branch.normalizeCode(command.code());
            if (!normalizedCode.equalsIgnoreCase(branch.getCode()) &&
                    branchRepositoryPort.existsByWorkshopIdAndCode(branch.getWorkshopId(), normalizedCode)) {
                throw new BranchCodeAlreadyExistsException(branch.getWorkshopId(), normalizedCode);
            }
        }

        branch.update(command.code(), command.name(), command.address(), command.phone());
        Branch saved = unitOfWork.callInTransaction(() -> branchRepositoryPort.save(branch));
        return BranchResult.from(saved);
    }

    @Override
    public void deactivate(UUID branchId) {
        Branch branch = branchRepositoryPort.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException(branchId));

        branch.deactivate();
        unitOfWork.runInTransaction(() -> branchRepositoryPort.save(branch));
    }

    @Override
    public BranchResult getById(UUID branchId) {
        return branchRepositoryPort.findById(branchId)
                .map(BranchResult::from)
                .orElseThrow(() -> new BranchNotFoundException(branchId));
    }

    @Override
    public List<BranchResult> listByWorkshop(UUID workshopId) {
        return branchRepositoryPort.findByWorkshopId(workshopId).stream()
                .filter(Branch::isActive)
                .map(BranchResult::from)
                .toList();
    }

    @Override
    public BranchSubscriptionResult assignSubscription(AssignBranchSubscriptionCommand command) {
        Branch branch = branchRepositoryPort.findById(command.branchId())
                .orElseThrow(() -> new BranchNotFoundException(command.branchId()));

        planRepositoryPort.findById(command.planId())
                .orElseThrow(() -> new PlanNotFoundException(command.planId().toString()));

        if (subscriptionRepositoryPort.findActiveByBranchId(command.branchId()).isPresent()) {
            throw new ActiveSubscriptionAlreadyExistsException(command.branchId());
        }

        BranchSubscription sub = BranchSubscription.create(
                branch.getTenantId(),
                branch.getId(),
                command.planId(),
                command.billingCycle()
        );

        BranchSubscription saved = unitOfWork.callInTransaction(() -> subscriptionRepositoryPort.save(sub));
        return BranchSubscriptionResult.from(saved);
    }

    @Override
    public void cancelSubscription(UUID branchId) {
        branchRepositoryPort.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException(branchId));

        BranchSubscription activeSub = subscriptionRepositoryPort.findActiveByBranchId(branchId)
                .orElseThrow(() -> new BranchNotFoundException(branchId));

        activeSub.cancel();
        unitOfWork.runInTransaction(() -> subscriptionRepositoryPort.save(activeSub));
    }
}
