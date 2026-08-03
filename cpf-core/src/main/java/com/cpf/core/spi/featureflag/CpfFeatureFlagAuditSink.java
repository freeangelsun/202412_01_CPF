package com.cpf.core.spi.featureflag;

import java.time.Instant;
import java.util.Map;

/** Persistent sanitized audit sink for evaluations and configuration changes. */
public interface CpfFeatureFlagAuditSink {
    void record(String eventType, String flagKey, String actorId, String reason,
                Map<String, String> sanitizedAttributes, Instant occurredAt);
}
