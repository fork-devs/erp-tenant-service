package com.erp.tenant.infrastructure.adapter.external;

import com.erp.tenant.domain.port.CustomerProfileQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class HttpCustomerProfileAdapter implements CustomerProfileQueryPort {

    private static final Logger log = LoggerFactory.getLogger(HttpCustomerProfileAdapter.class);

    private final RestClient restClient;
    private final String internalServiceKey;

    public HttpCustomerProfileAdapter(
            @Value("${erp.fleet.url:http://erp-fleet-service:8083}") String fleetBaseUrl,
            @Value("${erp.security.internal-service-key:}") String internalServiceKey) {
        this.restClient = RestClient.builder().baseUrl(fleetBaseUrl).build();
        this.internalServiceKey = internalServiceKey;
    }

    @Override
    public Optional<CustomerProfileSnapshot> findByUserId(UUID userId) {
        try {
            CustomerProfileSnapshot res = restClient.get()
                    .uri("/api/v1/internal/customers/by-user/{userId}", userId)
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .retrieve()
                    .body(CustomerProfileSnapshot.class);
            return Optional.ofNullable(res);
        } catch (Exception ex) {
            log.debug("Customer profile lookup by userId={} failed: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<CustomerProfileSnapshot> findByDocumentNumber(String documentNumber) {
        try {
            CustomerProfileSnapshot res = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/internal/customers/by-document")
                            .queryParam("documentNumber", documentNumber).build())
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .retrieve()
                    .body(CustomerProfileSnapshot.class);
            return Optional.ofNullable(res);
        } catch (Exception ex) {
            log.debug("Customer profile lookup by documentNumber failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<CustomerProfileSnapshot> updateProfileImage(UUID userId, String profileImageUrl) {
        try {
            CustomerProfileSnapshot res = restClient.patch()
                    .uri("/api/v1/internal/customers/by-user/{userId}/image", userId)
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("profileImageUrl", profileImageUrl))
                    .retrieve()
                    .body(CustomerProfileSnapshot.class);
            return Optional.ofNullable(res);
        } catch (Exception ex) {
            log.debug("Customer profile image update for userId={} failed: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }
}
