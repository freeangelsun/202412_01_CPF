package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayRateLimitCounterPort;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 다중 Gateway 인스턴스가 공유하는 JDBC Rate Limit Counter Provider입니다.
 *
 * <p>복합 Scope 판정은 하나의 DB Transaction과 Counter row lock 안에서 수행합니다.
 * Request journal이 일부 Scope에만 남은 상태는 정상 처리하지 않고 무결성 오류로
 * fail-closed 합니다. 테이블 Lifecycle DDL은 공식 3개 Vendor Migration에서 제공해야 합니다.</p>
 */
public final class JdbcCpfGatewayRateLimitCounterAdapter implements CpfGatewayRateLimitCounterPort {
    static final String COUNTER_TABLE = "cpf_gateway_rate_limit_counter";
    static final String REQUEST_TABLE = "cpf_gateway_rate_limit_request";
    private static final int MAX_ATOMIC_SCOPES = 16;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    public JdbcCpfGatewayRateLimitCounterAdapter(
            JdbcTemplate jdbc,
            PlatformTransactionManager transactionManager) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.transactions = new TransactionTemplate(
                Objects.requireNonNull(transactionManager, "transactionManager"));
    }

    @Override
    public CounterResult consume(CounterCommand command) {
        BatchResult result = consumeAtomically(List.of(Objects.requireNonNull(command, "command")));
        return result.results().getFirst();
    }

    @Override
    public BatchResult consumeAtomically(List<CounterCommand> commands) {
        Objects.requireNonNull(commands, "commands");
        if (commands.isEmpty()) return new BatchResult(true, -1, List.of());
        if (commands.size() > MAX_ATOMIC_SCOPES) {
            throw new IllegalArgumentException("too many atomic rate-limit scopes");
        }
        List<CounterCommand> immutable = commands.stream()
                .map(command -> Objects.requireNonNull(command, "command"))
                .toList();
        validateAtomicBatch(immutable);
        BatchResult result = transactions.execute(status -> consumeInTransaction(immutable));
        if (result == null) throw new IllegalStateException("Rate-limit transaction returned no result");
        return result;
    }

    @Override
    public CounterHealth health() {
        try {
            Integer probe = jdbc.queryForObject("SELECT 1", Integer.class);
            Long active = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM " + COUNTER_TABLE + " WHERE reset_at_ms > ?",
                    Long.class,
                    System.currentTimeMillis());
            return new CounterHealth(probe != null && probe == 1,
                    active == null ? 0L : active, "UP", Instant.now());
        } catch (RuntimeException failure) {
            return new CounterHealth(false, 0L, "DOWN", Instant.now());
        }
    }

    @Override
    public boolean distributed() {
        return true;
    }

    private BatchResult consumeInTransaction(List<CounterCommand> commands) {
        List<JournalRow> journals = findJournals(commands);
        int existing = presentCount(journals);
        if (existing == commands.size()) return replayJournal(journals);
        if (existing != 0) {
            throw new IllegalStateException("Partial rate-limit request journal detected");
        }

        List<CounterRow> locked = lockAll(commands);

        // 동일 request가 다른 인스턴스에서 row lock 대기 중 완료됐을 수 있으므로
        // lock 획득 뒤 journal을 다시 확인해 duplicate를 이중 소비하지 않습니다.
        journals = findJournals(commands);
        existing = presentCount(journals);
        if (existing == commands.size()) return replayJournal(journals);
        if (existing != 0) {
            throw new IllegalStateException("Partial rate-limit request journal detected after lock");
        }

        List<CounterResult> proposed = new ArrayList<>(commands.size());
        int limitingIndex = -1;
        for (int index = 0; index < commands.size(); index++) {
            CounterCommand command = commands.get(index);
            CounterResult result = evaluate(command, locked.get(index));
            proposed.add(result);
            if (!result.accepted() && limitingIndex < 0) limitingIndex = index;
        }

        if (limitingIndex < 0) {
            for (int index = 0; index < commands.size(); index++) {
                CounterCommand command = commands.get(index);
                CounterResult result = proposed.get(index);
                updateAccepted(command, locked.get(index), result);
            }
        } else {
            CounterCommand command = commands.get(limitingIndex);
            CounterResult result = proposed.get(limitingIndex);
            updateRejected(command, locked.get(limitingIndex), result);
        }

        for (int index = 0; index < commands.size(); index++) {
            insertJournal(commands.get(index), proposed.get(index), limitingIndex);
        }
        return new BatchResult(limitingIndex < 0, limitingIndex, proposed);
    }


    private static void validateAtomicBatch(List<CounterCommand> commands) {
        Set<String> rowKeys = new HashSet<>();
        Long policyVersion = null;
        Long now = null;
        for (CounterCommand command : commands) {
            String rowKey = lockKey(command);
            if (!rowKeys.add(rowKey)) {
                throw new IllegalArgumentException("duplicate counter row in atomic batch");
            }
            if (policyVersion == null) policyVersion = command.policyVersion();
            else if (policyVersion.longValue() != command.policyVersion()) {
                throw new IllegalArgumentException("atomic batch must use one policy version");
            }
            if (now == null) now = command.nowEpochMillis();
            else if (now.longValue() != command.nowEpochMillis()) {
                throw new IllegalArgumentException("atomic batch must use one observation time");
            }
        }
    }

    private List<JournalRow> findJournals(List<CounterCommand> commands) {
        List<JournalRow> journals = new ArrayList<>(commands.size());
        for (CounterCommand command : commands) journals.add(findJournal(command));
        return journals;
    }

    private static int presentCount(List<JournalRow> journals) {
        int count = 0;
        for (JournalRow journal : journals) if (journal != null) count++;
        return count;
    }

    private List<CounterRow> lockAll(List<CounterCommand> commands) {
        List<Integer> order = new ArrayList<>(commands.size());
        for (int index = 0; index < commands.size(); index++) order.add(index);
        order.sort(Comparator.comparing(index -> lockKey(commands.get(index))));

        List<CounterRow> locked = new ArrayList<>(java.util.Collections.nCopies(commands.size(), null));
        for (int index : order) locked.set(index, lockOrCreate(commands.get(index)));
        return locked;
    }

    private static String lockKey(CounterCommand command) {
        return command.counterKey() + '\u0000' + command.policyVersion() + '\u0000'
                + command.windowStartEpochMillis();
    }

    private JournalRow findJournal(CounterCommand command) {
        List<JournalRow> rows = jdbc.query(
                "SELECT accepted, used_units, remaining_units, reset_at_ms, blocked_until_ms, "
                        + "rejected_count, reason, limiting_index, request_hash FROM " + REQUEST_TABLE
                        + " WHERE counter_key=? AND policy_version=? AND window_start_ms=? AND request_id=?",
                (rs, rowNum) -> new JournalRow(
                        rs.getInt("accepted") == 1,
                        rs.getLong("used_units"),
                        rs.getLong("remaining_units"),
                        rs.getLong("reset_at_ms"),
                        rs.getLong("blocked_until_ms"),
                        rs.getInt("rejected_count"),
                        rs.getString("reason"),
                        rs.getInt("limiting_index"),
                        rs.getString("request_hash")),
                command.counterKey(), command.policyVersion(),
                command.windowStartEpochMillis(), command.requestId());
        if (rows.size() > 1) throw new IllegalStateException("Duplicate rate-limit request journal");
        if (rows.isEmpty()) return null;
        JournalRow row = rows.getFirst();
        validateJournalHash(command, row.requestHash());
        return row;
    }

    private BatchResult replayJournal(List<JournalRow> journals) {
        int limitingIndex = journals.getFirst().limitingIndex();
        List<CounterResult> results = new ArrayList<>(journals.size());
        for (JournalRow row : journals) {
            if (row.limitingIndex() != limitingIndex) {
                throw new IllegalStateException("Inconsistent rate-limit request journal");
            }
            results.add(new CounterResult(
                    row.accepted(), true, row.used(), row.remaining(), row.resetAt(),
                    row.blockedUntil(), row.rejectedCount(), row.reason()));
        }
        return new BatchResult(limitingIndex < 0, limitingIndex, results);
    }

    private CounterRow lockOrCreate(CounterCommand command) {
        List<CounterRow> rows = selectForUpdate(command);
        if (rows.isEmpty()) {
            try {
                jdbc.update("INSERT INTO " + COUNTER_TABLE
                                + " (counter_key, policy_version, window_start_ms, reset_at_ms, used_units, "
                                + "rejected_count, blocked_until_ms, version) VALUES (?,?,?,?,0,0,0,0)",
                        command.counterKey(), command.policyVersion(), command.windowStartEpochMillis(),
                        command.resetAtEpochMillis());
            } catch (DuplicateKeyException concurrentInsert) {
                // 다른 인스턴스가 같은 window row를 먼저 만들었다. 아래 row lock에서 합류한다.
            }
            rows = selectForUpdate(command);
        }
        if (rows.size() != 1) throw new IllegalStateException("Rate-limit counter row creation failed");

        CounterRow current = rows.getFirst();
        List<Long> activeBlocks = selectActiveBlocksForUpdate(command);
        return carryForwardActiveBlock(current, activeBlocks);
    }

    private List<Long> selectActiveBlocksForUpdate(CounterCommand command) {
        return jdbc.query(
                "SELECT blocked_until_ms FROM " + COUNTER_TABLE
                        + " WHERE counter_key=? AND policy_version=? AND blocked_until_ms>? FOR UPDATE",
                (rs, rowNum) -> rs.getLong("blocked_until_ms"),
                command.counterKey(), command.policyVersion(), command.nowEpochMillis());
    }

    static CounterRow carryForwardActiveBlock(CounterRow current, List<Long> activeBlocks) {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(activeBlocks, "activeBlocks");
        long blockedUntil = current.blockedUntil();
        for (Long candidate : activeBlocks) {
            if (candidate == null || candidate < 0L) {
                throw new IllegalStateException("Invalid active rate-limit block row");
            }
            blockedUntil = Math.max(blockedUntil, candidate);
        }
        if (blockedUntil == current.blockedUntil()) return current;
        return new CounterRow(current.used(), current.rejectedCount(), blockedUntil,
                current.version(), current.resetAt());
    }

    private List<CounterRow> selectForUpdate(CounterCommand command) {
        return jdbc.query(
                "SELECT used_units, rejected_count, blocked_until_ms, version, reset_at_ms FROM "
                        + COUNTER_TABLE
                        + " WHERE counter_key=? AND policy_version=? AND window_start_ms=? FOR UPDATE",
                (rs, rowNum) -> new CounterRow(
                        rs.getLong("used_units"), rs.getInt("rejected_count"),
                        rs.getLong("blocked_until_ms"), rs.getLong("version"),
                        rs.getLong("reset_at_ms")),
                command.counterKey(), command.policyVersion(), command.windowStartEpochMillis());
    }

    static CounterResult evaluate(CounterCommand command, CounterRow row) {
        long capacity = Math.addExact(command.quota(), command.burst());
        if (row.resetAt() != command.resetAtEpochMillis()) {
            throw new IllegalStateException("Rate-limit window metadata conflict");
        }
        if (row.blockedUntil() > command.nowEpochMillis()) {
            long effectiveReset = Math.max(row.resetAt(), row.blockedUntil());
            return new CounterResult(false, false, row.used(), Math.max(0L, capacity - row.used()),
                    effectiveReset, row.blockedUntil(), row.rejectedCount(), "ABUSE_BLOCKED");
        }
        long nextUsed = Math.addExact(row.used(), command.units());
        if (nextUsed > capacity) {
            int rejected = Math.addExact(row.rejectedCount(), 1);
            long blockedUntil = command.abuseThreshold() > 0 && rejected >= command.abuseThreshold()
                    ? Math.addExact(command.nowEpochMillis(), command.blockMillis()) : 0L;
            String reason = blockedUntil > command.nowEpochMillis() ? "ABUSE_BLOCKED" : "QUOTA_EXCEEDED";
            long effectiveReset = Math.max(row.resetAt(), blockedUntil);
            return new CounterResult(false, false, row.used(), Math.max(0L, capacity - row.used()),
                    effectiveReset, blockedUntil, rejected, reason);
        }
        return new CounterResult(true, false, nextUsed, Math.max(0L, capacity - nextUsed),
                row.resetAt(), 0L, row.rejectedCount(), "ALLOWED");
    }

    private void updateAccepted(CounterCommand command, CounterRow row, CounterResult result) {
        int updated = jdbc.update("UPDATE " + COUNTER_TABLE
                        + " SET used_units=?, blocked_until_ms=0, version=version+1 "
                        + "WHERE counter_key=? AND policy_version=? AND window_start_ms=? AND version=?",
                result.used(), command.counterKey(), command.policyVersion(),
                command.windowStartEpochMillis(), row.version());
        if (updated != 1) throw new IllegalStateException("Rate-limit counter CAS conflict");
    }

    private void updateRejected(CounterCommand command, CounterRow row, CounterResult result) {
        int updated = jdbc.update("UPDATE " + COUNTER_TABLE
                        + " SET rejected_count=?, blocked_until_ms=?, version=version+1 "
                        + "WHERE counter_key=? AND policy_version=? AND window_start_ms=? AND version=?",
                result.rejectedCount(), result.blockedUntilEpochMillis(), command.counterKey(),
                command.policyVersion(), command.windowStartEpochMillis(), row.version());
        if (updated != 1) throw new IllegalStateException("Rate-limit rejection CAS conflict");
    }

    private void insertJournal(CounterCommand command, CounterResult result, int limitingIndex) {
        jdbc.update("INSERT INTO " + REQUEST_TABLE
                        + " (counter_key, policy_version, window_start_ms, request_id, request_hash, "
                        + "accepted, used_units, remaining_units, reset_at_ms, blocked_until_ms, "
                        + "rejected_count, reason, limiting_index) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                command.counterKey(), command.policyVersion(), command.windowStartEpochMillis(),
                command.requestId(), requestHash(command), result.accepted() ? 1 : 0,
                result.used(), result.remaining(), result.resetAtEpochMillis(),
                result.blockedUntilEpochMillis(), result.rejectedCount(), result.reason(), limitingIndex);
    }

    static void validateJournalHash(CounterCommand command, String storedHash) {
        String expected = requestHash(command);
        String actual = storedHash == null ? "" : storedHash.trim();
        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                actual.getBytes(StandardCharsets.US_ASCII))) {
            throw new IllegalStateException("Rate-limit request payload conflict");
        }
    }

    static String requestHash(CounterCommand command) {
        Objects.requireNonNull(command, "command");
        String canonical = command.policyVersion() + "|" + command.counterKey() + "|"
                + command.requestId() + "|" + command.windowStartEpochMillis() + "|"
                + command.windowMillis() + "|" + command.quota() + "|" + command.burst() + "|"
                + command.units() + "|" + command.abuseThreshold() + "|"
                + command.blockMillis();
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    record CounterRow(long used, int rejectedCount, long blockedUntil, long version, long resetAt) {
    }

    private record JournalRow(
            boolean accepted, long used, long remaining, long resetAt, long blockedUntil,
            int rejectedCount, String reason, int limitingIndex, String requestHash) {
    }
}
