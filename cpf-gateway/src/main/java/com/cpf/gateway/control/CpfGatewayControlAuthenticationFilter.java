package com.cpf.gateway.control;

import com.cpf.gateway.api.CpfGatewayControlHeaders;
import com.cpf.gateway.api.CpfGatewayControlSigner;
import com.cpf.gateway.api.CpfGatewayControlNoncePort;
import com.cpf.gateway.api.CpfGatewayControlSecurityAuditPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 내부 Gateway Control API에 Body 무결성·Instance Audience·Key Rotation·Nonce 검증을 적용합니다. */
@Component
@ConditionalOnProperty(prefix = "cpf.gateway.control", name = "enabled", havingValue = "true")
public final class CpfGatewayControlAuthenticationFilter extends OncePerRequestFilter {
    private static final String PREFIX = "/internal/v1/gateway/registry";
    private final CpfGatewayControlSecurityProperties properties;
    private final Clock clock;
    private final CpfGatewayControlNoncePort noncePort;
    private final CpfGatewayControlSecurityAuditPort securityAuditPort;

    @Autowired
    public CpfGatewayControlAuthenticationFilter(
            CpfGatewayControlSecurityProperties properties,
            CpfGatewayControlNoncePort noncePort,
            CpfGatewayControlSecurityAuditPort securityAuditPort) {
        this(properties, Clock.systemUTC(), noncePort, securityAuditPort);
    }

    /** 기존 Source Compatibility는 유지하되 Nonce 저장소 없는 Runtime은 요청 시 fail-closed 합니다. */
    @Deprecated(forRemoval = false)
    public CpfGatewayControlAuthenticationFilter(CpfGatewayControlSecurityProperties properties) {
        this(properties, Clock.systemUTC(), claim -> {
            throw new IllegalStateException("Shared Gateway Control nonce store is not installed");
        }, event -> {
            throw new IllegalStateException("Gateway Control security audit store is not installed");
        });
    }

    CpfGatewayControlAuthenticationFilter(
            CpfGatewayControlSecurityProperties properties,
            Clock cpfStarterClock,
            CpfGatewayControlNoncePort noncePort,
            CpfGatewayControlSecurityAuditPort securityAuditPort) {
        this.properties = properties;
        this.clock = cpfStarterClock;
        this.noncePort = Objects.requireNonNull(noncePort, "noncePort");
        this.securityAuditPort = Objects.requireNonNull(securityAuditPort, "securityAuditPort");
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
            byte[] body = readBounded(request, properties.getMaxBodyBytes());
            ReplayableRequest replayable = new ReplayableRequest(request, body);
            String caller = required(request.getHeader(CpfGatewayControlHeaders.CALLER_SERVICE), "caller service");
            if (!"ADM".equals(caller)) throw new SecurityException("ADM caller만 허용됩니다.");
            String operator = required(request.getHeader(CpfGatewayControlHeaders.OPERATOR_ID), "operatorId");
            String nonce = required(request.getHeader(CpfGatewayControlHeaders.NONCE), "nonce");
            String keyId = required(request.getHeader(CpfGatewayControlHeaders.KEY_ID), "keyId");
            String audience = required(request.getHeader(CpfGatewayControlHeaders.AUDIENCE), "audience");
            String contentHash = required(request.getHeader(CpfGatewayControlHeaders.CONTENT_SHA256), "contentSha256");
            String signature = required(request.getHeader(CpfGatewayControlHeaders.SIGNATURE), "signature");
            long timestamp = Long.parseLong(required(request.getHeader(CpfGatewayControlHeaders.TIMESTAMP), "timestamp"));

            String expectedAudience = properties.resolvedAudience();
            if (!MessageDigest.isEqual(expectedAudience.getBytes(StandardCharsets.UTF_8),
                    audience.getBytes(StandardCharsets.UTF_8))) {
                throw new SecurityException("Gateway Control audience가 현재 Instance와 일치하지 않습니다.");
            }
            String actualContentHash = CpfGatewayControlSigner.sha256(body);
            if (!MessageDigest.isEqual(actualContentHash.getBytes(StandardCharsets.US_ASCII),
                    contentHash.toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.US_ASCII))) {
                throw new SecurityException("Gateway Control body hash가 일치하지 않습니다.");
            }
            CpfGatewayControlSigner.requireFresh(timestamp, clock, Duration.ofSeconds(properties.getAllowedSkewSeconds()));
            String target = request.getRequestURI()
                    + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
            String secret = properties.secretFor(keyId);
            if (!CpfGatewayControlSigner.verify(secret, request.getMethod(), target, request.getContentType(),
                    contentHash, caller, operator, timestamp, nonce, audience, keyId, signature)) {
                throw new SecurityException("Gateway Control 서명이 올바르지 않습니다.");
            }

            java.time.Instant claimedAt = clock.instant();
            java.time.Instant expiresAt = claimedAt.plusSeconds(properties.getNonceRetentionSeconds());
            boolean claimed;
            try {
                claimed = noncePort.claim(new CpfGatewayControlNoncePort.NonceClaim(
                        audience, keyId, caller, nonce, claimedAt, expiresAt));
            } catch (RuntimeException storeFailure) {
                throw new IllegalStateException("Gateway Control nonce 원장을 사용할 수 없습니다.", storeFailure);
            }
            if (!claimed) {
                throw new SecurityException("이미 사용된 Gateway Control nonce입니다.");
            }
            appendSecurityEvent(request, "GATEWAY_CONTROL_AUTHENTICATED", "서명·본문·Audience·Nonce 검증 완료");
            request.setAttribute("gateway.control.operatorId", operator);
            request.setAttribute("gateway.control.keyId", keyId);
            chain.doFilter(replayable, response);
        } catch (PayloadTooLargeException ex) {
            rejectWithAudit(request, response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "GATEWAY_CONTROL_BODY_TOO_LARGE", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            rejectWithAudit(request, response, HttpServletResponse.SC_BAD_REQUEST, "GATEWAY_CONTROL_INVALID", ex.getMessage());
        } catch (SecurityException ex) {
            rejectWithAudit(request, response, HttpServletResponse.SC_UNAUTHORIZED, "GATEWAY_CONTROL_UNAUTHORIZED", ex.getMessage());
        } catch (IllegalStateException ex) {
            rejectWithAudit(request, response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "GATEWAY_CONTROL_UNAVAILABLE", ex.getMessage());
        }
    }


    private void appendSecurityEvent(HttpServletRequest request, String code, String message) {
        String target = request.getRequestURI()
                + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
        try {
            securityAuditPort.append(new CpfGatewayControlSecurityAuditPort.SecurityFailure(
                    UUID.randomUUID().toString(), clock.instant(),
                    request.getHeader(CpfGatewayControlHeaders.AUDIENCE),
                    request.getHeader(CpfGatewayControlHeaders.KEY_ID),
                    request.getHeader(CpfGatewayControlHeaders.CALLER_SERVICE),
                    request.getHeader(CpfGatewayControlHeaders.OPERATOR_ID),
                    request.getMethod(), target, request.getRemoteAddr(), code, message));
        } catch (RuntimeException auditFailure) {
            throw new IllegalStateException("Gateway Control 보안 감사 원장을 기록할 수 없습니다.", auditFailure);
        }
    }

    private void rejectWithAudit(HttpServletRequest request, HttpServletResponse response, int status,
            String code, String message) throws IOException {
        try {
            String target = request.getRequestURI()
                    + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
            securityAuditPort.append(new CpfGatewayControlSecurityAuditPort.SecurityFailure(
                    UUID.randomUUID().toString(), clock.instant(),
                    request.getHeader(CpfGatewayControlHeaders.AUDIENCE),
                    request.getHeader(CpfGatewayControlHeaders.KEY_ID),
                    request.getHeader(CpfGatewayControlHeaders.CALLER_SERVICE),
                    request.getHeader(CpfGatewayControlHeaders.OPERATOR_ID),
                    request.getMethod(), target, request.getRemoteAddr(), code, message));
        } catch (RuntimeException auditFailure) {
            logger.error("Gateway Control security audit append failed", auditFailure);
            reject(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "GATEWAY_CONTROL_AUDIT_UNAVAILABLE", "보안 감사 원장을 기록할 수 없습니다.");
            return;
        }
        reject(response, status, code, message);
    }

    private static byte[] readBounded(HttpServletRequest request, int maxBytes) throws IOException {
        byte[] body = request.getInputStream().readNBytes(maxBytes + 1);
        if (body.length > maxBytes) throw new PayloadTooLargeException("Gateway Control body 상한을 초과했습니다.");
        return body;
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

    private static final class ReplayableRequest extends HttpServletRequestWrapper {
        private final byte[] body;
        private ReplayableRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body.clone();
        }
        @Override public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public int read() { return input.read(); }
                @Override public int read(byte[] b, int off, int len) { return input.read(b, off, len); }
                @Override public boolean isFinished() { return input.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) {
                    if (listener == null) return;
                    try {
                        if (isFinished()) listener.onAllDataRead(); else listener.onDataAvailable();
                    } catch (IOException ex) {
                        listener.onError(ex);
                    }
                }
            };
        }
        @Override public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
        @Override public int getContentLength() { return body.length; }
        @Override public long getContentLengthLong() { return body.length; }
    }

    private static final class PayloadTooLargeException extends RuntimeException {
        private PayloadTooLargeException(String message) { super(message); }
    }
}
