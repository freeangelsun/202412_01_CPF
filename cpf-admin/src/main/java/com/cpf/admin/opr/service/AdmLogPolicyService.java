package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmLogPolicyOverrideRequest;
import com.cpf.admin.opr.dto.AdmLogPolicyRequest;
import com.cpf.admin.opr.dto.AdmTraceBoostRequest;
import com.cpf.core.api.util.CpfStrings;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.CpfLogPolicyResolver;
import com.cpf.core.api.runtime.CpfRuntimePolicyDistributionPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ADM 로그 정책 관리 서비스입니다.
 *
 * <p>기본 정책과 임시 override를 DB 기준으로 관리하고 현재 인스턴스 cache를 즉시
 * 갱신합니다. 변경 Event는 Durable Outbox에 기록되며 각 Gateway 인스턴스가 Lease/Fencing으로
 * Claim한 뒤 ACK를 남기므로 부분 적용, 실패 재시도, Drift를 운영에서 추적할 수 있습니다.</p>
 */
@Service
@Transactional
public class AdmLogPolicyService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate cpfJdbcTemplate;
    private final ObjectProvider<CpfLogPolicyResolver> logPolicyResolverProvider;
    private final CpfRuntimePolicyDistributionPort policyDistribution;

    public AdmLogPolicyService(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            ObjectProvider<CpfLogPolicyResolver> logPolicyResolverProvider,
            @Qualifier("admRuntimePolicyDistributionPort") CpfRuntimePolicyDistributionPort policyDistribution) {
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.logPolicyResolverProvider = logPolicyResolverProvider;
        this.policyDistribution = policyDistribution;
    }

    public Map<String, Object> findPolicies(String targetType, String targetId, String activeYn, int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", tableAvailable("cpf_log_policy"));
        response.put("items", tableAvailable("cpf_log_policy")
                ? queryPolicies(targetType, targetId, activeYn, limit)
                : List.of());
        return response;
    }

    public Map<String, Object> findPolicy(long policyId) {
        Map<String, Object> response = new LinkedHashMap<>();
        boolean policyAvailable = tableAvailable("cpf_log_policy");
        boolean overrideAvailable = tableAvailable("cpf_log_policy_override");
        response.put("available", policyAvailable);
        response.put("item", policyAvailable ? findPolicyById(policyId).orElse(Map.of()) : Map.of());
        response.put("overrides", overrideAvailable
                ? cpfJdbcTemplate.queryForList("""
                        SELECT override_id, policy_id, target_type, target_id, override_reason, log_level,
                               db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                               error_stack_log_yn, masking_policy_key, effective_start_at, effective_end_at,
                               requested_by, approved_by, active_yn, created_at, updated_at
                        FROM cpf_log_policy_override
                        WHERE policy_id = ?
                        ORDER BY override_id DESC
                        """, policyId)
                : List.of());
        return response;
    }

    public Map<String, Object> createPolicy(AdmLogPolicyRequest request, String operatorId, String clientIp) {
        validatePolicy(request);
        String user = defaultIfBlank(operatorId, request.requestUser(), "ADM");
        PolicyValues values = policyValues(request, user);
        int updated = updatePolicyByKey(values);
        if (updated == 0) {
            try {
                insertPolicy(values);
            } catch (DuplicateKeyException concurrentInsert) {
                if (updatePolicyByKey(values) == 0) {
                    throw concurrentInsert;
                }
            }
        }
        Map<String, Object> after = findPolicyByKey(request.policyKey()).orElse(Map.of());
        insertPolicyAudit(after.get("policy_id"), null, "UPSERT", request.targetType(), request.targetId(),
                request.reason(), null, String.valueOf(after), "로그 정책 등록/수정", user, clientIp);
        evictPolicyCache(request.targetType(), request.targetId());
        return after;
    }

    private PolicyValues policyValues(AdmLogPolicyRequest request, String user) {
        return new PolicyValues(
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
                user);
    }

    private int updatePolicyByKey(PolicyValues values) {
        return cpfJdbcTemplate.update("""
                UPDATE cpf_log_policy
                SET policy_name = ?,
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
                WHERE policy_key = ?
                """,
                values.policyName(),
                values.targetType(),
                values.targetId(),
                values.logLevel(),
                values.dbLogEnabledYn(),
                values.fileLogEnabledYn(),
                values.requestBodyLogYn(),
                values.responseBodyLogYn(),
                values.errorStackLogYn(),
                values.maskingPolicyKey(),
                values.retentionDays(),
                values.samplingRate(),
                values.priority(),
                values.activeYn(),
                values.description(),
                values.user(),
                values.policyKey());
    }

    private void insertPolicy(PolicyValues values) {
        cpfJdbcTemplate.update("""
                INSERT INTO cpf_log_policy (
                    policy_key, policy_name, target_type, target_id, log_level,
                    db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                    error_stack_log_yn, masking_policy_key, retention_days, sampling_rate, priority,
                    active_yn, description, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                values.policyKey(),
                values.policyName(),
                values.targetType(),
                values.targetId(),
                values.logLevel(),
                values.dbLogEnabledYn(),
                values.fileLogEnabledYn(),
                values.requestBodyLogYn(),
                values.responseBodyLogYn(),
                values.errorStackLogYn(),
                values.maskingPolicyKey(),
                values.retentionDays(),
                values.samplingRate(),
                values.priority(),
                values.activeYn(),
                values.description(),
                values.user(),
                values.user());
    }

    public Map<String, Object> updatePolicy(long policyId, AdmLogPolicyRequest request, String operatorId, String clientIp) {
        validatePolicy(request);
        String user = defaultIfBlank(operatorId, request.requestUser(), "ADM");
        Map<String, Object> before = findPolicyById(policyId).orElseThrow(() -> new CpfValidationException("로그 정책을 찾을 수 없습니다."));
        cpfJdbcTemplate.update("""
                UPDATE cpf_log_policy
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
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int inserted = cpfJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO cpf_log_policy_override (
                        policy_id, target_type, target_id, override_reason, log_level,
                        db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                        error_stack_log_yn, masking_policy_key, effective_start_at, effective_end_at,
                        requested_by, approved_by, active_yn, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Y', ?, ?)
                    """, new String[] {"override_id"});
            statement.setObject(1, request.policyId());
            statement.setString(2, required(request.targetType(), "대상 유형"));
            statement.setString(3, required(request.targetId(), "대상 ID"));
            statement.setString(4, required(request.reason(), "감사 사유"));
            statement.setString(5, blankToNull(request.logLevel()));
            statement.setString(6, nullableYn(request.dbLogEnabledYn()));
            statement.setString(7, nullableYn(request.fileLogEnabledYn()));
            statement.setString(8, nullableYn(request.requestBodyLogYn()));
            statement.setString(9, nullableYn(request.responseBodyLogYn()));
            statement.setString(10, nullableYn(request.errorStackLogYn()));
            statement.setString(11, blankToNull(request.maskingPolicyKey()));
            statement.setObject(12, request.effectiveStartAt());
            statement.setObject(13, request.effectiveEndAt());
            statement.setString(14, user);
            statement.setString(15, blankToNull(request.approvedBy()));
            statement.setString(16, user);
            statement.setString(17, user);
            return statement;
        }, keyHolder);
        Number generatedKey = keyHolder.getKey();
        if (inserted != 1 || generatedKey == null) {
            throw new IllegalStateException("로그 정책 override 생성 ID를 확인할 수 없습니다.");
        }
        long overrideId = generatedKey.longValue();
        Map<String, Object> after = findOverrideById(overrideId).orElse(Map.of());
        insertPolicyAudit(request.policyId(), overrideId, "OVERRIDE_CREATE", request.targetType(), request.targetId(),
                request.reason(), null, String.valueOf(after), "로그 정책 override 등록", user, clientIp);
        evictPolicyCache(request.targetType(), request.targetId());
        return after;
    }

    public Map<String, Object> disableOverride(long overrideId, String reason, String operatorId, String clientIp) {
        String user = defaultIfBlank(operatorId, null, "ADM");
        Map<String, Object> before = findOverrideById(overrideId).orElseThrow(() -> new CpfValidationException("로그 정책 override를 찾을 수 없습니다."));
        cpfJdbcTemplate.update("""
                UPDATE cpf_log_policy_override
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
        CpfLogPolicyResolver resolver = requireResolver();
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
        CpfLogPolicyResolver resolver = requireResolver();
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
                defaultIfBlank(request.transactionId(), request.apiPath(), "*"),
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
                "transactionId", defaultIfBlank(request.transactionId(), ""),
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
        cpfJdbcTemplate.update("""
                UPDATE cpf_log_policy
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
        response.put("available", tableAvailable("cpf_log_policy_override"));
        response.put("items", tableAvailable("cpf_log_policy_override")
                ? AdmJdbcQueries.queryForList(
                        cpfJdbcTemplate,
                        """
                        SELECT override_id AS traceBoostPolicyId, policy_id, target_type, target_id,
                               override_reason, log_level, effective_start_at, effective_end_at,
                               active_yn, requested_by, created_at, updated_at
                        FROM cpf_log_policy_override
                        WHERE active_yn = 'Y'
                          AND effective_start_at <= CURRENT_TIMESTAMP
                          AND effective_end_at >= CURRENT_TIMESTAMP
                        ORDER BY override_id DESC
                        """,
                        List.of(),
                        Math.max(1, Math.min(limit, 500)))
                : List.of());
        return response;
    }

    public Map<String, Object> findTraceBoostHistory(int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", tableAvailable("cpf_log_policy_audit"));
        response.put("items", tableAvailable("cpf_log_policy_audit")
                ? AdmJdbcQueries.queryForList(
                        cpfJdbcTemplate,
                        """
                        SELECT audit_id, policy_id, override_id AS traceBoostPolicyId,
                               action_type, target_type, target_id, reason,
                               operator_id, client_ip, created_at
                        FROM cpf_log_policy_audit
                        WHERE action_type IN ('OVERRIDE_CREATE', 'OVERRIDE_DISABLE', 'POLICY_DISABLE')
                        ORDER BY audit_id DESC
                        """,
                        List.of(),
                        Math.max(1, Math.min(limit, 500)))
                : List.of());
        return response;
    }

    private List<Map<String, Object>> queryPolicies(String targetType, String targetId, String activeYn, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT policy_id, policy_key, policy_name, target_type, target_id, log_level,
                       db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                       error_stack_log_yn, masking_policy_key, retention_days, sampling_rate, priority,
                       active_yn, description, created_at, updated_at
                FROM cpf_log_policy
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        if (CpfStrings.hasText(targetType)) {
            sql.append(" AND target_type = ?");
            args.add(targetType.trim().toUpperCase());
        }
        if (CpfStrings.hasText(targetId)) {
            sql.append(" AND target_id LIKE ?");
            args.add("%" + targetId.trim() + "%");
        }
        if (CpfStrings.hasText(activeYn)) {
            sql.append(" AND active_yn = ?");
            args.add(yn(activeYn, "Y"));
        }
        sql.append(" ORDER BY priority, policy_id");
        return AdmJdbcQueries.queryForList(
                cpfJdbcTemplate,
                sql.toString(),
                args,
                Math.max(1, Math.min(limit, 500)));
    }

    private Optional<Map<String, Object>> findPolicyById(long policyId) {
        List<Map<String, Object>> rows = cpfJdbcTemplate.queryForList("""
                SELECT policy_id, policy_key, policy_name, target_type, target_id, log_level,
                       db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                       error_stack_log_yn, masking_policy_key, retention_days, sampling_rate, priority,
                       active_yn, description, created_at, updated_at
                FROM cpf_log_policy
                WHERE policy_id = ?
                """, policyId);
        return rows.stream().findFirst();
    }

    private Optional<Map<String, Object>> findPolicyByKey(String policyKey) {
        List<Map<String, Object>> rows = cpfJdbcTemplate.queryForList("""
                SELECT policy_id, policy_key, policy_name, target_type, target_id, log_level,
                       db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                       error_stack_log_yn, masking_policy_key, retention_days, sampling_rate, priority,
                       active_yn, description, created_at, updated_at
                FROM cpf_log_policy
                WHERE policy_key = ?
                """, required(policyKey, "정책 키"));
        return rows.stream().findFirst();
    }

    private Optional<Map<String, Object>> findOverrideById(long overrideId) {
        List<Map<String, Object>> rows = cpfJdbcTemplate.queryForList("""
                SELECT override_id, policy_id, target_type, target_id, override_reason, log_level,
                       db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
                       error_stack_log_yn, masking_policy_key, effective_start_at, effective_end_at,
                       requested_by, approved_by, active_yn, created_at, updated_at
                FROM cpf_log_policy_override
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
        cpfJdbcTemplate.update("""
                INSERT INTO cpf_log_policy_audit (
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
        return AdmJdbcQueries.tableExists(cpfJdbcTemplate, tableName);
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
        publishPolicyEvent(targetType, targetId, "CACHE_EVICT");
        CpfLogPolicyResolver resolver = logPolicyResolverProvider.getIfAvailable();
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


    private void publishPolicyEvent(String targetType, String targetId, String action) {
        String normalizedType = defaultIfBlank(targetType, null, "LOG_POLICY");
        String normalizedId = defaultIfBlank(targetId, null, "*");
        String source = normalizedType + "|" + normalizedId + "|" + action + "|" + System.nanoTime();
        policyDistribution.publish(new CpfRuntimePolicyDistributionPort.PublishCommand(
                UUID.randomUUID().toString(), "LOG_POLICY", normalizedType, normalizedId, System.currentTimeMillis(),
                action, sha256(source), Map.of("targetType", normalizedType, "targetId", normalizedId),
                "로그 정책 다중 인스턴스 동기화", "ADM", OffsetDateTime.now()));
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    /** 정책 변경 Event의 Gateway별 ACK, 실패, 재시도 상태를 조회합니다. */
    @Transactional(readOnly = true)
    public Map<String, Object> findDistributionStatus(String targetType, String targetId, int limit) {
        String normalizedType = defaultIfBlank(targetType, null, "LOG_POLICY");
        String normalizedId = defaultIfBlank(targetId, null, "*");
        List<CpfRuntimePolicyDistributionPort.DeliveryStatus> items =
                policyDistribution.findDeliveryStatus(normalizedType, normalizedId, Math.max(1, Math.min(limit, 1000)));
        long applied = items.stream().filter(item -> "APPLIED".equals(item.status())).count();
        long failed = items.stream().filter(item -> "FAILED".equals(item.status())).count();
        long pending = items.size() - applied - failed;
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("targetType", normalizedType);
        response.put("targetId", normalizedId);
        response.put("applied", applied);
        response.put("failed", failed);
        response.put("pending", pending);
        response.put("items", items);
        response.put("consistent", !items.isEmpty() && failed == 0 && pending == 0);
        return response;
    }

    private CpfLogPolicyResolver requireResolver() {
        CpfLogPolicyResolver resolver = logPolicyResolverProvider.getIfAvailable();
        if (resolver == null) {
            throw new CpfValidationException("로그 정책 cache resolver를 사용할 수 없습니다.");
        }
        return resolver;
    }

    private String required(String value, String label) {
        if (!CpfStrings.hasText(value)) {
            throw new CpfValidationException(label + " 값은 필수입니다.");
        }
        return value.trim();
    }

    private String defaultIfBlank(String first, String second, String fallback) {
        if (CpfStrings.hasText(first)) {
            return first.trim();
        }
        if (CpfStrings.hasText(second)) {
            return second.trim();
        }
        return fallback;
    }

    private String defaultIfBlank(String value, String fallback) {
        return CpfStrings.hasText(value) ? value.trim() : fallback;
    }

    private String blankToNull(String value) {
        return CpfStrings.hasText(value) ? value.trim() : null;
    }

    private String yn(String value, String fallback) {
        String normalized = defaultIfBlank(value, fallback).toUpperCase();
        return "Y".equals(normalized) ? "Y" : "N";
    }

    private String nullableYn(String value) {
        return CpfStrings.hasText(value) ? yn(value, "N") : null;
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

    private record PolicyValues(
            String policyKey,
            String policyName,
            String targetType,
            String targetId,
            String logLevel,
            String dbLogEnabledYn,
            String fileLogEnabledYn,
            String requestBodyLogYn,
            String responseBodyLogYn,
            String errorStackLogYn,
            String maskingPolicyKey,
            int retentionDays,
            BigDecimal samplingRate,
            int priority,
            String activeYn,
            String description,
            String user) {
    }
}
