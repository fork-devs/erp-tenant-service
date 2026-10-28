package com.erp.tenant.infrastructure.config;

import com.erp.tenant.domain.port.UnitOfWork;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Unit of work implementation backed by Spring's {@link TransactionTemplate}.
 *
 * <p>Uses default REQUIRED propagation so nested operations participate in active transactions.
 */
@Component
public class UnitOfWorkAdapter implements UnitOfWork {

    private final TransactionTemplate transactionTemplate;

    public UnitOfWorkAdapter(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void runInTransaction(Runnable work) {
        transactionTemplate.executeWithoutResult(status -> work.run());
    }

    @Override
    public <T> T callInTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
