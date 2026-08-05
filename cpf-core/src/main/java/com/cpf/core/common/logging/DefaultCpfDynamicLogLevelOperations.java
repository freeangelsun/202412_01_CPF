package com.cpf.core.common.logging;

import com.cpf.core.api.logging.CpfDynamicLogLevelCommandOperations;
import com.cpf.core.api.logging.CpfDynamicLogLevelOperations;
import com.cpf.core.api.logging.CpfDynamicLogLevelRuntimeStatus;
import com.cpf.core.api.logging.CpfLogLevel;
import com.cpf.core.api.logging.DynamicLogLevelAuditRecord;
import com.cpf.core.api.logging.DynamicLogLevelRequest;
import com.cpf.core.api.logging.DynamicLogLevelRule;
import com.cpf.core.api.logging.DynamicLogLevelRuntimeSnapshot;
import com.cpf.core.api.logging.DynamicLogLevelVersionConflictException;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * JVM-local dynamic log-level registry with versioned CAS commands, atomic audit snapshots and
 * deterministic TTL handling.
 *
 * <p>This is the safe local default. A distributed control-plane provider may replace the public
 * contracts without exposing this internal implementation.</p>
 */
public final class DefaultCpfDynamicLogLevelOperations implements
        CpfDynamicLogLevelOperations,
        CpfDynamicLogLevelCommandOperations,
        CpfDynamicLogLevelRuntimeStatus {
    private static final Duration DEFAULT_MAX_TTL = Duration.ofHours(24);
    private static final int DEFAULT_MAX_ACTIVE_RULES = 2_048;
    public static final int DEFAULT_MAX_AUDIT_RECORDS = 10_000;
    private static final int MAX_CONFIGURED_ENTRIES = 1_000_000;
    private static final int MAX_IDENTIFIER_LENGTH = 256;
    private static final int MAX_REASON_LENGTH = 2_000;
    private static final int MAX_USER_LENGTH = 128;

    private static final Comparator<DynamicLogLevelRule> RESOLUTION_ORDER = Comparator
            .comparingInt(DefaultCpfDynamicLogLevelOperations::specificity)
            .reversed()
            .thenComparing(DynamicLogLevelRule::createdAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(DynamicLogLevelRule::expiresAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(DynamicLogLevelRule::ruleId);

    private final Clock clock;
    private final Duration maxTtl;
    private final int maxActiveRules;
    private final int maxAuditRecords;
    private final AtomicReference<RegistryState> state = new AtomicReference<>(RegistryState.empty());

    public DefaultCpfDynamicLogLevelOperations() {
        this(Clock.systemUTC(), DEFAULT_MAX_TTL, DEFAULT_MAX_ACTIVE_RULES, DEFAULT_MAX_AUDIT_RECORDS);
    }

    public DefaultCpfDynamicLogLevelOperations(Clock clock) {
        this(clock, DEFAULT_MAX_TTL, DEFAULT_MAX_ACTIVE_RULES, DEFAULT_MAX_AUDIT_RECORDS);
    }

    public DefaultCpfDynamicLogLevelOperations(Clock clock, Duration maxTtl) {
        this(clock, maxTtl, DEFAULT_MAX_ACTIVE_RULES, DEFAULT_MAX_AUDIT_RECORDS);
    }

    public DefaultCpfDynamicLogLevelOperations(Clock clock, Duration maxTtl, int maxActiveRules) {
        this(clock, maxTtl, maxActiveRules, DEFAULT_MAX_AUDIT_RECORDS);
    }

    public DefaultCpfDynamicLogLevelOperations(
            Clock clock, Duration maxTtl, int maxActiveRules, int maxAuditRecords) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.maxTtl = requirePositive(maxTtl, "maxTtl");
        if (maxActiveRules < 1 || maxActiveRules > MAX_CONFIGURED_ENTRIES) {
            throw new IllegalArgumentException("maxActiveRules must be between 1 and 1000000");
        }
        if (maxAuditRecords < 1 || maxAuditRecords > MAX_CONFIGURED_ENTRIES) {
            throw new IllegalArgumentException("maxAuditRecords must be between 1 and 1000000");
        }
        this.maxActiveRules = maxActiveRules;
        this.maxAuditRecords = maxAuditRecords;
    }

    @Override
    public DynamicLogLevelRule register(DynamicLogLevelRequest request) {
        return registerInternal(request, null);
    }

    @Override
    public DynamicLogLevelRule register(DynamicLogLevelRequest request, long expectedVersion) {
        return registerInternal(request, requireVersion(expectedVersion));
    }

    private DynamicLogLevelRule registerInternal(DynamicLogLevelRequest request, Long expectedVersion) {
        Objects.requireNonNull(request, "request");
        LocalDateTime now = now();
        Duration ttl = requirePositive(request.getTtl(), "ttl");
        if (ttl.compareTo(maxTtl) > 0) {
            throw new IllegalArgumentException("ttl exceeds configured maximum: " + maxTtl);
        }
        DynamicLogLevelRule rule = normalize(new DynamicLogLevelRule(
                "DLR-" + UUID.randomUUID(),
                request.getTransactionId(),
                request.getBusinessTransactionId(),
                request.getModuleId(),
                request.getLogLevel(),
                request.getReason(),
                request.getRequestUser(),
                now,
                safePlus(now, ttl)));
        commit(expectedVersion, "REGISTER", rule.ruleId(), rule.createdBy(), rule.reason(), current -> {
            if (current.containsKey(rule.ruleId())) {
                throw new IllegalStateException("dynamic log-level rule already exists: " + rule.ruleId());
            }
            LinkedHashMap<String, DynamicLogLevelRule> next = new LinkedHashMap<>(current);
            next.put(rule.ruleId(), rule);
            return Map.copyOf(next);
        });
        return rule;
    }

    @Override
    public void upsert(DynamicLogLevelRule rule) {
        upsertInternal(rule, null);
    }

    @Override
    public void upsert(DynamicLogLevelRule rule, long expectedVersion) {
        upsertInternal(rule, requireVersion(expectedVersion));
    }

    private void upsertInternal(DynamicLogLevelRule rule, Long expectedVersion) {
        DynamicLogLevelRule normalized = normalize(rule);
        if (normalized.expired(now())) {
            throw new IllegalArgumentException("expired dynamic log-level rule cannot be upserted");
        }
        commit(expectedVersion, "UPSERT", normalized.ruleId(), normalized.createdBy(), normalized.reason(), current -> {
            LinkedHashMap<String, DynamicLogLevelRule> next = new LinkedHashMap<>(current);
            next.put(normalized.ruleId(), normalized);
            return Map.copyOf(next);
        });
    }

    @Override
    public void replaceAll(List<DynamicLogLevelRule> activeRules) {
        String actor = activeRules == null || activeRules.isEmpty() ? "SYSTEM" : activeRules.get(0).createdBy();
        String reason = activeRules == null || activeRules.isEmpty()
                ? "legacy replaceAll cleared the local registry"
                : activeRules.get(0).reason();
        replaceAllInternal(activeRules, null, actor, reason);
    }

    @Override
    public void replaceAll(
            List<DynamicLogLevelRule> activeRules,
            long expectedVersion,
            String actor,
            String reason) {
        replaceAllInternal(activeRules, requireVersion(expectedVersion), actor, reason);
    }

    private void replaceAllInternal(
            List<DynamicLogLevelRule> activeRules,
            Long expectedVersion,
            String actor,
            String reason) {
        Objects.requireNonNull(activeRules, "activeRules");
        LocalDateTime now = now();
        LinkedHashMap<String, DynamicLogLevelRule> replacement = new LinkedHashMap<>();
        for (DynamicLogLevelRule candidate : activeRules) {
            DynamicLogLevelRule normalized = normalize(candidate);
            if (normalized.expired(now)) {
                throw new IllegalArgumentException("replaceAll contains an expired rule: " + normalized.ruleId());
            }
            if (replacement.putIfAbsent(normalized.ruleId(), normalized) != null) {
                throw new IllegalArgumentException("duplicate dynamic log-level rule id: " + normalized.ruleId());
            }
        }
        Map<String, DynamicLogLevelRule> immutable = Map.copyOf(replacement);
        commit(expectedVersion, "REPLACE_ALL", "*", actor, reason, ignored -> immutable);
    }

    @Override
    public Optional<DynamicLogLevelRule> resolve(
            String transactionId,
            String businessTransactionId,
            String moduleId) {
        RegistryState current = removeExpired(now());
        String tx = identifier(transactionId, false);
        String business = identifier(businessTransactionId, false);
        String module = identifier(moduleId, true);
        return current.rules().values().stream()
                .filter(rule -> matches(rule, tx, business, module))
                .sorted(RESOLUTION_ORDER)
                .findFirst();
    }

    @Override
    public List<DynamicLogLevelRule> findActiveRules() {
        return sortedRules(removeExpired(now()).rules());
    }

    @Override
    public boolean remove(String ruleId) {
        return removeInternal(ruleId, null, "SYSTEM", "legacy remove by ruleId API");
    }

    @Override
    public boolean remove(String ruleId, long expectedVersion, String actor, String reason) {
        return removeInternal(ruleId, requireVersion(expectedVersion), actor, reason);
    }

    private boolean removeInternal(String ruleId, Long expectedVersion, String actor, String reason) {
        String normalizedId = requiredText(ruleId, "ruleId", MAX_IDENTIFIER_LENGTH);
        while (true) {
            RegistryState current = removeExpired(now());
            checkVersion(expectedVersion, current.version());
            DynamicLogLevelRule before = current.rules().get(normalizedId);
            if (before == null) return false;
            LinkedHashMap<String, DynamicLogLevelRule> nextRules = new LinkedHashMap<>(current.rules());
            nextRules.remove(normalizedId);
            RegistryState next = nextState(
                    current,
                    Map.copyOf(nextRules),
                    "REMOVE",
                    normalizedId,
                    actor,
                    reason,
                    before,
                    null);
            if (state.compareAndSet(current, next)) return true;
        }
    }

    @Override
    public void clear() {
        clearInternal(null, "SYSTEM", "legacy clear API");
    }

    @Override
    public void clear(long expectedVersion, String actor, String reason) {
        clearInternal(requireVersion(expectedVersion), actor, reason);
    }

    private void clearInternal(Long expectedVersion, String actor, String reason) {
        commit(expectedVersion, "CLEAR", "*", actor, reason, current -> current.isEmpty() ? current : Map.of());
    }

    @Override
    public DynamicLogLevelRuntimeSnapshot snapshot() {
        RegistryState current = removeExpired(now());
        return new DynamicLogLevelRuntimeSnapshot(
                current.version(),
                now(),
                sortedRules(current.rules()),
                current.auditRecords(),
                maxAuditRecords,
                current.droppedAuditRecordCount());
    }

    private void commit(
            Long expectedVersion,
            String action,
            String targetRuleId,
            String actor,
            String reason,
            UnaryOperator<Map<String, DynamicLogLevelRule>> updater) {
        while (true) {
            RegistryState current = removeExpired(now());
            checkVersion(expectedVersion, current.version());
            Map<String, DynamicLogLevelRule> updated = Objects.requireNonNull(
                    updater.apply(current.rules()), "updated rules");
            if (updated == current.rules() || updated.equals(current.rules())) return;
            if (updated.size() > maxActiveRules) {
                throw new IllegalStateException("dynamic log-level active rule limit exceeded: " + maxActiveRules);
            }
            DynamicLogLevelRule before = "*".equals(targetRuleId) ? null : current.rules().get(targetRuleId);
            DynamicLogLevelRule after = "*".equals(targetRuleId) ? null : updated.get(targetRuleId);
            RegistryState next = nextState(
                    current, updated, action, targetRuleId, actor, reason, before, after);
            if (state.compareAndSet(current, next)) return;
        }
    }

    private RegistryState nextState(
            RegistryState current,
            Map<String, DynamicLogLevelRule> rules,
            String action,
            String targetRuleId,
            String actor,
            String reason,
            DynamicLogLevelRule before,
            DynamicLogLevelRule after) {
        long nextVersion = Math.addExact(current.version(), 1L);
        DynamicLogLevelAuditRecord audit = new DynamicLogLevelAuditRecord(
                "DLA-" + UUID.randomUUID(),
                action,
                targetRuleId,
                requiredText(actor, "actor", MAX_USER_LENGTH),
                maskedReason(reason),
                before,
                after,
                nextVersion,
                now());
        AuditWindow auditWindow = appendAuditRecords(current, List.of(audit));
        return new RegistryState(
                nextVersion,
                Map.copyOf(rules),
                auditWindow.records(),
                auditWindow.droppedCount());
    }

    private RegistryState removeExpired(LocalDateTime now) {
        while (true) {
            RegistryState current = state.get();
            List<DynamicLogLevelRule> expired = current.rules().values().stream()
                    .filter(rule -> rule.expired(now))
                    .sorted(Comparator.comparing(DynamicLogLevelRule::ruleId))
                    .toList();
            if (expired.isEmpty()) return current;
            LinkedHashMap<String, DynamicLogLevelRule> nextRules = new LinkedHashMap<>(current.rules());
            expired.forEach(rule -> nextRules.remove(rule.ruleId()));
            long nextVersion = Math.addExact(current.version(), 1L);
            ArrayList<DynamicLogLevelAuditRecord> expirationAudits = new ArrayList<>(expired.size());
            for (DynamicLogLevelRule rule : expired) {
                expirationAudits.add(new DynamicLogLevelAuditRecord(
                        "DLA-" + UUID.randomUUID(),
                        "EXPIRE",
                        rule.ruleId(),
                        "SYSTEM",
                        "TTL_EXPIRED",
                        rule,
                        null,
                        nextVersion,
                        now));
            }
            AuditWindow auditWindow = appendAuditRecords(current, expirationAudits);
            RegistryState next = new RegistryState(
                    nextVersion,
                    Map.copyOf(nextRules),
                    auditWindow.records(),
                    auditWindow.droppedCount());
            if (state.compareAndSet(current, next)) return next;
        }
    }

    private AuditWindow appendAuditRecords(
            RegistryState current, List<DynamicLogLevelAuditRecord> additions) {
        ArrayList<DynamicLogLevelAuditRecord> combined = new ArrayList<>(
                Math.min(maxAuditRecords + additions.size(),
                        current.auditRecords().size() + additions.size()));
        combined.addAll(current.auditRecords());
        combined.addAll(additions);
        int overflow = Math.max(0, combined.size() - maxAuditRecords);
        List<DynamicLogLevelAuditRecord> retained = overflow == 0
                ? List.copyOf(combined)
                : List.copyOf(combined.subList(overflow, combined.size()));
        long dropped;
        try {
            dropped = Math.addExact(current.droppedAuditRecordCount(), overflow);
        } catch (ArithmeticException exhausted) {
            dropped = Long.MAX_VALUE;
        }
        return new AuditWindow(retained, dropped);
    }

    private List<DynamicLogLevelRule> sortedRules(Map<String, DynamicLogLevelRule> source) {
        ArrayList<DynamicLogLevelRule> result = new ArrayList<>(source.values());
        result.sort(RESOLUTION_ORDER);
        return List.copyOf(result);
    }

    private DynamicLogLevelRule normalize(DynamicLogLevelRule rule) {
        Objects.requireNonNull(rule, "rule");
        String ruleId = requiredText(rule.ruleId(), "ruleId", MAX_IDENTIFIER_LENGTH);
        String transactionId = identifier(rule.transactionId(), false);
        String businessTransactionId = identifier(rule.businessTransactionId(), false);
        String moduleId = identifier(rule.moduleId(), true);
        if (transactionId == null && businessTransactionId == null) {
            throw new IllegalArgumentException("transactionId or businessTransactionId is required");
        }
        CpfLogLevel logLevel = Objects.requireNonNull(rule.logLevel(), "logLevel");
        LocalDateTime createdAt = Objects.requireNonNull(rule.createdAt(), "createdAt");
        LocalDateTime expiresAt = Objects.requireNonNull(rule.expiresAt(), "expiresAt");
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }
        Duration ttl = Duration.between(createdAt, expiresAt);
        if (ttl.compareTo(maxTtl) > 0) {
            throw new IllegalArgumentException("rule TTL exceeds configured maximum: " + maxTtl);
        }
        return new DynamicLogLevelRule(
                ruleId,
                transactionId,
                businessTransactionId,
                moduleId,
                logLevel,
                maskedReason(rule.reason()),
                requiredText(rule.createdBy(), "createdBy", MAX_USER_LENGTH),
                createdAt,
                expiresAt);
    }

    private boolean matches(
            DynamicLogLevelRule rule,
            String transactionId,
            String businessTransactionId,
            String moduleId) {
        return equalsIfPresent(rule.transactionId(), transactionId)
                && equalsIfPresent(rule.businessTransactionId(), businessTransactionId)
                && equalsIfPresent(rule.moduleId(), moduleId);
    }

    private boolean equalsIfPresent(String expected, String actual) {
        return expected == null || expected.equals(actual);
    }

    private static int specificity(DynamicLogLevelRule rule) {
        int score = 0;
        if (rule.transactionId() != null) score += 4;
        if (rule.businessTransactionId() != null) score += 2;
        if (rule.moduleId() != null) score += 1;
        return score;
    }

    private String identifier(String value, boolean upperCase) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > MAX_IDENTIFIER_LENGTH) {
            throw new IllegalArgumentException("identifier exceeds " + MAX_IDENTIFIER_LENGTH + " characters");
        }
        return upperCase ? normalized.toUpperCase(Locale.ROOT) : normalized;
    }

    private String maskedReason(String value) {
        return SensitiveDataMasker.truncate(
                SensitiveDataMasker.mask(requiredText(value, "reason", MAX_REASON_LENGTH)),
                MAX_REASON_LENGTH);
    }

    private String requiredText(String value, String name, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(name + " exceeds " + maxLength + " characters");
        }
        return normalized;
    }

    private Duration requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name);
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return duration;
    }

    private Long requireVersion(long version) {
        if (version < 0L) throw new IllegalArgumentException("expectedVersion must be non-negative");
        return version;
    }

    private void checkVersion(Long expectedVersion, long actualVersion) {
        if (expectedVersion != null && expectedVersion.longValue() != actualVersion) {
            throw new DynamicLogLevelVersionConflictException(expectedVersion, actualVersion);
        }
    }

    private LocalDateTime safePlus(LocalDateTime value, Duration duration) {
        try {
            return value.plus(duration);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("dynamic log-level expiration overflows supported time range", ex);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), clock.getZone());
    }

    private record RegistryState(
            long version,
            Map<String, DynamicLogLevelRule> rules,
            List<DynamicLogLevelAuditRecord> auditRecords,
            long droppedAuditRecordCount) {
        private RegistryState {
            rules = rules == null ? Map.of() : Map.copyOf(rules);
            auditRecords = auditRecords == null ? List.of() : List.copyOf(auditRecords);
            if (droppedAuditRecordCount < 0L) {
                throw new IllegalArgumentException("droppedAuditRecordCount must be non-negative");
            }
        }

        private static RegistryState empty() {
            return new RegistryState(0L, Map.of(), List.of(), 0L);
        }
    }

    private record AuditWindow(List<DynamicLogLevelAuditRecord> records, long droppedCount) {
    }
}
