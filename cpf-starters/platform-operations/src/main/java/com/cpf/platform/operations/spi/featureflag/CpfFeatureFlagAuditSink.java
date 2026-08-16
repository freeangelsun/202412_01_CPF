package com.cpf.platform.operations.spi.featureflag;

import java.time.Instant;
import java.util.Map;

/** Persistent sanitized audit sink for evaluations and configuration changes. */
/** CpfFeatureFlagAuditSink 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfFeatureFlagAuditSink {
    void record(String eventType, String flagKey, String actorId, String reason,
                Map<String, String> sanitizedAttributes, Instant occurredAt);
}
