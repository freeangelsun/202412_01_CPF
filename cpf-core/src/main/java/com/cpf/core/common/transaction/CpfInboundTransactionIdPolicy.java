package com.cpf.core.common.transaction;

import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.common.header.CpfHeaderNames;
import com.cpf.core.common.header.CpfTrustedProxyPolicy;
import com.cpf.core.common.idempotency.CpfIdempotencyPort;
import com.cpf.core.common.idempotency.CpfIdempotencyRecord;
import com.cpf.core.common.logging.TransactionIdGenerator;
import com.cpf.core.common.system.CpfSystemCodes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Trust-boundary policy for inbound CPF transaction ids.
 *
 * <p>An official transaction starter or a trusted propagation hop may preserve the canonical
 * transaction id. Header presence and syntax alone are never sufficient. A transport trust signal,
 * origin-system provenance and (for a new channel start) durable replay identity are required.</p>
 */
@Component
public final class CpfInboundTransactionIdPolicy {
    public static final String TRUSTED_CONTEXT_ATTRIBUTE = "cpf.trusted-transaction-context";
    public static final String AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE = "cpf.authenticated-system-code";
    private static final String START_SCOPE = "CPF_TRANSACTION_START";
    private static final Duration START_CLAIM_TTL = Duration.ofHours(24);

    private final CpfIdempotencyPort idempotencyPort;

    @Autowired
    public CpfInboundTransactionIdPolicy(ObjectProvider<CpfIdempotencyPort> provider) {
        this.idempotencyPort = provider == null ? null : provider.getIfAvailable();
    }

    /** Test/embedded constructor; an inbound channel-start id is rejected when no durable replay port exists. */
    public CpfInboundTransactionIdPolicy(CpfIdempotencyPort idempotencyPort) {
        this.idempotencyPort = idempotencyPort;
    }

    public Resolution resolve(HttpServletRequest request, TransactionIdGenerator generator) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(generator, "generator");
        String incoming = text(request.getHeader(CpfHeaderNames.TRANSACTION_ID));
        if (incoming == null) {
            return new Resolution(generator.generate(), false, false, "LOCAL_START");
        }
        if (!generator.isValid(incoming)) {
            throw rejected("CPF transactionId 형식이 올바르지 않습니다.");
        }
        if (!trustedTransport(request)) {
            throw rejected("비신뢰 호출자의 CPF transactionId 주입은 허용되지 않습니다.");
        }

        boolean start = isTransactionStart(request);
        String encodedOrigin = incoming.substring(17, 20).toUpperCase(Locale.ROOT);
        if (start) {
            String starterSystem = authoritativeStarterSystem(request);
            if (starterSystem == null) {
                throw rejected("transactionId 최초 기동 System provenance를 확인할 수 없습니다.");
            }
            if (!encodedOrigin.equals(CpfSystemCodes.normalize(starterSystem, CpfSystemCodes.CORE))) {
                throw rejected("transactionId SystemCode와 인증된 최초 기동 System이 일치하지 않습니다.");
            }
            guardStartReplay(request, incoming, encodedOrigin);
        } else if (authoritativePropagatingSystem(request) == null) {
            // A downstream hop is not required to have the same system code as the transaction
            // origin. It is required to prove the identity of the currently propagating system.
            throw rejected("transactionId propagation System provenance를 확인할 수 없습니다.");
        }
        return new Resolution(incoming, true, start, start ? "TRUSTED_CHANNEL_START" : "TRUSTED_PROPAGATION");
    }

    private void guardStartReplay(HttpServletRequest request, String transactionId, String originSystem) {
        if (idempotencyPort == null) {
            throw rejected("거래 시작 replay guard 저장소가 구성되지 않았습니다.");
        }
        String requestIdentity = first(
                request.getHeader(CpfHeaderNames.REQUEST_ID),
                request.getHeader(CpfHeaderNames.IDEMPOTENCY_KEY),
                request.getHeader(CpfHeaderNames.EXTERNAL_REQUEST_ID));
        if (requestIdentity == null) {
            throw rejected("정식 Channel transaction 시작에는 request/idempotency identity가 필요합니다.");
        }
        String channel = first(request.getHeader(CpfHeaderNames.ORIGINAL_CHANNEL_CODE),
                request.getHeader(CpfHeaderNames.CHANNEL_CODE), originSystem);
        String requestHash = sha256(originSystem + "|" + channel + "|" + requestIdentity);
        var existing = idempotencyPort.find(START_SCOPE, transactionId);
        if (existing.isPresent()) {
            if (!existing.get().sameRequest(requestHash)) {
                throw rejected("다른 거래가 기존 CPF transactionId를 replay할 수 없습니다.");
            }
            return; // the same channel request may retry while preserving the transaction id
        }
        Instant now = Instant.now();
        CpfIdempotencyRecord claim = new CpfIdempotencyRecord(
                START_SCOPE, transactionId, requestHash, sha256(channel), "COMPLETED", null,
                false, now, now, now.plus(START_CLAIM_TTL));
        if (!idempotencyPort.reserve(claim)) {
            var raced = idempotencyPort.find(START_SCOPE, transactionId);
            if (raced.isEmpty() || !raced.get().sameRequest(requestHash)) {
                throw rejected("CPF transactionId start claim 충돌이 감지되었습니다.");
            }
        }
    }

    private static boolean trustedTransport(HttpServletRequest request) {
        if (Boolean.TRUE.equals(request.getAttribute(TRUSTED_CONTEXT_ATTRIBUTE))) return true;
        if (text(request.getAttribute(AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE)) != null) return true;
        Object certs = request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (certs instanceof X509Certificate[] array && array.length > 0) return true;
        // A trusted Gateway/LB is useful only when it supplies explicit CPF ingress identity.
        return CpfTrustedProxyPolicy.isTrustedProxy(request.getRemoteAddr())
                && text(request.getHeader(CpfHeaderNames.INGRESS_TYPE)) != null
                && (text(request.getHeader(CpfHeaderNames.GATEWAY_INSTANCE_ID)) != null
                    || text(request.getHeader(CpfHeaderNames.CALLER_SERVICE)) != null
                    || text(request.getHeader(CpfHeaderNames.ORIGINAL_CHANNEL_CODE)) != null);
    }

    private static String authoritativeStarterSystem(HttpServletRequest request) {
        // Channel metadata (CHANNEL_CODE / ORIGINAL_CHANNEL_CODE) is not a system identity.
        // Prefer a server-side authenticated identity. A trusted gateway/internal-context
        // filter may also authenticate the caller service after stripping client input.
        String serverAuthenticated = text(request.getAttribute(AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE));
        if (serverAuthenticated != null) return serverAuthenticated;
        if (Boolean.TRUE.equals(request.getAttribute(TRUSTED_CONTEXT_ATTRIBUTE))) {
            return text(request.getHeader(CpfHeaderNames.CALLER_SERVICE));
        }
        return null;
    }

    private static String authoritativePropagatingSystem(HttpServletRequest request) {
        String serverAuthenticated = text(request.getAttribute(AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE));
        if (serverAuthenticated != null) return serverAuthenticated;
        if (Boolean.TRUE.equals(request.getAttribute(TRUSTED_CONTEXT_ATTRIBUTE))) {
            return text(request.getHeader(CpfHeaderNames.CALLER_SERVICE));
        }
        Object certs = request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (certs instanceof X509Certificate[] array && array.length > 0) {
            return "MTLS_AUTHENTICATED";
        }
        if (CpfTrustedProxyPolicy.isTrustedProxy(request.getRemoteAddr())) {
            return first(request.getHeader(CpfHeaderNames.CALLER_SERVICE),
                    request.getHeader(CpfHeaderNames.GATEWAY_INSTANCE_ID));
        }
        return null;
    }

    private static boolean isTransactionStart(HttpServletRequest request) {
        String ingress = text(request.getHeader(CpfHeaderNames.INGRESS_TYPE));
        if (ingress != null) {
            String normalized = ingress.toUpperCase(Locale.ROOT).replace('-', '_');
            if (normalized.equals("CHANNEL") || normalized.equals("PUBLIC") || normalized.equals("EDGE")
                    || normalized.equals("START") || normalized.equals("TRANSACTION_START")) return true;
            if (normalized.equals("INTERNAL") || normalized.equals("SERVICE") || normalized.equals("GATEWAY")
                    || normalized.equals("MESSAGE") || normalized.equals("ASYNC")) return false;
        }
        // A channel identity without a caller service is treated as an initial start, never propagation.
        return text(request.getHeader(CpfHeaderNames.CHANNEL_CODE)) != null
                && text(request.getHeader(CpfHeaderNames.CALLER_SERVICE)) == null;
    }

    private static CpfValidationException rejected(String message) {
        return new CpfValidationException("CPF transactionId trust boundary rejected: " + message);
    }

    private static String first(Object... values) {
        for (Object value : values) {
            String text = text(value);
            if (text != null) return text;
        }
        return null;
    }

    private static String text(Object value) {
        if (value == null) return null;
        String normalized = String.valueOf(value).trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public record Resolution(String transactionId, boolean propagated, boolean transactionStart, String reason) {}
}
