package com.cpf.bizadmin.approval.repository;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.bizadmin.approval.api.BzaApprovalTargetType;
import com.cpf.bizadmin.approval.spi.BzaApprovalDirectoryEntry;
import com.cpf.bizadmin.approval.spi.BzaApprovalDirectoryPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/**
 * BZA 결재 Policy/Participant Snapshot 정본 adapter.
 *
 * <p>정책 Target 해석 결과는 상신 시 participant로 고정하며 이후 조직/Role 변경으로
 * 진행 중 결재 분모가 바뀌지 않습니다.</p>
 */
@Repository
public class BzaApprovalPolicyRepository implements BzaApprovalDirectoryPort {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;
    private final CpfVendorSqlCatalog sql;

    public BzaApprovalPolicyRepository(
            @Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.sql = sqlCatalogProvider.forModule("bza");
    }

    public List<Map<String,Object>> findPolicies(String businessDomain, String approvalType) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-policies-01"), new MapSqlParameterSource()
                .addValue("businessDomain", blankToNull(businessDomain))
                .addValue("approvalType", blankToNull(approvalType)));
    }

    public Optional<Map<String,Object>> findPolicy(String policyCode, int version) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-policy-01"), new MapSqlParameterSource()
                .addValue("policyCode", policyCode).addValue("policyVersion", version))
                .stream().findFirst();
    }

    public Optional<Map<String,Object>> findActivePolicy(
            String businessDomain, String approvalType, Instant effectiveAt) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-active-policy-01"), new MapSqlParameterSource()
                .addValue("businessDomain", businessDomain)
                .addValue("approvalType", approvalType)
                .addValue("at", Timestamp.from(effectiveAt)))
                .stream().findFirst();
    }

    public List<Map<String,Object>> findPolicySteps(String policyCode, int version) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-policy-steps-01"), new MapSqlParameterSource()
                .addValue("policyCode", policyCode).addValue("policyVersion", version));
    }

    public void replacePolicy(
            Map<String,Object> policy, List<Map<String,Object>> steps) {
        int updated = jdbc().update(sql.required("approval-policy-repository-replace-policy-01"), policy);
        if (updated == 0) {
            jdbc().update(sql.required("approval-policy-repository-replace-policy-02"), policy);
        }
        jdbc().update(sql.required("approval-policy-repository-replace-policy-03"), policy);
        for (Map<String,Object> step : steps) {
            jdbc().update(sql.required("approval-policy-repository-replace-policy-04"), step);
        }
    }

    @Override
    public List<BzaApprovalDirectoryEntry> resolve(
            BzaApprovalTargetType targetType, String targetCode, Instant effectiveAt) {
        Timestamp at = Timestamp.from(effectiveAt);
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("targetCode", targetCode).addValue("at", at);
        String query = switch (targetType) {
            case EMPLOYEE -> sql.required("approval-policy-repository-resolve-01");
            case ORGANIZATION -> sql.required("approval-policy-repository-resolve-02");
            case POSITION -> sql.required("approval-policy-repository-resolve-03");
            case ORG_MANAGER -> sql.required("approval-policy-repository-resolve-04");
            case ROLE -> sql.required("approval-policy-repository-resolve-05");
        };
        LinkedHashMap<String,BzaApprovalDirectoryEntry> unique = new LinkedHashMap<>();
        for (Map<String,Object> row : jdbc().queryForList(query, p)) {
            String employeeNo = Objects.toString(row.get("employeeNo"), "");
            if (employeeNo.isBlank()) continue;
            unique.putIfAbsent(employeeNo, new BzaApprovalDirectoryEntry(
                    employeeNo, nullable(row.get("organizationCode")),
                    nullable(row.get("positionCode")), nullable(row.get("jobTitleCode"))));
        }
        return List.copyOf(unique.values());
    }

    public Optional<BzaApprovalDirectoryEntry> findPrimaryAssignment(String employeeNo, Instant at) {
        return resolve(BzaApprovalTargetType.EMPLOYEE, employeeNo, at).stream().findFirst();
    }

    public Optional<String> findActiveDelegate(
            String employeeNo, String businessDomain, String approvalType, Instant at) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-active-delegate-01"), new MapSqlParameterSource()
                .addValue("employeeNo", employeeNo)
                .addValue("businessDomain", businessDomain)
                .addValue("approvalType", approvalType)
                .addValue("at", Timestamp.from(at))).stream()
                .map(row -> Objects.toString(row.get("delegateEmployeeNo"), null))
                .filter(Objects::nonNull).findFirst();
    }

    public List<Map<String,Object>> findDelegations(String employeeNo, Instant at) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-delegations-01"), new MapSqlParameterSource()
                .addValue("employeeNo", blankToNull(employeeNo))
                .addValue("at", at == null ? null : Timestamp.from(at)));
    }

    public List<Map<String,Object>> findSubmissions(String employeeNo, String status, int limit) {
        return jdbc().queryForList(
                sql.required("approval-policy-repository-find-submissions-01"),
                new MapSqlParameterSource()
                        .addValue("employeeNo", employeeNo)
                        .addValue("status", blankToNull(status))
                        .addValue("limit", limit));
    }

    public List<Map<String,Object>> findInbox(String employeeNo, String decisionStatus, int limit) {
        return jdbc().queryForList(
                sql.required("approval-policy-repository-find-inbox-01"),
                new MapSqlParameterSource()
                        .addValue("employeeNo", employeeNo)
                        .addValue("decisionStatus", blankToNull(decisionStatus))
                        .addValue("limit", limit));
    }

    public void saveDelegation(Map<String,Object> values) {
        Object id = values.get("delegationId");
        if (id == null) {
            jdbc().update(sql.required("approval-policy-repository-save-delegation-01"), values);
        } else {
            jdbc().update(sql.required("approval-policy-repository-save-delegation-02"), values);
        }
    }

    public Optional<Long> findApprovalByIdempotencyKey(String key) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-approval-by-idempotency-key-01"), new MapSqlParameterSource("key", key)).stream()
                .map(row -> ((Number)row.get("approvalId")).longValue()).findFirst();
    }

    public long insertPolicyApproval(Map<String,Object> values) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc().update(sql.required("approval-policy-repository-insert-policy-approval-01"), new MapSqlParameterSource(values), key, new String[]{"approval_id"});
        Number id = key.getKey();
        if (id == null) throw new IllegalStateException("BZA policy approval 생성 키를 확인할 수 없습니다.");
        return id.longValue();
    }

    public long insertLine(Map<String,Object> values) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc().update(sql.required("approval-policy-repository-insert-line-01"), new MapSqlParameterSource(values), key, new String[]{"approval_line_id"});
        Number id = key.getKey();
        if (id == null) throw new IllegalStateException("BZA approval line 생성 키를 확인할 수 없습니다.");
        return id.longValue();
    }

    public void insertParticipant(Map<String,Object> values) {
        jdbc().update(sql.required("approval-policy-repository-insert-participant-01"), values);
    }

    public List<Map<String,Object>> findParticipants(long approvalId) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-participants-01"), new MapSqlParameterSource("approvalId", approvalId));
    }


    public Optional<String> findEmployeeNoByLoginId(String loginId) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-employee-no-by-login-id-01"), new MapSqlParameterSource("loginId", loginId)).stream()
                .map(row -> Objects.toString(row.get("employeeNo"), null))
                .filter(Objects::nonNull).findFirst();
    }

    public Optional<Map<String,Object>> findDocument(long approvalId) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-document-01"), new MapSqlParameterSource("approvalId", approvalId)).stream().findFirst();
    }

    public Optional<Map<String,Object>> findWaitingParticipant(long approvalId, String employeeNo, String approvalMode, int currentStep) {
        String statementKey = "SEQUENTIAL".equalsIgnoreCase(approvalMode)
                ? "approval-policy-repository-find-waiting-participant-sequential"
                : "approval-policy-repository-find-waiting-participant-parallel";
        return jdbc().queryForList(sql.required(statementKey), new MapSqlParameterSource()
                .addValue("approvalId", approvalId)
                .addValue("employeeNo", employeeNo)
                .addValue("currentStep", currentStep)).stream().findFirst();
    }

    public boolean participantDecisionExists(String idempotencyKey) {
        Long count = jdbc().queryForObject(sql.required("approval-policy-repository-participant-decision-exists-01"), new MapSqlParameterSource("key", idempotencyKey), Long.class);
        return count != null && count > 0;
    }

    public int decideParticipant(long participantId, String decisionStatus, String idempotencyKey, String comment, String operatorId) {
        return jdbc().update(sql.required("approval-policy-repository-decide-participant-01"), new MapSqlParameterSource()
                .addValue("participantId", participantId)
                .addValue("decisionStatus", decisionStatus)
                .addValue("idempotencyKey", idempotencyKey)
                .addValue("comment", comment)
                .addValue("operatorId", operatorId));
    }

    public Map<String,Object> participantCounts(long lineId) {
        return jdbc().queryForMap(sql.required("approval-policy-repository-participant-counts-01"), new MapSqlParameterSource("lineId", lineId));
    }

    public int updateLineStatus(long lineId, String status, String comment, String operatorId) {
        return jdbc().update(sql.required("approval-policy-repository-update-line-status-01"), new MapSqlParameterSource()
                .addValue("lineId", lineId).addValue("status", status)
                .addValue("comment", comment).addValue("operatorId", operatorId));
    }

    public List<Map<String,Object>> findLineStatuses(long approvalId) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-line-statuses-01"), new MapSqlParameterSource("approvalId", approvalId));
    }

    public boolean historyActionExists(String idempotencyKey) {
        Long count = jdbc().queryForObject(sql.required("approval-policy-repository-history-action-exists-01"), new MapSqlParameterSource("key", idempotencyKey), Long.class);
        return count != null && count > 0;
    }

    public List<Long> findDueApprovalIds(Instant now, int limit) {
        return jdbc().queryForList(sql.required("approval-policy-repository-find-due-approval-ids-01"), new MapSqlParameterSource().addValue("now", Timestamp.from(now)).addValue("limit", limit))
                .stream().map(row -> ((Number) row.get("approvalId")).longValue()).toList();
    }

    public int updateDocumentStatus(long approvalId, long versionNo, String status, int currentStep, boolean completed, String operatorId) {
        return jdbc().update(sql.required("approval-policy-repository-update-document-status-01"), new MapSqlParameterSource()
                .addValue("approvalId", approvalId).addValue("versionNo", versionNo)
                .addValue("status", status).addValue("currentStep", currentStep)
                .addValue("completed", completed ? 1 : 0).addValue("operatorId", operatorId));
    }

    public void insertHistory(long approvalId, String actionType, String actorEmployeeNo,
                              String idempotencyKey, String reason, String beforeStatus,
                              String afterStatus, String comment, String transactionId, String operatorId) {
        jdbc().update(sql.required("approval-policy-repository-insert-history-01"), new MapSqlParameterSource()
                .addValue("approvalId", approvalId).addValue("actionType", actionType)
                .addValue("actorEmployeeNo", actorEmployeeNo).addValue("idempotencyKey", idempotencyKey)
                .addValue("reason", reason).addValue("beforeStatus", beforeStatus)
                .addValue("afterStatus", afterStatus).addValue("comment", comment)
                .addValue("transactionId", transactionId).addValue("operatorId", operatorId));
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbc = jdbcTemplateProvider.getIfAvailable();
        if (jdbc == null) throw new IllegalStateException("BZA datasource가 구성되지 않았습니다.");
        return jdbc;
    }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static String nullable(Object value) { return value == null ? null : String.valueOf(value); }
}
