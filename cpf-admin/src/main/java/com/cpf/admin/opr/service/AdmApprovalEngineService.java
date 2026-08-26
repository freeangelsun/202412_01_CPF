package com.cpf.admin.opr.service;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.batch.api.CpfBatchRiskCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @deprecated V9 canonical state owner is {@code com.cpf.admin.approval.service.AdmApprovalService}.
 * This source remains only for source compatibility and is intentionally not a Spring bean.
 *
 * <p>Legacy versioned ADM Approval Engine. Policy, participant snapshots, decisions and the immutable
 * owner-command snapshot are persisted before a dangerous owner command can execute. Work-package
 * evidence may be shared, but every request keeps its own policy version, participants, command hash
 * and execution ledger.</p>
 */
@Deprecated(forRemoval = true, since = "V9")
public class AdmApprovalEngineService extends com.cpf.admin.common.base.AdmBaseService {
    private static final Set<String> SENSITIVE_INLINE_KEYS = Set.of(
            "password", "passwd", "secret", "token", "authorization", "apikey", "api_key");

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final List<AdmApprovalOwnerCommandDispatcher> dispatchers;
    private final AdmApprovalDecisionEvaluator decisionEvaluator = new AdmApprovalDecisionEvaluator();

    @Deprecated
    public AdmApprovalEngineService(
            @Qualifier("admJdbcTemplate") JdbcTemplate jdbc,
            ObjectMapper objectMapper,
            List<AdmApprovalOwnerCommandDispatcher> dispatchers) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.dispatchers = List.copyOf(dispatchers);
    }

    @Deprecated
    public List<Map<String, Object>> policies(String actionType) {
        String select = "SELECT policy_code AS policyCode,policy_version AS policyVersion,"
                + "policy_name AS policyName,action_type AS actionType,effective_from AS effectiveFrom,"
                + "effective_to AS effectiveTo,enabled_yn AS enabledYn,"
                + "self_approval_allowed_yn AS selfApprovalAllowedYn,"
                + "break_glass_allowed_yn AS breakGlassAllowedYn,description "
                + "FROM adm_approval_policy";
        if (actionType == null || actionType.isBlank()) {
            return jdbc.queryForList(select + " ORDER BY action_type,policy_code,policy_version DESC");
        }
        return jdbc.queryForList(
                select + " WHERE action_type=? ORDER BY policy_code,policy_version DESC",
                actionType.trim().toUpperCase(Locale.ROOT));
    }

    @Deprecated
    public Map<String, Object> policy(String code, int version) {
        String policyCode = required(code, "policyCode");
        int policyVersion = positive(version, "policyVersion");
        Map<String, Object> result = new LinkedHashMap<>(jdbc.queryForMap(
                "SELECT policy_code AS policyCode,policy_version AS policyVersion,"
                        + "policy_name AS policyName,action_type AS actionType,effective_from AS effectiveFrom,"
                        + "effective_to AS effectiveTo,enabled_yn AS enabledYn,"
                        + "self_approval_allowed_yn AS selfApprovalAllowedYn,"
                        + "break_glass_allowed_yn AS breakGlassAllowedYn,description "
                        + "FROM adm_approval_policy WHERE policy_code=? AND policy_version=?",
                policyCode, policyVersion));
        result.put("steps", jdbc.queryForList(
                "SELECT step_no AS stepNo,step_type AS stepType,target_type AS targetType,"
                        + "target_code AS targetCode,decision_rule AS decisionRule,"
                        + "required_count AS requiredCount,required_yn AS requiredYn "
                        + "FROM adm_approval_policy_step WHERE policy_code=? AND policy_version=? "
                        + "ORDER BY step_no,target_type,target_code",
                policyCode, policyVersion));
        return result;
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    @Deprecated
    public Map<String, Object> savePolicy(PolicyCommand command, String operator) {
        Objects.requireNonNull(command, "command");
        String actor = required(operator, "operatorId");
        if (command.steps() == null || command.steps().isEmpty()) {
            throw new IllegalArgumentException("approval policy requires at least one step");
        }
        validatePolicySteps(command.steps());
        Timestamp effectiveFrom = requiredTimestamp(command.effectiveFrom(), "effectiveFrom");
        Timestamp effectiveTo = timestamp(command.effectiveTo());
        if (effectiveTo != null && !effectiveTo.after(effectiveFrom)) {
            throw new IllegalArgumentException("effectiveTo must be after effectiveFrom");
        }
        try {
            jdbc.update(
                    "INSERT INTO adm_approval_policy(policy_code,policy_version,policy_name,action_type,"
                            + "effective_from,effective_to,enabled_yn,self_approval_allowed_yn,"
                            + "break_glass_allowed_yn,description,created_by,updated_by) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                    required(command.policyCode(), "policyCode"),
                    positive(command.policyVersion(), "policyVersion"),
                    required(command.policyName(), "policyName"),
                    required(command.actionType(), "actionType").toUpperCase(Locale.ROOT),
                    effectiveFrom, effectiveTo, flag(command.enabledYn()),
                    flag(command.selfApprovalAllowedYn()), flag(command.breakGlassAllowedYn()),
                    nullable(command.description()), actor, actor);
        } catch (DataIntegrityViolationException duplicate) {
            throw new IllegalStateException("approval policy version already exists", duplicate);
        }
        for (StepCommand step : command.steps()) {
            String rule = required(step.decisionRule(), "decisionRule").toUpperCase(Locale.ROOT);
            Integer count = "N_OF_M".equals(rule)
                    ? positive(step.requiredCount() == null ? 0 : step.requiredCount(), "requiredCount")
                    : null;
            jdbc.update(
                    "INSERT INTO adm_approval_policy_step(policy_code,policy_version,step_no,step_type,"
                            + "target_type,target_code,decision_rule,required_count,required_yn,"
                            + "created_by,updated_by) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                    command.policyCode(), command.policyVersion(), positive(step.stepNo(), "stepNo"),
                    required(step.stepType(), "stepType").toUpperCase(Locale.ROOT),
                    required(step.targetType(), "targetType").toUpperCase(Locale.ROOT),
                    required(step.targetCode(), "targetCode"), rule, count,
                    flag(step.requiredYn()), actor, actor);
        }
        return policy(command.policyCode(), command.policyVersion());
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    @Deprecated
    public Map<String, Object> createRequest(RequestCommand command, String operator) {
        Objects.requireNonNull(command, "command");
        String actor = required(operator, "operatorId");
        Map<String, Object> policy = resolvePolicy(command);
        String requestKey = required(command.requestKey(), "requestKey");
        String ownerModule = normalizeOwner(command.ownerModule());
        String ownerCommand = required(command.ownerCommand(), "ownerCommand");
        String action = required(command.actionType(), "actionType").toUpperCase(Locale.ROOT);
        String targetType = required(command.targetType(), "targetType");
        String targetId = required(command.targetId(), "targetId");
        String reason = required(command.reason(), "reason");
        Timestamp expires = requiredTimestamp(command.expireAt(), "expireAt");
        if (!expires.toInstant().isAfter(Instant.now())) {
            throw new IllegalArgumentException("expireAt must be in the future");
        }
        Map<String, Object> payloadObject = readObject(command.payloadSnapshot());
        rejectInlineSecrets(payloadObject, "payloadSnapshot");
        String normalizedPayload = write(payloadObject);

        Map<String, Object> existing = requestByKeyOrNull(requestKey);
        if (existing != null) {
            assertIdempotentRequest(existing, command, actor, policy, normalizedPayload);
            return request(number(existing, "approval_request_id"));
        }

        KeyHolder keys = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                var ps = connection.prepareStatement(
                        "INSERT INTO adm_approval_request(request_key,policy_code,policy_version,"
                                + "action_type,owner_module,owner_command,target_type,target_id,requested_by,"
                                + "request_reason,command_payload_hash,command_payload_snapshot,approval_status,"
                                + "current_step_no,expire_at,created_by,updated_by) "
                                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,'PENDING',1,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                int i = 1;
                ps.setObject(i++, requestKey);
                ps.setObject(i++, value(policy, "policyCode"));
                ps.setObject(i++, value(policy, "policyVersion"));
                ps.setObject(i++, action);
                ps.setObject(i++, ownerModule);
                ps.setObject(i++, ownerCommand);
                ps.setObject(i++, targetType);
                ps.setObject(i++, targetId);
                ps.setObject(i++, actor);
                ps.setObject(i++, reason);
                ps.setObject(i++, "0".repeat(64));
                ps.setObject(i++, normalizedPayload);
                ps.setObject(i++, expires);
                ps.setObject(i++, actor);
                ps.setObject(i, actor);
                return ps;
            }, keys);
        } catch (DataIntegrityViolationException concurrentDuplicate) {
            throw new IllegalStateException(
                    "approval request key was created concurrently; retry with the same requestKey",
                    concurrentDuplicate);
        }

        long requestId = generatedId(keys);
        String canonicalSnapshot = normalizedPayload;
        String hash = sha256(normalizedPayload);
        if ("BAT".equals(ownerModule)) {
            CpfBatchRiskCommand risk = buildBatCommand(requestId, requestKey, actor, command, payloadObject);
            hash = risk.fingerprint();
            canonicalSnapshot = write(riskSnapshot(risk));
        }
        int updated = jdbc.update(
                "UPDATE adm_approval_request SET command_payload_hash=?,command_payload_snapshot=?,"
                        + "updated_by=? WHERE approval_request_id=? AND approval_status='PENDING'",
                hash, canonicalSnapshot, actor, requestId);
        if (updated != 1) {
            throw new IllegalStateException("approval command snapshot could not be finalized");
        }
        createParticipants(
                requestId,
                String.valueOf(value(policy, "policyCode")),
                ((Number) value(policy, "policyVersion")).intValue(),
                actor,
                "Y".equalsIgnoreCase(String.valueOf(value(policy, "selfApprovalAllowedYn"))));
        return request(requestId);
    }

    @Deprecated
    public Map<String, Object> request(long id) {
        Map<String, Object> result = new LinkedHashMap<>(jdbc.queryForMap(
                "SELECT approval_request_id AS requestId,request_key AS requestKey,"
                        + "policy_code AS policyCode,policy_version AS policyVersion,action_type AS actionType,"
                        + "owner_module AS ownerModule,owner_command AS ownerCommand,target_type AS targetType,"
                        + "target_id AS targetId,requested_by AS requestedBy,request_reason AS reason,"
                        + "command_payload_hash AS commandPayloadHash,"
                        + "command_payload_snapshot AS commandPayloadSnapshot,approval_status AS approvalStatus,"
                        + "current_step_no AS currentStepNo,expire_at AS expireAt,transaction_id AS transactionId,"
                        + "version_no AS versionNo,created_at AS createdAt,updated_at AS updatedAt "
                        + "FROM adm_approval_request WHERE approval_request_id=?",
                positiveLong(id, "requestId")));
        result.put("participants", jdbc.queryForList(
                "SELECT approval_participant_id AS participantId,step_no AS stepNo,operator_id AS operatorId,"
                        + "source_target_type AS sourceTargetType,source_target_code AS sourceTargetCode,"
                        + "organization_code_snapshot AS organizationCodeSnapshot,"
                        + "position_code_snapshot AS positionCodeSnapshot,"
                        + "job_title_code_snapshot AS jobTitleCodeSnapshot,decision_status AS decisionStatus,"
                        + "idempotency_key AS idempotencyKey,decision_reason AS decisionReason,"
                        + "decided_at AS decidedAt FROM adm_approval_participant "
                        + "WHERE approval_request_id=? ORDER BY step_no,operator_id",
                id));
        try {
            result.put("execution", jdbc.queryForMap(
                    "SELECT command_request_id AS commandRequestId,execution_status AS executionStatus,"
                            + "owner_result_code AS ownerResultCode,owner_result_message AS ownerResultMessage,"
                            + "started_at AS startedAt,completed_at AS completedAt,"
                            + "recovery_required_yn AS recoveryRequiredYn "
                            + "FROM adm_approval_execution WHERE approval_request_id=?",
                    id));
        } catch (EmptyResultDataAccessException ignored) {
            // Execution has not started.
        }
        return result;
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    @Deprecated
    public Map<String, Object> decide(long id, DecisionCommand command, String operator) {
        Objects.requireNonNull(command, "command");
        String actor = required(operator, "operatorId");
        String action = required(command.action(), "action").toUpperCase(Locale.ROOT);
        if (!List.of("APPROVE", "REJECT").contains(action)) {
            throw new IllegalArgumentException("decision action must be APPROVE or REJECT");
        }
        Map<String, Object> req = request(id);
        String status = text(req, "approvalStatus").toUpperCase(Locale.ROOT);
        if (!"PENDING".equals(status)) return req;
        assertNotExpired(req, actor, id);

        int currentStep = ((Number) value(req, "currentStepNo")).intValue();
        Map<String, Object> participant;
        try {
            participant = jdbc.queryForMap(
                    "SELECT approval_participant_id,step_no,decision_status,idempotency_key "
                            + "FROM adm_approval_participant "
                            + "WHERE approval_request_id=? AND step_no=? AND operator_id=?",
                    id, currentStep, actor);
        } catch (EmptyResultDataAccessException denied) {
            throw new IllegalStateException("operator is not a participant of the current approval step", denied);
        }
        String prior = text(participant, "decision_status");
        String idempotencyKey = required(command.idempotencyKey(), "idempotencyKey");
        if (!"WAITING".equalsIgnoreCase(prior)) {
            if (idempotencyKey.equals(nullableText(participant, "idempotency_key"))) return request(id);
            throw new IllegalStateException("approval decision is already finalized");
        }
        int changed = jdbc.update(
                "UPDATE adm_approval_participant SET decision_status=?,idempotency_key=?,"
                        + "decision_reason=?,decided_at=CURRENT_TIMESTAMP,updated_by=? "
                        + "WHERE approval_participant_id=? AND decision_status='WAITING'",
                action.equals("APPROVE") ? "APPROVED" : "REJECTED",
                idempotencyKey, required(command.reason(), "reason"), actor,
                value(participant, "approval_participant_id"));
        if (changed != 1) throw new IllegalStateException("approval decision changed concurrently");
        evaluate(id, actor);
        return request(id);
    }

    /**
     * 승인된 Owner Command를 실행합니다. 실행 Ledger를 먼저 예약하고 CAS로 RUNNING을 획득한
     * 인스턴스만 실제 Owner를 호출하므로 재호출/다중 인스턴스에서도 side effect가 중복되지 않습니다.
     * Owner 호출 후 저장 결과를 확정할 수 없으면 실패로 추정하지 않고 UNKNOWN으로 보존합니다.
     */
    @Deprecated
    public Object execute(long id, String operator) {
        String actor = required(operator, "operatorId");
        Map<String, Object> req = request(id);
        String approvalStatus = text(req, "approvalStatus").toUpperCase(Locale.ROOT);
        if (!Set.of("APPROVED", "EXECUTING", "COMPLETED", "FAILED", "UNKNOWN").contains(approvalStatus)) {
            throw new IllegalStateException("only APPROVED requests can execute");
        }
        assertNotExpired(req, actor, id);

        Map<String, Object> execution = reserveExecution(id, req, actor);
        String prior = text(execution, "execution_status").toUpperCase(Locale.ROOT);
        if (Set.of("SUCCEEDED", "FAILED", "UNKNOWN", "RECOVERED").contains(prior)) {
            // 동일 승인 요청의 재호출은 Owner를 다시 호출하지 않고 현재 Ledger를 반환한다.
            return request(id);
        }
        if ("RUNNING".equals(prior)) return request(id);

        int reserved = jdbc.update(
                "UPDATE adm_approval_execution SET execution_status='RUNNING',started_at=CURRENT_TIMESTAMP,"
                        + "recovery_required_yn='N',updated_by=? "
                        + "WHERE approval_request_id=? AND execution_status='PENDING'",
                actor, id);
        if (reserved != 1) return request(id);
        jdbc.update(
                "UPDATE adm_approval_request SET approval_status='EXECUTING',version_no=version_no+1,updated_by=? "
                        + "WHERE approval_request_id=? AND approval_status='APPROVED'",
                actor, id);

        String owner = text(req, "ownerModule");
        String ownerCommand = text(req, "ownerCommand");
        AdmApprovalOwnerCommandDispatcher dispatcher = dispatchers.stream()
                .filter(candidate -> candidate.supports(owner, ownerCommand))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "no Owner Command dispatcher: " + owner + "/" + ownerCommand));
        try {
            Object ownerResult = dispatcher.execute(req);
            String classified = classifyOwnerResult(ownerResult);
            if ("UNKNOWN".equals(classified)) {
                finishExecution(id, actor, "UNKNOWN", "OWNER_RESULT_UNKNOWN",
                        summarizeOwnerResult(ownerResult), true);
                return request(id);
            }
            if ("FAILED".equals(classified)) {
                finishExecution(id, actor, "FAILED", "OWNER_COMMAND_FAILED",
                        summarizeOwnerResult(ownerResult), false);
                return ownerResult;
            }
            finishExecution(id, actor, "SUCCEEDED", "OWNER_COMMAND_SUCCEEDED",
                    summarizeOwnerResult(ownerResult), false);
            return ownerResult;
        } catch (RuntimeException ownerFailure) {
            finishExecution(id, actor, "UNKNOWN", "OWNER_COMMAND_RESULT_UNKNOWN",
                    ownerFailure.getClass().getSimpleName() + ":" + nullable(ownerFailure.getMessage()), true);
            throw new IllegalStateException("Owner command result is UNKNOWN; reconcile is required", ownerFailure);
        }
    }

    private Map<String, Object> reserveExecution(long id, Map<String, Object> request, String actor) {
        String commandRequestId = text(request, "requestKey") + ":owner";
        try {
            jdbc.update(
                    "INSERT INTO adm_approval_execution(approval_request_id,command_request_id,execution_status,"
                            + "recovery_required_yn,created_by,updated_by) VALUES(?,?,'PENDING','N',?,?)",
                    id, commandRequestId, actor, actor);
        } catch (DataIntegrityViolationException replay) {
            // approval_request_id/command_request_id unique key가 멱등 재호출을 단일 Ledger로 수렴시킨다.
        }
        return jdbc.queryForMap(
                "SELECT approval_request_id,command_request_id,execution_status,owner_result_code,"
                        + "owner_result_message,recovery_required_yn FROM adm_approval_execution "
                        + "WHERE approval_request_id=?", id);
    }

    private void finishExecution(
            long id, String actor, String executionStatus, String resultCode,
            String resultMessage, boolean recoveryRequired) {
        int executionChanged = jdbc.update(
                "UPDATE adm_approval_execution SET execution_status=?,owner_result_code=?,owner_result_message=?,"
                        + "completed_at=CURRENT_TIMESTAMP,recovery_required_yn=?,updated_by=? "
                        + "WHERE approval_request_id=? AND execution_status='RUNNING'",
                executionStatus, resultCode, boundedText(resultMessage, 1000),
                recoveryRequired ? "Y" : "N", actor, id);
        if (executionChanged != 1) {
            throw new IllegalStateException("approval execution finalization changed concurrently");
        }
        String requestStatus = switch (executionStatus) {
            case "SUCCEEDED", "RECOVERED" -> "COMPLETED";
            case "FAILED" -> "FAILED";
            default -> "UNKNOWN";
        };
        int requestChanged = jdbc.update(
                "UPDATE adm_approval_request SET approval_status=?,version_no=version_no+1,updated_by=? "
                        + "WHERE approval_request_id=? AND approval_status='EXECUTING'",
                requestStatus, actor, id);
        if (requestChanged != 1) {
            // Owner side effect 이후 request 상태 저장 실패는 결과를 추정할 수 없으므로 execution도 UNKNOWN으로 강등한다.
            jdbc.update(
                    "UPDATE adm_approval_execution SET execution_status='UNKNOWN',recovery_required_yn='Y',"
                            + "owner_result_code='ADM_FINALIZATION_UNKNOWN',updated_by=? WHERE approval_request_id=?",
                    actor, id);
            throw new IllegalStateException("approval request finalization is UNKNOWN");
        }
    }

    private String classifyOwnerResult(Object ownerResult) {
        if (ownerResult == null) return "UNKNOWN";
        String text = String.valueOf(ownerResult).toUpperCase(Locale.ROOT);
        if (text.contains("UNKNOWN_RESULT") || text.contains("UNKNOWN")) return "UNKNOWN";
        if (text.contains("FAILED") || text.contains("REJECTED") || text.contains("ERROR")) return "FAILED";
        return "SUCCEEDED";
    }

    private String summarizeOwnerResult(Object ownerResult) {
        return boundedText(ownerResult == null ? "null owner result" : String.valueOf(ownerResult), 1000);
    }

    private static String boundedText(String value, int max) {
        String text = value == null ? "" : value.replaceAll("[\r\n\t]+", " ").trim();
        return text.length() <= max ? text : text.substring(0, max);
    }

    private Map<String, Object> resolvePolicy(RequestCommand command) {
        String action = required(command.actionType(), "actionType").toUpperCase(Locale.ROOT);
        if (command.policyCode() != null && !command.policyCode().isBlank()) {
            Map<String, Object> selected = policy(
                    command.policyCode(),
                    positive(command.policyVersion() == null ? 0 : command.policyVersion(), "policyVersion"));
            assertPolicyActive(selected, action);
            return selected;
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT policy_code AS policyCode,policy_version AS policyVersion,policy_name AS policyName,"
                        + "action_type AS actionType,self_approval_allowed_yn AS selfApprovalAllowedYn "
                        + "FROM adm_approval_policy WHERE action_type=? AND enabled_yn='Y' "
                        + "AND effective_from<=CURRENT_TIMESTAMP "
                        + "AND (effective_to IS NULL OR effective_to>CURRENT_TIMESTAMP) "
                        + "ORDER BY policy_version DESC",
                action);
        if (rows.isEmpty()) throw new IllegalStateException("no active approval policy for action");
        return rows.get(0);
    }

    private void assertPolicyActive(Map<String, Object> policy, String action) {
        if (!text(policy, "actionType").equalsIgnoreCase(action)) {
            throw new IllegalArgumentException("policy action type mismatch");
        }
        if (!"Y".equalsIgnoreCase(nullableText(policy, "enabledYn"))) {
            throw new IllegalStateException("approval policy is disabled");
        }
        Instant now = Instant.now();
        Instant from = instant(value(policy, "effectiveFrom"));
        Instant to = instant(value(policy, "effectiveTo"));
        if (from != null && from.isAfter(now)) throw new IllegalStateException("approval policy is not effective yet");
        if (to != null && !to.isAfter(now)) throw new IllegalStateException("approval policy has expired");
    }

    private void createParticipants(
            long requestId, String policyCode, int policyVersion, String requester, boolean selfAllowed) {
        List<Map<String, Object>> targets = jdbc.queryForList(
                "SELECT step_no,target_type,target_code FROM adm_approval_policy_step "
                        + "WHERE policy_code=? AND policy_version=? AND required_yn='Y' ORDER BY step_no",
                policyCode, policyVersion);
        if (targets.isEmpty()) throw new IllegalStateException("approval policy has no required steps");

        Set<String> insertedKeys = new HashSet<>();
        int inserted = 0;
        for (Map<String, Object> targetRow : targets) {
            int stepNo = ((Number) value(targetRow, "step_no")).intValue();
            String targetType = text(targetRow, "target_type");
            String targetCode = text(targetRow, "target_code");
            for (String approver : resolveOperators(targetType, targetCode)) {
                if (!selfAllowed && requester.equals(approver)) continue;
                if (!insertedKeys.add(stepNo + "\u0000" + approver)) continue;
                Map<String, Object> profile = profile(approver);
                jdbc.update(
                        "INSERT INTO adm_approval_participant(approval_request_id,step_no,operator_id,"
                                + "source_target_type,source_target_code,organization_code_snapshot,"
                                + "position_code_snapshot,job_title_code_snapshot,decision_status,"
                                + "created_by,updated_by) VALUES(?,?,?,?,?,?,?,?, 'WAITING',?,?)",
                        requestId, stepNo, approver, targetType, targetCode,
                        value(profile, "organization_code"), value(profile, "position_code"),
                        value(profile, "job_title_code"), requester, requester);
                inserted++;
            }
        }
        if (inserted == 0) {
            throw new IllegalStateException("approval policy resolved no independent participants");
        }
        for (Integer stepNo : requiredStepNumbers(policyCode, policyVersion)) {
            Long count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM adm_approval_participant "
                            + "WHERE approval_request_id=? AND step_no=?",
                    Long.class, requestId, stepNo);
            if (count == null || count == 0) {
                throw new IllegalStateException("approval step resolved no participants: " + stepNo);
            }
        }
    }

    private List<Integer> requiredStepNumbers(String policyCode, int policyVersion) {
        List<Number> values = jdbc.queryForList(
                "SELECT DISTINCT step_no FROM adm_approval_policy_step "
                        + "WHERE policy_code=? AND policy_version=? AND required_yn='Y' ORDER BY step_no",
                Number.class, policyCode, policyVersion);
        List<Integer> result = new ArrayList<>(values.size());
        for (Number value : values) result.add(value.intValue());
        return result;
    }

    private List<String> resolveOperators(String type, String code) {
        return switch (type.toUpperCase(Locale.ROOT)) {
            case "OPERATOR" -> List.of(code);
            case "ROLE" -> jdbc.queryForList(
                    "SELECT r.operator_id FROM adm_operator_role r JOIN adm_operator o "
                            + "ON o.operator_id=r.operator_id WHERE r.role_id=? "
                            + "AND o.account_status='ACTIVE' AND o.use_yn='Y'",
                    String.class, code);
            case "ORGANIZATION" -> jdbc.queryForList(
                    "SELECT p.operator_id FROM adm_operator_profile p JOIN adm_operator o "
                            + "ON o.operator_id=p.operator_id WHERE p.organization_code=? "
                            + "AND (p.effective_to IS NULL OR p.effective_to>CURRENT_TIMESTAMP) "
                            + "AND o.account_status='ACTIVE' AND o.use_yn='Y'",
                    String.class, code);
            case "ORG_MANAGER" -> jdbc.queryForList(
                    "SELECT manager_operator_id FROM adm_organization WHERE organization_code=? "
                            + "AND use_yn='Y' AND manager_operator_id IS NOT NULL",
                    String.class, code);
            default -> throw new IllegalArgumentException("unsupported approval target type: " + type);
        };
    }

    private Map<String, Object> profile(String operator) {
        try {
            return jdbc.queryForMap(
                    "SELECT organization_code,position_code,job_title_code "
                            + "FROM adm_operator_profile WHERE operator_id=? "
                            + "AND (effective_to IS NULL OR effective_to>CURRENT_TIMESTAMP)",
                    operator);
        } catch (EmptyResultDataAccessException missing) {
            return Map.of();
        }
    }

    /**
     * Evaluate each unique approval step independently. Policy target rows must never multiply
     * participant counts; otherwise a multi-target N_OF_M step can approve too early.
     */
    private void evaluate(long requestId, String actor) {
        Map<String, Object> request = request(requestId);
        int currentStep = ((Number) value(request, "currentStepNo")).intValue();
        Long rejected = jdbc.queryForObject(
                "SELECT COUNT(*) FROM adm_approval_participant "
                        + "WHERE approval_request_id=? AND step_no=? AND decision_status='REJECTED'",
                Long.class, requestId, currentStep);
        if (rejected != null && rejected > 0) {
            jdbc.update(
                    "UPDATE adm_approval_request SET approval_status='REJECTED',version_no=version_no+1,"
                            + "updated_by=? WHERE approval_request_id=? AND approval_status='PENDING'",
                    actor, requestId);
            return;
        }

        Map<String, Object> rule = jdbc.queryForMap(
                "SELECT DISTINCT s.step_no,s.decision_rule,s.required_count "
                        + "FROM adm_approval_request r JOIN adm_approval_policy_step s "
                        + "ON s.policy_code=r.policy_code AND s.policy_version=r.policy_version "
                        + "WHERE r.approval_request_id=? AND s.step_no=? AND s.required_yn='Y'",
                requestId, currentStep);
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM adm_approval_participant WHERE approval_request_id=? AND step_no=?",
                Long.class, requestId, currentStep);
        Long approved = jdbc.queryForObject(
                "SELECT COUNT(*) FROM adm_approval_participant "
                        + "WHERE approval_request_id=? AND step_no=? AND decision_status='APPROVED'",
                Long.class, requestId, currentStep);
        Object requiredCount = value(rule, "required_count");
        AdmApprovalDecisionEvaluator.Evaluation evaluation = decisionEvaluator.evaluate(false, List.of(
                new AdmApprovalDecisionEvaluator.StepDecision(
                        currentStep, text(rule, "decision_rule"),
                        requiredCount == null ? null : ((Number) requiredCount).longValue(),
                        total == null ? 0 : total, approved == null ? 0 : approved)));
        if (evaluation.status() != AdmApprovalDecisionEvaluator.Status.APPROVED) return;

        Integer nextStep = jdbc.queryForObject(
                "SELECT MIN(s.step_no) FROM adm_approval_request r JOIN adm_approval_policy_step s "
                        + "ON s.policy_code=r.policy_code AND s.policy_version=r.policy_version "
                        + "WHERE r.approval_request_id=? AND s.required_yn='Y' AND s.step_no>?",
                Integer.class, requestId, currentStep);
        if (nextStep != null) {
            int changed = jdbc.update(
                    "UPDATE adm_approval_request SET current_step_no=?,version_no=version_no+1,updated_by=? "
                            + "WHERE approval_request_id=? AND approval_status='PENDING' AND current_step_no=?",
                    nextStep, actor, requestId, currentStep);
            if (changed != 1) throw new IllegalStateException("approval step changed concurrently");
            return;
        }
        int approvedRequest = jdbc.update(
                "UPDATE adm_approval_request SET approval_status='APPROVED',version_no=version_no+1,updated_by=? "
                        + "WHERE approval_request_id=? AND approval_status='PENDING' AND current_step_no=?",
                actor, requestId, currentStep);
        if (approvedRequest != 1) throw new IllegalStateException("approval request changed concurrently");
    }

    private CpfBatchRiskCommand buildBatCommand(
            long requestId, String requestKey, String actor,
            RequestCommand command, Map<String, Object> payload) {
        Long expectedVersion = longOrNull(value(payload, "expectedVersion"));
        String operation = required(command.ownerCommand(), "ownerCommand");
        String action = required(command.actionType(), "actionType").toUpperCase(Locale.ROOT);
        String commandPayload = switch (operation) {
            case "actGhostExecution" -> action.startsWith("BATCH_GHOST_")
                    ? action.substring("BATCH_GHOST_".length())
                    : nullableText(payload, "actionType");
            case "updateScheduleEnabled" -> action.endsWith("ENABLE") ? "enabled=true" : "enabled=false";
            case "requestRun" -> jsonOrText(value(payload, "jobParameters"));
            default -> "";
        };
        return new CpfBatchRiskCommand(
                operation, required(command.targetType(), "targetType"),
                required(command.targetId(), "targetId"), action, actor,
                required(command.reason(), "reason"), String.valueOf(requestId), requestKey,
                expectedVersion, commandPayload);
    }

    private Map<String, Object> riskSnapshot(CpfBatchRiskCommand risk) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("operation", risk.operation());
        snapshot.put("targetType", risk.targetType());
        snapshot.put("targetId", risk.targetId());
        snapshot.put("actionType", risk.actionType());
        snapshot.put("requestUser", risk.requestUser());
        snapshot.put("reason", risk.reason());
        snapshot.put("approvalRequestId", risk.approvalRequestId());
        snapshot.put("idempotencyKey", risk.idempotencyKey());
        snapshot.put("expectedVersion", risk.expectedVersion());
        snapshot.put("payload", risk.payload());
        return snapshot;
    }

    private void assertIdempotentRequest(
            Map<String, Object> existing, RequestCommand command, String actor,
            Map<String, Object> policy, String normalizedPayload) {
        equal(existing, "requested_by", actor, "requester");
        equalIgnoreCase(existing, "owner_module", normalizeOwner(command.ownerModule()), "owner module");
        equal(existing, "owner_command", required(command.ownerCommand(), "ownerCommand"), "owner command");
        equalIgnoreCase(existing, "action_type", required(command.actionType(), "actionType"), "action type");
        equalIgnoreCase(existing, "target_type", required(command.targetType(), "targetType"), "target type");
        equal(existing, "target_id", required(command.targetId(), "targetId"), "target id");
        equal(existing, "request_reason", required(command.reason(), "reason"), "reason");
        equal(existing, "policy_code", String.valueOf(value(policy, "policyCode")), "policy code");
        if (number(existing, "policy_version") != ((Number) value(policy, "policyVersion")).longValue()) {
            throw new IllegalStateException("requestKey is already used with a different policy version");
        }
        String owner = normalizeOwner(command.ownerModule());
        String expectedHash;
        if ("BAT".equals(owner)) {
            long requestId = number(existing, "approval_request_id");
            CpfBatchRiskCommand expected = buildBatCommand(
                    requestId, command.requestKey(), actor, command, readObject(normalizedPayload));
            expectedHash = expected.fingerprint();
        } else {
            expectedHash = sha256(normalizedPayload);
        }
        if (!expectedHash.equalsIgnoreCase(text(existing, "command_payload_hash"))) {
            throw new IllegalStateException("requestKey is already used with a different command payload");
        }
    }

    private Map<String, Object> requestByKeyOrNull(String key) {
        try {
            return jdbc.queryForMap(
                    "SELECT approval_request_id,request_key,policy_code,policy_version,action_type,"
                            + "owner_module,owner_command,target_type,target_id,requested_by,request_reason,"
                            + "command_payload_hash,approval_status,expire_at,version_no "
                            + "FROM adm_approval_request WHERE request_key=?",
                    key);
        } catch (EmptyResultDataAccessException missing) {
            return null;
        }
    }

    private void assertNotExpired(Map<String, Object> request, String actor, long id) {
        Instant expires = instant(value(request, "expireAt"));
        if (expires != null && !expires.isAfter(Instant.now())) {
            jdbc.update(
                    "UPDATE adm_approval_request SET approval_status='EXPIRED',version_no=version_no+1,"
                            + "updated_by=? WHERE approval_request_id=? AND approval_status IN ('PENDING','APPROVED')",
                    actor, id);
            throw new IllegalStateException("approval request has expired");
        }
    }

    private void validatePolicySteps(List<StepCommand> steps) {
        Map<Integer, StepRule> rules = new LinkedHashMap<>();
        Set<String> targets = new HashSet<>();
        for (StepCommand step : steps) {
            int stepNo = positive(step.stepNo(), "stepNo");
            String rule = required(step.decisionRule(), "decisionRule").toUpperCase(Locale.ROOT);
            if (!Set.of("ALL", "ANY", "N_OF_M").contains(rule)) {
                throw new IllegalArgumentException("decisionRule must be ALL, ANY or N_OF_M");
            }
            Integer requiredCount = "N_OF_M".equals(rule)
                    ? positive(step.requiredCount() == null ? 0 : step.requiredCount(), "requiredCount")
                    : null;
            StepRule prior = rules.putIfAbsent(stepNo, new StepRule(rule, requiredCount));
            if (prior != null && (!prior.rule().equals(rule)
                    || !Objects.equals(prior.requiredCount(), requiredCount))) {
                throw new IllegalArgumentException("all targets in one step must share the same decision rule");
            }
            String targetKey = stepNo + "\u0000"
                    + required(step.targetType(), "targetType").toUpperCase(Locale.ROOT)
                    + "\u0000" + required(step.targetCode(), "targetCode");
            if (!targets.add(targetKey)) {
                throw new IllegalArgumentException("duplicate approval step target: " + targetKey);
            }
        }
        List<Integer> ordered = new ArrayList<>(rules.keySet());
        ordered.sort((left, right) -> left.compareTo(right));
        for (int index = 0; index < ordered.size(); index++) {
            if (ordered.get(index) != index + 1) {
                throw new IllegalArgumentException("approval step numbers must be contiguous from 1");
            }
        }
    }

    private Map<String, Object> readObject(String value) {
        String json = value == null || value.isBlank() ? "{}" : value;
        try {
            Map<String, Object> parsed = objectMapper.readValue(json, new TypeReference<>() {});
            if (parsed == null) return new LinkedHashMap<>();
            return new LinkedHashMap<>(parsed);
        } catch (Exception invalid) {
            throw new IllegalArgumentException("payloadSnapshot must be a JSON object", invalid);
        }
    }

    private void rejectInlineSecrets(Object value, String path) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String normalized = key.replace("-", "_").toLowerCase(Locale.ROOT);
                if (SENSITIVE_INLINE_KEYS.contains(normalized)) {
                    throw new IllegalArgumentException(
                            "inline sensitive value is not allowed; use a secret reference: " + path + "." + key);
                }
                rejectInlineSecrets(entry.getValue(), path + "." + key);
            }
        } else if (value instanceof Iterable<?> iterable) {
            int index = 0;
            for (Object element : iterable) rejectInlineSecrets(element, path + "[" + index++ + "]");
        }
    }

    private String jsonOrText(Object value) {
        if (value == null) return "{}";
        if (value instanceof String text) return text;
        return write(value);
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException invalid) {
            throw new IllegalStateException("payload serialization failed", invalid);
        }
    }

    private static long generatedId(KeyHolder keys) {
        if (keys.getKey() != null) return keys.getKey().longValue();
        for (Object value : Objects.requireNonNullElse(keys.getKeys(), Map.<String, Object>of()).values()) {
            if (value instanceof Number number) return number.longValue();
        }
        throw new IllegalStateException("approval request id was not generated");
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static String normalizeOwner(String value) {
        String normalized = required(value, "ownerModule");
        return normalized.equalsIgnoreCase("cpf-batch") ? "BAT" : normalized.toUpperCase(Locale.ROOT);
    }

    private static Object value(Map<String, Object> row, String key) {
        if (row == null) return null;
        Object result = row.get(key);
        if (result != null) return result;
        result = row.get(key.toUpperCase(Locale.ROOT));
        if (result != null) return result;
        String snake = key.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        result = row.get(snake);
        return result != null ? result : row.get(snake.toUpperCase(Locale.ROOT));
    }

    private static String text(Map<String, Object> row, String key) {
        Object result = value(row, key);
        return required(result == null ? null : String.valueOf(result), key);
    }

    private static String nullableText(Map<String, Object> row, String key) {
        Object result = value(row, key);
        return result == null ? "" : String.valueOf(result).trim();
    }

    private static long number(Map<String, Object> row, String key) {
        Object result = value(row, key);
        if (result instanceof Number number) return number.longValue();
        return Long.parseLong(required(result == null ? null : String.valueOf(result), key));
    }

    private static Long longOrNull(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        long parsed = Long.parseLong(String.valueOf(value));
        if (parsed < 0) throw new IllegalArgumentException("expectedVersion must be non-negative");
        return parsed;
    }

    private static void equal(
            Map<String, Object> row, String key, String expected, String description) {
        if (!text(row, key).equals(expected)) {
            throw new IllegalStateException("requestKey is already used with a different " + description);
        }
    }

    private static void equalIgnoreCase(
            Map<String, Object> row, String key, String expected, String description) {
        if (!text(row, key).equalsIgnoreCase(expected)) {
            throw new IllegalStateException("requestKey is already used with a different " + description);
        }
    }

    private static Instant instant(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        if (value instanceof OffsetDateTime offset) return offset.toInstant();
        return Timestamp.valueOf(String.valueOf(value)).toInstant();
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private static long positiveLong(long value, String field) {
        if (value < 1) throw new IllegalArgumentException(field + " must be positive");
        return value;
    }

    private static int positive(int value, String field) {
        if (value < 1) throw new IllegalArgumentException(field + " must be positive");
        return value;
    }

    private static String flag(String value) {
        String normalized = required(value, "flag").toUpperCase(Locale.ROOT);
        if (!normalized.equals("Y") && !normalized.equals("N")) {
            throw new IllegalArgumentException("flag must be Y or N");
        }
        return normalized;
    }

    private static String nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static Timestamp requiredTimestamp(String value, String field) {
        Timestamp result = timestamp(required(value, field));
        if (result == null) throw new IllegalArgumentException(field + " is required");
        return result;
    }

    private static Timestamp timestamp(String value) {
        return value == null || value.isBlank()
                ? null
                : Timestamp.from(OffsetDateTime.parse(value).toInstant());
    }

    private record StepRule(String rule, Integer requiredCount) {}

    @Deprecated
    public record StepCommand(
            int stepNo, String stepType, String targetType, String targetCode,
            String decisionRule, Integer requiredCount, String requiredYn) {}

    @Deprecated
    public record PolicyCommand(
            String policyCode, int policyVersion, String policyName, String actionType,
            String effectiveFrom, String effectiveTo, String enabledYn,
            String selfApprovalAllowedYn, String breakGlassAllowedYn,
            String description, List<StepCommand> steps, String reason) {}

    @Deprecated
    public record RequestCommand(
            String requestKey, String policyCode, Integer policyVersion, String actionType,
            String ownerModule, String ownerCommand, String targetType, String targetId,
            String payloadSnapshot, String expireAt, String reason) {}

    @Deprecated
    public record DecisionCommand(String action, String idempotencyKey, String reason) {}
}
