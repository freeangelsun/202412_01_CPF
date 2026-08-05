package com.cpf.core.common.logging.audit;

import com.cpf.core.api.logging.policy.CpfLogPolicyVersionAuditEvent;
import com.cpf.core.api.logging.policy.CpfLogPolicyVersionSnapshot;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** Executable audit sink contract for hash-only fields and physical write outcomes. */
public final class CpfFileLogPolicyVersionAuditSinkHarness {
    private static final String HASH_A = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String HASH_B = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
    private static final String HASH_C = "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc";
    private static final String HASH_D = "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd";
    private static final String HASH_E = "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee";
    private static final String HASH_F = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

    private CpfFileLogPolicyVersionAuditSinkHarness() { }

    public static void main(String[] args) {
        AtomicReference<Map<String, Object>> recorded = new AtomicReference<>();
        CpfFileLogPolicyVersionAuditSink sink = new CpfFileLogPolicyVersionAuditSink(
                new CpfFileLogPolicyVersionAuditSink.EventWriter() {
                    @Override public Map<String, Object> newBaseEvent(String module, String type) {
                        require("CPF".equals(module) && "audit".equals(type), "audit route mismatch");
                        return new LinkedHashMap<>();
                    }
                    @Override public boolean writeEventWithOutcome(
                            String module, String type, Map<String, Object> event) {
                        recorded.set(Map.copyOf(event));
                        return true;
                    }
                });
        sink.record(event("credential password=raw-secret"));
        Map<String, Object> values = recorded.get();
        require(values != null, "physical event must be written");
        require("LOG_POLICY_VERSION_CHANGE".equals(values.get("eventType")), "event type mismatch");
        require(HASH_A.equals(values.get("commandIdHash")) && HASH_C.equals(values.get("targetHash")),
                "hash identifiers must be preserved");
        require(!values.toString().contains("raw-secret"), "raw reason secret must not be persisted");
        require(values.containsKey("approverHash") && values.containsKey("occurredAt"),
                "approval and timestamp evidence are required");

        CpfFileLogPolicyVersionAuditSink failing = new CpfFileLogPolicyVersionAuditSink(
                new CpfFileLogPolicyVersionAuditSink.EventWriter() {
                    @Override public Map<String, Object> newBaseEvent(String module, String type) {
                        return new LinkedHashMap<>();
                    }
                    @Override public boolean writeEventWithOutcome(
                            String module, String type, Map<String, Object> event) { return false; }
                });
        expectFailure(() -> failing.record(event("write failure")),
                "false physical write outcome must fail closed");
        CpfFileLogPolicyVersionAuditSink noBase = new CpfFileLogPolicyVersionAuditSink(
                new CpfFileLogPolicyVersionAuditSink.EventWriter() {
                    @Override public Map<String, Object> newBaseEvent(String module, String type) { return null; }
                    @Override public boolean writeEventWithOutcome(
                            String module, String type, Map<String, Object> event) { return true; }
                });
        expectFailure(() -> noBase.record(event("base failure")),
                "missing base event must fail closed");
        System.out.println("CPF_LOG_POLICY_VERSION_AUDIT_SINK_HARNESS_PASS");
    }

    private static CpfLogPolicyVersionAuditEvent event(String reason) {
        return new CpfLogPolicyVersionAuditEvent(CpfLogPolicyVersionAuditEvent.Phase.APPLIED,
                HASH_A, HASH_B, HASH_C, HASH_D, HASH_E, 1L, 2L,
                CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                reason, "APPLIED", Instant.parse("2026-08-05T00:00:00Z"));
    }

    private static void expectFailure(Runnable action, String message) {
        try { action.run(); } catch (IllegalStateException expected) { return; }
        throw new AssertionError(message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
