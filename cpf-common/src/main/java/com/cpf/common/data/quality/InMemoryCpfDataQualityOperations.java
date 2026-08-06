
package com.cpf.common.data.quality;

import com.cpf.core.api.data.quality.CpfDataQualityDecision;
import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.api.data.quality.CpfDataQualityRule;
import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/** Deterministic reference implementation for rule lifecycle, quarantine, approved correction, replay and reconcile. */
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
                    Map.copyOf(record),
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
                || command.approvedAt() == null) {
            throw new SecurityException("server approval execution metadata is required");
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
                    Map.copyOf(command.corrected()), "CORRECTED", old.version() + 1, old.violations());
            audit.add(new Audit(Instant.now(), actor, "CORRECT", command.quarantineId(), reason,
                    command.approvalExecutionReference(), next.version()));
            return next;
        });
    }

    @Override
    public CpfDataQualityDecision replay(String id, String actor, String reason) {
        require(actor, "actor");
        require(reason, "reason");
        QuarantineItem item = Optional.ofNullable(quarantine.get(id)).orElseThrow();
        Map<String, Object> candidate = item.corrected().isEmpty() ? item.original() : item.corrected();
        CpfDataQualityDecision decision = validate(item.recordId(), candidate);
        if (decision.accepted()) {
            quarantine.computeIfPresent(id, (key, old) -> new QuarantineItem(
                    id,
                    old.recordId(),
                    old.original(),
                    old.corrected(),
                    "REPLAYED",
                    old.version() + 1,
                    old.violations()));
            audit.add(new Audit(Instant.now(), actor, "REPLAY", id, reason, "", item.version() + 1));
        }
        return decision;
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
                if (replay(item.quarantineId(), actor, reason).accepted()) replayed++;
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
