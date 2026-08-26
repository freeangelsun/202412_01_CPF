package com.cpf.common.runtime.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/** CMN_CACHE_REFRESH_EVENT/CHECKPOINT의 CPF Data JDBC repository입니다. */
@Repository
public class CpfCommonCacheRefreshEventRepository {
    private final JdbcTemplate jdbc;

    public CpfCommonCacheRefreshEventRepository(@Qualifier("cpfCommonJdbcTemplate") JdbcTemplate cpfCommonJdbcTemplate) {
        this.jdbc = cpfCommonJdbcTemplate;
    }

    /** Common 변경을 다중 인스턴스가 재생할 durable cache event로 기록하고 생성 ID를 반환합니다. */
    public long insertEvent(String cacheName, String eventType, String eventKey, String sourceWasId, String actor) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int changed = jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO CMN_CACHE_REFRESH_EVENT(cache_name,event_type,event_key,source_was_id,published_by,published_at,created_by,created_at,updated_by,updated_at) " +
                            "VALUES(?,?,?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, cacheName);
            ps.setString(2, eventType);
            ps.setString(3, eventKey);
            ps.setString(4, sourceWasId);
            ps.setString(5, actor);
            ps.setString(6, actor);
            ps.setString(7, actor);
            return ps;
        }, keyHolder);
        if (changed != 1 || keyHolder.getKey() == null) throw new IllegalStateException("Common cache refresh event persistence failed");
        return keyHolder.getKey().longValue();
    }

    /** 초기 bootstrap 시 full refresh 이후 기준점으로 사용할 현재 최대 event ID를 조회합니다. */
    public long maxEventId() {
        Long value = jdbc.queryForObject("SELECT COALESCE(MAX(event_id),0) FROM CMN_CACHE_REFRESH_EVENT", Long.class);
        return value == null ? 0L : value;
    }

    /** checkpoint 이후 event를 ID 오름차순으로 제한 조회하여 gap 없이 replay할 수 있게 합니다. */
    public List<Map<String, Object>> findAfter(long eventId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        long _ = eventId + safeLimit;
        return jdbc.queryForList(
                "SELECT event_id,cache_name,event_type,event_key,source_was_id,published_by,published_at FROM (" +
                        "SELECT e.*, ROW_NUMBER() OVER(ORDER BY event_id) cpf_rn FROM CMN_CACHE_REFRESH_EVENT e WHERE event_id>?" +
                        ") cpf_page WHERE cpf_rn<=? ORDER BY event_id",
                eventId, safeLimit);
    }

    /** 인스턴스별 마지막 성공 replay event ID를 조회하며 미등록이면 null을 반환합니다. */
    public Long checkpoint(String consumerId) {
        List<Long> rows = jdbc.query("SELECT last_event_id FROM CMN_CACHE_REFRESH_CHECKPOINT WHERE consumer_id=?",
                (rs, rowNum) -> rs.getLong(1), consumerId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 최초 full refresh 직후 checkpoint를 한 번 생성하며 동시 bootstrap은 중복키로 안전하게 수렴시킵니다. */
    public void establishCheckpoint(String consumerId, long eventId, String actor) {
        try {
            int changed = jdbc.update(
                    "INSERT INTO CMN_CACHE_REFRESH_CHECKPOINT(consumer_id,last_event_id,last_applied_at,created_by,created_at,updated_by,updated_at) " +
                            "VALUES(?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)",
                    consumerId, eventId, actor, actor);
            if (changed != 1) throw new IllegalStateException("Common cache checkpoint insert failed");
        // 여러 인스턴스가 동시에 최초 checkpoint를 만들면 이미 생성된 값을 확인해 정상 경쟁으로 처리합니다.
        } catch (DuplicateKeyException concurrentInsert) {
            if (checkpoint(consumerId) == null) throw concurrentInsert;
        }
    }

    /** cache refresh가 성공한 event에 대해서만 checkpoint를 단조 증가시켜 실패 event 재시도를 보장합니다. */
    public void advanceCheckpoint(String consumerId, long eventId, String actor) {
        int changed = jdbc.update(
                "UPDATE CMN_CACHE_REFRESH_CHECKPOINT SET last_event_id=?,last_applied_at=CURRENT_TIMESTAMP,updated_by=?,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE consumer_id=? AND last_event_id<?",
                eventId, actor, consumerId, eventId);
        if (changed == 0) {
            Long current = checkpoint(consumerId);
            if (current == null || current < eventId) throw new IllegalStateException("Common cache checkpoint advance failed");
        }
    }
}
