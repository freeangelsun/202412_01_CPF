package com.cpf.common.cache;

import com.cpf.core.api.cache.CpfCacheInvalidationEvent;
import com.cpf.core.api.cache.CpfCacheInvalidationPort;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/** Redis 장애와 무관하게 무효화 명령을 보존하는 DB Durable 원장입니다. */
public final class CpfJdbcCacheInvalidationStore implements CpfCacheInvalidationPort {
    private final JdbcTemplate jdbc;

    public CpfJdbcCacheInvalidationStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public CpfCacheInvalidationEvent append(CpfCacheInvalidationEvent event) {
        try {
            jdbc.update("""
                    INSERT INTO cpf_cache_invalidation_event
                    (event_key,tenant_id,namespace_cd,cache_key,event_version,reason,requested_by,created_at)
                    VALUES (?,?,?,?,?,?,?,?)
                    """, event.eventKey(), event.tenantId(), event.namespace(), event.cacheKey(),
                    event.version(), event.reason(), event.requestedBy(), Timestamp.from(event.createdAt()));
        } catch (DuplicateKeyException duplicate) {
            // event_key idempotency: 동일 operation의 중복 요청은 기존 Event를 반환합니다.
        }
        return jdbc.queryForObject("""
                SELECT event_id,event_key,tenant_id,namespace_cd,cache_key,event_version,
                       reason,requested_by,created_at
                  FROM cpf_cache_invalidation_event WHERE event_key=?
                """, (rs, row) -> map(rs), event.eventKey());
    }

    @Override
    public List<CpfCacheInvalidationEvent> loadAfter(long checkpoint, int limit) {
        int bounded = Math.max(1, Math.min(limit, 2000));
        return jdbc.query("""
                SELECT event_id,event_key,tenant_id,namespace_cd,cache_key,event_version,
                       reason,requested_by,created_at
                  FROM cpf_cache_invalidation_event
                 WHERE event_id > ?
                 ORDER BY event_id
                """, ps -> {
                    ps.setLong(1, checkpoint);
                    ps.setMaxRows(bounded);
                }, (rs, row) -> map(rs));
    }

    @Override
    public long checkpoint(String consumerId) {
        Long value = jdbc.query("""
                SELECT last_event_id FROM cpf_cache_invalidation_checkpoint WHERE consumer_id=?
                """, ps -> ps.setString(1, consumerId),
                rs -> rs.next() ? rs.getLong(1) : 0L);
        return value == null ? 0L : value;
    }

    @Override
    public void checkpoint(String consumerId, long eventId) {
        int updated = jdbc.update("""
                UPDATE cpf_cache_invalidation_checkpoint
                   SET last_event_id=?,updated_at=?
                 WHERE consumer_id=? AND last_event_id < ?
                """, eventId, Timestamp.from(Instant.now()), consumerId, eventId);
        if (updated == 0) {
            try {
                jdbc.update("""
                        INSERT INTO cpf_cache_invalidation_checkpoint
                        (consumer_id,last_event_id,updated_at) VALUES (?,?,?)
                        """, consumerId, eventId, Timestamp.from(Instant.now()));
            } catch (DuplicateKeyException ignored) {
                jdbc.update("""
                        UPDATE cpf_cache_invalidation_checkpoint
                           SET last_event_id=?,updated_at=?
                         WHERE consumer_id=? AND last_event_id < ?
                        """, eventId, Timestamp.from(Instant.now()), consumerId, eventId);
            }
        }
    }

    @Override
    public long backlog(String consumerId) {
        Long max = jdbc.queryForObject("SELECT COALESCE(MAX(event_id),0) FROM cpf_cache_invalidation_event", Long.class);
        return Math.max(0, (max == null ? 0 : max) - checkpoint(consumerId));
    }

    private CpfCacheInvalidationEvent map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new CpfCacheInvalidationEvent(rs.getLong("event_id"), rs.getString("event_key"),
                rs.getString("tenant_id"), rs.getString("namespace_cd"), rs.getString("cache_key"),
                rs.getLong("event_version"), rs.getString("reason"), rs.getString("requested_by"),
                rs.getTimestamp("created_at").toInstant());
    }
}
