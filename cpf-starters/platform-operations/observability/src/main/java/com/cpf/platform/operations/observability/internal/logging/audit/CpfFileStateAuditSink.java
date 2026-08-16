package com.cpf.platform.operations.observability.internal.logging.audit;

import com.cpf.platform.operations.api.state.CpfStateAuditEvent;
import com.cpf.platform.operations.observability.internal.logging.file.CpfFileLogWriter;
import com.cpf.platform.operations.spi.state.CpfStateAuditSink;
import java.util.Map;
import java.util.Objects;

/** Immutable structured file-audit consumer for canonical state decisions. */
public final class CpfFileStateAuditSink implements CpfStateAuditSink {
    private final CpfFileLogWriter writer;

    public CpfFileStateAuditSink(CpfFileLogWriter writer) {
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    @Override
    public void record(CpfStateAuditEvent event) {
        Objects.requireNonNull(event, "event");
        Map<String, Object> audit = writer.newBaseEvent("CPF", "audit");
        audit.put("eventType", "CPF_STATE_DECISION");
        audit.put("stateKeyHash", event.stateKeyHash());
        audit.put("operationIdHash", event.operationIdHash());
        audit.put("actor", event.actor());
        audit.put("beforeState", event.beforeState() == null ? null : event.beforeState().name());
        audit.put("requestedState", event.requestedState().name());
        audit.put("resultingState", event.resultingState() == null ? null : event.resultingState().name());
        audit.put("beforeVersion", event.beforeVersion());
        audit.put("resultingVersion", event.resultingVersion());
        audit.put("decision", event.decision());
        audit.put("reasonMasked", event.reason());
        audit.put("decidedAt", event.decidedAt().toString());
        if (!writer.writeEventWithOutcome("CPF", "audit", audit)) {
            throw new IllegalStateException("state audit write failed");
        }
    }
}
