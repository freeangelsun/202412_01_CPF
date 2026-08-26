package com.cpf.platform.operations.observability.internal.tracking;

import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfException;
import com.cpf.core.api.tracking.CpfSubjectCandidate;
import com.cpf.core.api.tracking.CpfSubjectRole;
import com.cpf.core.api.tracking.CpfSubjectTrackingOperations;
import com.cpf.core.api.tracking.CpfSubjectTrustLevel;
import com.cpf.core.api.tracking.CpfSubjectType;
import com.cpf.platform.operations.observability.api.tracking.CpfSubjectTimelineQueryPort;
import com.cpf.security.api.crypto.CpfCryptoOperations;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Subject 원문을 저장하지 않고 deterministic protected search key로 transactionId와 연결합니다.
 * 동일 transaction/role/type의 다른 값은 silent replace하지 않고 충돌로 처리합니다.
 */
public final class JdbcCpfSubjectTrackingRepository implements CpfSubjectTrackingOperations, CpfSubjectTimelineQueryPort {
    static final String TABLE = "OPS_TRANSACTION_SUBJECT";
    private final JdbcTemplate jdbc;
    private final CpfCryptoOperations crypto;
    private final CpfSubjectTrackingProperties properties;
    private final Clock clock;

    public JdbcCpfSubjectTrackingRepository(JdbcTemplate jdbc, CpfCryptoOperations crypto,
            CpfSubjectTrackingProperties properties, Clock clock) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.crypto = Objects.requireNonNull(crypto, "crypto");
        this.properties = properties == null ? new CpfSubjectTrackingProperties() : properties;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @Override
    public void collect(String transactionId, Collection<CpfSubjectCandidate> candidates) {
        if (!properties.isEnabled()) return;
        String tx = required(transactionId, "transactionId", 128);
        if (candidates == null || candidates.isEmpty()) return;
        Map<String, CpfSubjectCandidate> strongest = strongestCandidates(candidates);
        for (CpfSubjectCandidate candidate : strongest.values()) bind(tx, candidate);
    }

    @Override
    public SearchResult findTransactions(SearchRequest request) {
        Objects.requireNonNull(request, "request");
        if (!properties.isEnabled()) return new SearchResult(false, mask(request.subjectType(), request.subjectId()), List.of(), request.limit(), "Subject tracking is disabled.");
        String canonical = normalize(request.subjectType(), request.subjectId());
        List<TokenVersion> tokens = searchTokens(request.subjectType(), canonical);
        if (tokens.isEmpty()) return new SearchResult(false, mask(request.subjectType(), canonical), List.of(), request.limit(), "No readable search-key version is configured.");
        String placeholders = String.join(",", tokens.stream().map(ignored -> "?").toList());
        StringBuilder sql = new StringBuilder("""
                SELECT s.transaction_id AS transactionId,
                       s.subject_role AS subjectRole,
                       s.subject_type AS subjectType,
                       s.subject_masked_value AS subjectMaskedValue,
                       s.source_type AS sourceType,
                       s.trust_level AS trustLevel,
                       s.search_key_version AS searchKeyVersion,
                       s.first_seen_at AS firstSeenAt,
                       s.last_seen_at AS lastSeenAt,
                       l.transaction_start_time AS transactionStartedAt
                  FROM OPS_TRANSACTION_SUBJECT s
                  JOIN (
                        SELECT TRANSACTION_ID, MIN(START_TIME) AS transaction_start_time
                          FROM CPF_TRANSACTION_LOG
                         GROUP BY TRANSACTION_ID
                       ) l ON l.TRANSACTION_ID = s.transaction_id
                 WHERE s.subject_role = ?
                   AND s.subject_type = ?
                   AND s.subject_search_key IN (""").append(placeholders).append(")");
        ArrayList<Object> args = new ArrayList<>();
        args.add(request.subjectRole().name());
        args.add(request.subjectType().name());
        tokens.forEach(token -> args.add(token.token()));
        List<CpfSubjectTrustLevel> acceptedTrust = java.util.Arrays.stream(CpfSubjectTrustLevel.values())
                .filter(level -> level.weight() >= request.minimumTrust().weight()).toList();
        if (acceptedTrust.isEmpty()) return new SearchResult(true, mask(request.subjectType(), canonical), List.of(), request.limit(), null);
        sql.append(" AND trust_level IN (")
                .append(String.join(",", acceptedTrust.stream().map(ignored -> "?").toList()))
                .append(")");
        acceptedTrust.forEach(level -> args.add(level.name()));
        if (request.from() != null) { sql.append(" AND l.transaction_start_time >= ?"); args.add(Timestamp.from(request.from())); }
        if (request.to() != null) { sql.append(" AND l.transaction_start_time <= ?"); args.add(Timestamp.from(request.to())); }
        sql.append(" ORDER BY l.transaction_start_time DESC, s.transaction_id DESC");
        try {
            List<Map<String,Object>> rows = queryLimited(sql.toString(), args, request.limit()).stream()
                    .map(this::safeSearchRow).toList();
            return new SearchResult(true, mask(request.subjectType(), canonical), rows, request.limit(), null);
        } catch (DataAccessException ex) {
            if (properties.isFailOnStoreUnavailable()) throw unavailable(ex);
            return new SearchResult(false, mask(request.subjectType(), canonical), List.of(), request.limit(), "Subject tracking store is unavailable.");
        }
    }

    private void bind(String transactionId, CpfSubjectCandidate candidate) {
        String canonical = normalize(candidate.subjectType(), candidate.subjectId());
        String keyVersion = required(crypto.activeKeyVersion(), "activeKeyVersion", 64);
        String token = token(candidate.subjectType(), canonical, keyVersion);
        Instant now = clock.instant();
        try {
            List<Map<String,Object>> existing = jdbc.queryForList("""
                    SELECT subject_search_key AS subjectSearchKey,
                           trust_level AS trustLevel,
                           source_type AS sourceType
                      FROM OPS_TRANSACTION_SUBJECT
                     WHERE transaction_id = ? AND subject_role = ? AND subject_type = ?
                    """, transactionId, candidate.subjectRole().name(), candidate.subjectType().name());
            if (!existing.isEmpty()) {
                String existingToken = text(existing.get(0), "subjectSearchKey");
                if (!constantEquals(existingToken, token)) throw conflict(candidate.subjectType(), candidate.subjectRole());
                String trust = strongerTrust(text(existing.get(0), "trustLevel"), candidate.trustLevel());
                String source = trust.equals(candidate.trustLevel().name()) ? candidate.sourceType().name() : text(existing.get(0), "sourceType");
                jdbc.update("""
                        UPDATE OPS_TRANSACTION_SUBJECT
                           SET last_seen_at = ?, trust_level = ?, source_type = ?, updated_at = ?
                         WHERE transaction_id = ? AND subject_role = ? AND subject_type = ?
                        """, Timestamp.from(now), trust, source, Timestamp.from(now), transactionId,
                        candidate.subjectRole().name(), candidate.subjectType().name());
                return;
            }
            try {
                jdbc.update("""
                        INSERT INTO OPS_TRANSACTION_SUBJECT
                        (transaction_id, subject_role, subject_type, subject_search_key, subject_masked_value,
                         source_type, trust_level, search_key_version, first_seen_at, last_seen_at, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, transactionId, candidate.subjectRole().name(), candidate.subjectType().name(), token,
                        mask(candidate.subjectType(), canonical), candidate.sourceType().name(), candidate.trustLevel().name(),
                        keyVersion, Timestamp.from(now), Timestamp.from(now), Timestamp.from(now), Timestamp.from(now));
            } catch (DuplicateKeyException concurrent) {
                // Same transaction/role/type concurrent bind is resolved by re-reading the canonical row.
                bind(transactionId, candidate);
            }
        } catch (CpfException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            if (properties.isFailOnStoreUnavailable()) throw unavailable(ex);
        }
    }

    private Map<String,CpfSubjectCandidate> strongestCandidates(Collection<CpfSubjectCandidate> candidates) {
        LinkedHashMap<String,CpfSubjectCandidate> result = new LinkedHashMap<>();
        for (CpfSubjectCandidate candidate : candidates) {
            if (candidate == null) continue;
            String canonical = normalize(candidate.subjectType(), candidate.subjectId());
            String key = candidate.subjectRole().name() + ':' + candidate.subjectType().name();
            CpfSubjectCandidate previous = result.get(key);
            if (previous == null) {
                result.put(key, candidate);
                continue;
            }
            String previousCanonical = normalize(previous.subjectType(), previous.subjectId());
            if (!previousCanonical.equals(canonical)) throw conflict(candidate.subjectType(), candidate.subjectRole());
            if (candidate.trustLevel().weight() > previous.trustLevel().weight()) result.put(key, candidate);
        }
        return result;
    }

    private List<TokenVersion> searchTokens(CpfSubjectType type, String canonical) {
        LinkedHashSet<String> versions = new LinkedHashSet<>();
        versions.add(crypto.activeKeyVersion());
        versions.addAll(properties.getReadableKeyVersions());
        ArrayList<TokenVersion> result = new ArrayList<>();
        for (String version : versions) {
            if (version == null || version.isBlank()) continue;
            result.add(new TokenVersion(version.trim(), token(type, canonical, version.trim())));
        }
        return result;
    }

    private String token(CpfSubjectType type, String canonical, String keyVersion) {
        byte[] bytes = (type.name() + ':' + canonical).getBytes(StandardCharsets.UTF_8);
        return crypto.searchableToken(bytes, keyVersion);
    }

    static String normalize(CpfSubjectType type, String value) {
        String v = required(value, "subjectId", 256);
        if (!v.equals(value)) throw new IllegalArgumentException("subjectId must not contain surrounding whitespace");
        boolean valid = switch (type) {
            case CUSTOMER_NO -> v.matches("[0-9]{1,32}");
            case CUSTOMER_ID, MEMBER_NO -> v.matches("[A-Za-z0-9._-]{1,64}");
            case LOGIN_ID -> v.matches("[A-Za-z0-9@._+-]{1,128}");
        };
        if (!valid) throw new IllegalArgumentException("subjectId does not match canonical " + type + " format");
        return v;
    }

    static String mask(CpfSubjectType type, String value) {
        String v = value == null ? "" : value;
        if (v.length() <= 2) return "**";
        int prefix = Math.min(type == CpfSubjectType.CUSTOMER_NO ? 4 : 2, Math.max(1, v.length() - 2));
        int suffix = Math.min(2, v.length() - prefix);
        return v.substring(0, prefix) + "****" + v.substring(v.length() - suffix);
    }

    private List<Map<String,Object>> queryLimited(String sql, List<Object> args, int limit) {
        return jdbc.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setMaxRows(limit);
            for (int i = 0; i < args.size(); i++) ps.setObject(i + 1, args.get(i));
            return ps;
        }, (rs, rowNum) -> {
            LinkedHashMap<String,Object> row = new LinkedHashMap<>();
            var md = rs.getMetaData();
            for (int i=1;i<=md.getColumnCount();i++) row.put(md.getColumnLabel(i), rs.getObject(i));
            return Map.copyOf(row);
        });
    }

    private Map<String,Object> safeSearchRow(Map<String,Object> source) {
        LinkedHashMap<String,Object> row = new LinkedHashMap<>(source);
        // Search-key/token is intentionally never projected to ADM callers.
        row.remove("subjectSearchKey");
        return Map.copyOf(row);
    }

    private static String strongerTrust(String existing, CpfSubjectTrustLevel incoming) {
        CpfSubjectTrustLevel old;
        try { old = CpfSubjectTrustLevel.valueOf(existing == null ? "UNVERIFIED" : existing.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { old = CpfSubjectTrustLevel.UNVERIFIED; }
        return incoming.weight() > old.weight() ? incoming.name() : old.name();
    }

    private static CpfException conflict(CpfSubjectType type, CpfSubjectRole role) {
        return new CpfException(CpfErrorCode.CONFLICT, "Subject identity conflict",
                Map.of("subjectType", type.name(), "subjectRole", role.name()));
    }

    private static CpfException unavailable(Throwable cause) {
        return new CpfException(CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                "Subject tracking store unavailable", cause, Map.of());
    }

    private static boolean constantEquals(String left, String right) {
        if (left == null || right == null) return false;
        return java.security.MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    private static String required(String value, String name, int max) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        String v = value.strip();
        if (v.length() > max) throw new IllegalArgumentException(name + " exceeds " + max + " characters");
        return v;
    }

    private static String text(Map<String,Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            for (Map.Entry<String,Object> entry : row.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) { value = entry.getValue(); break; }
            }
        }
        return value == null ? "" : String.valueOf(value);
    }

    private record TokenVersion(String version, String token) { }
}
