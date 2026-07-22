package com.cpf.core.common.transaction;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CPF 온라인 거래 메타 저장소입니다.
 *
 * <p>cpfDB가 아직 설치되지 않았거나 신규 테이블이 적용되지 않은 개발 환경에서도
 * 애플리케이션 기동을 막지 않도록 쓰기 계열은 실패를 호출자에게 전파하지 않습니다.
 * ADM 조회 API처럼 운영자가 직접 호출하는 기능은 조회 결과에 available 상태를 노출해
 * DB 적용 여부를 분명히 확인할 수 있게 합니다.</p>
 */
public class CpfTransactionMetaRepository {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<DataSource> dataSourceProvider;

    public CpfTransactionMetaRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("cpfDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.dataSourceProvider = dataSourceProvider;
    }

    public boolean available() {
        return jdbcTemplateProvider.getIfAvailable() != null || dataSourceProvider.getIfAvailable() != null;
    }

    public boolean tableAvailable() {
        if (!available()) {
            return false;
        }
        try {
            Integer count = jdbc().queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = DATABASE()
                      AND table_name = 'cpf_transaction_meta'
                    """, Integer.class);
            return count != null && count > 0;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    public int upsertAll(Collection<CpfTransactionMeta> metas, String requestUser) {
        if (!tableAvailable() || metas == null || metas.isEmpty()) {
            return 0;
        }
        String user = defaultIfBlank(requestUser, "CPF_TRANSACTION_SCAN");
        int count = 0;
        for (CpfTransactionMeta meta : metas) {
            jdbc().update("""
                    INSERT INTO cpf_transaction_meta (
                        transaction_id, transaction_name, module_code, domain_code,
                        http_method, api_path, controller_class, handler_method,
                        swagger_operation_id, log_policy_key, sensitive_yn, masking_policy_key,
                        active_yn, first_detected_at, last_detected_at, last_scanned_at,
                        created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Y', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), ?, ?)
                    ON DUPLICATE KEY UPDATE
                        transaction_name = VALUES(transaction_name),
                        module_code = VALUES(module_code),
                        domain_code = VALUES(domain_code),
                        http_method = VALUES(http_method),
                        api_path = VALUES(api_path),
                        controller_class = VALUES(controller_class),
                        handler_method = VALUES(handler_method),
                        swagger_operation_id = VALUES(swagger_operation_id),
                        log_policy_key = COALESCE(VALUES(log_policy_key), log_policy_key),
                        sensitive_yn = VALUES(sensitive_yn),
                        masking_policy_key = COALESCE(VALUES(masking_policy_key), masking_policy_key),
                        active_yn = 'Y',
                        last_detected_at = CURRENT_TIMESTAMP(3),
                        last_scanned_at = CURRENT_TIMESTAMP(3),
                        updated_by = VALUES(updated_by),
                        updated_at = CURRENT_TIMESTAMP
                    """,
                    required(meta.transactionId(), "transactionId"),
                    required(meta.transactionName(), "transactionName"),
                    defaultIfBlank(meta.moduleCode(), "CPF"),
                    blankToNull(meta.domainCode()),
                    defaultIfBlank(meta.httpMethod(), "ANY"),
                    defaultIfBlank(meta.apiPath(), "/"),
                    defaultIfBlank(meta.controllerClass(), "UNKNOWN"),
                    defaultIfBlank(meta.handlerMethod(), "UNKNOWN"),
                    blankToNull(meta.swaggerOperationId()),
                    blankToNull(meta.logPolicyKey()),
                    yn(meta.sensitiveYn(), "N"),
                    blankToNull(meta.maskingPolicyKey()),
                    user,
                    user);
            count++;
        }
        return count;
    }

    public int markMissingInactive(Collection<String> activeTransactionIds, String requestUser) {
        if (!tableAvailable() || activeTransactionIds == null || activeTransactionIds.isEmpty()) {
            return 0;
        }
        List<String> ids = activeTransactionIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
        List<Object> args = new ArrayList<>(ids);
        args.add(defaultIfBlank(requestUser, "CPF_TRANSACTION_SCAN"));
        return jdbc().update("""
                UPDATE cpf_transaction_meta
                SET active_yn = 'N',
                    last_scanned_at = CURRENT_TIMESTAMP(3),
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE active_yn = 'Y'
                  AND transaction_id NOT IN (%s)
                """.formatted(placeholders), rotateUserToFront(args).toArray());
    }

    public Optional<Map<String, Object>> findById(String transactionId) {
        if (!tableAvailable() || transactionId == null || transactionId.isBlank()) {
            return Optional.empty();
        }
        List<Map<String, Object>> rows = jdbc().queryForList("""
                SELECT transaction_id, transaction_name, module_code, domain_code, http_method, api_path,
                       controller_class, handler_method, swagger_operation_id, log_policy_key,
                       sensitive_yn, masking_policy_key, active_yn, first_detected_at, last_detected_at,
                       last_scanned_at, created_by, created_at, updated_by, updated_at
                FROM cpf_transaction_meta
                WHERE transaction_id = ?
                """, transactionId.trim());
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

    public List<Map<String, Object>> findAll(String moduleCode, String activeYn, String transactionId, int limit) {
        if (!tableAvailable()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT transaction_id, transaction_name, module_code, domain_code, http_method, api_path,
                       controller_class, handler_method, swagger_operation_id, log_policy_key,
                       sensitive_yn, masking_policy_key, active_yn, first_detected_at, last_detected_at,
                       last_scanned_at, created_by, created_at, updated_by, updated_at
                FROM cpf_transaction_meta
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        if (moduleCode != null && !moduleCode.isBlank()) {
            sql.append(" AND module_code = ?");
            args.add(moduleCode.trim().toUpperCase());
        }
        if (activeYn != null && !activeYn.isBlank()) {
            sql.append(" AND active_yn = ?");
            args.add(yn(activeYn, "Y"));
        }
        if (transactionId != null && !transactionId.isBlank()) {
            sql.append(" AND transaction_id LIKE ?");
            args.add("%" + transactionId.trim() + "%");
        }
        sql.append(" ORDER BY module_code, transaction_id LIMIT ?");
        args.add(Math.max(1, Math.min(limit, 1000)));
        return jdbc().queryForList(sql.toString(), args.toArray());
    }

    public Map<String, Object> inactivate(String transactionId, String requestUser) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<Map<String, Object>> before = findById(transactionId);
        int updated = 0;
        if (before.isPresent()) {
            updated = jdbc().update("""
                    UPDATE cpf_transaction_meta
                    SET active_yn = 'N',
                        updated_by = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE transaction_id = ?
                    """, defaultIfBlank(requestUser, "ADM"), transactionId.trim());
        }
        result.put("updated", updated);
        result.put("before", before.orElse(Map.of()));
        result.put("after", findById(transactionId).orElse(Map.of()));
        return result;
    }

    private JdbcTemplate jdbc() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("cpfDataSource 또는 cpfJdbcTemplate이 필요합니다.");
        }
        return new JdbcTemplate(dataSource);
    }

    private List<Object> rotateUserToFront(List<Object> argsWithUserLast) {
        List<Object> rotated = new ArrayList<>();
        Object user = argsWithUserLast.remove(argsWithUserLast.size() - 1);
        rotated.add(user);
        rotated.addAll(argsWithUserLast);
        return rotated;
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 값은 필수입니다.");
        }
        return value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String yn(String value, String fallback) {
        String normalized = defaultIfBlank(value, fallback).toUpperCase();
        return "Y".equals(normalized) ? "Y" : "N";
    }
}
