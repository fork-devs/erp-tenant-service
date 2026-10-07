package com.erp.tenant.application.usecase;

public interface ProcessOutboxMessagesUseCase {
    int processUnpublishedMessages(int batchSize);
}
