package com.cpf.platform.operations.observability.internal.logging.audit;

import com.cpf.security.api.CpfSensitiveDataAccessOperations.AccessGrant;
import com.cpf.security.api.CpfSensitiveDataAccessOperations.AccessStatus;
import com.cpf.security.api.CpfMaskingRuntime;
import com.cpf.platform.operations.observability.internal.logging.file.CpfFileLogWriter;
import com.cpf.security.spi.CpfSensitiveDataAccessAuditSink;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** 민감정보 원문 조회 상태 전이를 전용 append-only 파일 감사 로그로 기록합니다. */
public final class CpfFileSensitiveDataAccessAuditSink implements CpfSensitiveDataAccessAuditSink {
    private final CpfFileLogWriter writer;

    public CpfFileSensitiveDataAccessAuditSink(CpfFileLogWriter writer) {
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    @Override
    public void record(
            String action,
            AccessStatus result,
            AccessGrant grant,
            String actorId,
            Instant occurredAt,
            String errorCode) {
        Objects.requireNonNull(grant, "grant");
        Map<String, Object> event = writer.newBaseEvent("CPF", "audit");
        event.put("eventType", "SENSITIVE_DATA_RAW_ACCESS");
        event.put("action", action);
        event.put("result", result.name());
        event.put("requestId", grant.requestId());
        event.put("requestVersion", grant.version());
        event.put("resourceType", grant.resourceType());
        event.put("resourceIdHash", grant.resourceIdHash());
        event.put("dataScope", CpfMaskingRuntime.mask(grant.dataScope(), 256));
        event.put("actorId", actorId);
        event.put("requesterId", grant.requesterId());
        event.put("approverId", grant.approverId());
        event.put("occurredAt", occurredAt.toString());
        if (errorCode != null) {
            event.put("errorCode", errorCode);
        }
        // 원문 데이터와 승인 사유는 감사 로그에 저장하지 않습니다.
        if (!writer.writeEventWithOutcome("CPF", "audit", event)) {
            throw new IllegalStateException("sensitive data access audit write failed");
        }
    }
}
