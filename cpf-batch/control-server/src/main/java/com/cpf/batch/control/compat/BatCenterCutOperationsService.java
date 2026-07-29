package com.cpf.batch.control.compat;

import com.cpf.core.api.batch.CpfCenterCutOperationsPort;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.core.api.security.CpfMasking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * BAT 표준 Center-Cut 테이블의 조회 전용 Owner 구현입니다.
 *
 * <p>업무 Domain/REF extension 저장소를 추측하지 않으며 DB 장애를 빈 결과로 변환하지 않습니다.
 * Payload 원문은 조회 SQL에서 가져오지 않고 길이와 마스킹 표지만 반환합니다.</p>
 */
@Service
public final class BatCenterCutOperationsService implements CpfCenterCutOperationsPort {
    static final int DEFAULT_LIMIT = 100;
    static final int MAX_LIMIT = 500;
    private static final Map<String, String> CANONICAL_RESPONSE_KEYS = List.of(
                    "centerCutJobId",
                    "batchJobId",
                    "centerCutJobName",
                    "providerKey",
                    "handlerKey",
                    "chunkSize",
                    "retryLimit",
                    "useYn",
                    "description",
                    "createdAt",
                    "updatedAt",
                    "batchJobName",
                    "batchJobType",
                    "parameterId",
                    "parameterKey",
                    "parameterValue",
                    "encryptedYn",
                    "totalCount",
                    "readyCount",
                    "runningCount",
                    "successCount",
                    "failedCount",
                    "skippedCount",
                    "retryRequestedCount",
                    "stopRequestedCount",
                    "lastStartedAt",
                    "lastCompletedAt",
                    "lastCreatedAt",
                    "targetId",
                    "executionId",
                    "businessKey",
                    "businessDate",
                    "statusCode",
                    "retryCount",
                    "transactionId",
                    "parentSegmentId",
                    "transactionSegmentId",
                    "startedAt",
                    "completedAt",
                    "lastErrorMessage",
                    "targetPayloadLength",
                    "resultId",
                    "resultStatus",
                    "resultMessage",
                    "resultPayloadLength")
            .stream()
            .collect(Collectors.toUnmodifiableMap(
                    key -> key.toLowerCase(Locale.ROOT),
                    Function.identity()));

    private final JdbcTemplate jdbc;
    private final Statements statements;

    @Autowired
    public BatCenterCutOperationsService(
            @Qualifier("batJdbcTemplate") JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this(jdbc, sqlCatalogProvider.forModule("bat"));
    }

    BatCenterCutOperationsService(JdbcTemplate jdbc, CpfVendorSqlCatalog catalog) {
        this.jdbc = jdbc;
        this.statements = Statements.load(catalog);
    }

    @Override
    public List<Map<String, Object>> findJobs() {
        return rows(statements.findJobs());
    }

    @Override
    public Map<String, Object> findJobDetail(String centerCutJobId) {
        String jobId = required(centerCutJobId, "centerCutJobId");
        Map<String, Object> job = one(statements.findJobDetail(), jobId);
        if (job.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Object> detail = new LinkedHashMap<>();
        detail.put("job", job);
        detail.put("parameters", findParameters(jobId));
        detail.put("summary", findSummary(jobId));
        detail.put("targets", findTargets(jobId, null, DEFAULT_LIMIT));
        detail.put("results", findResults(jobId, null, DEFAULT_LIMIT));
        return detail;
    }

    @Override
    public List<Map<String, Object>> findParameters(String centerCutJobId) {
        return rows(
                statements.findParameters(),
                required(centerCutJobId, "centerCutJobId"));
    }

    @Override
    public Map<String, Object> findSummary(String centerCutJobId) {
        String jobId = required(centerCutJobId, "centerCutJobId");
        LinkedHashMap<String, Object> summary = new LinkedHashMap<>();
        summary.put("centerCutJobId", jobId);
        summary.put("adapterType", "BAT_STANDARD");
        summary.putAll(one(statements.summarizeItems(), jobId));
        one(statements.summarizeResults(), jobId).forEach((key, value) ->
                summary.put("result" + Character.toUpperCase(key.charAt(0)) + key.substring(1), value));
        return summary;
    }

    @Override
    public List<Map<String, Object>> findTargets(
            String centerCutJobId,
            String statusCode,
            int limit) {
        String status = normalized(statusCode);
        return maskedRows(
                statements.findTargets(),
                "lastErrorMessage",
                "targetPayloadLength",
                "targetPayloadMasked",
                required(centerCutJobId, "centerCutJobId"),
                status,
                status,
                boundedLimit(limit));
    }

    @Override
    public List<Map<String, Object>> findResults(
            String centerCutJobId,
            String resultStatus,
            int limit) {
        String status = normalized(resultStatus);
        return maskedRows(
                statements.findResults(),
                "resultMessage",
                "resultPayloadLength",
                "resultPayloadMasked",
                required(centerCutJobId, "centerCutJobId"),
                status,
                status,
                boundedLimit(limit));
    }

    @Override
    public Map<String, Object> findResultDetail(String resultId) {
        String value = required(resultId, "resultId");
        long id;
        try {
            id = Long.parseLong(value);
        } catch (NumberFormatException failure) {
            throw new IllegalArgumentException("resultId must be a positive number", failure);
        }
        if (id < 1) {
            throw new IllegalArgumentException("resultId must be a positive number");
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

    private List<Map<String, Object>> rows(String sql, Object... parameters) {
        List<Map<String, Object>> queried = parameters.length == 0
                ? jdbc.queryForList(sql)
                : jdbc.queryForList(sql, parameters);
        return queried.stream()
                .map(BatCenterCutOperationsService::canonicalRow)
                .toList();
    }

    private Map<String, Object> one(String sql, Object... parameters) {
        List<Map<String, Object>> found = rows(sql, parameters);
        return found.isEmpty() ? Map.of() : found.getFirst();
    }

    private List<Map<String, Object>> maskedRows(
            String sql,
            String messageKey,
            String lengthKey,
            String maskedKey,
            Object... parameters) {
        return rows(sql, parameters).stream()
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
                CANONICAL_RESPONSE_KEYS.getOrDefault(
                        key.toLowerCase(Locale.ROOT),
                        key),
                value));
        return Collections.unmodifiableMap(canonical);
    }

    private static String keyIgnoreCase(Map<String, Object> values, String expected) {
        return values.keySet().stream()
                .filter(key -> key.equalsIgnoreCase(expected))
                .findFirst()
                .orElse(null);
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static String normalized(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private static int boundedLimit(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
        return limit;
    }

    record Statements(
            String findJobs,
            String findJobDetail,
            String findParameters,
            String summarizeItems,
            String summarizeResults,
            String findTargets,
            String findResults,
            String findResultDetail) {
        static Statements load(CpfVendorSqlCatalog catalog) {
            return new Statements(
                    catalog.required("centercut-operations-find-jobs"),
                    catalog.required("centercut-operations-find-job-detail"),
                    catalog.required("centercut-operations-find-parameters"),
                    catalog.required("centercut-operations-summarize-items"),
                    catalog.required("centercut-operations-summarize-results"),
                    catalog.required("centercut-operations-find-targets"),
                    catalog.required("centercut-operations-find-results"),
                    catalog.required("centercut-operations-find-result-detail"));
        }
    }
}
