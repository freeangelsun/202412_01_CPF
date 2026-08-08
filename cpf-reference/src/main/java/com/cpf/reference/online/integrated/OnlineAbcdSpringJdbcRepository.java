package com.cpf.reference.online.integrated;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;
import java.util.Optional;

/**
 * Spring transaction-bound Connection을 사용하는 Online reference JDBC Repository입니다.
 * begin/commit/rollback은 Domain port 호환용 no-op이며 실제 commit/rollback은 PlatformTransactionManager가 소유합니다.
 */
public final class OnlineAbcdSpringJdbcRepository implements OnlineAbcdReferenceFlow.Repository {
    private final JdbcTemplate jdbc;
    private final String table;

    /**
     * Spring JdbcTemplate 기반 Repository를 생성합니다.
     * @param jdbc transaction-aware JdbcTemplate
     * @param table 검증된 reference table 이름
     * @throws NullPointerException jdbc가 null인 경우
     * @throws IllegalArgumentException table 이름이 allow 규칙을 벗어난 경우
     */
    public OnlineAbcdSpringJdbcRepository(JdbcTemplate jdbc, String table) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        if (table == null || !table.matches("[A-Za-z][A-Za-z0-9_]{0,62}")) {
            throw new IllegalArgumentException("safe table name required");
        }
        this.table = table;
    }

    /** @param key business key. Spring transaction은 외부 service가 시작합니다. */
    @Override public void begin(String key) { }

    /**
     * 현재 Spring transaction에서 값을 조회합니다.
     * @param key business key
     * @return 값이 있으면 Optional, 없으면 empty
     */
    @Override public Optional<String> find(String key) {
        return jdbc.query("SELECT value_text FROM " + table + " WHERE business_key=?",
                rs -> rs.next() ? Optional.ofNullable(rs.getString(1)) : Optional.empty(), key);
    }

    /**
     * 현재 Spring transaction에서 upsert합니다.
     * @param key business key
     * @param value 저장 값
     */
    @Override public void save(String key, String value) {
        int updated = jdbc.update("UPDATE " + table + " SET value_text=? WHERE business_key=?", value, key);
        if (updated == 0) jdbc.update("INSERT INTO " + table + " (business_key,value_text) VALUES (?,?)", key, value);
    }

    /** @param key business key. 실제 commit은 PlatformTransactionManager가 수행합니다. */
    @Override public void commit(String key) { }
    /** @param key business key. 실제 rollback은 rollback-only 상태로 PlatformTransactionManager가 수행합니다. */
    @Override public void rollback(String key) { }
}
