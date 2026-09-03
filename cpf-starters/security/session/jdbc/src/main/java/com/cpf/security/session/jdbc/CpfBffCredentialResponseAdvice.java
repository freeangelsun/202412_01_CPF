package com.cpf.security.session.jdbc;

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

/**
 * 인증 DTO/Map의 Credential을 Browser Body에서 제거하고 암호화 Vault에 저장합니다.
 *
 * <p>{@code @ControllerAdvice} 가 반드시 필요하다. Spring MVC 는 {@code ResponseBodyAdvice} 를
 * {@code ControllerAdviceBean.findAnnotatedBeans} 로만 수집하므로, 이 annotation 이 없으면
 * {@code @Bean} 으로 등록만 되고 **한 번도 실행되지 않는다**. 그 상태에서는
 * (1) accessToken/refreshToken 이 Browser 응답 Body 에 그대로 노출되고,
 * (2) Session 에 {@code CPF_BFF_CREDENTIAL_HANDLE} 이 저장되지 않아 이후 모든 ADM API 가
 *     인증되지 않는다. Browser 는 Authorization Header 사용이 금지되어 있으므로
 *     ({@code CpfBffSessionBridgeFilter}) 로그인 이후 아무 것도 호출할 수 없다.
 * 실제로 1-WAS 에서 로그인 직후 {@code POST /adm/api/log-policies/cache/refresh} 가 401 이었다.</p>
 */
@org.springframework.web.bind.annotation.ControllerAdvice
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

    /** ADM BFF Chain 이 소유하는 경로 접두사입니다. {@code CpfBffSessionBridgeFilter} 와 같은 경계입니다. */
    private static final String ADM_BFF_PREFIX = "/adm/api/";

    /**
     * ADM BFF 의 인증 발급 경로만 대상으로 합니다.
     *
     * <p>경로 접미사만 보면 Backoffice(MBW)의 {@code /api/v1/backoffice/auth/login} 까지 잡힌다.
     * MBW 는 Channel Front(cpf-backoffice-web)가 Bearer 로 연동하는 **다른 인증 경계**이며
     * HttpOnly BFF Session 을 쓰지 않는다. 실제로 이 Advice 가 MBW 로그인 응답까지 처리하면서
     * 1-WAS 의 {@code POST /api/v1/backoffice/auth/login} 이 500(ECPF990000)으로 실패했다.
     * ADM 과 Backoffice Web 의 인증 경계를 같은 것으로 취급하지 않는다.</p>
     */
    private static boolean isAuthenticationIssuePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null || !path.startsWith(ADM_BFF_PREFIX)) {
            return false;
        }
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
