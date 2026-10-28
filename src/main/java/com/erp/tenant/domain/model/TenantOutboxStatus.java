package com.erp.tenant.domain.model;

/**
 * State of an outbox message.
 *
 * <p>Explicit typed state replaces raw booleans to distinguish freshly enqueued messages
 * from retrying or dead-lettered messages.
 */
public enum TenantOutboxStatus {

    /** Pending publication, scheduled with its next retry timestamp. */
    PENDING,

    /** Successfully published to the event bus. */
    PUBLISHED,

    /** Exhausted retry attempts: retained in Dead Letter Queue for inspection. */
    DEAD_LETTER
}
