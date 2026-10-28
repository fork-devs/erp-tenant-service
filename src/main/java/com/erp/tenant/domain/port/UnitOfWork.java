package com.erp.tenant.domain.port;

/**
 * Transaction boundary abstraction for domain business operations.
 *
 * <p>Ensures that operations mutating multiple aggregate roots or outbox entries
 * commit or roll back atomically without coupling the domain to Spring transaction APIs.
 */
public interface UnitOfWork {

    void runInTransaction(Runnable work);

    <T> T callInTransaction(java.util.function.Supplier<T> work);
}
