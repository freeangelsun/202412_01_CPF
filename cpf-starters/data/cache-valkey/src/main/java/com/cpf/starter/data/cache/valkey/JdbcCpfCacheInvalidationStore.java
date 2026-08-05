package com.cpf.starter.data.cache.valkey;

import com.cpf.core.api.cache.CpfCacheInvalidationEvent;
import com.cpf.core.api.cache.CpfCacheInvalidationPort;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/** JDBC implementation of the durable cache invalidation ledger and per-instance checkpoint. */
public final class JdbcCpfCacheInvalidationStore implements CpfCacheInvalidationPort {
    private enum Dialect { ORACLE, POSTGRESQL, MARIADB }

    private static final String SELECT_COLUMNS = "EVENT_ID, EVENT_KEY, TENANT_ID, NAMESPACE_NAME, "
            + "CACHE_KEY_VALUE, EVENT_VERSION, REASON_TEXT, REQUESTED_BY, CREATED_AT";
    private final JdbcTemplate jdbc;
    private final Dialect dialect;

    public JdbcCpfCacheInvalidationStore(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.dialect = detectDialect(jdbc);
    }

    @Override
    public CpfCacheInvalidationEvent append(CpfCacheInvalidationEvent event) {
        Objects.requireNonNull(event, "event");
        CpfCacheInvalidationEvent existing = findByEventKey(event.eventKey());
        if (existing != null) {
            assertSameRequest(existing, event);
            return existing;
        }
        KeyHolder keys = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO CPF_CACHE_INVALIDATION_EVENT "
                                + "(EVENT_KEY, TENANT_ID, NAMESPACE_NAME, CACHE_KEY_VALUE, EVENT_VERSION, "
                                + "REASON_TEXT, REQUESTED_BY, CREATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[] {"EVENT_ID"});
                statement.setString(1, event.eventKey());
                statement.setString(2, event.tenantId());
                statement.setString(3, event.namespace());
                if (event.cacheKey().isBlank()) {
                    statement.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    statement.setString(4, event.cacheKey());
                }
                statement.setLong(5, event.version());
                statement.setString(6, event.reason());
                statement.setString(7, event.requestedBy());
                statement.setTimestamp(8, Timestamp.from(event.createdAt()));
                return statement;
            }, keys);
        } catch (DataIntegrityViolationException duplicate) {
            CpfCacheInvalidationEvent concurrent = findByEventKey(event.eventKey());
            if (concurrent != null) {
                assertSameRequest(concurrent, event);
                return concurrent;
            }
            throw duplicate;
        }
        Number generated = keys.getKey();
        if (generated == null) {
            CpfCacheInvalidationEvent inserted = findByEventKey(event.eventKey());
            if (inserted == null) {
                throw new IllegalStateException("cache invalidation insert did not return or persist an event id");
            }
            return inserted;
        }
        return new CpfCacheInvalidationEvent(
                generated.longValue(), event.eventKey(), event.tenantId(), event.namespace(),
                event.cacheKey(), event.version(), event.reason(), event.requestedBy(), event.createdAt());
    }

    @Override
    public List<CpfCacheInvalidationEvent> loadAfter(long checkpoint, int limit) {
        if (checkpoint < 0 || limit < 1 || limit > 10_000) {
            throw new IllegalArgumentException("checkpoint/limit contract is invalid");
        }
        String pagination = dialect == Dialect.MARIADB ? " LIMIT ?" : " FETCH FIRST ? ROWS ONLY";
        return jdbc.query(
                "SELECT " + SELECT_COLUMNS + " FROM CPF_CACHE_INVALIDATION_EVENT "
                        + "WHERE EVENT_ID > ? ORDER BY EVENT_ID" + pagination,
                (rs, row) -> map(rs), checkpoint, limit);
    }

    @Override
    public long checkpoint(String consumerId) {
        List<Long> values = jdbc.query(
                "SELECT LAST_EVENT_ID FROM CPF_CACHE_INVALIDATION_CHECKPOINT WHERE CONSUMER_ID = ?",
                (rs, row) -> rs.getLong(1), requiredConsumer(consumerId));
        return values.isEmpty() ? 0L : values.get(0);
    }

    @Override
    public void checkpoint(String consumerId, long eventId) {
        String consumer = requiredConsumer(consumerId);
        if (eventId < 0) {
            throw new IllegalArgumentException("eventId must not be negative");
        }
        int updated = jdbc.update(
                "UPDATE CPF_CACHE_INVALIDATION_CHECKPOINT SET LAST_EVENT_ID = ?, UPDATED_AT = CURRENT_TIMESTAMP "
                        + "WHERE CONSUMER_ID = ? AND LAST_EVENT_ID < ?",
                eventId, consumer, eventId);
        if (updated > 0 || checkpoint(consumer) >= eventId) {
            return;
        }
        try {
            jdbc.update(
                    "INSERT INTO CPF_CACHE_INVALIDATION_CHECKPOINT "
                            + "(CONSUMER_ID, LAST_EVENT_ID, UPDATED_AT) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    consumer, eventId);
        } catch (DataIntegrityViolationException race) {
            jdbc.update(
                    "UPDATE CPF_CACHE_INVALIDATION_CHECKPOINT SET LAST_EVENT_ID = ?, UPDATED_AT = CURRENT_TIMESTAMP "
                            + "WHERE CONSUMER_ID = ? AND LAST_EVENT_ID < ?",
                    eventId, consumer, eventId);
        }
    }

    @Override
    public long backlog(String consumerId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM CPF_CACHE_INVALIDATION_EVENT WHERE EVENT_ID > ?",
                Long.class, checkpoint(consumerId));
        return count == null ? 0 : count;
    }

    private CpfCacheInvalidationEvent findByEventKey(String eventKey) {
        List<CpfCacheInvalidationEvent> matches = jdbc.query(
                "SELECT " + SELECT_COLUMNS + " FROM CPF_CACHE_INVALIDATION_EVENT WHERE EVENT_KEY = ?",
                (rs, row) -> map(rs), eventKey);
        return matches.isEmpty() ? null : matches.get(0);
    }

    private CpfCacheInvalidationEvent map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp created = rs.getTimestamp("CREATED_AT");
        if (created == null) {
            throw new IllegalStateException("cache invalidation CREATED_AT must not be null");
        }
        return new CpfCacheInvalidationEvent(
                rs.getLong("EVENT_ID"), rs.getString("EVENT_KEY"), rs.getString("TENANT_ID"),
                rs.getString("NAMESPACE_NAME"), rs.getString("CACHE_KEY_VALUE"),
                rs.getLong("EVENT_VERSION"), rs.getString("REASON_TEXT"),
                rs.getString("REQUESTED_BY"), created.toInstant());
    }

    private static void assertSameRequest(
            CpfCacheInvalidationEvent persisted,
            CpfCacheInvalidationEvent requested) {
        boolean same = persisted.tenantId().equals(requested.tenantId())
                && persisted.namespace().equals(requested.namespace())
                && persisted.cacheKey().equals(requested.cacheKey())
                && persisted.version() == requested.version()
                && persisted.reason().equals(requested.reason())
                && persisted.requestedBy().equals(requested.requestedBy());
        if (!same) {
            throw new IllegalStateException(
                    "cache invalidation eventKey conflict: same key was submitted with a different payload");
        }
    }

    private static Dialect detectDialect(JdbcTemplate jdbc) {
        if (jdbc.getDataSource() == null) {
            throw new IllegalStateException("CPF cache invalidation requires a DataSource");
        }
        try (var connection = jdbc.getDataSource().getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            String product = meta.getDatabaseProductName().toLowerCase(Locale.ROOT);
            if (product.contains("oracle")) return Dialect.ORACLE;
            if (product.contains("postgresql")) return Dialect.POSTGRESQL;
            if (product.contains("mariadb")) return Dialect.MARIADB;
            throw new IllegalStateException("Unsupported CPF database vendor: " + meta.getDatabaseProductName());
        } catch (java.sql.SQLException failure) {
            throw new IllegalStateException("Unable to detect CPF database vendor", failure);
        }
    }

    private static String requiredConsumer(String consumerId) {
        String normalized = Objects.requireNonNull(consumerId, "consumerId").trim();
        if (normalized.isEmpty() || normalized.length() > 180
                || !normalized.matches("[A-Za-z0-9._:-]+")) {
            throw new IllegalArgumentException("consumerId format is invalid");
        }
        return normalized;
    }
}
