package com.cpf.common.parameter.service;

import com.cpf.common.parameter.api.CpfParameter;
import com.cpf.common.parameter.api.CpfParameterService;
import com.cpf.common.parameter.api.CpfParameterValueDecoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

/** CMN_PARAMETER Data JDBC implementation. Encrypted values fail closed when no decoder Provider exists. */
@Service
public final class JdbcCpfParameterService implements CpfParameterService {
    private static final String CACHE = "configCache";
    private final JdbcTemplate jdbc;
    private final CacheManager cacheManager;
    private final ObjectProvider<CpfParameterValueDecoder> decoder;

    public JdbcCpfParameterService(@Qualifier("cpfCommonJdbcTemplate") JdbcTemplate jdbc, CacheManager cacheManager,
                                   ObjectProvider<CpfParameterValueDecoder> decoder) {
        this.jdbc = jdbc; this.cacheManager = cacheManager; this.decoder = decoder;
    }

    @Override
    public Optional<CpfParameter> find(String key) {
        String normalized = normalize(key);
        Cache cache = requireCache();
        CpfParameter cached = cache.get("KEY:" + normalized, CpfParameter.class);
        if (cached != null) return Optional.of(cached);
        List<CpfParameter> rows = jdbc.query(
                "SELECT config_id,config_key,config_value,config_type,description,encrypted_yn,use_yn FROM CMN_PARAMETER WHERE config_key=? AND use_yn='Y'",
                (rs, i) -> {
                    boolean encrypted = "Y".equalsIgnoreCase(rs.getString("encrypted_yn"));
                    String stored = rs.getString("config_value");
                    String value = encrypted ? decode(normalized, stored) : stored;
                    return new CpfParameter(rs.getLong("config_id"), rs.getString("config_key"), rs.getString("config_type"),
                            value, rs.getString("description"), encrypted, true);
                }, normalized);
        if (rows.size() > 1) throw new IllegalStateException("CPF Common parameter key is not unique");
        if (rows.isEmpty()) return Optional.empty();
        cache.put("KEY:" + normalized, rows.get(0));
        return Optional.of(rows.get(0));
    }

    @Override public String requiredValue(String key) {
        return find(key).map(CpfParameter::value).orElseThrow(() -> new NoSuchElementException("CPF Common parameter not found"));
    }
    @Override public void refresh() { requireCache().clear(); }

    private String decode(String key, String stored) {
        CpfParameterValueDecoder selected = decoder.getIfUnique();
        if (selected == null) throw new IllegalStateException("Encrypted CPF Common parameter requires a single decoder Provider");
        return selected.decode(key, stored);
    }
    private Cache requireCache() {
        Cache cache = cacheManager.getCache(CACHE);
        if (cache == null) throw new IllegalStateException("CPF Common parameter cache is not configured");
        return cache;
    }
    private static String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("parameter key is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
