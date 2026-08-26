package com.cpf.integration.ai;

import com.cpf.integration.ai.api.CpfAiRequest;
import com.cpf.core.api.context.CpfContexts;
import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Tenant-scoped request/token quota and payload/output-token resource guard. */
public final class CpfAiResourceLimiter {
    private final Clock clock;
    private final int requestsPerMinute;
    private final long estimatedTokensPerMinute;
    private final int maxPayloadChars;
    private final long maxRequestedOutputTokens;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public CpfAiResourceLimiter(CpfAiProperties properties, Clock clock) {
        Objects.requireNonNull(properties, "properties");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.requestsPerMinute = properties.getRequestsPerMinute();
        this.estimatedTokensPerMinute = properties.getEstimatedTokensPerMinute();
        this.maxPayloadChars = properties.getMaxPayloadChars();
        this.maxRequestedOutputTokens = properties.getMaxRequestedOutputTokens();
    }

    public void check(CpfAiRequest request) {
        Objects.requireNonNull(request, "request");
        String payload = request.maskedPayload();
        if (payload != null && payload.length() > maxPayloadChars) {
            throw new CpfAiLimitExceededException("AI_PAYLOAD_LIMIT", "masked payload exceeds configured character limit");
        }
        long requestedOutput = positiveLong(request.attributes().get("maxOutputTokens"), 0L, "maxOutputTokens");
        if (requestedOutput > maxRequestedOutputTokens) {
            throw new CpfAiLimitExceededException("AI_OUTPUT_TOKEN_LIMIT", "requested output tokens exceed configured limit");
        }
        long estimated = positiveLong(request.attributes().get("estimatedTokens"), estimate(payload), "estimatedTokens");
        String tenant = normalizeTenant(request);
        long minute = clock.instant().getEpochSecond() / 60L;
        windows.compute(tenant, (key, previous) -> {
            Window current = previous == null || previous.minute != minute ? new Window(minute, 0, 0L) : previous;
            if (current.requests + 1 > requestsPerMinute) {
                throw new CpfAiLimitExceededException("AI_RATE_LIMIT", "requests-per-minute quota exceeded");
            }
            if (current.tokens + estimated > estimatedTokensPerMinute) {
                throw new CpfAiLimitExceededException("AI_TOKEN_QUOTA", "estimated token quota exceeded");
            }
            return new Window(minute, current.requests + 1, current.tokens + estimated);
        });
    }

    int trackedTenants() { return windows.size(); }

    private static String normalizeTenant(CpfAiRequest request) {
        String tenant = request.attributes().get("tenantId");
        if (tenant == null || tenant.isBlank()) tenant = "tx:" + CpfContexts.transactionId();
        return tenant.trim();
    }

    private static long estimate(String payload) {
        if (payload == null || payload.isEmpty()) return 1L;
        return Math.max(1L, (payload.length() + 3L) / 4L);
    }

    private static long positiveLong(String value, long defaultValue, String name) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            long parsed = Long.parseLong(value);
            if (parsed < 0) throw new NumberFormatException("negative");
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a non-negative integer", e);
        }
    }

    private record Window(long minute, int requests, long tokens) { }
}
