package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.http.CpfWebhookSignaturePort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.http.CpfWebhookRuntimePolicy;
import com.cpf.core.common.runtimecontrol.CpfRuntimePayloadJson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Webhook endpoint/signature/retry/idempotency 정책을 실제 Runtime snapshot에 적용합니다. */
public final class CpfWebhookCallbackRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfWebhookRuntimePolicy policy;
    private final CpfWebhookSignaturePort signaturePort;

    public CpfWebhookCallbackRuntimeApplier(
            CpfWebhookRuntimePolicy policy,
            CpfWebhookSignaturePort signaturePort) {
        this.policy = policy;
        this.signaturePort = signaturePort;
    }

    @Override
    public String changeType() {
        return "WEBHOOK_CALLBACK";
    }

    @Override
    public boolean supportsIdempotentReplay() {
        return true;
    }

    @Override
    public boolean snapshotCapable() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            Object raw = CpfRuntimePayloadJson.value(delivery.payload(), "callbacks");
            if (!(raw instanceof List<?> entries)) {
                throw new IllegalArgumentException("callbacks array 필수");
            }
            LinkedHashMap<String, CpfWebhookRuntimePolicy.Callback> callbacks = new LinkedHashMap<>();
            for (Object entry : entries) {
                if (!(entry instanceof Map<?, ?> source)) {
                    throw new IllegalArgumentException("callback object 필요");
                }
                Map<String, Object> value = (Map<String, Object>) source;
                String callbackId = required(value, "callbackId");
                String signatureRef = optional(value, "signatureRef", "");
                if (!signatureRef.isBlank() && signaturePort == null) {
                    throw new IllegalStateException("signature port missing");
                }
                callbacks.put(
                        callbackId,
                        new CpfWebhookRuntimePolicy.Callback(
                                callbackId,
                                required(value, "serviceId"),
                                required(value, "path"),
                                signatureRef,
                                optional(value, "idempotencyHeader", "Idempotency-Key"),
                                (int) number(value.get("timeoutMillis"), 3000L),
                                (int) number(value.get("retryCount"), 0L),
                                bool(value, "active", true)));
            }
            policy.replace(delivery.desiredVersion(), callbacks);
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "WEBHOOK_CALLBACK_INVALID",
                    "Webhook endpoint/signature/retry/idempotency 정책 오류");
        }
    }

    private String required(Map<String, Object> source, String key) {
        String value = optional(source, key, "");
        if (value.isBlank()) throw new IllegalArgumentException(key + " 필수");
        return value;
    }

    private String optional(Map<String, Object> source, String key, String fallback) {
        Object value = source.get(key);
        return value == null ? fallback : String.valueOf(value).trim();
    }

    private long number(Object value, long fallback) {
        if (value instanceof Number number) return number.longValue();
        return value == null ? fallback : Long.parseLong(String.valueOf(value));
    }

    private boolean bool(Map<String, Object> source, String key, boolean fallback) {
        Object value = source.get(key);
        if (value instanceof Boolean bool) return bool;
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }
}
