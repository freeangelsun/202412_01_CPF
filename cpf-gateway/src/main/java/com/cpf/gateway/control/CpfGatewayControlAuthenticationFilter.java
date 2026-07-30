package com.cpf.gateway.control;

import com.cpf.core.api.gateway.CpfGatewayControlHeaders;
import com.cpf.core.api.gateway.CpfGatewayControlSigner;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 내부 Gateway Control API에 ADM 서명·시각·Nonce 검증을 적용합니다. */
@Component
@ConditionalOnProperty(prefix = "cpf.gateway.control", name = "enabled", havingValue = "true")
public final class CpfGatewayControlAuthenticationFilter extends OncePerRequestFilter {
    private static final String PREFIX = "/internal/v1/gateway/registry";
    private final CpfGatewayControlSecurityProperties properties;
    private final Clock clock;
    private final ConcurrentHashMap<String, Long> nonces = new ConcurrentHashMap<>();

    public CpfGatewayControlAuthenticationFilter(CpfGatewayControlSecurityProperties properties) {
        this(properties, Clock.systemUTC());
    }

    CpfGatewayControlAuthenticationFilter(CpfGatewayControlSecurityProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        properties.validate();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String caller = required(request.getHeader(CpfGatewayControlHeaders.CALLER_SERVICE), "caller service");
            if (!"ADM".equals(caller)) throw new SecurityException("ADM caller만 허용됩니다.");
            String operator = required(request.getHeader(CpfGatewayControlHeaders.OPERATOR_ID), "operatorId");
            String nonce = required(request.getHeader(CpfGatewayControlHeaders.NONCE), "nonce");
            String signature = required(request.getHeader(CpfGatewayControlHeaders.SIGNATURE), "signature");
            long timestamp = Long.parseLong(required(request.getHeader(CpfGatewayControlHeaders.TIMESTAMP), "timestamp"));
            CpfGatewayControlSigner.requireFresh(timestamp, clock, Duration.ofSeconds(properties.getAllowedSkewSeconds()));
            String target = request.getRequestURI()
                    + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
            if (!CpfGatewayControlSigner.verify(properties.getSharedSecret(), request.getMethod(), target,
                    caller, operator, timestamp, nonce, signature)) {
                throw new SecurityException("Gateway Control 서명이 올바르지 않습니다.");
            }
            evictExpired();
            long expiresAt = clock.millis() + Duration.ofSeconds(properties.getNonceRetentionSeconds()).toMillis();
            if (nonces.putIfAbsent(caller + ":" + nonce, expiresAt) != null) {
                throw new SecurityException("이미 사용된 Gateway Control nonce입니다.");
            }
            request.setAttribute("gateway.control.operatorId", operator);
            chain.doFilter(request, response);
        } catch (IllegalArgumentException ex) {
            reject(response, HttpServletResponse.SC_BAD_REQUEST, "GATEWAY_CONTROL_INVALID", ex.getMessage());
        } catch (SecurityException ex) {
            reject(response, HttpServletResponse.SC_UNAUTHORIZED, "GATEWAY_CONTROL_UNAUTHORIZED", ex.getMessage());
        }
    }

    private void evictExpired() {
        long now = clock.millis();
        for (Map.Entry<String, Long> item : nonces.entrySet()) {
            if (item.getValue() < now) nonces.remove(item.getKey(), item.getValue());
        }
        if (nonces.size() > 100_000) {
            throw new IllegalStateException("Gateway Control nonce 저장소 안전 상한을 초과했습니다.");
        }
    }

    private static void reject(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String safe = Objects.toString(message, "요청이 거부되었습니다.")
                .replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", " ").replace("\n", " ");
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + safe + "\"}");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
