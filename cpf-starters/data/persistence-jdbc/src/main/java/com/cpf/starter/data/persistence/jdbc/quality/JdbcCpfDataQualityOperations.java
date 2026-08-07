package com.cpf.starter.data.persistence.jdbc.quality;

import com.cpf.core.api.data.quality.CpfDataQualityDecision;
import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.api.data.quality.CpfDataQualityRule;
import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionOperations;

/**
 * DB-backed production provider for data-quality rules, quarantine correction, replay and reconcile.
 * All mutation paths are transactional, versioned and idempotent across JVM instances.
 */
public final class JdbcCpfDataQualityOperations implements CpfDataQualityOperations, CpfDataQualityCorrectionPort {
    static final int RECONCILE_BATCH_SIZE = 500;
    private static final TypeReference<Map<String,Object>> OBJECT_MAP = new TypeReference<>() {};
    private static final TypeReference<Map<String,String>> STRING_MAP = new TypeReference<>() {};
    private static final TypeReference<List<CpfDataQualityDecision.Violation>> VIOLATIONS = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final TransactionOperations transactions;
    private final ObjectMapper objectMapper;

    JdbcCpfDataQualityOperations(JdbcTemplate jdbc, TransactionOperations transactions, ObjectMapper objectMapper) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public CpfDataQualityRule register(CpfDataQualityRule rule, String actorId, String reason) {
        Objects.requireNonNull(rule, "rule");
        String actor = require(actorId, "actorId");
        String why = require(reason, "reason");
        return Objects.requireNonNull(transactions.execute(status -> {
            List<Long> versions = jdbc.query(
                    "SELECT RULE_VERSION FROM CPF_DATA_QUALITY_RULE WHERE RULE_ID=? ORDER BY RULE_VERSION DESC FOR UPDATE",
                    ps -> { ps.setString(1, rule.ruleId()); ps.setMaxRows(1); },
                    (rs, rowNum) -> rs.getLong(1));
            if (!versions.isEmpty() && rule.version() <= versions.get(0)) {
                throw new IllegalStateException("rule version must increase");
            }
            jdbc.update("INSERT INTO CPF_DATA_QUALITY_RULE "
                            + "(RULE_ID,RULE_VERSION,FIELD_NAME,EXPRESSION,SEVERITY,RULE_STATE,PARAMETERS_PAYLOAD,UPDATED_BY,UPDATED_AT) "
                            + "VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                    rule.ruleId(), rule.version(), rule.fieldName(), rule.expression(), rule.severity().name(),
                    rule.state().name(), json(rule.parameters()), actor);
            audit("RULE_REGISTER", rule.ruleId(), actor, why, "SUCCEEDED", "version=" + rule.version());
            return rule;
        }));
    }

    @Override
    public CpfDataQualityDecision validate(String recordId, Map<String,Object> record) {
        String id = require(recordId, "recordId");
        Map<String,Object> source = immutableNullable(record);
        CpfDataQualityDecision decision = validateOnly(id, source);
        if (!decision.accepted()) {
            transactions.executeWithoutResult(status -> {
                jdbc.update("INSERT INTO CPF_DATA_QUALITY_QUARANTINE "
                                + "(QUARANTINE_ID,RECORD_ID,ORIGINAL_PAYLOAD,CORRECTED_PAYLOAD,QUARANTINE_STATE,VIOLATION_SUMMARY,VIOLATION_PAYLOAD,ROW_VERSION,UPDATED_BY,UPDATE_REASON,UPDATED_AT) "
                                + "VALUES (?,?,?,NULL,'QUARANTINED',?,?,1,'CPF','validation',CURRENT_TIMESTAMP)",
                        decision.quarantineId(), id, json(source), violationSummary(decision.violations()), json(decision.violations()));
                audit("QUARANTINE", decision.quarantineId(), "CPF", "validation", "QUARANTINED",
                        "violations=" + decision.violations().size());
            });
        }
        return decision;
    }

    @Override
    public Optional<QuarantineItem> quarantine(String quarantineId) {
        String id = require(quarantineId, "quarantineId");
        List<QuarantineItem> rows = jdbc.query(
                "SELECT QUARANTINE_ID,RECORD_ID,ORIGINAL_PAYLOAD,CORRECTED_PAYLOAD,QUARANTINE_STATE,ROW_VERSION,VIOLATION_PAYLOAD "
                        + "FROM CPF_DATA_QUALITY_QUARANTINE WHERE QUARANTINE_ID=?",
                ps -> ps.setString(1, id), (rs, rowNum) -> quarantineRow(
                        rs.getString(1), rs.getString(2), rs.getBytes(3), rs.getBytes(4), rs.getString(5),
                        rs.getLong(6), rs.getBytes(7)));
        return rows.stream().findFirst();
    }

    @Override
    public QuarantineItem correctApproved(ApprovedCorrection command) {
        Objects.requireNonNull(command, "command");
        return Objects.requireNonNull(transactions.execute(status -> {
            QuarantineItem current = lockedQuarantine(command.quarantineId());
            if (!"QUARANTINED".equals(current.state())) {
                throw new IllegalStateException("only QUARANTINED data can be corrected");
            }
            if (current.version() != command.expectedVersion()) {
                throw new ConcurrentModificationException("quarantine version conflict");
            }
            long nextVersion = current.version() + 1;
            int changed = jdbc.update("UPDATE CPF_DATA_QUALITY_QUARANTINE SET CORRECTED_PAYLOAD=?,QUARANTINE_STATE='CORRECTED',"
                            + "ROW_VERSION=?,UPDATED_BY=?,UPDATE_REASON=?,UPDATED_AT=CURRENT_TIMESTAMP "
                            + "WHERE QUARANTINE_ID=? AND ROW_VERSION=? AND QUARANTINE_STATE='QUARANTINED'",
                    json(command.corrected()), nextVersion, command.actorId(), command.reason(),
                    command.quarantineId(), command.expectedVersion());
            if (changed != 1) throw new ConcurrentModificationException("quarantine correction CAS conflict");
            audit("CORRECT", command.quarantineId(), command.actorId(), command.reason(), "CORRECTED",
                    safeDetail(command.approvalExecutionReference()));
            return new QuarantineItem(current.quarantineId(), current.recordId(), current.original(),
                    immutableNullable(command.corrected()), "CORRECTED", nextVersion, current.violations());
        }));
    }

    @Override
    public CpfDataQualityDecision replay(ReplayCommand command) {
        Objects.requireNonNull(command, "command");
        String fingerprint = replayFingerprint(command);
        Optional<CpfDataQualityDecision> existing = priorOperation(command.operationId(), fingerprint, false);
        if (existing.isPresent()) return existing.get();
        try {
            return Objects.requireNonNull(transactions.execute(status -> {
                Optional<CpfDataQualityDecision> lockedExisting = priorOperation(command.operationId(), fingerprint, true);
                if (lockedExisting.isPresent()) return lockedExisting.get();
                QuarantineItem item = lockedQuarantine(command.quarantineId());
                // A waiter can see the first transaction only after this row lock is released; re-read the operation ledger.
                Optional<CpfDataQualityDecision> afterWait = priorOperation(command.operationId(), fingerprint, false);
                if (afterWait.isPresent()) return afterWait.get();
                if (item.version() != command.expectedVersion()) {
                    throw new ConcurrentModificationException("quarantine replay version conflict");
                }
                if (!Set.of("QUARANTINED", "CORRECTED").contains(item.state())) {
                    throw new IllegalStateException("only QUARANTINED/CORRECTED data can be replayed");
                }
                Map<String,Object> candidate = item.corrected().isEmpty() ? item.original() : item.corrected();
                CpfDataQualityDecision result = validateOnly(item.recordId(), candidate);
                if (result.accepted()) {
                    int changed = jdbc.update("UPDATE CPF_DATA_QUALITY_QUARANTINE SET QUARANTINE_STATE='REPLAYED',ROW_VERSION=ROW_VERSION+1,"
                                    + "UPDATED_BY=?,UPDATE_REASON=?,UPDATED_AT=CURRENT_TIMESTAMP "
                                    + "WHERE QUARANTINE_ID=? AND ROW_VERSION=? AND QUARANTINE_STATE IN ('QUARANTINED','CORRECTED')",
                            command.actorId(), command.reason(), item.quarantineId(), item.version());
                    if (changed != 1) throw new ConcurrentModificationException("quarantine replay CAS conflict");
                }
                jdbc.update("INSERT INTO CPF_DATA_QUALITY_OPERATION "
                                + "(OPERATION_ID,OPERATION_TYPE,QUARANTINE_ID,COMMAND_FINGERPRINT,RESULT_PAYLOAD,ACTOR_ID,ACTION_REASON,CREATED_AT) "
                                + "VALUES (?,'REPLAY',?,?,?,?,?,CURRENT_TIMESTAMP)",
                        command.operationId(), command.quarantineId(), fingerprint, json(result),
                        command.actorId(), command.reason());
                audit(result.accepted() ? "REPLAY" : "REPLAY_REJECTED", item.quarantineId(), command.actorId(),
                        command.reason(), result.accepted() ? "REPLAYED" : item.state(), command.operationId());
                return result;
            }));
        } catch (DuplicateKeyException concurrentSameOperation) {
            return priorOperation(command.operationId(), fingerprint, false)
                    .orElseThrow(() -> concurrentSameOperation);
        }
    }

    @Override
    public ReconcileResult reconcile(String actorId, String reason) {
        String actor = require(actorId, "actorId");
        String why = require(reason, "reason");
        List<QuarantineItem> corrected = jdbc.query(
                "SELECT QUARANTINE_ID,RECORD_ID,ORIGINAL_PAYLOAD,CORRECTED_PAYLOAD,QUARANTINE_STATE,ROW_VERSION,VIOLATION_PAYLOAD "
                        + "FROM CPF_DATA_QUALITY_QUARANTINE WHERE QUARANTINE_STATE='CORRECTED' ORDER BY UPDATED_AT,QUARANTINE_ID",
                ps -> ps.setMaxRows(RECONCILE_BATCH_SIZE),
                (rs, rowNum) -> quarantineRow(rs.getString(1), rs.getString(2), rs.getBytes(3), rs.getBytes(4),
                        rs.getString(5), rs.getLong(6), rs.getBytes(7)));
        int replayed = 0;
        for (QuarantineItem item : corrected) {
            ReplayCommand command = new ReplayCommand(item.quarantineId(), item.version(),
                    "reconcile-" + item.quarantineId() + "-v" + item.version(), actor, why);
            if (replay(command).accepted()) replayed++;
        }
        Integer remaining = jdbc.queryForObject(
                "SELECT COUNT(*) FROM CPF_DATA_QUALITY_QUARANTINE WHERE QUARANTINE_STATE<>'REPLAYED'",
                Integer.class);
        audit("RECONCILE", "DATA_QUALITY", actor, why, "SUCCEEDED",
                "inspected=" + corrected.size() + ",replayed=" + replayed + ",remaining=" + Objects.requireNonNullElse(remaining, 0));
        return new ReconcileResult(corrected.size(), replayed, Objects.requireNonNullElse(remaining, 0));
    }

    private List<CpfDataQualityRule> activeRules() {
        return jdbc.query("SELECT RULE_ID,RULE_VERSION,FIELD_NAME,EXPRESSION,SEVERITY,RULE_STATE,PARAMETERS_PAYLOAD "
                        + "FROM CPF_DATA_QUALITY_RULE r WHERE RULE_STATE='ACTIVE' AND RULE_VERSION=(SELECT MAX(r2.RULE_VERSION) "
                        + "FROM CPF_DATA_QUALITY_RULE r2 WHERE r2.RULE_ID=r.RULE_ID)",
                (rs, rowNum) -> new CpfDataQualityRule(rs.getString(1), rs.getLong(2), rs.getString(3), rs.getString(4),
                        CpfDataQualityRule.Severity.valueOf(rs.getString(5)), CpfDataQualityRule.State.valueOf(rs.getString(6)),
                        read(rs.getBytes(7), STRING_MAP, Map.of())));
    }

    private CpfDataQualityDecision validateOnly(String recordId, Map<String,Object> record) {
        List<CpfDataQualityDecision.Violation> violations = new ArrayList<>();
        for (CpfDataQualityRule rule : activeRules()) {
            if (!matches(rule, record.get(rule.fieldName()))) {
                violations.add(new CpfDataQualityDecision.Violation(rule.ruleId(), rule.severity(), rule.fieldName(),
                        "Rule failed: " + rule.expression()));
            }
        }
        boolean accepted = violations.stream().noneMatch(v -> v.severity() == CpfDataQualityRule.Severity.ERROR
                || v.severity() == CpfDataQualityRule.Severity.CRITICAL);
        return new CpfDataQualityDecision(recordId, accepted, List.copyOf(violations),
                accepted ? "" : "DQ-" + UUID.randomUUID(), Instant.now());
    }

    private QuarantineItem lockedQuarantine(String quarantineId) {
        List<QuarantineItem> rows = jdbc.query(
                "SELECT QUARANTINE_ID,RECORD_ID,ORIGINAL_PAYLOAD,CORRECTED_PAYLOAD,QUARANTINE_STATE,ROW_VERSION,VIOLATION_PAYLOAD "
                        + "FROM CPF_DATA_QUALITY_QUARANTINE WHERE QUARANTINE_ID=? FOR UPDATE",
                ps -> ps.setString(1, quarantineId), (rs, rowNum) -> quarantineRow(
                        rs.getString(1), rs.getString(2), rs.getBytes(3), rs.getBytes(4), rs.getString(5), rs.getLong(6), rs.getBytes(7)));
        if (rows.isEmpty()) throw new NoSuchElementException(quarantineId);
        return rows.get(0);
    }

    private Optional<CpfDataQualityDecision> priorOperation(String operationId, String fingerprint, boolean forUpdate) {
        String sql = "SELECT COMMAND_FINGERPRINT,RESULT_PAYLOAD FROM CPF_DATA_QUALITY_OPERATION WHERE OPERATION_ID=?"
                + (forUpdate ? " FOR UPDATE" : "");
        List<Map.Entry<String,byte[]>> rows = jdbc.query(sql, ps -> ps.setString(1, operationId),
                (rs, rowNum) -> Map.entry(rs.getString(1), rs.getBytes(2)));
        if (rows.isEmpty()) return Optional.empty();
        Map.Entry<String,byte[]> row = rows.get(0);
        if (!MessageDigest.isEqual(row.getKey().getBytes(StandardCharsets.US_ASCII),
                fingerprint.getBytes(StandardCharsets.US_ASCII))) {
            throw new IllegalStateException("operationId is already bound to a different replay command");
        }
        return Optional.of(read(row.getValue(), new TypeReference<CpfDataQualityDecision>() {}, null));
    }

    private QuarantineItem quarantineRow(String quarantineId, String recordId, byte[] original, byte[] corrected,
            String state, long version, byte[] violations) {
        return new QuarantineItem(quarantineId, recordId,
                read(original, OBJECT_MAP, Map.of()), read(corrected, OBJECT_MAP, Map.of()), state, version,
                read(violations, VIOLATIONS, List.of()));
    }

    private void audit(String action, String target, String actor, String reason, String state, String detail) {
        jdbc.update("INSERT INTO CPF_INTEGRATION_CLOSURE_AUDIT "
                        + "(AUDIT_ID,CATEGORY,TARGET_ID,ACTION_NAME,ACTOR_ID,ACTION_REASON,RESULT_STATE,RESULT_DETAIL,OCCURRED_AT) "
                        + "VALUES (?,'DATA_QUALITY',?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                UUID.randomUUID().toString(), safe(target, 64), safe(action, 64), safe(actor, 64),
                safe(reason, 1000), safe(state, 64), safe(detail, 1000));
    }

    private byte[] json(Object value) {
        try { return objectMapper.writeValueAsBytes(value); }
        catch (Exception ex) { throw new IllegalStateException("data-quality JSON encoding failed", ex); }
    }

    private <T> T read(byte[] value, TypeReference<T> type, T empty) {
        if (value == null || value.length == 0) return empty;
        try { return objectMapper.readValue(value, type); }
        catch (Exception ex) { throw new IllegalStateException("data-quality JSON decoding failed", ex); }
    }

    static boolean matches(CpfDataQualityRule rule, Object value) {
        String expression = rule.expression().trim();
        String text = value == null ? "" : String.valueOf(value);
        if ("NOT_BLANK".equals(expression)) return !text.isBlank();
        if (expression.startsWith("REGEX:")) return Pattern.compile(expression.substring(6)).matcher(text).matches();
        if (expression.startsWith("MIN_LENGTH:")) return text.length() >= Integer.parseInt(expression.substring(11));
        if (expression.startsWith("MAX_LENGTH:")) return text.length() <= Integer.parseInt(expression.substring(11));
        throw new IllegalArgumentException("Unsupported rule expression: " + expression);
    }

    static String replayFingerprint(ReplayCommand command) {
        String material = command.quarantineId() + "\n" + command.expectedVersion() + "\n"
                + command.actorId().trim() + "\n" + command.reason().trim();
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException("SHA-256 unavailable", ex); }
    }

    private static <K,V> Map<K,V> immutableNullable(Map<K,V> source) {
        if (source == null || source.isEmpty()) return Map.of();
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static String violationSummary(List<CpfDataQualityDecision.Violation> violations) {
        return safe(violations.stream().map(v -> v.ruleId() + ":" + v.severity()).reduce((a,b) -> a + "," + b).orElse("none"), 1000);
    }
    private static String safeDetail(String value) { return safe(value, 1000); }
    private static String safe(String value, int max) {
        String text = Objects.toString(value, "").replaceAll("[\\r\\n\\t]", " ").trim();
        return text.length() <= max ? text : text.substring(0, max);
    }
    private static String require(String value, String field) {
        String text = Objects.toString(value, "").trim();
        if (text.isBlank()) throw new IllegalArgumentException(field + " is required");
        return text;
    }
}
