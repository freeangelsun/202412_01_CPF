package com.cpf.starter.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** 인증 DTO/Map의 Credential을 Browser Body에서 제거하고 암호화 Vault에 저장합니다. */
public final class CpfBffCredentialResponseAdvice implements ResponseBodyAdvice<Object> {
    private static final Set<String> CREDENTIAL_KEYS = Set.of("accessToken", "refreshToken");
    private static final Set<String> PRINCIPAL_KEYS =
            Set.of("operatorId", "adminId", "userId", "memberId", "loginId");

    private final CpfBffCredentialVault vault;
    private final CpfServerSessionProperties properties;
    private final ObjectMapper mapper;
    private final CpfBffConcurrentSessionController concurrentSessions;
    private final Clock clock;

    public CpfBffCredentialResponseAdvice(
            CpfBffCredentialVault vault,
            CpfServerSessionProperties properties,
            ObjectMapper mapper,
            CpfBffConcurrentSessionController concurrentSessions) {
        this(vault, properties, mapper, concurrentSessions, Clock.systemUTC());
    }

    CpfBffCredentialResponseAdvice(
            CpfBffCredentialVault vault,
            CpfServerSessionProperties properties,
            ObjectMapper mapper,
            CpfBffConcurrentSessionController concurrentSessions,
            Clock clock) {
        this.vault = vault;
        this.properties = properties;
        this.mapper = mapper;
        this.concurrentSessions = concurrentSessions;
        this.clock = clock;
    }

    @Override
    public boolean supports(
            MethodParameter parameter,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter parameter,
            MediaType mediaType,
            Class<? extends HttpMessageConverter<?>> converterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)
                || !isAuthenticationIssuePath(servletRequest.getServletRequest())) {
            return body;
        }

        JsonNode tree = mapper.valueToTree(body);
        if (!containsCredential(tree)) {
            return body;
        }
        if (!(tree instanceof ObjectNode object)) {
            throw new IllegalStateException(
                    "Authentication credential response must use the approved object contract.");
        }

        String accessToken = requiredCredential(object, "accessToken");
        String refreshToken = optionalCredential(object, "refreshToken");
        String principal = findPrincipal(object);
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        boolean refresh = httpRequest.getRequestURI().endsWith("/auth/refresh");
        HttpSession session = refresh ? httpRequest.getSession(false) : httpRequest.getSession(true);
        if (session == null) {
            throw new IllegalStateException("CPF_BFF_SESSION_REQUIRED");
        }

        String previousHandle = stringAttribute(session, CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE);
        Instant now = clock.instant();
        Instant accessExpiry = now.plus(properties.accessTokenTtl());
        Instant refreshExpiry = refreshToken == null
                ? accessExpiry
                : now.plus(properties.refreshTokenTtl());

        String activeHandle = null;
        try {
            if (refresh) {
                if (previousHandle == null) {
                    throw new IllegalStateException("CPF_BFF_REFRESH_SESSION_HANDLE_REQUIRED");
                }
                CpfBffCredential current = vault.find(previousHandle)
                        .orElseThrow(() -> new IllegalStateException("CPF_BFF_CREDENTIAL_HANDLE_STALE"));
                String effectiveRefreshToken = refreshToken == null
                        ? current.refreshToken()
                        : refreshToken;
                Instant effectiveRefreshExpiry = refreshToken == null
                        ? current.refreshExpiresAt()
                        : refreshExpiry;
                if (effectiveRefreshToken == null || current.refreshExpired(now)) {
                    throw new IllegalStateException("CPF_BFF_REFRESH_CREDENTIAL_EXPIRED");
                }
                vault.rotate(previousHandle, accessToken, effectiveRefreshToken,
                        accessExpiry, effectiveRefreshExpiry, current.version());
                activeHandle = previousHandle;
            } else {
                activeHandle = vault.create(accessToken, refreshToken, accessExpiry, refreshExpiry);
                httpRequest.changeSessionId();
                if (previousHandle != null && !previousHandle.equals(activeHandle)) {
                    vault.revoke(previousHandle);
                }
            }
            session.setMaxInactiveInterval(Math.toIntExact(properties.timeout().toSeconds()));
            session.setAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE, activeHandle);
            session.setAttribute(CpfBffSessionBridgeFilter.PRINCIPAL_ID, principal);
            concurrentSessions.register(session, principal, properties.maxSessions());
        } catch (RuntimeException failure) {
            if (!refresh && activeHandle != null) {
                try {
                    vault.revoke(activeHandle);
                } catch (RuntimeException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }
            if (!refresh) {
                try {
                    session.invalidate();
                } catch (IllegalStateException ignored) {
                    // 이미 무효화된 Session입니다.
                }
            }
            throw failure;
        }

        ObjectNode sanitized = object.deepCopy();
        CREDENTIAL_KEYS.forEach(sanitized::remove);
        sanitized.remove("sessionId");
        return mapper.convertValue(sanitized, LinkedHashMap.class);
    }

    private static boolean isAuthenticationIssuePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.endsWith("/auth/login") || path.endsWith("/auth/refresh");
    }

    private static String requiredCredential(ObjectNode object, String key) {
        JsonNode value = object.get(key);
        if (value == null || !value.isTextual() || value.textValue().isBlank()) {
            throw new IllegalStateException(
                    "Authentication credential response contains an invalid " + key + ".");
        }
        return value.textValue();
    }

    private static String optionalCredential(ObjectNode object, String key) {
        JsonNode value = object.get(key);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isTextual() || value.textValue().isBlank()) {
            throw new IllegalStateException(
                    "Authentication credential response contains an invalid " + key + ".");
        }
        return value.textValue();
    }

    private static String findPrincipal(JsonNode value) {
        if (value.isObject()) {
            for (String key : PRINCIPAL_KEYS) {
                JsonNode candidate = value.get(key);
                if (candidate != null && !candidate.isNull() && !candidate.asText().isBlank()) {
                    return candidate.asText();
                }
            }
            Iterator<JsonNode> children = value.elements();
            while (children.hasNext()) {
                String nested = findPrincipalOrNull(children.next());
                if (nested != null) {
                    return nested;
                }
            }
        }
        throw new IllegalStateException("Authentication credential response does not contain an approved principal.");
    }

    private static String findPrincipalOrNull(JsonNode value) {
        if (value == null) {
            return null;
        }
        if (value.isObject()) {
            for (String key : PRINCIPAL_KEYS) {
                JsonNode candidate = value.get(key);
                if (candidate != null && !candidate.isNull() && !candidate.asText().isBlank()) {
                    return candidate.asText();
                }
            }
        }
        if (value.isContainerNode()) {
            Iterator<JsonNode> children = value.elements();
            while (children.hasNext()) {
                String nested = findPrincipalOrNull(children.next());
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static boolean containsCredential(JsonNode value) {
        if (value == null) {
            return false;
        }
        if (value.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = value.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (CREDENTIAL_KEYS.contains(entry.getKey()) || containsCredential(entry.getValue())) {
                    return true;
                }
            }
        } else if (value.isArray()) {
            for (JsonNode child : value) {
                if (containsCredential(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String stringAttribute(HttpSession session, String name) {
        Object value = session.getAttribute(name);
        return value instanceof String text && !text.isBlank() ? text : null;
    }
}
