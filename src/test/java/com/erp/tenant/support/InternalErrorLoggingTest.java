package com.erp.tenant.support;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Un error interno queda registrado, y no expone su contenido al cliente.
 *
 * <p>El manejador global registra la excepcion con su traza, y devuelve una
 * respuesta generica. El diagnostico va al registro, que es del operador; al
 * cliente le llega un error que no dice nada del interior del servicio.
 */
class InternalErrorLoggingTest extends AbstractPostgresIntegrationTest {

    private MockMvc mockMvc;
    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new com.erp.tenant.interfaces.rest.controller.TenantController(
                                null, null, null, null, null, null))
                .setControllerAdvice(new com.erp.tenant.interfaces.rest.advice.GlobalRestExceptionHandler())
                .build();

        logger = (Logger) LoggerFactory.getLogger(
                com.erp.tenant.interfaces.rest.advice.GlobalRestExceptionHandler.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    @DisplayName("El error interno se registra y la respuesta al cliente es generica")
    void elErrorInternoSeRegistraYLaRespuestaEsGenerica() throws Exception {
        // Un servicio que falla produce un error interno.
        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commercialName\":\"Taller\",\"ruc\":\"20601234567\","
                                + "\"planCode\":\"BASICO\",\"brandColor\":\"#1A2B3C\"}"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("NullPointerException"))));

        boolean registrado = appender.list.stream()
                .anyMatch(evento -> evento.getFormattedMessage().contains("error interno")
                        || evento.getFormattedMessage().contains("Unhandled internal server error"));
        assertTrue(registrado, "El error interno debe quedar registrado");
    }
}
