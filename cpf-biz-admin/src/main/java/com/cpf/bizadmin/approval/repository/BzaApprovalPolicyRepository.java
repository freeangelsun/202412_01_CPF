package com.cpf.bizadmin.approval.repository;

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

    public BzaApprovalPolicyRepository(
            @Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    public List<Map<String,Object>> findPolicies(String businessDomain, String approvalType) {
        return jdbc().queryForList("""
                SELECT policy_code AS policyCode, policy_version AS policyVersion,
                       policy_name AS policyName, business_domain AS businessDomain,
                       approval_type AS approvalType, effective_from AS effectiveFrom,
                       effective_to AS effectiveTo, enabled_yn AS enabledYn,
                       self_approval_allowed_yn AS selfApprovalAllowedYn, description,
                       created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_approval_policy
                 WHERE (:businessDomain IS NULL OR business_domain = :businessDomain)
                   AND (:approvalType IS NULL OR approval_type = :approvalType)
                 ORDER BY policy_code, policy_version DESC
                """, new MapSqlParameterSource()
                .addValue("businessDomain", blankToNull(businessDomain))
                .addValue("approvalType", blankToNull(approvalType)));
    }

    public Optional<Map<String,Object>> findPolicy(String policyCode, int version) {
        return jdbc().queryForList("""
                SELECT policy_code AS policyCode, policy_version AS policyVersion,
                       policy_name AS policyName, business_domain AS businessDomain,
                       approval_type AS approvalType, effective_from AS effectiveFrom,
                       effective_to AS effectiveTo, enabled_yn AS enabledYn,
                       self_approval_allowed_yn AS selfApprovalAllowedYn, description
                  FROM bza_approval_policy
                 WHERE policy_code = :policyCode AND policy_version = :policyVersion
                """, new MapSqlParameterSource()
                .addValue("policyCode", policyCode).addValue("policyVersion", version))
                .stream().findFirst();
    }

    public Optional<Map<String,Object>> findActivePolicy(
            String businessDomain, String approvalType, Instant effectiveAt) {
        return jdbc().queryForList("""
                SELECT policy_code AS policyCode, policy_version AS policyVersion,
                       policy_name AS policyName, business_domain AS businessDomain,
                       approval_type AS approvalType, effective_from AS effectiveFrom,
                       effective_to AS effectiveTo, enabled_yn AS enabledYn,
                       self_approval_allowed_yn AS selfApprovalAllowedYn, description
                  FROM bza_approval_policy
                 WHERE business_domain = :businessDomain
                   AND approval_type = :approvalType
                   AND enabled_yn = 'Y'
                   AND effective_from <= :at
                   AND (effective_to IS NULL OR effective_to > :at)
                 ORDER BY policy_version DESC
                """, new MapSqlParameterSource()
                .addValue("businessDomain", businessDomain)
                .addValue("approvalType", approvalType)
                .addValue("at", Timestamp.from(effectiveAt)))
                .stream().findFirst();
    }

    public List<Map<String,Object>> findPolicySteps(String policyCode, int version) {
        return jdbc().queryForList("""
                SELECT policy_code AS policyCode, policy_version AS policyVersion,
                       step_no AS stepNo, step_type AS stepType,
                       target_type AS targetType, target_code AS targetCode,
                       decision_rule AS decisionRule, required_count AS requiredCount,
                       required_yn AS requiredYn, sort_order AS sortOrder
                  FROM bza_approval_policy_step
                 WHERE policy_code = :policyCode AND policy_version = :policyVersion
                 ORDER BY step_no, sort_order, target_type, target_code
                """, new MapSqlParameterSource()
                .addValue("policyCode", policyCode).addValue("policyVersion", version));
    }

    public void replacePolicy(
            Map<String,Object> policy, List<Map<String,Object>> steps) {
        int updated = jdbc().update("""
                UPDATE bza_approval_policy
                   SET policy_name=:policyName, business_domain=:businessDomain,
                       approval_type=:approvalType, effective_from=:effectiveFrom,
                       effective_to=:effectiveTo, enabled_yn=:enabledYn,
                       self_approval_allowed_yn=:selfApprovalAllowedYn,
                       description=:description, updated_by=:operatorId
                 WHERE policy_code=:policyCode AND policy_version=:policyVersion
                """, policy);
        if (updated == 0) {
            jdbc().update("""
                    INSERT INTO bza_approval_policy (
                        policy_code, policy_version, policy_name, business_domain, approval_type,
                        effective_from, effective_to, enabled_yn, self_approval_allowed_yn,
                        description, created_by, updated_by
                    ) VALUES (
                        :policyCode, :policyVersion, :policyName, :businessDomain, :approvalType,
                        :effectiveFrom, :effectiveTo, :enabledYn, :selfApprovalAllowedYn,
                        :description, :operatorId, :operatorId
                    )
                    """, policy);
        }
        jdbc().update("""
                DELETE FROM bza_approval_policy_step
                 WHERE policy_code=:policyCode AND policy_version=:policyVersion
                """, policy);
        for (Map<String,Object> step : steps) {
            jdbc().update("""
                    INSERT INTO bza_approval_policy_step (
                        policy_code, policy_version, step_no, step_type, target_type, target_code,
                        decision_rule, required_count, required_yn, sort_order, created_by, updated_by
                    ) VALUES (
                        :policyCode, :policyVersion, :stepNo, :stepType, :targetType, :targetCode,
                        :decisionRule, :requiredCount, :requiredYn, :sortOrder, :operatorId, :operatorId
                    )
                    """, step);
        }
    }

    @Override
    public List<BzaApprovalDirectoryEntry> resolve(
            BzaApprovalTargetType targetType, String targetCode, Instant effectiveAt) {
        Timestamp at = Timestamp.from(effectiveAt);
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("targetCode", targetCode).addValue("at", at);
        String sql = switch (targetType) {
            case EMPLOYEE -> """
                    SELECT e.employee_no AS employeeNo,
                           a.organization_code AS organizationCode,
                           a.position_code AS positionCode, a.job_title_code AS jobTitleCode
                      FROM bza_employee e
                      LEFT JOIN bza_employee_assignment a
                        ON a.employee_no=e.employee_no AND a.primary_yn='Y'
                       AND a.effective_from <= :at AND (a.effective_to IS NULL OR a.effective_to > :at)
                     WHERE e.employee_no=:targetCode AND e.use_yn='Y' AND e.employment_status='ACTIVE'
                    """;
            case ORGANIZATION -> """
                    SELECT e.employee_no AS employeeNo, a.organization_code AS organizationCode,
                           a.position_code AS positionCode, a.job_title_code AS jobTitleCode
                      FROM bza_employee_assignment a
                      JOIN bza_employee e ON e.employee_no=a.employee_no
                     WHERE a.organization_code=:targetCode
                       AND a.effective_from <= :at AND (a.effective_to IS NULL OR a.effective_to > :at)
                       AND e.use_yn='Y' AND e.employment_status='ACTIVE'
                    """;
            case POSITION -> """
                    SELECT e.employee_no AS employeeNo, a.organization_code AS organizationCode,
                           a.position_code AS positionCode, a.job_title_code AS jobTitleCode
                      FROM bza_employee_assignment a
                      JOIN bza_employee e ON e.employee_no=a.employee_no
                     WHERE a.position_code=:targetCode
                       AND a.effective_from <= :at AND (a.effective_to IS NULL OR a.effective_to > :at)
                       AND e.use_yn='Y' AND e.employment_status='ACTIVE'
                    """;
            case ORG_MANAGER -> """
                    SELECT e.employee_no AS employeeNo, r.organization_code AS organizationCode,
                           a.position_code AS positionCode, a.job_title_code AS jobTitleCode
                      FROM bza_organization_responsibility r
                      JOIN bza_employee e ON e.employee_no=r.employee_no
                      LEFT JOIN bza_employee_assignment a
                        ON a.employee_no=e.employee_no AND a.primary_yn='Y'
                       AND a.effective_from <= :at AND (a.effective_to IS NULL OR a.effective_to > :at)
                     WHERE r.organization_code=:targetCode
                       AND r.responsibility_type IN ('MANAGER','APPROVAL_OWNER','ACTING')
                       AND r.effective_from <= :at AND (r.effective_to IS NULL OR r.effective_to > :at)
                       AND e.use_yn='Y' AND e.employment_status='ACTIVE'
                    """;
            case ROLE -> """
                    SELECT e.employee_no AS employeeNo, a.organization_code AS organizationCode,
                           a.position_code AS positionCode, a.job_title_code AS jobTitleCode
                      FROM bza_user_role ur
                      JOIN bza_admin_user u ON u.admin_user_id=ur.admin_user_id
                      JOIN bza_employee e ON e.admin_user_id=u.admin_user_id
                      LEFT JOIN bza_employee_assignment a
                        ON a.employee_no=e.employee_no AND a.primary_yn='Y'
                       AND a.effective_from <= :at AND (a.effective_to IS NULL OR a.effective_to > :at)
                     WHERE ur.role_code=:targetCode
                       AND (ur.valid_from IS NULL OR ur.valid_from <= :at)
                       AND (ur.valid_to IS NULL OR ur.valid_to > :at)
                       AND u.use_yn='Y' AND e.use_yn='Y' AND e.employment_status='ACTIVE'
                    """;
        };
        LinkedHashMap<String,BzaApprovalDirectoryEntry> unique = new LinkedHashMap<>();
        for (Map<String,Object> row : jdbc().queryForList(sql, p)) {
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
        return jdbc().queryForList("""
                SELECT delegate_employee_no AS delegateEmployeeNo
                  FROM bza_approval_delegation
                 WHERE delegator_employee_no=:employeeNo AND use_yn='Y'
                   AND valid_from <= :at AND valid_to > :at
                   AND (business_domain IS NULL OR business_domain=:businessDomain)
                   AND (approval_type IS NULL OR approval_type=:approvalType)
                 ORDER BY
                   CASE WHEN business_domain IS NULL THEN 1 ELSE 0 END,
                   CASE WHEN approval_type IS NULL THEN 1 ELSE 0 END,
                   valid_from DESC, delegation_id DESC
                """, new MapSqlParameterSource()
                .addValue("employeeNo", employeeNo)
                .addValue("businessDomain", businessDomain)
                .addValue("approvalType", approvalType)
                .addValue("at", Timestamp.from(at))).stream()
                .map(row -> Objects.toString(row.get("delegateEmployeeNo"), null))
                .filter(Objects::nonNull).findFirst();
    }

    public List<Map<String,Object>> findDelegations(String employeeNo, Instant at) {
        return jdbc().queryForList("""
                SELECT delegation_id AS delegationId, delegator_employee_no AS delegatorEmployeeNo,
                       delegate_employee_no AS delegateEmployeeNo, business_domain AS businessDomain,
                       approval_type AS approvalType, valid_from AS validFrom, valid_to AS validTo,
                       reason, use_yn AS useYn, created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_approval_delegation
                 WHERE (:employeeNo IS NULL OR delegator_employee_no=:employeeNo OR delegate_employee_no=:employeeNo)
                   AND (:at IS NULL OR (valid_from <= :at AND valid_to > :at))
                 ORDER BY valid_from DESC, delegation_id DESC
                """, new MapSqlParameterSource()
                .addValue("employeeNo", blankToNull(employeeNo))
                .addValue("at", at == null ? null : Timestamp.from(at)));
    }

    public void saveDelegation(Map<String,Object> values) {
        Object id = values.get("delegationId");
        if (id == null) {
            jdbc().update("""
                    INSERT INTO bza_approval_delegation (
                        delegator_employee_no, delegate_employee_no, business_domain, approval_type,
                        valid_from, valid_to, reason, use_yn, created_by, updated_by
                    ) VALUES (
                        :delegatorEmployeeNo, :delegateEmployeeNo, :businessDomain, :approvalType,
                        :validFrom, :validTo, :reason, :useYn, :operatorId, :operatorId
                    )
                    """, values);
        } else {
            jdbc().update("""
                    UPDATE bza_approval_delegation
                       SET delegator_employee_no=:delegatorEmployeeNo, delegate_employee_no=:delegateEmployeeNo,
                           business_domain=:businessDomain, approval_type=:approvalType,
                           valid_from=:validFrom, valid_to=:validTo, reason=:reason,
                           use_yn=:useYn, updated_by=:operatorId
                     WHERE delegation_id=:delegationId
                    """, values);
        }
    }

    public Optional<Long> findApprovalByIdempotencyKey(String key) {
        return jdbc().queryForList("""
                SELECT approval_id AS approvalId FROM bza_approval_document
                 WHERE request_idempotency_key=:key
                """, new MapSqlParameterSource("key", key)).stream()
                .map(row -> ((Number)row.get("approvalId")).longValue()).findFirst();
    }

    public long insertPolicyApproval(Map<String,Object> values) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc().update("""
                INSERT INTO bza_approval_document (
                    approval_no, approval_type, business_domain, policy_code, policy_version,
                    policy_snapshot_json, title, requester_employee_no,
                    requester_organization_code, requester_position_code, requester_job_title_code,
                    approval_status, approval_mode, current_step_no, due_at,
                    payload_json, payload_hash, request_idempotency_key, attachment_group_id,
                    resubmitted_from_approval_id, version_no, transaction_id, submitted_at, created_by, updated_by
                ) VALUES (
                    :approvalNo, :approvalType, :businessDomain, :policyCode, :policyVersion,
                    :policySnapshotJson, :title, :requesterEmployeeNo,
                    :requesterOrganizationCode, :requesterPositionCode, :requesterJobTitleCode,
                    'IN_REVIEW', :approvalMode, 1, :dueAt,
                    :payloadJson, :payloadHash, :requestIdempotencyKey, :attachmentGroupId,
                    :resubmittedFromApprovalId, 0, :transactionId, CURRENT_TIMESTAMP(3), :operatorId, :operatorId
                )
                """, new MapSqlParameterSource(values), key, new String[]{"approval_id"});
        Number id = key.getKey();
        if (id == null) throw new IllegalStateException("BZA policy approval 생성 키를 확인할 수 없습니다.");
        return id.longValue();
    }

    public long insertLine(Map<String,Object> values) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc().update("""
                INSERT INTO bza_approval_line (
                    approval_id, step_no, approver_employee_no, step_type, target_type, target_code,
                    target_name_snapshot, decision_rule, required_count, required_yn,
                    decision_status, created_by, updated_by
                ) VALUES (
                    :approvalId, :stepNo, :directApprover, :stepType, :targetType, :targetCode,
                    :targetName, :decisionRule, :requiredCount, :requiredYn,
                    'WAITING', :operatorId, :operatorId
                )
                """, new MapSqlParameterSource(values), key, new String[]{"approval_line_id"});
        Number id = key.getKey();
        if (id == null) throw new IllegalStateException("BZA approval line 생성 키를 확인할 수 없습니다.");
        return id.longValue();
    }

    public void insertParticipant(Map<String,Object> values) {
        jdbc().update("""
                INSERT INTO bza_approval_participant (
                    approval_id, approval_line_id, step_no, approver_employee_no,
                    approver_name_snapshot, organization_code_snapshot, position_code_snapshot,
                    job_title_code_snapshot, delegated_from_employee_no, resolution_source,
                    decision_status, created_by, updated_by
                )
                SELECT :approvalId, :approvalLineId, :stepNo, :approverEmployeeNo,
                       e.employee_name, :organizationCode, :positionCode,
                       :jobTitleCode, :delegatedFrom, :resolutionSource,
                       'WAITING', :operatorId, :operatorId
                  FROM bza_employee e WHERE e.employee_no=:approverEmployeeNo
                """, values);
    }

    public List<Map<String,Object>> findParticipants(long approvalId) {
        return jdbc().queryForList("""
                SELECT p.approval_participant_id AS approvalParticipantId, p.approval_id AS approvalId,
                       p.approval_line_id AS approvalLineId, p.step_no AS stepNo,
                       p.approver_employee_no AS approverEmployeeNo,
                       p.approver_name_snapshot AS approverNameSnapshot,
                       p.organization_code_snapshot AS organizationCodeSnapshot,
                       p.position_code_snapshot AS positionCodeSnapshot,
                       p.job_title_code_snapshot AS jobTitleCodeSnapshot,
                       p.delegated_from_employee_no AS delegatedFromEmployeeNo,
                       p.resolution_source AS resolutionSource, p.decision_status AS decisionStatus,
                       p.decision_comment AS decisionComment, p.decided_at AS decidedAt,
                       l.step_type AS stepType, l.decision_rule AS decisionRule,
                       l.required_count AS requiredCount, l.required_yn AS requiredYn
                  FROM bza_approval_participant p
                  JOIN bza_approval_line l ON l.approval_line_id=p.approval_line_id
                 WHERE p.approval_id=:approvalId
                 ORDER BY p.step_no, p.approval_line_id, p.approval_participant_id
                """, new MapSqlParameterSource("approvalId", approvalId));
    }


    public Optional<String> findEmployeeNoByLoginId(String loginId) {
        return jdbc().queryForList("""
                SELECT e.employee_no AS employeeNo
                  FROM bza_admin_user u
                  JOIN bza_employee e ON e.admin_user_id=u.admin_user_id
                 WHERE u.admin_login_id=:loginId AND u.use_yn='Y' AND e.use_yn='Y'
                """, new MapSqlParameterSource("loginId", loginId)).stream()
                .map(row -> Objects.toString(row.get("employeeNo"), null))
                .filter(Objects::nonNull).findFirst();
    }

    public Optional<Map<String,Object>> findDocument(long approvalId) {
        return jdbc().queryForList("""
                SELECT approval_id AS approvalId, approval_no AS approvalNo,
                       approval_type AS approvalType, business_domain AS businessDomain,
                       policy_code AS policyCode, policy_version AS policyVersion,
                       title, requester_employee_no AS requesterEmployeeNo,
                       approval_status AS approvalStatus, approval_mode AS approvalMode,
                       current_step_no AS currentStepNo, version_no AS versionNo,
                       due_at AS dueAt, payload_json AS payloadJson, payload_hash AS payloadHash,
                       attachment_group_id AS attachmentGroupId,
                       resubmitted_from_approval_id AS resubmittedFromApprovalId, transaction_id AS transactionId
                  FROM bza_approval_document WHERE approval_id=:approvalId
                """, new MapSqlParameterSource("approvalId", approvalId)).stream().findFirst();
    }

    public Optional<Map<String,Object>> findWaitingParticipant(long approvalId, String employeeNo, String approvalMode, int currentStep) {
        String stepClause = "SEQUENTIAL".equalsIgnoreCase(approvalMode) ? " AND p.step_no=:currentStep " : "";
        return jdbc().queryForList("""
                SELECT p.approval_participant_id AS participantId, p.approval_line_id AS lineId,
                       p.step_no AS stepNo, p.decision_status AS decisionStatus,
                       l.step_type AS stepType, l.decision_rule AS decisionRule,
                       l.required_count AS requiredCount, l.required_yn AS requiredYn
                  FROM bza_approval_participant p
                  JOIN bza_approval_line l ON l.approval_line_id=p.approval_line_id
                 WHERE p.approval_id=:approvalId AND p.approver_employee_no=:employeeNo
                   AND p.decision_status='WAITING'
                """ + stepClause + """
                 ORDER BY p.step_no, p.approval_participant_id
                """, new MapSqlParameterSource()
                .addValue("approvalId", approvalId)
                .addValue("employeeNo", employeeNo)
                .addValue("currentStep", currentStep)).stream().findFirst();
    }

    public boolean participantDecisionExists(String idempotencyKey) {
        Long count = jdbc().queryForObject("""
                SELECT COUNT(*) FROM bza_approval_participant WHERE idempotency_key=:key
                """, new MapSqlParameterSource("key", idempotencyKey), Long.class);
        return count != null && count > 0;
    }

    public int decideParticipant(long participantId, String decisionStatus, String idempotencyKey, String comment, String operatorId) {
        return jdbc().update("""
                UPDATE bza_approval_participant
                   SET decision_status=:decisionStatus, idempotency_key=:idempotencyKey,
                       decision_comment=:comment, decided_at=CURRENT_TIMESTAMP(3), updated_by=:operatorId
                 WHERE approval_participant_id=:participantId AND decision_status='WAITING'
                """, new MapSqlParameterSource()
                .addValue("participantId", participantId)
                .addValue("decisionStatus", decisionStatus)
                .addValue("idempotencyKey", idempotencyKey)
                .addValue("comment", comment)
                .addValue("operatorId", operatorId));
    }

    public Map<String,Object> participantCounts(long lineId) {
        return jdbc().queryForMap("""
                SELECT COUNT(*) AS participantCount,
                       SUM(CASE WHEN decision_status IN ('APPROVED','AGREED') THEN 1 ELSE 0 END) AS approvedCount,
                       SUM(CASE WHEN decision_status='REJECTED' THEN 1 ELSE 0 END) AS rejectedCount
                  FROM bza_approval_participant WHERE approval_line_id=:lineId
                """, new MapSqlParameterSource("lineId", lineId));
    }

    public int updateLineStatus(long lineId, String status, String comment, String operatorId) {
        return jdbc().update("""
                UPDATE bza_approval_line
                   SET decision_status=:status, decision_comment=:comment,
                       decided_at=CASE WHEN :status='WAITING' THEN NULL ELSE CURRENT_TIMESTAMP END,
                       updated_by=:operatorId
                 WHERE approval_line_id=:lineId
                """, new MapSqlParameterSource()
                .addValue("lineId", lineId).addValue("status", status)
                .addValue("comment", comment).addValue("operatorId", operatorId));
    }

    public List<Map<String,Object>> findLineStatuses(long approvalId) {
        return jdbc().queryForList("""
                SELECT approval_line_id AS lineId, step_no AS stepNo, step_type AS stepType,
                       decision_rule AS decisionRule, required_count AS requiredCount,
                       required_yn AS requiredYn, decision_status AS decisionStatus
                  FROM bza_approval_line
                 WHERE approval_id=:approvalId
                 ORDER BY step_no, approval_line_id
                """, new MapSqlParameterSource("approvalId", approvalId));
    }

    public boolean historyActionExists(String idempotencyKey) {
        Long count = jdbc().queryForObject("""
                SELECT COUNT(*) FROM bza_approval_history WHERE idempotency_key=:key
                """, new MapSqlParameterSource("key", idempotencyKey), Long.class);
        return count != null && count > 0;
    }

    public List<Long> findDueApprovalIds(Instant now, int limit) {
        return jdbc().queryForList("""
                SELECT approval_id AS approvalId
                  FROM bza_approval_document
                 WHERE approval_status='IN_REVIEW' AND due_at IS NOT NULL AND due_at <= :now
                 ORDER BY due_at, approval_id
                 LIMIT :limit
                """, new MapSqlParameterSource().addValue("now", Timestamp.from(now)).addValue("limit", limit))
                .stream().map(row -> ((Number) row.get("approvalId")).longValue()).toList();
    }

    public int updateDocumentStatus(long approvalId, long versionNo, String status, int currentStep, boolean completed, String operatorId) {
        return jdbc().update("""
                UPDATE bza_approval_document
                   SET approval_status=:status, current_step_no=:currentStep,
                       version_no=version_no+1, updated_by=:operatorId,
                       completed_at=CASE WHEN :completed=1 THEN CURRENT_TIMESTAMP(3) ELSE completed_at END
                 WHERE approval_id=:approvalId AND version_no=:versionNo
                """, new MapSqlParameterSource()
                .addValue("approvalId", approvalId).addValue("versionNo", versionNo)
                .addValue("status", status).addValue("currentStep", currentStep)
                .addValue("completed", completed ? 1 : 0).addValue("operatorId", operatorId));
    }

    public void insertHistory(long approvalId, String actionType, String actorEmployeeNo,
                              String idempotencyKey, String reason, String beforeStatus,
                              String afterStatus, String comment, String transactionId, String operatorId) {
        jdbc().update("""
                INSERT INTO bza_approval_history (
                    approval_id, action_type, actor_employee_no, idempotency_key,
                    reason, before_status, after_status, comment_text, transaction_id,
                    created_by, updated_by
                ) VALUES (
                    :approvalId, :actionType, :actorEmployeeNo, :idempotencyKey,
                    :reason, :beforeStatus, :afterStatus, :comment, :transactionId,
                    :operatorId, :operatorId
                )
                """, new MapSqlParameterSource()
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
