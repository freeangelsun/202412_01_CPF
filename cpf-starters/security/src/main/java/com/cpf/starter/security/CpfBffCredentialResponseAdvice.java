package com.cpf.starter.security;

import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 인증 응답의 credential을 Browser body에서 제거하고 server-side session으로 이동한다.
 *
 * <p>인증 성공 응답에 credential이 포함됐는데 계약 형태가 예상과 다르면 원문을 반환하지 않고
 * fail-closed 한다. Session ID는 Cookie transport만 사용하며 응답 body에 노출하지 않는다.</p>
 */
@ControllerAdvice
public final class CpfBffCredentialResponseAdvice implements ResponseBodyAdvice<Object> {
    private static final Set<String> CREDENTIAL_KEYS = Set.of("accessToken", "refreshToken");

    @Override
    public boolean supports(MethodParameter parameter, Class<? extends HttpMessageConverter<?>> converterType) {
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
        if (!(request instanceof ServletServerHttpRequest servletRequest) || !isAuthenticationPath(servletRequest)) {
            return body;
        }

        if (!containsCredential(body)) {
            return body;
        }
        if (!(body instanceof Map<?, ?> source)) {
            throw new IllegalStateException("Authentication credential response must use the approved top-level contract.");
        }

        String accessToken = requiredCredential(source, "accessToken");
        String refreshToken = optionalCredential(source, "refreshToken");
        HttpSession session = servletRequest.getServletRequest().getSession(true);
        session.setAttribute(CpfBffSessionBridgeFilter.ACCESS_TOKEN, accessToken);
        if (refreshToken != null) {
            session.setAttribute(CpfBffSessionBridgeFilter.REFRESH_TOKEN, refreshToken);
        } else {
            session.removeAttribute(CpfBffSessionBridgeFilter.REFRESH_TOKEN);
        }

        Map<String, Object> sanitized = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            String name = String.valueOf(key);
            if (!CREDENTIAL_KEYS.contains(name)) {
                sanitized.put(name, value);
            }
        });
        return sanitized;
    }

    private static boolean isAuthenticationPath(ServletServerHttpRequest request) {
        String path = request.getServletRequest().getRequestURI();
        return path.endsWith("/auth/login") || path.endsWith("/auth/refresh");
    }

    private static String requiredCredential(Map<?, ?> source, String key) {
        Object value = source.get(key);
        if (!(value instanceof String credential) || credential.isBlank()) {
            throw new IllegalStateException("Authentication credential response contains an invalid " + key + ".");
        }
        return credential;
    }

    private static String optionalCredential(Map<?, ?> source, String key) {
        if (!source.containsKey(key)) {
            return null;
        }
        Object value = source.get(key);
        if (!(value instanceof String credential) || credential.isBlank()) {
            throw new IllegalStateException("Authentication credential response contains an invalid " + key + ".");
        }
        return credential;
    }

    private static boolean containsCredential(Object value) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (CREDENTIAL_KEYS.contains(String.valueOf(entry.getKey())) || containsCredential(entry.getValue())) {
                    return true;
                }
            }
        } else if (value instanceof Iterable<?> iterable) {
            for (Object element : iterable) {
                if (containsCredential(element)) {
                    return true;
                }
            }
        } else if (value instanceof Object[] array) {
            for (Object element : array) {
                if (containsCredential(element)) {
                    return true;
                }
            }
        }
        return false;
    }
}
