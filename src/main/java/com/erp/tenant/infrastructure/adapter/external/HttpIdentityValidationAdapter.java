package com.erp.tenant.infrastructure.adapter.external;

import com.erp.tenant.domain.port.DniValidationResult;
import com.erp.tenant.domain.port.IdentityValidationPort;
import com.erp.tenant.domain.port.RucValidationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Adapter that invokes the official verification endpoints of erp-integration-hub
 * for authenticating national identity (RENIEC) and taxpayer records (SUNAT).
 */
@Component
public class HttpIdentityValidationAdapter implements IdentityValidationPort {

    private static final Logger log = LoggerFactory.getLogger(HttpIdentityValidationAdapter.class);
    private static final String PLATFORM_TENANT = "00000000-0000-0000-0000-000000000000";

    private final String baseUrl;
    private final String internalServiceKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpIdentityValidationAdapter(
            @Value("${erp.integration-hub.base-url:http://localhost:8085}") String baseUrl,
            @Value("${erp.security.internal-service-key:erp_internal_secret_service_key_2026}") String internalServiceKey) {
        this(baseUrl, internalServiceKey, HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5))
                .build(), new ObjectMapper());
    }

    public HttpIdentityValidationAdapter(
            String baseUrl,
            String internalServiceKey,
            HttpClient httpClient,
            ObjectMapper objectMapper) {
        this.baseUrl = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl.trim().replaceAll("/+$", "") : "http://localhost:8085";
        this.internalServiceKey = internalServiceKey != null ? internalServiceKey.trim() : "";
        this.httpClient = httpClient != null ? httpClient : HttpClient.newHttpClient();
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public DniValidationResult validateDni(String dni) {
        if (dni == null || !dni.matches("^\\d{8}$")) {
            log.warn("[IDENTITY ACL] Invalid DNI format: {}", dni);
            return DniValidationResult.invalid(dni);
        }

        try {
            String uriString = baseUrl + "/api/v1/integrations/validate/lookup?type=DNI&number=" + dni;
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(uriString))
                    .timeout(Duration.ofSeconds(6))
                    .header("Accept", "application/json")
                    .header("X-Service-Name", "erp-tenant-service")
                    .header("X-Tenant-ID", PLATFORM_TENANT)
                    .GET();

            if (!internalServiceKey.isBlank()) {
                reqBuilder.header("X-Internal-Service-Key", internalServiceKey);
            }

            HttpResponse<String> resp = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                JsonNode root = objectMapper.readTree(resp.body());
                boolean verified = root.has("verified") && root.get("verified").asBoolean();
                if (verified) {
                    String firstName = extractField(root, "firstName", "nombres");
                    String paternalSurname = extractField(root, "firstSurname", "paternalSurname", "lastName", "apellidoPaterno");
                    String maternalSurname = extractField(root, "secondSurname", "maternalSurname", "apellidoMaterno");
                    String fullName = extractField(root, "fullName", "nombreCompleto");

                    return new DniValidationResult(dni, firstName, paternalSurname, maternalSurname, fullName, true);
                }
            }
            log.info("[IDENTITY ACL] DNI {} not verified in integration hub (status: {})", dni, resp.statusCode());
        } catch (Exception e) {
            log.error("[IDENTITY ACL] Failure querying integration hub for DNI {}: {}", dni, e.getMessage());
        }

        return DniValidationResult.invalid(dni);
    }

    @Override
    public RucValidationResult validateRuc(String ruc) {
        if (ruc == null || !ruc.matches("^\\d{11}$")) {
            log.warn("[IDENTITY ACL] Invalid RUC format: {}", ruc);
            return RucValidationResult.invalid(ruc);
        }

        try {
            String uriString = baseUrl + "/api/v1/integrations/validate/lookup?type=RUC&number=" + ruc;
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(uriString))
                    .timeout(Duration.ofSeconds(6))
                    .header("Accept", "application/json")
                    .header("X-Service-Name", "erp-tenant-service")
                    .header("X-Tenant-ID", PLATFORM_TENANT)
                    .GET();

            if (!internalServiceKey.isBlank()) {
                reqBuilder.header("X-Internal-Service-Key", internalServiceKey);
            }

            HttpResponse<String> resp = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                JsonNode root = objectMapper.readTree(resp.body());
                boolean verified = root.has("verified") && root.get("verified").asBoolean();
                if (verified) {
                    String legalName = extractField(root, "businessName", "razonSocial", "razon_social");
                    String tradeName = extractField(root, "tradeName", "nombreComercial", "nombre_comercial");
                    String status = extractField(root, "status", "estado", "estado_contribuyente");
                    String condition = extractField(root, "condition", "condicion", "condicion_domicilio");
                    String fiscalAddress = extractField(root, "address", "domicilioFiscal", "direccion");

                    return new RucValidationResult(
                            ruc,
                            legalName != null ? legalName : "",
                            tradeName != null ? tradeName : (legalName != null ? legalName : ""),
                            status != null ? status : "ACTIVO",
                            condition != null ? condition : "HABIDO",
                            fiscalAddress != null ? fiscalAddress : "",
                            true
                    );
                }
            }
            log.info("[IDENTITY ACL] RUC {} not verified in integration hub (status: {})", ruc, resp.statusCode());
        } catch (Exception e) {
            log.error("[IDENTITY ACL] Failure querying integration hub for RUC {}: {}", ruc, e.getMessage());
        }

        return RucValidationResult.invalid(ruc);
    }

    private String extractField(JsonNode node, String... fieldNames) {
        for (String field : fieldNames) {
            if (node.hasNonNull(field)) {
                String val = node.get(field).asText().trim();
                if (!val.isBlank()) {
                    return val;
                }
            }
        }
        return null;
    }
}
