
package com.cpf.common.data.quality;

import com.cpf.core.api.data.quality.CpfDataQualityDecision;
import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.api.data.quality.CpfDataQualityRule;
import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * Deterministic local/development reference implementation for rule lifecycle, quarantine,
 * approved correction, replay and reconcile. This implementation is intentionally process-local:
 * it is not production-safe evidence for multi-instance CAS, process-kill idempotency or durable
 * reconcile. Production/staging consumers must provide a persistent CpfDataQualityOperations /
 * CpfDataQualityCorrectionPort implementation; ADM auto-configuration never exposes this bean
 * outside local/dev profiles.
 */
public final class InMemoryCpfDataQualityOperations implements CpfDataQualityOperations, CpfDataQualityCorrectionPort {
    public record Audit(
            Instant at,
            String actor,
            String action,
            String target,
            String reason,
            String approvalReference,
            long version) {
    }

    private final Map<String, CpfDataQualityRule> rules = new ConcurrentHashMap<>();
    private final Map<String, QuarantineItem> quarantine = new ConcurrentHashMap<>();
    private final List<Audit> audit = new CopyOnWriteArrayList<>();
    private final Map<String, CpfDataQualityDecision> replayResults = new ConcurrentHashMap<>();
    private final Map<String, String> replayFingerprints = new ConcurrentHashMap<>();
    private final Map<String, Object> replayLocks = new ConcurrentHashMap<>();
    private final Predicate<ApprovedCorrection> approvalProofVerifier;

    /** Fail-closed compatibility constructor; approved correction is unavailable without a verifier. */
    @Deprecated(forRemoval = true)
    public InMemoryCpfDataQualityOperations() { this(command -> false); }

    public InMemoryCpfDataQualityOperations(Predicate<ApprovedCorrection> approvalProofVerifier) {
        this.approvalProofVerifier = java.util.Objects.requireNonNull(approvalProofVerifier, "approvalProofVerifier");
    }

    @Override
    public CpfDataQualityRule register(CpfDataQualityRule rule, String actor, String reason) {
        require(actor, "actor");
        require(reason, "reason");
        rules.compute(rule.ruleId(), (id, old) -> {
            if (old != null && rule.version() <= old.version()) {
                throw new IllegalStateException("rule version must increase");
            }
            return rule;
        });
        audit.add(new Audit(Instant.now(), actor, "RULE_REGISTER", rule.ruleId(), reason, "", rule.version()));
        return rule;
    }

    @Override
    public CpfDataQualityDecision validate(String recordId, Map<String, Object> record) {
        List<CpfDataQualityDecision.Violation> violations = new ArrayList<>();
        for (CpfDataQualityRule rule : rules.values()) {
            if (rule.state() == CpfDataQualityRule.State.ACTIVE && !matches(rule, record.get(rule.fieldName()))) {
                violations.add(new CpfDataQualityDecision.Violation(
                        rule.ruleId(),
                        rule.severity(),
                        rule.fieldName(),
                        "Rule failed: " + rule.expression()));
            }
        }
        boolean accepted = violations.stream().noneMatch(violation ->
                violation.severity() == CpfDataQualityRule.Severity.ERROR
                        || violation.severity() == CpfDataQualityRule.Severity.CRITICAL);
        String quarantineId = accepted ? "" : "DQ-" + UUID.randomUUID();
        CpfDataQualityDecision decision =
                new CpfDataQualityDecision(recordId, accepted, violations, quarantineId, Instant.now());
        if (!accepted) {
            quarantine.put(quarantineId, new QuarantineItem(
                    quarantineId,
                    recordId,
                    immutableNullable(record),
                    Map.of(),
                    "QUARANTINED",
                    1,
                    List.copyOf(violations)));
        }
        return decision;
    }

    @Override
    public Optional<QuarantineItem> quarantine(String id) {
        return Optional.ofNullable(quarantine.get(id));
    }

    @Override
    public QuarantineItem correctApproved(ApprovedCorrection command) {
        if (command == null) throw new SecurityException("approved owner command is required");
        String actor = require(command.actorId(), "actor");
        String reason = require(command.reason(), "reason");
        if (command.approvalExecutionReference() == null || command.approvalExecutionReference().isBlank()
                || command.approvedAt() == null || !approvalProofVerifier.test(command)) {
            throw new SecurityException("server approval execution proof is invalid");
        }
        return quarantine.compute(command.quarantineId(), (key, old) -> {
            if (old == null) throw new NoSuchElementException(command.quarantineId());
            if (!"QUARANTINED".equals(old.state())) {
                throw new IllegalStateException("only QUARANTINED data can be corrected");
            }
            if (old.version() != command.expectedVersion()) {
                throw new ConcurrentModificationException("quarantine version conflict");
            }
            QuarantineItem next = new QuarantineItem(
                    command.quarantineId(), old.recordId(), old.original(),
                    immutableNullable(command.corrected()), "CORRECTED", old.version() + 1, old.violations());
            audit.add(new Audit(Instant.now(), actor, "CORRECT", command.quarantineId(), reason,
                    command.approvalExecutionReference(), next.version()));
            return next;
        });
    }

    @Override
    public CpfDataQualityDecision replay(ReplayCommand command) {
        require(command.actorId(), "actor");
        require(command.reason(), "reason");
        String fingerprint = replayFingerprint(command);
        CpfDataQualityDecision previous = replayResults.get(command.operationId());
        if (previous != null) {
            requireSameReplay(command.operationId(), fingerprint);
            return previous;
        }
        Object lock = replayLocks.computeIfAbsent(command.operationId(), ignored -> new Object());
        try {
            synchronized (lock) {
                previous = replayResults.get(command.operationId());
                if (previous != null) {
                    requireSameReplay(command.operationId(), fingerprint);
                    return previous;
                }
                final CpfDataQualityDecision[] result = new CpfDataQualityDecision[1];
                quarantine.compute(command.quarantineId(), (id, item) -> {
                    if (item == null) throw new NoSuchElementException(id);
                    if (item.version() != command.expectedVersion())
                        throw new ConcurrentModificationException("quarantine replay version conflict");
                    if (!Set.of("QUARANTINED", "CORRECTED").contains(item.state()))
                        throw new IllegalStateException("only QUARANTINED/CORRECTED data can be replayed");
                    Map<String, Object> candidate = item.corrected().isEmpty() ? item.original() : item.corrected();
                    CpfDataQualityDecision decision = validateOnly(item.recordId(), candidate);
                    result[0] = decision;
                    if (!decision.accepted()) {
                        audit.add(new Audit(Instant.now(), command.actorId(), "REPLAY_REJECTED", id,
                                command.reason(), command.operationId(), item.version()));
                        return item;
                    }
                    QuarantineItem next = new QuarantineItem(id, item.recordId(), item.original(), item.corrected(),
                            "REPLAYED", item.version() + 1, item.violations());
                    audit.add(new Audit(Instant.now(), command.actorId(), "REPLAY", id,
                            command.reason(), command.operationId(), next.version()));
                    return next;
                });
                replayFingerprints.put(command.operationId(), fingerprint);
                replayResults.put(command.operationId(), result[0]);
                return result[0];
            }
        } finally {
            replayLocks.remove(command.operationId(), lock);
        }
    }

    @Override
    public ReconcileResult reconcile(String actor, String reason) {
        require(actor, "actor");
        require(reason, "reason");
        int inspected = 0;
        int replayed = 0;
        for (QuarantineItem item : new ArrayList<>(quarantine.values())) {
            if ("CORRECTED".equals(item.state())) {
                inspected++;
                ReplayCommand command = new ReplayCommand(item.quarantineId(), item.version(),
                        "reconcile-" + item.quarantineId() + "-v" + item.version(), actor, reason);
                if (replay(command).accepted()) replayed++;
            }
        }
        long remaining = quarantine.values().stream()
                .filter(item -> !"REPLAYED".equals(item.state()))
                .count();
        return new ReconcileResult(inspected, replayed, (int) remaining);
    }

    public List<Audit> audit() {
        return List.copyOf(audit);
    }

    private CpfDataQualityDecision validateOnly(String recordId, Map<String, Object> record) {
        List<CpfDataQualityDecision.Violation> violations = new ArrayList<>();
        for (CpfDataQualityRule rule : rules.values()) {
            if (rule.state() == CpfDataQualityRule.State.ACTIVE && !matches(rule, record.get(rule.fieldName()))) {
                violations.add(new CpfDataQualityDecision.Violation(rule.ruleId(), rule.severity(),
                        rule.fieldName(), "Rule failed: " + rule.expression()));
            }
        }
        boolean accepted = violations.stream().noneMatch(v -> v.severity() == CpfDataQualityRule.Severity.ERROR
                || v.severity() == CpfDataQualityRule.Severity.CRITICAL);
        return new CpfDataQualityDecision(recordId, accepted, List.copyOf(violations), "", Instant.now());
    }

    private void requireSameReplay(String operationId, String fingerprint) {
        String previous = replayFingerprints.get(operationId);
        if (previous == null || !previous.equals(fingerprint)) {
            throw new IllegalStateException("operationId is already bound to a different replay command");
        }
    }

    private static String replayFingerprint(ReplayCommand command) {
        return command.quarantineId() + "\n" + command.expectedVersion() + "\n"
                + command.actorId().trim() + "\n" + command.reason().trim();
    }

    private static Map<String, Object> immutableNullable(Map<String, Object> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source == null ? Map.of() : source));
    }

    private static boolean matches(CpfDataQualityRule rule, Object value) {
        String expression = rule.expression().trim();
        String text = value == null ? "" : String.valueOf(value);
        if ("NOT_BLANK".equals(expression)) return !text.isBlank();
        if (expression.startsWith("REGEX:")) return Pattern.compile(expression.substring(6)).matcher(text).matches();
        if (expression.startsWith("MIN_LENGTH:")) return text.length() >= Integer.parseInt(expression.substring(11));
        if (expression.startsWith("MAX_LENGTH:")) return text.length() <= Integer.parseInt(expression.substring(11));
        throw new IllegalArgumentException("Unsupported rule expression: " + expression);
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " required");
        return value.trim();
    }
}
