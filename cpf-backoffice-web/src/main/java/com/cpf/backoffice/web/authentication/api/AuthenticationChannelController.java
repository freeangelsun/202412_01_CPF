package com.cpf.backoffice.web.authentication.api;

import com.cpf.backoffice.web.shared.api.ChannelRequestForwarder;
import com.cpf.backoffice.web.shared.client.BusinessApiHttpClient;
import com.cpf.backoffice.web.shared.config.BackofficeWebProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Browser credential를 CPF 업무 Domain에서 분리하는 Backoffice BFF 인증 Adapter입니다.
 *
 * <p>Access/Refresh token은 JavaScript에 보관하지 않고 HttpOnly Cookie로 관리합니다. 실제 업무 Domain에는
 * BFF가 Access token을 Authorization으로 주입하며 Refresh token은 refresh/logout Operation body로만 전달합니다.</p>
 */
@RestController
@RequestMapping({"/api/v1/backoffice/auth/**"})
public final class AuthenticationChannelController {
    private final ChannelRequestForwarder requestForwarder;
    private final BackofficeWebProperties properties;
    private final ObjectMapper objectMapper;

    public AuthenticationChannelController(ChannelRequestForwarder requestForwarder,
                                           BackofficeWebProperties properties,
                                           ObjectMapper objectMapper) {
        this.requestForwarder = requestForwarder;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @RequestMapping
    ResponseEntity<byte[]> forward(HttpServletRequest request) throws IOException, InterruptedException {
        String path = request.getRequestURI();
        if (path.endsWith("/auth/refresh") || path.endsWith("/auth/logout")) {
            String refreshToken = BusinessApiHttpClient.cookie(request, properties.refreshCookieName());
            if (refreshToken == null || refreshToken.isBlank()) {
                if (path.endsWith("/auth/logout")) return cleared(ResponseEntity.ok("{\"loggedOut\":true}".getBytes(StandardCharsets.UTF_8)));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Refresh credential is missing\"}".getBytes(StandardCharsets.UTF_8));
            }
            byte[] body = objectMapper.writeValueAsBytes(Map.of("refreshToken", refreshToken));
            ResponseEntity<byte[]> upstream = requestForwarder.forward(request, body, Map.of("Content-Type", "application/json"));
            if (path.endsWith("/auth/logout")) return cleared(upstream);
            return upstream.getStatusCode().is2xxSuccessful() ? sessionized(upstream) : upstream;
        }
        ResponseEntity<byte[]> upstream = requestForwarder.forward(request);
        if (path.endsWith("/auth/login") && upstream.getStatusCode().is2xxSuccessful()) return sessionized(upstream);
        return upstream;
    }

    private ResponseEntity<byte[]> sessionized(ResponseEntity<byte[]> upstream) throws IOException {
        JsonNode parsed = objectMapper.readTree(upstream.getBody());
        if (!(parsed instanceof ObjectNode json)) throw new IOException("MBW auth response must be a JSON object");
        String accessToken = text(json, "accessToken");
        String refreshToken = text(json, "refreshToken");
        if (accessToken == null || refreshToken == null) throw new IOException("MBW auth response does not contain required session credentials");
        long accessTtl = json.path("expiresIn").asLong(600L);
        long refreshTtl = 7200L;
        String expiresAt = text(json, "refreshExpiresAt");
        if (expiresAt != null) {
            try { refreshTtl = Math.max(1L, Duration.between(Instant.now(), Instant.parse(expiresAt)).toSeconds()); }
            catch (RuntimeException ignored) { refreshTtl = 7200L; }
        }
        json.remove("accessToken");
        json.remove("refreshToken");
        json.put("authenticated", true);
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(upstream.getHeaders());
        headers.add(HttpHeaders.SET_COOKIE, cookie(properties.accessCookieName(), accessToken, accessTtl).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookie(properties.refreshCookieName(), refreshToken, refreshTtl).toString());
        return new ResponseEntity<>(objectMapper.writeValueAsBytes(json), headers, upstream.getStatusCode());
    }

    private ResponseEntity<byte[]> cleared(ResponseEntity<byte[]> upstream) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(upstream.getHeaders());
        headers.add(HttpHeaders.SET_COOKIE, clear(properties.accessCookieName()).toString());
        headers.add(HttpHeaders.SET_COOKIE, clear(properties.refreshCookieName()).toString());
        return new ResponseEntity<>(upstream.getBody(), headers, upstream.getStatusCode());
    }

    private ResponseCookie cookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value).httpOnly(true).secure(properties.secureCookies()).sameSite(properties.cookieSameSite())
                .path("/").maxAge(Duration.ofSeconds(Math.max(1L, maxAgeSeconds))).build();
    }

    private ResponseCookie clear(String name) {
        return ResponseCookie.from(name, "").httpOnly(true).secure(properties.secureCookies()).sameSite(properties.cookieSameSite())
                .path("/").maxAge(Duration.ZERO).build();
    }

    private static String text(ObjectNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() || node.asText().isBlank() ? null : node.asText();
    }
}
