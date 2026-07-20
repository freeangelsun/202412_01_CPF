package cpf.adm.opr.service;

import cpf.adm.opr.dto.AdmLogPolicyOverrideRequest;
import cpf.adm.opr.dto.AdmLogPolicyRequest;
import cpf.adm.opr.dto.AdmTraceBoostRequest;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.CpfValidationException;
import cpf.pfw.common.logging.policy.LogPolicyDecision;
import cpf.pfw.common.logging.policy.LogPolicyResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ADM 로그 정책 관리 서비스입니다.
 *
 * <p>기본 정책과 임시 override를 DB 기준으로 관리하고, 현재 인스턴스의
 * 런타임 로그 정책 cache evict/refresh까지 연결합니다. 다중 인스턴스 broker 전파는
 * 별도 운영 보강에서 확장합니다.</p>
 */
@Service
public class AdmLogPolicyService extends cpf.adm.common.base.AdmBaseService {
    private final JdbcTemplate pfwJdbcTemplate;
    private final ObjectProvider<LogPolicyResolver> logPolicyResolverProvider;

    public AdmLogPolicyService(
            @Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate,
            ObjectProvider<LogPolicyResolver> logPolicyResolverProvider) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
        this.logPolicyResolverProvider = logPolicyResolverProvider;
    }

    public Map<String, Object> findPolicies(String targetType, String targetId, String activeYn, int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", tableAvailable("pfw_log_policy"));
        response.put("items", tableAvailable("pfw_log_policy")
                ? queryPolicies(targetType, targetId, activeYn, limit)
                : List.of());
        return response;
    }

    public Map<String, Object> findPolicy(long policyId) {
        Map<String, Object> response = new LinkedHashMap<>();
        boolean policyAvailable = tableAvailable("pfw_log_policy");
        boolean overrideAvailable = tableAvailable("pfw_log_policy_override");
        response.put("available", policyAvailable);
        response.put("item", policyAvailable ? findPolicyById(policyId).orElse(Map.of()) : Map.of());
        response.put("overrides", overrideAvailable
                ? pfwJdbcTemplate.queryForList("""
                        SELECT override_id, policy_id, target_type, target_id, override_reason, log_level,
                               db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                               error_stack_log_yn, masking_policy_key, effective_start_at, effective_end_at,
                               requested_by, approved_by, active_yn, created_at, updated_at
                        FROM pfw_log_policy_override
                        WHERE policy_id = ?
                        ORDER BY override_id DESC
                        """, policyId)
                : List.of());
        return response;
    }

    public Map<String, Object> createPolicy(AdmLogPolicyRequest request, String operatorId, String clientIp) {
        validatePolicy(request);
        String user = defaultIfBlank(operatorId, request.requestUser(), "ADM");
        pfwJdbcTemplate.update("""
                INSERT INTO pfw_log_policy (
                    policy_key, policy_name, target_type, target_id, log_level,
                    db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                    error_stack_log_yn, masking_policy_key, retention_days, sampling_rate, priority,
                    active_yn, description, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    policy_name = VALUES(policy_name),
                    target_type = VALUES(target_type),
                    target_id = VALUES(target_id),
                    log_level = VALUES(log_level),
                    db_log_enabled_yn = VALUES(db_log_enabled_yn),
                    file_log_enabled_yn = VALUES(file_log_enabled_yn),
                    request_body_log_yn = VALUES(request_body_log_yn),
                    response_body_log_yn = VALUES(response_body_log_yn),
                    error_stack_log_yn = VALUES(error_stack_log_yn),
                    masking_policy_key = VALUES(masking_policy_key),
                    retention_days = VALUES(retention_days),
                    sampling_rate = VALUES(sampling_rate),
                    priority = VALUES(priority),
                    active_yn = VALUES(active_yn),
                    description = VALUES(description),
                    updated_by = VALUES(updated_by),
                    updated_at = CURRENT_TIMESTAMP
                """,
                required(request.policyKey(), "정책 키"),
                required(request.policyName(), "정책명"),
                required(request.targetType(), "대상 유형"),
                required(request.targetId(), "대상 ID"),
                defaultIfBlank(request.logLevel(), "INFO"),
                yn(request.dbLogEnabledYn(), "Y"),
                yn(request.fileLogEnabledYn(), "Y"),
                yn(request.requestBodyLogYn(), "N"),
                yn(request.responseBodyLogYn(), "N"),
                yn(request.errorStackLogYn(), "Y"),
                blankToNull(request.maskingPolicyKey()),
                safeInt(request.retentionDays(), 90, 1, 3650),
                safeDecimal(request.samplingRate()),
                safeInt(request.priority(), 100, 1, 9999),
                yn(request.activeYn(), "Y"),
                blankToNull(request.description()),
                user,
                user);
        Map<String, Object> after = findPolicyByKey(request.policyKey()).orElse(Map.of());
        insertPolicyAudit(after.get("policy_id"), null, "UPSERT", request.targetType(), request.targetId(),
                request.reason(), null, String.valueOf(after), "로그 정책 등록/수정", user, clientIp);
        evictPolicyCache(request.targetType(), request.targetId());
        return after;
    }

    public Map<String, Object> updatePolicy(long policyId, AdmLogPolicyRequest request, String operatorId, String clientIp) {
        validatePolicy(request);
        String user = defaultIfBlank(operatorId, request.requestUser(), "ADM");
        Map<String, Object> before = findPolicyById(policyId).orElseThrow(() -> new CpfValidationException("로그 정책을 찾을 수 없습니다."));
        pfwJdbcTemplate.update("""
                UPDATE pfw_log_policy
                SET policy_key = ?,
                    policy_name = ?,
                    target_type = ?,
                    target_id = ?,
                    log_level = ?,
                    db_log_enabled_yn = ?,
                    file_log_enabled_yn = ?,
                    request_body_log_yn = ?,
                    response_body_log_yn = ?,
                    error_stack_log_yn = ?,
                    masking_policy_key = ?,
                    retention_days = ?,
                    sampling_rate = ?,
                    priority = ?,
                    active_yn = ?,
                    description = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE policy_id = ?
                """,
                required(request.policyKey(), "정책 키"),
                required(request.policyName(), "정책명"),
                required(request.targetType(), "대상 유형"),
                required(request.targetId(), "대상 ID"),
                defaultIfBlank(request.logLevel(), "INFO"),
                yn(request.dbLogEnabledYn(), "Y"),
                yn(request.fileLogEnabledYn(), "Y"),
                yn(request.requestBodyLogYn(), "N"),
                yn(request.responseBodyLogYn(), "N"),
                yn(request.errorStackLogYn(), "Y"),
                blankToNull(request.maskingPolicyKey()),
                safeInt(request.retentionDays(), 90, 1, 3650),
                safeDecimal(request.samplingRate()),
                safeInt(request.priority(), 100, 1, 9999),
                yn(request.activeYn(), "Y"),
                blankToNull(request.description()),
                user,
                policyId);
        Map<String, Object> after = findPolicyById(policyId).orElse(Map.of());
        insertPolicyAudit(policyId, null, "UPDATE", request.targetType(), request.targetId(),
                request.reason(), String.valueOf(before), String.valueOf(after), "로그 정책 변경", user, clientIp);
        evictPolicyCache(request.targetType(), request.targetId());
        return after;
    }

    public Map<String, Object> createOverride(AdmLogPolicyOverrideRequest request, String operatorId, String clientIp) {
        validateOverride(request);
        String user = defaultIfBlank(operatorId, request.requestUser(), "ADM");
        pfwJdbcTemplate.update("""
                INSERT INTO pfw_log_policy_override (
                    policy_id, target_type, target_id, override_reason, log_level,
                    db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                    error_stack_log_yn, masking_policy_key, effective_start_at, effective_end_at,
                    requested_by, approved_by, active_yn, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Y', ?, ?)
                """,
                request.policyId(),
                required(request.targetType(), "대상 유형"),
                required(request.targetId(), "대상 ID"),
                required(request.reason(), "감사 사유"),
                blankToNull(request.logLevel()),
                nullableYn(request.dbLogEnabledYn()),
                nullableYn(request.fileLogEnabledYn()),
                nullableYn(request.requestBodyLogYn()),
                nullableYn(request.responseBodyLogYn()),
                nullableYn(request.errorStackLogYn()),
                blankToNull(request.maskingPolicyKey()),
                request.effectiveStartAt(),
                request.effectiveEndAt(),
                user,
                blankToNull(request.approvedBy()),
                user,
                user);
        Long overrideId = pfwJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        Map<String, Object> after = findOverrideById(overrideId == null ? -1 : overrideId).orElse(Map.of());
        insertPolicyAudit(request.policyId(), overrideId, "OVERRIDE_CREATE", request.targetType(), request.targetId(),
                request.reason(), null, String.valueOf(after), "로그 정책 override 등록", user, clientIp);
        evictPolicyCache(request.targetType(), request.targetId());
        return after;
    }

    public Map<String, Object> disableOverride(long overrideId, String reason, String operatorId, String clientIp) {
        String user = defaultIfBlank(operatorId, null, "ADM");
        Map<String, Object> before = findOverrideById(overrideId).orElseThrow(() -> new CpfValidationException("로그 정책 override를 찾을 수 없습니다."));
        pfwJdbcTemplate.update("""
                UPDATE pfw_log_policy_override
                SET active_yn = 'N',
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE override_id = ?
                """, user, overrideId);
        Map<String, Object> after = findOverrideById(overrideId).orElse(Map.of());
        insertPolicyAudit(before.get("policy_id"), overrideId, "OVERRIDE_DISABLE",
                String.valueOf(before.get("target_type")), String.valueOf(before.get("target_id")),
                reason, String.valueOf(before), String.valueOf(after), "로그 정책 override 중지", user, clientIp);
        evictPolicyCache(String.valueOf(before.get("target_type")), String.valueOf(before.get("target_id")));
        return after;
    }

    public Map<String, Object> refreshCache(String targetType, String targetId, String reason, String operatorId, String clientIp) {
        String normalizedTargetType = required(targetType, "대상 유형");
        String normalizedTargetId = required(targetId, "대상 ID");
        String user = defaultIfBlank(operatorId, null, "ADM");
        LogPolicyResolver resolver = requireResolver();
        LogPolicyDecision decision = resolver.refresh(normalizedTargetType, normalizedTargetId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("targetType", decision.targetType());
        result.put("targetId", decision.targetId());
        result.put("dbLogEnabledYn", decision.dbLogEnabledYn());
        result.put("requestBodySaveYn", decision.requestBodySaveYn());
        result.put("responseBodySaveYn", decision.responseBodySaveYn());
        result.put("errorStackSaveYn", decision.errorStackSaveYn());
        result.put("resolvedSource", decision.resolvedSource());
        result.put("policyId", decision.policyId());
        result.put("overrideId", decision.overrideId());
        result.put("cacheSize", resolver.cachedSize());
        insertPolicyAudit(null, null, "CACHE_REFRESH", normalizedTargetType, normalizedTargetId,
                reason, null, String.valueOf(result), "로그 정책 cache refresh", user, clientIp);
        return result;
    }

    public Map<String, Object> clearCache(String reason, String operatorId, String clientIp) {
        String user = defaultIfBlank(operatorId, null, "ADM");
        LogPolicyResolver resolver = requireResolver();
        resolver.clear();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("targetType", "LOG_POLICY_CACHE");
        result.put("targetId", "*");
        result.put("cacheSize", resolver.cachedSize());
        result.put("cleared", true);
        insertPolicyAudit(null, null, "CACHE_CLEAR", "LOG_POLICY_CACHE", "*",
                reason, null, String.valueOf(result), "로그 정책 cache clear", user, clientIp);
        return result;
    }

    public Map<String, Object> createTraceBoost(AdmTraceBoostRequest request, String operatorId, String clientIp) {
        String targetId = defaultIfBlank(
                request.businessTransactionId(),
                defaultIfBlank(request.transactionGlobalId(), request.apiPath(), "*"),
                "*");
        long ttlSeconds = request.ttlSeconds() == null || request.ttlSeconds() < 60 ? 600 : Math.min(request.ttlSeconds(), 86_400);
        LocalDateTime startAt = LocalDateTime.now().minusSeconds(5);
        LocalDateTime endAt = LocalDateTime.now().plusSeconds(ttlSeconds);
        AdmLogPolicyOverrideRequest overrideRequest = new AdmLogPolicyOverrideRequest(
                request.policyId(),
                "ONLINE_TRANSACTION",
                targetId,
                defaultIfBlank(request.logLevel(), "DEBUG"),
                "Y",
                "Y",
                "N",
                "N",
                "Y",
                "TRACE_BOOST_SAFE_MASKING",
                startAt,
                endAt,
                defaultIfBlank(operatorId, request.requestUser(), "ADM"),
                request.requestUser(),
                required(request.reason(), "감사 사유"));
        Map<String, Object> created = createOverride(overrideRequest, operatorId, clientIp);
        Map<String, Object> response = new LinkedHashMap<>(created);
        response.put("traceBoostPolicyId", created.get("override_id"));
        response.put("targetType", "ONLINE_TRANSACTION");
        response.put("targetId", targetId);
        response.put("ttlSeconds", ttlSeconds);
        response.put("conditions", Map.of(
                "transactionGlobalId", defaultIfBlank(request.transactionGlobalId(), ""),
                "businessTransactionId", defaultIfBlank(request.businessTransactionId(), ""),
                "apiPath", defaultIfBlank(request.apiPath(), ""),
                "status", defaultIfBlank(request.status(), ""),
                "failureCode", defaultIfBlank(request.failureCode(), ""),
                "durationMsGreaterThan", request.durationMsGreaterThan() == null ? 0 : request.durationMsGreaterThan()));
        return response;
    }

    public Map<String, Object> disablePolicy(long policyId, String reason, String operatorId, String clientIp) {
        String user = defaultIfBlank(operatorId, null, "ADM");
        Map<String, Object> before = findPolicyById(policyId)
                .orElseThrow(() -> new CpfValidationException("로그 정책을 찾을 수 없습니다."));
        pfwJdbcTemplate.update("""
                UPDATE pfw_log_policy
                SET active_yn = 'N',
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE policy_id = ?
                """, user, policyId);
        Map<String, Object> after = findPolicyById(policyId).orElse(Map.of());
        insertPolicyAudit(policyId, null, "POLICY_DISABLE",
                String.valueOf(before.get("target_type")),
                String.valueOf(before.get("target_id")),
                reason, String.valueOf(before), String.valueOf(after), "Trace Boost 정책 비활성화", user, clientIp);
        evictPolicyCache(String.valueOf(before.get("target_type")), String.valueOf(before.get("target_id")));
        return after;
    }

    public Map<String, Object> findTraceBoostRuntimeState(int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", tableAvailable("pfw_log_policy_override"));
        response.put("items", tableAvailable("pfw_log_policy_override")
                ? pfwJdbcTemplate.queryForList("""
                        SELECT override_id AS traceBoostPolicyId, policy_id, target_type, target_id,
                               override_reason, log_level, effective_start_at, effective_end_at,
                               active_yn, requested_by, created_at, updated_at
                        FROM pfw_log_policy_override
                        WHERE active_yn = 'Y'
                          AND effective_start_at <= CURRENT_TIMESTAMP(3)
                          AND effective_end_at >= CURRENT_TIMESTAMP(3)
                        ORDER BY override_id DESC
                        LIMIT ?
                        """, Math.max(1, Math.min(limit, 500)))
                : List.of());
        return response;
    }

    public Map<String, Object> findTraceBoostHistory(int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", tableAvailable("pfw_log_policy_audit"));
        response.put("items", tableAvailable("pfw_log_policy_audit")
                ? pfwJdbcTemplate.queryForList("""
                        SELECT audit_id, policy_id, override_id AS traceBoostPolicyId,
                               action_type, target_type, target_id, reason,
                               operator_id, client_ip, created_at
                        FROM pfw_log_policy_audit
                        WHERE action_type IN ('OVERRIDE_CREATE', 'OVERRIDE_DISABLE', 'POLICY_DISABLE')
                        ORDER BY audit_id DESC
                        LIMIT ?
                        """, Math.max(1, Math.min(limit, 500)))
                : List.of());
        return response;
    }

    private List<Map<String, Object>> queryPolicies(String targetType, String targetId, String activeYn, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT policy_id, policy_key, policy_name, target_type, target_id, log_level,
                       db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                       error_stack_log_yn, masking_policy_key, retention_days, sampling_rate, priority,
                       active_yn, description, created_at, updated_at
                FROM pfw_log_policy
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        if (TextUtils.hasText(targetType)) {
            sql.append(" AND target_type = ?");
            args.add(targetType.trim().toUpperCase());
        }
        if (TextUtils.hasText(targetId)) {
            sql.append(" AND target_id LIKE ?");
            args.add("%" + targetId.trim() + "%");
        }
        if (TextUtils.hasText(activeYn)) {
            sql.append(" AND active_yn = ?");
            args.add(yn(activeYn, "Y"));
        }
        sql.append(" ORDER BY priority, policy_id LIMIT ?");
        args.add(Math.max(1, Math.min(limit, 500)));
        return pfwJdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    private Optional<Map<String, Object>> findPolicyById(long policyId) {
        List<Map<String, Object>> rows = pfwJdbcTemplate.queryForList("""
                SELECT policy_id, policy_key, policy_name, target_type, target_id, log_level,
                       db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                       error_stack_log_yn, masking_policy_key, retention_days, sampling_rate, priority,
                       active_yn, description, created_at, updated_at
                FROM pfw_log_policy
                WHERE policy_id = ?
                """, policyId);
        return rows.stream().findFirst();
    }

    private Optional<Map<String, Object>> findPolicyByKey(String policyKey) {
        List<Map<String, Object>> rows = pfwJdbcTemplate.queryForList("""
                SELECT policy_id, policy_key, policy_name, target_type, target_id, log_level,
                       db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                       error_stack_log_yn, masking_policy_key, retention_days, sampling_rate, priority,
                       active_yn, description, created_at, updated_at
                FROM pfw_log_policy
                WHERE policy_key = ?
                """, required(policyKey, "정책 키"));
        return rows.stream().findFirst();
    }

    private Optional<Map<String, Object>> findOverrideById(long overrideId) {
        List<Map<String, Object>> rows = pfwJdbcTemplate.queryForList("""
                SELECT override_id, policy_id, target_type, target_id, override_reason, log_level,
                       db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                       error_stack_log_yn, masking_policy_key, effective_start_at, effective_end_at,
                       requested_by, approved_by, active_yn, created_at, updated_at
                FROM pfw_log_policy_override
                WHERE override_id = ?
                """, overrideId);
        return rows.stream().findFirst();
    }

    private void insertPolicyAudit(
            Object policyId,
            Object overrideId,
            String actionType,
            String targetType,
            String targetId,
            String reason,
            String beforeData,
            String afterData,
            String diffData,
            String operatorId,
            String clientIp) {
        pfwJdbcTemplate.update("""
                INSERT INTO pfw_log_policy_audit (
                    policy_id, override_id, action_type, target_type, target_id, reason,
                    before_data, after_data, diff_data, operator_id, client_ip, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                policyId,
                overrideId,
                required(actionType, "행위 유형"),
                required(targetType, "대상 유형"),
                required(targetId, "대상 ID"),
                required(reason, "감사 사유"),
                beforeData,
                afterData,
                diffData,
                defaultIfBlank(operatorId, null, "ADM"),
                clientIp,
                defaultIfBlank(operatorId, null, "ADM"),
                defaultIfBlank(operatorId, null, "ADM"));
    }

    private boolean tableAvailable(String tableName) {
        try {
            Integer count = pfwJdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = DATABASE()
                      AND table_name = ?
                    """, Integer.class, tableName);
            return count != null && count > 0;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    private void validatePolicy(AdmLogPolicyRequest request) {
        required(request.policyKey(), "정책 키");
        required(request.policyName(), "정책명");
        required(request.targetType(), "대상 유형");
        required(request.targetId(), "대상 ID");
        required(request.reason(), "감사 사유");
    }

    private void validateOverride(AdmLogPolicyOverrideRequest request) {
        required(request.targetType(), "대상 유형");
        required(request.targetId(), "대상 ID");
        required(request.reason(), "감사 사유");
        if (request.effectiveStartAt() == null || request.effectiveEndAt() == null) {
            throw new CpfValidationException("override 적용 시작/종료 일시는 필수입니다.");
        }
        if (!request.effectiveStartAt().isBefore(request.effectiveEndAt())) {
            throw new CpfValidationException("override 적용 시작일시는 종료일시보다 이전이어야 합니다.");
        }
        if (request.effectiveEndAt().isBefore(LocalDateTime.now())) {
            throw new CpfValidationException("override 종료일시는 현재 이후여야 합니다.");
        }
    }

    private void evictPolicyCache(String targetType, String targetId) {
        LogPolicyResolver resolver = logPolicyResolverProvider.getIfAvailable();
        if (resolver == null) {
            return;
        }
        try {
            if ("*".equals(targetId)) {
                resolver.clear();
            } else {
                resolver.evict(targetType, targetId);
            }
        } catch (RuntimeException ex) {
            // 로그 정책 변경 자체를 실패시키지 않기 위해 cache 반영 실패는 감사 로그와 재시도 대상에 남깁니다.
            insertPolicyAudit(null, null, "CACHE_EVICT_FAILED", defaultIfBlank(targetType, "UNKNOWN"), defaultIfBlank(targetId, "*"),
                    "로그 정책 cache evict 실패", null, ex.getMessage(), "로그 정책 cache evict 실패", "ADM", null);
        }
    }

    private LogPolicyResolver requireResolver() {
        LogPolicyResolver resolver = logPolicyResolverProvider.getIfAvailable();
        if (resolver == null) {
            throw new CpfValidationException("로그 정책 cache resolver를 사용할 수 없습니다.");
        }
        return resolver;
    }

    private String required(String value, String label) {
        if (!TextUtils.hasText(value)) {
            throw new CpfValidationException(label + " 값은 필수입니다.");
        }
        return value.trim();
    }

    private String defaultIfBlank(String first, String second, String fallback) {
        if (TextUtils.hasText(first)) {
            return first.trim();
        }
        if (TextUtils.hasText(second)) {
            return second.trim();
        }
        return fallback;
    }

    private String defaultIfBlank(String value, String fallback) {
        return TextUtils.hasText(value) ? value.trim() : fallback;
    }

    private String blankToNull(String value) {
        return TextUtils.hasText(value) ? value.trim() : null;
    }

    private String yn(String value, String fallback) {
        String normalized = defaultIfBlank(value, fallback).toUpperCase();
        return "Y".equals(normalized) ? "Y" : "N";
    }

    private String nullableYn(String value) {
        return TextUtils.hasText(value) ? yn(value, "N") : null;
    }

    private int safeInt(Integer value, int fallback, int min, int max) {
        int normalized = value == null ? fallback : value;
        return Math.max(min, Math.min(max, normalized));
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        BigDecimal normalized = value == null ? BigDecimal.valueOf(100) : value;
        if (normalized.compareTo(BigDecimal.ZERO) < 0 || normalized.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new CpfValidationException("샘플링 비율은 0 이상 100 이하이어야 합니다.");
        }
        return normalized;
    }
}
