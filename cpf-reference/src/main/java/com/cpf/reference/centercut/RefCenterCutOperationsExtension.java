package com.cpf.reference.centercut;

import com.cpf.core.api.batch.CpfCenterCutOperationsExtension;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.core.api.security.CpfMasking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * REF EDU 전용 Center-Cut 조회 확장.
 *
 * <p>플랫폼 ADM이 refDB를 직접 조회하지 않도록 REF가 자기 DB ownership을 유지한 채
 * {@link CpfCenterCutOperationsExtension}을 통해 조회 기능만 노출한다.</p>
 */
@Component
public class RefCenterCutOperationsExtension implements CpfCenterCutOperationsExtension {

    private static final String JOB = "CPF_REF_CENTER_CUT_SAMPLE_JOB";
    private static final Map<String, String> CANONICAL_RESPONSE_KEYS = List.of(
                    "totalCount", "readyCount", "runningCount", "successCount", "failedCount",
                    "skippedCount", "retryRequestedCount", "stopRequestedCount", "lastStartedAt",
                    "lastCompletedAt", "lastCreatedAt", "targetId", "centerCutJobId", "businessKey",
                    "businessDate", "statusCode", "retryCount", "transactionId", "parentSegmentId",
                    "transactionSegmentId", "startedAt", "completedAt", "lastErrorMessage",
                    "targetPayloadLength", "resultId", "resultStatus", "resultMessage",
                    "resultPayloadLength", "createdAt", "updatedAt")
            .stream()
            .collect(Collectors.toUnmodifiableMap(
                    key -> key.toLowerCase(Locale.ROOT), Function.identity()));
    private final JdbcTemplate jdbc;
    private final Statements statements;

    @Autowired
    public RefCenterCutOperationsExtension(
            @Qualifier("refJdbcTemplate") JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this(jdbc, sqlCatalogProvider.forModule("ref"));
    }

    RefCenterCutOperationsExtension(JdbcTemplate jdbc, CpfVendorSqlCatalog sqlCatalog) {
        this.jdbc = jdbc;
        this.statements = Statements.load(sqlCatalog);
    }

    @Override
    public boolean supports(String centerCutJobId) {
        return JOB.equals(centerCutJobId);
    }

    @Override
    public Map<String, Object> findSummary(String centerCutJobId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("centerCutJobId", centerCutJobId);
        result.put("adapterType", "REF_SAMPLE");
        result.putAll(one(statements.summarizeTargets(), centerCutJobId));

        Map<String, Object> summary = one(statements.summarizeResults(), centerCutJobId);
        summary.forEach((key, value) -> result.put(
                "result" + Character.toUpperCase(key.charAt(0)) + key.substring(1), value));
        return result;
    }

    @Override
    public List<Map<String, Object>> findTargets(String centerCutJobId, String status, int limit) {
        String normalizedStatus = normalized(status);
        return maskedRows(
                statements.findTargets(),
                "lastErrorMessage",
                "targetPayloadLength",
                "targetPayloadMasked",
                centerCutJobId,
                normalizedStatus,
                normalizedStatus,
                limit);
    }

    @Override
    public List<Map<String, Object>> findResults(String centerCutJobId, String status, int limit) {
        String normalizedStatus = normalized(status);
        return maskedRows(
                statements.findResults(),
                "resultMessage",
                "resultPayloadLength",
                "resultPayloadMasked",
                centerCutJobId,
                normalizedStatus,
                normalizedStatus,
                limit);
    }

    @Override
    public Map<String, Object> findResultDetail(String resultId) {
        Long id;
        try {
            id = Long.valueOf(resultId);
        } catch (RuntimeException ex) {
            return Map.of();
        }
        Map<String, Object> result = one(statements.findResultDetail(), id);
        return result.isEmpty()
                ? Map.of()
                : maskRow(
                        result,
                        "resultMessage",
                        "resultPayloadLength",
                        "resultPayloadMasked");
    }

    private List<Map<String, Object>> query(String sql, Object... args) {
        try {
            return jdbc.queryForList(sql, args).stream()
                    .map(RefCenterCutOperationsExtension::canonicalRow)
                    .toList();
        } catch (DataAccessException ex) {
            throw new IllegalStateException("REF Center-Cut query failed", ex);
        }
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = query(sql, args);
        return rows.isEmpty() ? new LinkedHashMap<>() : new LinkedHashMap<>(rows.get(0));
    }

    private List<Map<String, Object>> maskedRows(
            String sql,
            String messageKey,
            String lengthKey,
            String maskedKey,
            Object... args) {
        return query(sql, args).stream()
                .map(row -> maskRow(row, messageKey, lengthKey, maskedKey))
                .toList();
    }

    private static Map<String, Object> maskRow(
            Map<String, Object> source,
            String messageKey,
            String lengthKey,
            String maskedKey) {
        LinkedHashMap<String, Object> masked = new LinkedHashMap<>(source);
        String actualMessageKey = keyIgnoreCase(masked, messageKey);
        if (actualMessageKey != null) {
            Object message = masked.get(actualMessageKey);
            masked.put(
                    actualMessageKey,
                    message == null ? null : CpfMasking.mask(String.valueOf(message), 1000));
        }
        String actualLengthKey = keyIgnoreCase(masked, lengthKey);
        if (actualLengthKey != null && masked.get(actualLengthKey) != null) {
            masked.put(maskedKey, "[MASKED payload length=" + masked.get(actualLengthKey) + "]");
        }
        return Collections.unmodifiableMap(masked);
    }

    private static Map<String, Object> canonicalRow(Map<String, Object> source) {
        LinkedHashMap<String, Object> canonical = new LinkedHashMap<>();
        source.forEach((key, value) -> canonical.put(
                CANONICAL_RESPONSE_KEYS.getOrDefault(key.toLowerCase(Locale.ROOT), key),
                value));
        return Collections.unmodifiableMap(canonical);
    }

    private static String keyIgnoreCase(Map<String, Object> values, String expected) {
        return values.keySet().stream()
                .filter(key -> key.equalsIgnoreCase(expected))
                .findFirst()
                .orElse(null);
    }

    private static String normalized(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }

    record Statements(
            String summarizeTargets,
            String summarizeResults,
            String findTargets,
            String findResults,
            String findResultDetail) {
        static Statements load(CpfVendorSqlCatalog catalog) {
            return new Statements(
                    catalog.required("centercut-operations-summarize-targets"),
                    catalog.required("centercut-operations-summarize-results"),
                    catalog.required("centercut-operations-find-targets"),
                    catalog.required("centercut-operations-find-results"),
                    catalog.required("centercut-operations-find-result-detail"));
        }
    }
}
