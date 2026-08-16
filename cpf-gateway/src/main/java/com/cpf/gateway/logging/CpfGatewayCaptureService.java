package com.cpf.gateway.logging;

import com.cpf.gateway.api.CpfGatewayLedgerPort;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogCaptureGuard;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyResolver;
import com.cpf.platform.operations.observability.api.logging.policy.CpfPayloadProtectionPort;
import com.cpf.platform.operations.observability.api.logging.policy.LogCaptureMode;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.gateway.config.CpfGatewaySafetyEnforcer;
import com.cpf.gateway.scg.CpfGatewayLedgerRecoverySpool;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/** SCG MVC 요청/응답 Capture 정책을 저장 직전에 강제하는 Runtime Consumer입니다. */
@Component
public final class CpfGatewayCaptureService {
    private static final int UTF8_LOOKAHEAD_BYTES = 4;

    private final ObjectProvider<CpfLogPolicyResolver> resolverProvider;
    private final ObjectProvider<CpfPayloadProtectionPort> protectionProvider;
    private final CpfGatewayLedgerRecoverySpool ledgerRecovery;
    private final CpfGatewaySafetyEnforcer safety;

    public CpfGatewayCaptureService(
            ObjectProvider<CpfLogPolicyResolver> resolverProvider,
            ObjectProvider<CpfPayloadProtectionPort> protectionProvider,
            CpfGatewayLedgerRecoverySpool ledgerRecovery,
            CpfGatewaySafetyEnforcer safety) {
        this.resolverProvider = resolverProvider;
        this.protectionProvider = protectionProvider;
        this.ledgerRecovery = ledgerRecovery;
        this.safety = safety;
    }

    public LogPolicyDecision resolve(String routeOrTransactionId) {
        CpfLogPolicyResolver resolver = resolverProvider.getIfAvailable();
        LogPolicyDecision decision = resolver == null
                ? LogPolicyDecision.cpfDefault(
                        com.cpf.platform.operations.observability.api.logging.policy.LogPolicyTargetType.ONLINE_TRANSACTION,
                        routeOrTransactionId)
                : resolver.resolveOnlineTransaction(routeOrTransactionId);
        safety.validateLogPolicy(decision);
        return decision;
    }

    public void captureRequestMetadata(
            String gatewayTransactionId,
            String rawQuery,
            HttpHeaders headers,
            LogPolicyDecision policy) {
        safety.validateLogPolicy(policy);
        record(gatewayTransactionId, "QUERY", policy,
                CpfLogCaptureGuard.query(rawQuery, policy), utf8Length(rawQuery));
        record(gatewayTransactionId, "REQUEST_HEADERS", policy,
                CpfLogCaptureGuard.headers(headerMap(headers), false, policy), headerBytes(headers));
    }

    public void captureRequestBody(
            String gatewayTransactionId,
            byte[] preview,
            long observedBytes,
            String sha256,
            boolean truncated,
            LogPolicyDecision policy) {
        captureBody(gatewayTransactionId, "REQUEST_BODY", preview, observedBytes, sha256,
                truncated, false, policy);
    }

    public void captureResponseHeaders(
            String gatewayTransactionId,
            HttpHeaders headers,
            LogPolicyDecision policy) {
        safety.validateLogPolicy(policy);
        safety.validateResponse(headers);
        record(gatewayTransactionId, "RESPONSE_HEADERS", policy,
                CpfLogCaptureGuard.headers(headerMap(headers), true, policy), headerBytes(headers));
    }

    public void captureResponseBody(
            String gatewayTransactionId,
            byte[] preview,
            long observedBytes,
            String sha256,
            boolean truncated,
            LogPolicyDecision policy) {
        captureBody(gatewayTransactionId, "RESPONSE_BODY", preview, observedBytes, sha256,
                truncated, true, policy);
    }

    public void captureError(
            String gatewayTransactionId,
            Throwable error,
            LogPolicyDecision policy) {
        StringBuilder stack = new StringBuilder(error.toString());
        for (StackTraceElement element : error.getStackTrace()) {
            stack.append('\n').append("at ").append(element);
        }
        record(gatewayTransactionId, "ERROR_STACK", policy,
                CpfLogCaptureGuard.stack(stack.toString(), policy), utf8Length(stack.toString()));
    }

    public int requestCaptureLimit(LogPolicyDecision policy) {
        return policy.requestBodyCaptureMode() == LogCaptureMode.NONE
                ? 0
                : previewLimit(policy.maxRequestBodyBytes());
    }

    public int responseCaptureLimit(LogPolicyDecision policy) {
        return policy.responseBodyCaptureMode() == LogCaptureMode.NONE
                ? 0
                : previewLimit(policy.maxResponseBodyBytes());
    }

    private void captureBody(
            String transactionId,
            String segment,
            byte[] preview,
            long observedBytes,
            String bodySha256,
            boolean truncated,
            boolean response,
            LogPolicyDecision policy) {
        safety.validateLogPolicy(policy);
        LogCaptureMode mode = response
                ? policy.responseBodyCaptureMode()
                : policy.requestBodyCaptureMode();
        if (mode == LogCaptureMode.NONE || observedBytes <= 0L) return;

        CpfLogCaptureGuard.CapturedValue captured;
        if (mode == LogCaptureMode.METADATA_ONLY) {
            String hash = bodySha256 == null || bodySha256.isBlank()
                    ? sha256(preview == null ? new byte[0] : preview)
                    : bodySha256;
            captured = new CpfLogCaptureGuard.CapturedValue(
                    "bytes=" + observedBytes + ",sha256=" + hash, false, true);
        } else {
            byte[] safePreview = preview == null ? new byte[0] : preview;
            String text = new String(safePreview, StandardCharsets.UTF_8);
            captured = CpfLogCaptureGuard.body(
                    text, response, policy, protectionProvider.getIfAvailable());
        }
        if ((truncated || observedBytes > (preview == null ? 0L : preview.length))
                && !captured.truncated()) {
            captured = new CpfLogCaptureGuard.CapturedValue(
                    captured.value(), true, captured.metadataOnly());
        }
        record(transactionId, segment, policy, captured, observedBytes);
    }

    private void record(
            String transactionId,
            String segment,
            LogPolicyDecision policy,
            CpfLogCaptureGuard.CapturedValue value,
            long observedBytes) {
        if (value == null || value.value() == null || value.value().isEmpty()) return;
        ledgerRecovery.recordCapture(new CpfGatewayLedgerPort.CaptureSegment(
                transactionId,
                segment,
                policy.schemaVersion(),
                policy.policyChecksum(),
                value.value(),
                value.truncated(),
                value.metadataOnly(),
                Math.max(0L, observedBytes),
                OffsetDateTime.now()));
    }

    private static int previewLimit(int configured) {
        return (int) Math.min(Integer.MAX_VALUE, (long) Math.max(0, configured) + UTF8_LOOKAHEAD_BYTES);
    }

    private static long headerBytes(HttpHeaders headers) {
        if (headers == null) return 0L;
        return headers.headerSet().stream().mapToLong(entry ->
                utf8Length(entry.getKey())
                        + entry.getValue().stream().mapToLong(CpfGatewayCaptureService::utf8Length).sum())
                .sum();
    }

    private static Map<String, List<String>> headerMap(HttpHeaders headers) {
        LinkedHashMap<String, List<String>> values = new LinkedHashMap<>();
        if (headers != null) {
            headers.forEach((name, entries) -> values.put(name, List.copyOf(entries)));
        }
        return Map.copyOf(values);
    }

    private static long utf8Length(String value) {
        return value == null ? 0L : value.getBytes(StandardCharsets.UTF_8).length;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }
}
