package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.api.resilience.CpfResiliencePolicy;
import com.cpf.core.spi.resilience.CpfResilienceAuditSink;
import com.cpf.core.spi.resilience.CpfResiliencePolicyStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Ensures policy changes use bounded actors, sanitized reasons, SoD and atomic audit boundaries. */
public final class CpfResiliencePolicySecurityHarness {
    private CpfResiliencePolicySecurityHarness() { }

    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        RecordingStore store = new RecordingStore();
        RecordingAudit audit = new RecordingAudit();
        CpfResiliencePolicyCommandService service =
                new CpfResiliencePolicyCommandService(store, audit, clock);
        CpfResiliencePolicy policy = policy("payment.post");

        String requestId = service.requestChange(
                policy,
                "requester@example.com",
                "incident email=user@example.com token=raw-secret");
        require("request-1".equals(requestId), "request ID");
        require(!store.reason.contains("raw-secret") && !store.reason.contains("user@example.com"),
                "store reason is sanitized");
        require(store.reason.contains("[REDACTED]") && store.reason.contains("[PII_REDACTED]"),
                "sanitization markers retained");
        require(audit.reasons.equals(List.of(store.reason)), "audit uses the exact sanitized reason");
        expectFailure(() -> service.approveChange(requestId, "requester@example.com", "self approval"),
                "separation of duties");

        CpfResiliencePolicy approved = service.approveChange(
                requestId, "approver@example.com", "approved password=raw-password");
        require(approved.operationId().equals(policy.operationId()), "approved policy");
        require(!store.reason.contains("raw-password") && !audit.reasons.getLast().contains("raw-password"),
                "approval secret removed");

        service.rejectChange("request-2", "approver@example.com", "reject Bearer abc.def.ghi");
        require(!store.reason.contains("abc.def.ghi") && !audit.reasons.getLast().contains("abc.def.ghi"),
                "rejection bearer token removed");

        expectFailure(() -> service.requestChange(policy, "bad actor", "reason"),
                "actor allowlist");
        expectFailure(() -> service.requestChange(policy, "actor\nadmin", "reason"),
                "actor control character");
        expectFailure(() -> service.approveChange("x".repeat(129), "approver", "reason"),
                "request id bound");
        expectFailure(() -> service.search("", Integer.MAX_VALUE, 500),
                "page multiplication overflow");
        expectFailure(() -> policy("x".repeat(257)), "operation id bound");
        expectFailure(() -> policy("operation\nsecret"), "operation id control character");

        require(store.selfApprovalBlocked, "store enforces separation of duties");
        System.out.println("CPF_RESILIENCE_POLICY_SECURITY_HARNESS_PASS");
    }

    private static CpfResiliencePolicy policy(String operationId) {
        return new CpfResiliencePolicy(
                operationId, 0L, Duration.ofSeconds(2), 2, Duration.ofMillis(10),
                3, Duration.ofSeconds(5), 10, 100, Duration.ofMinutes(1), true, true);
    }

    private static final class RecordingStore implements CpfResiliencePolicyStore {
        private String reason;
        private String requester;
        private boolean selfApprovalBlocked = true;

        @Override public Optional<CpfResiliencePolicy> findActive(String operationId) {
            return Optional.empty();
        }
        @Override public List<CpfResiliencePolicy> search(String filter, int offset, int limit) {
            return List.of();
        }
        @Override public String request(CpfResiliencePolicy policy, String requesterId, String reason) {
            this.requester = requesterId;
            this.reason = reason;
            return "request-1";
        }
        @Override public CpfResiliencePolicy approve(String requestId, String approverId, String reason) {
            if (approverId.equals(requester)) {
                selfApprovalBlocked = true;
                throw new IllegalArgumentException("self approval is forbidden");
            }
            selfApprovalBlocked = true;
            this.reason = reason;
            return policy("payment.post");
        }
        @Override public void reject(String requestId, String approverId, String reason) {
            this.reason = reason;
        }
    }

    private static final class RecordingAudit implements CpfResilienceAuditSink {
        private final List<String> reasons = new ArrayList<>();
        @Override public void record(
                String eventType, String operationId, String actorId, String reason,
                Map<String, String> attributes, Instant occurredAt) {
            require(!actorId.isBlank() && !reason.isBlank(), "audit actor/reason");
            reasons.add(reason);
        }
    }

    private static void expectFailure(Runnable action, String label) {
        boolean failed = false;
        try { action.run(); }
        catch (IllegalArgumentException | ArithmeticException expected) { failed = true; }
        require(failed, label);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
