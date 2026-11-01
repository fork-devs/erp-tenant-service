package com.erp.tenant.infrastructure.adapter.external;

import com.erp.tenant.domain.port.DniValidationResult;
import com.erp.tenant.domain.port.RucValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpIdentityValidationAdapterTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private HttpIdentityValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new HttpIdentityValidationAdapter(
                "http://localhost:8085",
                "test-internal-key",
                httpClient,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("validateDni with valid RENIEC response should return verified result")
    void validateDni_WhenValidResponse_ShouldReturnVerified() throws Exception {
        String json = """
                {
                    "type": "DNI",
                    "documentNumber": "70514079",
                    "firstName": "ALDO JEANFRANCO",
                    "firstSurname": "MACHACCA",
                    "secondSurname": "SOTO",
                    "fullName": "MACHACCA SOTO, ALDO JEANFRANCO",
                    "verified": true,
                    "source": "RENIEC"
                }
                """;
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(json);
        doReturn(httpResponse).when(httpClient).send(any(HttpRequest.class), any());

        DniValidationResult result = adapter.validateDni("70514079");

        assertTrue(result.valid());
        assertEquals("70514079", result.documentNumber());
        assertEquals("ALDO JEANFRANCO", result.firstName());
        assertEquals("MACHACCA", result.paternalSurname());
        assertEquals("SOTO", result.maternalSurname());
    }

    @Test
    @DisplayName("validateDni with not verified response should return invalid")
    void validateDni_WhenNotVerified_ShouldReturnInvalid() throws Exception {
        String json = """
                {
                    "type": "DNI",
                    "documentNumber": "00000000",
                    "verified": false,
                    "message": "Documento no encontrado"
                }
                """;
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(json);
        doReturn(httpResponse).when(httpClient).send(any(HttpRequest.class), any());

        DniValidationResult result = adapter.validateDni("00000000");

        assertFalse(result.valid());
    }

    @Test
    @DisplayName("validateRuc with active and habido SUNAT response should return verified result")
    void validateRuc_WhenActiveAndHabido_ShouldReturnVerified() throws Exception {
        String json = """
                {
                    "type": "RUC",
                    "documentNumber": "20100070970",
                    "businessName": "SUPERMERCADOS PERUANOS S.A.",
                    "tradeName": "PLAZA VEA",
                    "status": "ACTIVO",
                    "condition": "HABIDO",
                    "address": "CAL. MORELLI NRO. 181",
                    "verified": true,
                    "source": "SUNAT"
                }
                """;
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(json);
        doReturn(httpResponse).when(httpClient).send(any(HttpRequest.class), any());

        RucValidationResult result = adapter.validateRuc("20100070970");

        assertTrue(result.valid());
        assertTrue(result.isActiveAndHabido());
        assertEquals("20100070970", result.ruc());
        assertEquals("SUPERMERCADOS PERUANOS S.A.", result.legalName());
        assertEquals("ACTIVO", result.status());
        assertEquals("HABIDO", result.condition());
    }

    @Test
    @DisplayName("validateRuc with inactive status should return non-active result")
    void validateRuc_WhenInactive_ShouldReturnNonActive() throws Exception {
        String json = """
                {
                    "type": "RUC",
                    "documentNumber": "20999999999",
                    "businessName": "EMPRESA BAJA",
                    "status": "BAJA",
                    "condition": "NO HABIDO",
                    "verified": true,
                    "source": "SUNAT"
                }
                """;
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(json);
        doReturn(httpResponse).when(httpClient).send(any(HttpRequest.class), any());

        RucValidationResult result = adapter.validateRuc("20999999999");

        assertTrue(result.valid());
        assertFalse(result.isActiveAndHabido());
    }
}
