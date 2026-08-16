package com.cpf.education.operations.centercut;
import com.cpf.batch.api.CpfCenterCutOperationsExtension;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.security.api.CpfMasking;
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
 * EDU EDU 전용 Center-Cut 조회 확장.
 *
 * <p>플랫폼 ADM이 cpfDB를 직접 조회하지 않도록 EDU가 자기 DB ownership을 유지한 채
 * {@link CpfCenterCutOperationsExtension}을 통해 조회 기능만 노출한다.</p>
 */
@Component
public class EduCenterCutOperationsExtension implements CpfCenterCutOperationsExtension {

    private static final String JOB = "CPF_EDU_CENTER_CUT_SAMPLE_JOB";
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
    /** EduCenterCutOperationsExtension 작업을 CPF 표준 계약에 따라 수행한다. */
    public EduCenterCutOperationsExtension(
            @Qualifier("educationReferenceFixtureJdbcTemplate") JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this(jdbc, sqlCatalogProvider.forModule("ref"));
    }

    EduCenterCutOperationsExtension(JdbcTemplate jdbc, CpfVendorSqlCatalog sqlCatalog) {
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
        result.put("adapterType", "EDU_SAMPLE");
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
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
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
                    .map(EduCenterCutOperationsExtension::canonicalRow)
                    .toList();
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (DataAccessException ex) {
            throw new IllegalStateException("EDU Center-Cut query failed", ex);
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

    /** Statements 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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
