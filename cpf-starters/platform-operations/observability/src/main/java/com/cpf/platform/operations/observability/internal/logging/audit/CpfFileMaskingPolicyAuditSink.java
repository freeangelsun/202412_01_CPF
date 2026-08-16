package com.cpf.platform.operations.observability.internal.logging.audit;

import com.cpf.security.api.CpfMaskingPolicyAuditEvent;
import com.cpf.platform.operations.observability.internal.logging.file.CpfFileLogWriter;
import com.cpf.security.spi.CpfMaskingPolicyAuditSink;

import java.util.Map;
import java.util.Objects;

/** Append-only file audit consumer for masking policy changes. */
public final class CpfFileMaskingPolicyAuditSink implements CpfMaskingPolicyAuditSink {
    private final CpfFileLogWriter writer;

    public CpfFileMaskingPolicyAuditSink(CpfFileLogWriter writer) {
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    @Override public void record(CpfMaskingPolicyAuditEvent source) {
        Map<String, Object> event = writer.newBaseEvent("CPF", "audit");
        event.put("eventType", "MASKING_POLICY_CHANGE");
        event.put("phase", source.phase().name());
        event.put("commandIdHash", source.commandIdHash());
        event.put("commandHash", source.commandHash());
        event.put("actorHash", source.actorHash());
        event.put("approverHash", source.approverHash());
        event.put("beforeVersion", source.beforeVersion());
        event.put("afterVersion", source.afterVersion());
        event.put("reasonMasked", source.reason());
        event.put("result", source.result());
        event.put("occurredAt", source.occurredAt().toString());
        if (!writer.writeEventWithOutcome("CPF", "audit", event)) {
            throw new IllegalStateException("masking policy audit write failed");
        }
    }
}
