package com.erp.tenant.infrastructure.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client for public JSON Web Key Sets (JWKS) published by auth-service.
 *
 * <p>Enforces three key security invariants:
 * <ul>
 *   <li><strong>Interval-based cache refresh:</strong> refreshes cached keys when stale to support key rotation without service restart.</li>
 *   <li><strong>Fail-closed on unknown key ID:</strong> unknown key IDs are rejected rather than falling back to unverified defaults.</li>
 *   <li><strong>Bounded timeouts and backoff:</strong> bounded connection and read timeouts prevent cascading outages on issuer latency.</li>
 * </ul>
 */
@Component
public class JwksKeyManager {

    private static final Logger log = LoggerFactory.getLogger(JwksKeyManager.class);

    private static final long CACHE_TTL_SECONDS = 300;
    private static final long MIN_RETRY_SECONDS = 5;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final String jwksUrl;
    private final HttpClient httpClient;
    private final Map<String, RSAPublicKey> publicKeys = new ConcurrentHashMap<>();
    private volatile Instant lastSuccessfulFetch = Instant.EPOCH;
    private volatile Instant lastAttempt = Instant.EPOCH;

    public JwksKeyManager(@Value("${erp.security.jwks-url:http://localhost:8081/.well-known/jwks.json}") String jwksUrl) {
        this.jwksUrl = jwksUrl != null ? jwksUrl.trim() : "";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * Returns the public key for the given key ID (kid), or null if unknown.
     */
    public RSAPublicKey getPublicKey(String keyId) {
        refrescarSiHaCaducado(false);

        RSAPublicKey key = keyId != null ? publicKeys.get(keyId) : null;
        if (key != null) {
            return key;
        }

        // Unknown key ID: may indicate a recently rotated key, force refresh before rejecting.
        refrescarSiHaCaducado(true);
        return keyId != null ? publicKeys.get(keyId) : null;
    }

    private synchronized void refrescarSiHaCaducado(boolean force) {
        Instant now = Instant.now();

        if (!force) {
            if (lastSuccessfulFetch.plusSeconds(CACHE_TTL_SECONDS).isAfter(now)) {
                return;
            }
            if (lastAttempt.plusSeconds(MIN_RETRY_SECONDS).isAfter(now)) {
                // Recently attempted: avoid hitting the issuer repeatedly.
                return;
            }
        }

        lastAttempt = now;

        if (jwksUrl.isEmpty()) {
            log.warn("URL del conjunto de claves no configurada");
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(jwksUrl))
                    .timeout(READ_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("El emisor respondio {} al conjunto de claves; se conservan las claves ya cargadas",
                        response.statusCode());
                return;
            }

            JWKSet jwkSet = JWKSet.parse(response.body());

            Map<String, RSAPublicKey> cargadas = new ConcurrentHashMap<>();
            for (JWK jwk : jwkSet.getKeys()) {
                if (jwk instanceof RSAKey rsaKey) {
                    try {
                        String id = rsaKey.getKeyID() != null ? rsaKey.getKeyID() : "default";
                        cargadas.put(id, rsaKey.toRSAPublicKey());
                    } catch (Exception e) {
                        log.error("No se pudo convertir la clave publica RSA del conjunto de claves", e);
                    }
                }
            }

            if (cargadas.isEmpty()) {
                log.warn("El conjunto de claves no contiene ninguna clave RSA utilizable; se conservan las claves ya cargadas");
                return;
            }

            publicKeys.keySet().retainAll(cargadas.keySet());
            publicKeys.putAll(cargadas);
            lastSuccessfulFetch = now;
            log.info("Conjunto de claves actualizado: {}", publicKeys.keySet());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Se interrumpio la descarga del conjunto de claves; se conservan las claves ya cargadas");
        } catch (Exception e) {
            log.warn("No se pudo cargar el conjunto de claves ({}); se conservan las claves ya cargadas", e.getMessage());
        }
    }
}
