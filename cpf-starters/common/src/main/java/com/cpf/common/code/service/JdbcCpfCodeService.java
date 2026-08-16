package com.cpf.common.code.service;

import com.cpf.common.code.api.CpfCode;
import com.cpf.common.code.api.CpfCodeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** CMN_CODE Data JDBC implementation. DB miss/outage is never converted to a synthetic success. */
@Service
public final class JdbcCpfCodeService implements CpfCodeService {
    private static final String CACHE = "codeCache";
    private final JdbcTemplate jdbc;
    private final CacheManager cacheManager;

    public JdbcCpfCodeService(@Qualifier("cpfCommonJdbcTemplate") JdbcTemplate jdbc, CacheManager cacheManager) {
        this.jdbc = jdbc;
        this.cacheManager = cacheManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CpfCode> values(String group) {
        String key = normalize(group);
        Cache cache = requireCache();
        Cache.ValueWrapper cached = cache.get("GROUP:" + key);
        if (cached != null) return (List<CpfCode>) cached.get();
        List<CpfCode> rows = List.copyOf(jdbc.query(
                "SELECT code_id,parent_id,code_key,code_value,description,use_yn FROM CMN_CODE WHERE code_key=? AND use_yn='Y' ORDER BY code_id",
                (rs, i) -> new CpfCode(rs.getLong("code_id"), nullableLong(rs, "parent_id"), rs.getString("code_key"),
                        rs.getString("code_value"), rs.getString("description"), true), key));
        cache.put("GROUP:" + key, rows);
        return rows;
    }

    @Override
    public Optional<CpfCode> find(String group, String value) {
        String expected = normalize(value);
        return values(group).stream().filter(row -> row.value() != null && row.value().trim().toUpperCase(Locale.ROOT).equals(expected)).findFirst();
    }

    @Override public void refresh() { requireCache().clear(); }

    private Cache requireCache() {
        Cache cache = cacheManager.getCache(CACHE);
        if (cache == null) throw new IllegalStateException("CPF Common code cache is not configured");
        return cache;
    }
    private static Long nullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long v = rs.getLong(column); return rs.wasNull() ? null : v;
    }
    private static String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("code key/value is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
