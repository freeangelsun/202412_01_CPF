package com.cpf.core.common.logging;

import com.cpf.core.api.observability.CpfTraceContext;
import java.util.Map;

public final class CpfTraceContextHarness {
    public static void main(String[] args) {
        CpfTraceContext root = CpfTraceContext.root(
                "TX-20260805-0001", CpfTraceContext.SpanKind.LOCAL,
                "GET /members/550e8400-e29b-41d4-a716-446655440000/orders/123456",
                Map.of("cpf.module", "member", "cpf.execution", "O-MBR-0001"));
        check(root.spanName().equals("local.get_/members/{id}/orders/{id}"), "low-cardinality span name");
        CpfTraceContext remote = root.child(CpfTraceContext.SpanKind.REMOTE,
                "POST /payments/987654", "PAYMENT", 2, Map.of("cpf.channel", "MOBILE"));
        check(remote.traceId().equals(root.traceId()), "trace correlation");
        check(remote.parentSpanId().equals(root.spanId()), "parent correlation");
        check(remote.attributes().get("cpf.attempt").equals("2"), "attempt correlation");
        for (CpfTraceContext.SpanKind kind : CpfTraceContext.SpanKind.values()) {
            check(root.child(kind, "operation", kind.name(), 1, Map.of()).spanName()
                    .startsWith(kind.name().toLowerCase() + "."), "span kind naming " + kind);
        }
        expectFailure(() -> CpfTraceContext.root("TX", CpfTraceContext.SpanKind.LOCAL,
                "op", Map.of("email", "user@example.com")), "baggage key allowlist");
        expectFailure(() -> CpfTraceContext.root("TX", CpfTraceContext.SpanKind.LOCAL,
                "op", Map.of("cpf.correlation", "user@example.com")), "PII exclusion");
        CpfTraceContext sensitiveCorrelation = CpfTraceContext.root(
                "user@example.com", CpfTraceContext.SpanKind.REMOTE, "lookup", Map.of());
        check(!sensitiveCorrelation.attributes().toString().contains("user@example.com"),
                "sensitive correlation identifier is hashed");
        check(sensitiveCorrelation.attributes().get("cpf.transaction_id").startsWith("sha256:"),
                "sensitive correlation hash marker");
        expectFailure(() -> new CpfTraceContext("00000000000000000000000000000000",
                "1111111111111111", null, "TX", null, 0,
                CpfTraceContext.SpanKind.LOCAL, "op", Map.of(), 1), "zero trace id");

        TransactionLogRecord record = new TransactionLogRecord();
        record.setTransactionId("TX-CORRELATION-01");
        record.setModuleId("core");
        record.setStandardExecutionId("O-CORE-0001");
        record.setUri("/api/orders/123456");
        record.setHttpMethod("GET");
        record.setRequestType("HTTP_REMOTE");
        record.setSequenceNo(3);
        CpfTraceContext enriched = CpfTransactionTraceEnricher.enrich(record);
        check(record.getTraceId().equals(enriched.traceId()), "record trace enriched");
        check(record.getSpanId().equals(enriched.spanId()), "record span enriched");
        check(enriched.kind() == CpfTraceContext.SpanKind.REMOTE, "remote consumer kind");
        CpfTraceContext replay = CpfTransactionTraceEnricher.enrich(record);
        check(replay.equals(enriched), "idempotent enrichment");
        System.out.println("CPF_TRACE_CONTEXT_HARNESS_PASS");
    }

    private static void expectFailure(Runnable action, String label) {
        try { action.run(); throw new AssertionError(label + " must fail"); }
        catch (IllegalArgumentException expected) { }
    }
    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }
}
