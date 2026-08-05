package com.cpf.batch.execution;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * DB Unique Key와 Lease를 사용해 Kafka Remote Message의 중복 실행과 Replay를 차단합니다.
 */
public final class JdbcCpfBatchRemoteMessageLedger implements CpfBatchRemoteMessageLedger {
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final long leaseSeconds;
    private final CpfVendorSqlCatalog sql;

    public JdbcCpfBatchRemoteMessageLedger(
            JdbcTemplate jdbc, long leaseSeconds, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this(jdbc, Clock.systemUTC(), leaseSeconds, sqlCatalogProvider.forModule("bat"));
    }

    JdbcCpfBatchRemoteMessageLedger(
            JdbcTemplate jdbc, Clock clock, long leaseSeconds, CpfVendorSqlCatalog sql) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.leaseSeconds = Math.max(10, leaseSeconds);
        this.sql = sql;
    }

    @Override
    public Claim claim(
            String direction,
            String messageId,
            String payloadHash,
            Instant expiresAt,
            String ownerId) {
        Instant now = clock.instant();
        if (!expiresAt.isAfter(now)) {
            throw new SecurityException("BATCH_REMOTE_MESSAGE_EXPIRED");
        }
        try {
            jdbc.update(sql.required("execution-remote-message-insert"),
                    direction,
                    messageId,
                    payloadHash,
                    "PROCESSING",
                    ownerId,
                    Timestamp.from(now.plusSeconds(leaseSeconds)),
                    Timestamp.from(expiresAt),
                    1,
                    Timestamp.from(now),
                    Timestamp.from(now));
            return Claim.CLAIMED;
        } catch (DuplicateKeyException duplicate) {
            State state = jdbc.query(sql.required("execution-remote-message-find"), rs -> {
                        if (!rs.next()) {
                            throw new IllegalStateException("BATCH_REMOTE_LEDGER_DUPLICATE_LOST");
                        }
                        return new State(
                                rs.getString(1),
                                rs.getString(2),
                                rs.getString(3),
                                rs.getTimestamp(4).toInstant(),
                                rs.getTimestamp(5).toInstant(),
                                rs.getLong(6));
                    }, direction, messageId);

            if (!payloadHash.equals(state.payloadHash())) {
                throw new SecurityException("BATCH_REMOTE_MESSAGE_ID_COLLISION");
            }
            if (!state.expiresAt().isAfter(now)) {
                throw new SecurityException("BATCH_REMOTE_MESSAGE_REPLAY_EXPIRED");
            }
            if ("COMPLETE".equals(state.status())) {
                return Claim.DUPLICATE_COMPLETE;
            }
            if ("UNKNOWN".equals(state.status())) {
                return Claim.UNKNOWN_RECONCILE_REQUIRED;
            }
            // 같은 Instance에 재전달된 경우도 Lease가 살아 있으면 병렬 실행하지 않습니다.
            if (state.leaseUntil().isAfter(now)) {
                return Claim.IN_PROGRESS;
            }
            int updated = jdbc.update(sql.required("execution-remote-message-reclaim"),
                    ownerId,
                    Timestamp.from(now.plusSeconds(leaseSeconds)),
                    Timestamp.from(now),
                    direction,
                    messageId,
                    state.version());
            return updated == 1 ? Claim.CLAIMED : Claim.IN_PROGRESS;
        }
    }

    @Override
    public void complete(String direction, String messageId, String ownerId) {
        transition(direction, messageId, ownerId, "COMPLETE", null);
    }

    @Override
    public void fail(String direction, String messageId, String ownerId, String errorCode) {
        transition(direction, messageId, ownerId, "FAILED", sanitize(errorCode));
    }

    @Override
    public void unknown(String direction, String messageId, String ownerId, String errorCode) {
        transition(direction, messageId, ownerId, "UNKNOWN", sanitize(errorCode));
    }

    private void transition(
            String direction,
            String messageId,
            String ownerId,
            String status,
            String error) {
        Instant now = clock.instant();
        int updated = jdbc.update(sql.required("execution-remote-message-transition"),
                status,
                error,
                Timestamp.from(now),
                Timestamp.from(now),
                direction,
                messageId,
                ownerId);
        if (updated != 1) {
            throw new IllegalStateException("BATCH_REMOTE_LEDGER_FENCE_CONFLICT");
        }
    }

    private static String sanitize(String code) {
        if (code == null) {
            return null;
        }
        String safe = code.replaceAll("[^A-Za-z0-9_.:-]", "_");
        return safe.length() > 100 ? safe.substring(0, 100) : safe;
    }

    private record State(
            String payloadHash,
            String status,
            String owner,
            Instant leaseUntil,
            Instant expiresAt,
            long version) {
    }
}
