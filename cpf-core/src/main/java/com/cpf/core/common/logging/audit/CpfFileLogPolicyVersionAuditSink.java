package com.cpf.core.common.logging.audit;

import com.cpf.core.api.logging.policy.CpfLogPolicyVersionAuditEvent;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.cpf.core.spi.logging.CpfLogPolicyVersionAuditSink;
import java.util.Map;
import java.util.Objects;

/** Append-only audit consumer that never persists raw actor or target identifiers. */
public final class CpfFileLogPolicyVersionAuditSink implements CpfLogPolicyVersionAuditSink {
    private final EventWriter writer;

    public CpfFileLogPolicyVersionAuditSink(CpfFileLogWriter writer) {
        Objects.requireNonNull(writer, "writer");
        this.writer = new EventWriter() {
            @Override public Map<String, Object> newBaseEvent(String module, String type) {
                return writer.newBaseEvent(module, type);
            }
            @Override public boolean writeEventWithOutcome(
                    String module, String type, Map<String, Object> event) {
                return writer.writeEventWithOutcome(module, type, event);
            }
        };
    }

    CpfFileLogPolicyVersionAuditSink(EventWriter writer) {
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    @Override public void record(CpfLogPolicyVersionAuditEvent source) {
        Objects.requireNonNull(source, "source");
        Map<String, Object> event = writer.newBaseEvent("CPF", "audit");
        if (event == null) throw new IllegalStateException("log policy version audit base event unavailable");
        event.put("eventType", "LOG_POLICY_VERSION_CHANGE");
        event.put("phase", source.phase().name());
        event.put("commandIdHash", source.commandIdHash());
        event.put("commandHash", source.commandHash());
        event.put("targetHash", source.targetHash());
        event.put("actorHash", source.actorHash());
        event.put("approverHash", source.approverHash());
        event.put("beforeVersion", source.beforeVersion());
        event.put("afterVersion", source.afterVersion());
        event.put("beforeStatus", source.beforeStatus().name());
        event.put("afterStatus", source.afterStatus().name());
        event.put("reasonMasked", source.reason());
        event.put("result", source.result());
        event.put("occurredAt", source.occurredAt().toString());
        if (!writer.writeEventWithOutcome("CPF", "audit", event)) {
            throw new IllegalStateException("log policy version audit write failed");
        }
    }

    interface EventWriter {
        Map<String, Object> newBaseEvent(String module, String type);
        boolean writeEventWithOutcome(String module, String type, Map<String, Object> event);
    }
}
