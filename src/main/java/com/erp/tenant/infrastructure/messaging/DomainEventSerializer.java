package com.erp.tenant.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.erp.tenant.domain.event.DomainEvent;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Serializes domain events to robust JSON format using Jackson.
 *
 * <p>Preserves immutable event identifiers across retry cycles for consumer deduplication.
 */
@Component
public class DomainEventSerializer {

    private final ObjectMapper objectMapper;

    public DomainEventSerializer() {
        this(new ObjectMapper().findAndRegisterModules());
    }

    public DomainEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(DomainEvent event) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("eventId", event.getEventId().toString());
        node.put("eventType", event.getEventType());
        node.put("occurredAt", DateTimeFormatter.ISO_INSTANT.format(event.getOccurredAt()));

        if (event instanceof com.erp.tenant.domain.event.TenantProvisionedEvent provisioned) {
            node.put("tenantId", provisioned.getTenantId().toString());
            node.put("commercialName", provisioned.getCommercialName());
            node.put("ruc", provisioned.getRuc());
            node.put("planCode", provisioned.getPlanCode());
            node.put("maxUsers", provisioned.getMaxUsers());
            node.put("maxVehicles", provisioned.getMaxVehicles());
            node.put("maxIotMsgPerDay", provisioned.getMaxIotMsgPerDay());
        } else if (event instanceof com.erp.tenant.domain.event.TenantPlanChangedEvent planChanged) {
            node.put("tenantId", planChanged.getTenantId().toString());
            node.put("oldPlanId", planChanged.getOldPlanId().toString());
            node.put("newPlanId", planChanged.getNewPlanId().toString());
            node.put("newPlanCode", planChanged.getNewPlanCode());
            node.put("newMaxUsers", planChanged.getNewMaxUsers());
            node.put("newMaxVehicles", planChanged.getNewMaxVehicles());
            node.put("newMaxIotMsgPerDay", planChanged.getNewMaxIotMsgPerDay());
        } else if (event instanceof com.erp.tenant.domain.event.TenantSuspendedEvent suspended) {
            node.put("tenantId", suspended.getTenantId().toString());
            node.put("ruc", suspended.getRuc());
            node.put("reason", suspended.getReason());
        }

        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el evento de dominio", e);
        }
    }
}
