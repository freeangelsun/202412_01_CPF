package com.cpf.backoffice.online.approval.service;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.backoffice.online.approval.api.BackofficeApprovalDecisionEvaluator;
import com.cpf.backoffice.online.approval.api.BackofficeApprovalDecisionRule;
import com.cpf.backoffice.online.approval.api.BackofficeApprovalStepStatus;
import com.cpf.backoffice.online.approval.api.BackofficeApprovalTargetType;
import com.cpf.backoffice.online.approval.repository.BackofficeApprovalPolicyRepository;
import com.cpf.backoffice.online.approval.spi.BackofficeApprovalDirectoryEntry;
import com.cpf.backoffice.online.base.BackofficeBaseService;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.platform.operations.context.CpfApprovalContextSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.foundation.annotation.CpfService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * MBW Versioned Approval Policy Engine.
 *
 * <p>정책 Target은 상신 시점에 실제 참여자로 해석하여 Snapshot하고 ALL/ANY/N_OF_M 결정을
 * Snapshot 분모로 평가합니다. 진행 중 조직/Role 변경은 이미 상신된 결재의 참여자를 바꾸지 않습니다.</p>
 */
@CpfService
public class BackofficeApprovalPolicyService extends BackofficeBaseService {
    private static final Set<String> STEP_TYPES = Set.of("APPROVAL", "AGREEMENT", "REVIEW");
    private static final Set<String> MODES = Set.of("SEQUENTIAL", "PARALLEL");
    private static final DateTimeFormatter NO_DATE =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);

    private final BackofficeApprovalPolicyRepository repository;
    private final ObjectMapper objectMapper;
    private final CpfApprovalContextSupport approvalContexts = new CpfApprovalContextSupport();

    public BackofficeApprovalPolicyService(BackofficeApprovalPolicyRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public List<Map<String,Object>> findPolicies(String businessDomain, String approvalType) {
        return repository.findPolicies(businessDomain, approvalType);
    }

    public Map<String,Object> findPolicy(String policyCode, int version) {
        Map<String,Object> policy = new LinkedHashMap<>(repository.findPolicy(policyCode, version)
                .orElseThrow(() -> new CpfValidationException("결재 정책을 찾을 수 없습니다.")));
        policy.put("steps", repository.findPolicySteps(policyCode, version));
        return policy;
    }

    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public Map<String,Object> savePolicy(PolicyRequest request, String operatorId) {
        String policyCode = required(request.policyCode(), "policyCode");
        int version = request.policyVersion() == null ? 1 : request.policyVersion();
        if (version < 1) throw new CpfValidationException("policyVersion은 1 이상이어야 합니다.");
        if (repository.findPolicy(policyCode, version).isPresent()) {
            throw new CpfValidationException(
                    "이미 생성된 결재 정책 Version은 수정할 수 없습니다. 새 policyVersion으로 등록하세요.");
        }
        Instant from = required(request.effectiveFrom(), "effectiveFrom");
        Instant to = request.effectiveTo();
        if (to != null && !to.isAfter(from)) throw new CpfValidationException("effectiveTo는 effectiveFrom보다 뒤여야 합니다.");
        if (request.steps() == null || request.steps().isEmpty()) throw new CpfValidationException("결재 정책 단계가 필요합니다.");

        Map<String,Object> policy = new LinkedHashMap<>();
        policy.put("policyCode", policyCode);
        policy.put("policyVersion", version);
        policy.put("policyName", required(request.policyName(), "policyName"));
        policy.put("businessDomain", required(request.businessDomain(), "businessDomain"));
        policy.put("approvalType", required(request.approvalType(), "approvalType"));
        policy.put("effectiveFrom", Timestamp.from(from));
        policy.put("effectiveTo", to == null ? null : Timestamp.from(to));
        policy.put("enabledYn", yn(request.enabledYn(), "Y"));
        policy.put("selfApprovalAllowedYn", yn(request.selfApprovalAllowedYn(), "N"));
        policy.put("description", blankToNull(request.description()));
        policy.put("operatorId", required(operatorId, "operatorId"));

        List<Map<String,Object>> steps = new ArrayList<>();
        Set<String> unique = new HashSet<>();
        for (PolicyStepRequest step : request.steps()) {
            int stepNo = step.stepNo() == null ? 1 : step.stepNo();
            if (stepNo < 1) throw new CpfValidationException("stepNo는 1 이상이어야 합니다.");
            String stepType = upper(step.stepType(), "APPROVAL");
            if (!STEP_TYPES.contains(stepType)) throw new CpfValidationException("지원하지 않는 stepType입니다.");
            BackofficeApprovalTargetType targetType;
            try { targetType = BackofficeApprovalTargetType.valueOf(required(step.targetType(), "targetType").toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ex) { throw new CpfValidationException("지원하지 않는 targetType입니다."); }
            BackofficeApprovalDecisionRule rule;
            try { rule = BackofficeApprovalDecisionRule.valueOf(upper(step.decisionRule(), "ALL")); }
            catch (IllegalArgumentException ex) { throw new CpfValidationException("지원하지 않는 decisionRule입니다."); }
            Integer requiredCount = step.requiredCount();
            if (rule == BackofficeApprovalDecisionRule.N_OF_M && (requiredCount == null || requiredCount < 1)) {
                throw new CpfValidationException("N_OF_M에는 requiredCount가 필요합니다.");
            }
            if (rule != BackofficeApprovalDecisionRule.N_OF_M) requiredCount = null;
            String targetCode = required(step.targetCode(), "targetCode");
            String key = stepNo + "|" + targetType + "|" + targetCode;
            if (!unique.add(key)) throw new CpfValidationException("중복 정책 Target입니다: " + key);
            Map<String,Object> row = new LinkedHashMap<>(policy);
            row.put("stepNo", stepNo);
            row.put("stepType", stepType);
            row.put("targetType", targetType.name());
            row.put("targetCode", targetCode);
            row.put("decisionRule", rule.name());
            row.put("requiredCount", requiredCount);
            row.put("requiredYn", yn(step.requiredYn(), "Y"));
            row.put("sortOrder", step.sortOrder() == null ? 0 : step.sortOrder());
            steps.add(row);
        }
        repository.replacePolicy(policy, steps);
        return findPolicy(policyCode, version);
    }

    public Map<String,Object> simulate(
            String policyCode, Integer policyVersion, String businessDomain, String approvalType,
            String requesterEmployeeNo, Instant effectiveAt) {
        Instant at = effectiveAt == null ? Instant.now() : effectiveAt;
        Map<String,Object> policy = resolvePolicy(policyCode, policyVersion, businessDomain, approvalType, at);
        String code = string(policy, "policyCode");
        int version = number(policy, "policyVersion").intValue();
        List<Map<String,Object>> steps = repository.findPolicySteps(code, version);
        List<Map<String,Object>> resolved = resolveParticipants(policy, steps, blankToNull(requesterEmployeeNo), at);
        return Map.of("policy", policy, "steps", steps, "participants", resolved, "effectiveAt", at.toString());
    }

    public List<Map<String,Object>> findDelegations(String employeeNo, Instant effectiveAt) {
        return repository.findDelegations(employeeNo, effectiveAt);
    }

    public List<Map<String,Object>> findSubmissions(String operatorId, String status, int limit) {
        return repository.findSubmissions(employeeNo(operatorId), upperOrNull(status), boundedLimit(limit));
    }

    public List<Map<String,Object>> findInbox(String operatorId, String decisionStatus, int limit) {
        return repository.findInbox(employeeNo(operatorId), upperOrNull(decisionStatus), boundedLimit(limit));
    }

    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public Map<String,Object> saveDelegation(DelegationRequest request, String operatorId) {
        Instant from = required(request.validFrom(), "validFrom");
        Instant to = required(request.validTo(), "validTo");
        if (!to.isAfter(from)) throw new CpfValidationException("validTo는 validFrom보다 뒤여야 합니다.");
        String delegator = required(request.delegatorEmployeeNo(), "delegatorEmployeeNo");
        String delegate = required(request.delegateEmployeeNo(), "delegateEmployeeNo");
        if (delegator.equals(delegate)) throw new CpfValidationException("자기 자신에게 위임할 수 없습니다.");
        Map<String,Object> values = new LinkedHashMap<>();
        values.put("delegationId", request.delegationId());
        values.put("delegatorEmployeeNo", delegator);
        values.put("delegateEmployeeNo", delegate);
        values.put("businessDomain", blankToNull(request.businessDomain()));
        values.put("approvalType", blankToNull(request.approvalType()));
        values.put("validFrom", Timestamp.from(from));
        values.put("validTo", Timestamp.from(to));
        values.put("reason", required(request.reason(), "reason"));
        values.put("useYn", yn(request.useYn(), "Y"));
        values.put("operatorId", required(operatorId, "operatorId"));
        repository.saveDelegation(values);
        return Map.of("saved", true);
    }

    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public Map<String,Object> submit(SubmitRequest request, String operatorId) {
        try (var ignored = approvalContexts.bind(request.requestIdempotencyKey(), request.policyCode(), operatorId, null, "SUBMIT", "IN_REVIEW", "MBW_APPROVAL")) {
            return submitInternal(request, operatorId, null);
        }
    }

    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public Map<String,Object> resubmit(long previousApprovalId, SubmitRequest request, String operatorId) {
        try (var ignored = approvalContexts.bind(Long.toString(previousApprovalId), request.policyCode(), operatorId, null, "RESUBMIT", "IN_REVIEW", "MBW_APPROVAL")) {

            Map<String,Object> previous = repository.findDocument(previousApprovalId)
                    .orElseThrow(() -> new CpfValidationException("재상신 원본 결재 문서를 찾을 수 없습니다."));
            String status = string(previous, "approvalStatus");
            if (!Set.of("REJECTED", "WITHDRAWN", "CANCELED", "EXPIRED").contains(status)) {
                throw new CpfValidationException("반려/철회/취소/만료된 결재만 새 문서로 재상신할 수 있습니다.");
            }
            String actor = repository.findEmployeeNoByLoginId(required(operatorId, "operatorId"))
                    .orElseThrow(() -> new CpfValidationException("로그인 사용자와 연결된 직원이 없습니다."));
            if (!Objects.equals(string(previous, "requesterEmployeeNo"), actor)) {
                throw new CpfValidationException("재상신 요청자는 원본 요청자와 같아야 합니다.");
            }
            if (request.requesterEmployeeNo() != null && !request.requesterEmployeeNo().isBlank()
                    && !actor.equalsIgnoreCase(request.requesterEmployeeNo().trim())) {
                throw new CpfValidationException("requesterEmployeeNo는 인증 사용자와 일치해야 합니다.");
            }
            Map<String,Object> created = submitInternal(request, operatorId, previousApprovalId);
            long newApprovalId = number(created, "approvalId").longValue();
            repository.insertHistory(previousApprovalId, "RESUBMIT", actor,
                    request.requestIdempotencyKey() + ":resubmit-source", required(request.reason(), "reason"),
                    status, status, "새 결재 문서 " + newApprovalId + " 로 재상신",
                    string(previous, "transactionId"), operatorId);
            return created;

        }
    }

    private Map<String,Object> submitInternal(SubmitRequest request, String operatorId, Long resubmittedFromApprovalId) {
        String idem = required(request.requestIdempotencyKey(), "requestIdempotencyKey");
        String normalizedOperator = required(operatorId, "operatorId");
        Instant at = Instant.now();
        String requester = repository.findEmployeeNoByLoginId(normalizedOperator)
                .orElseThrow(() -> new CpfValidationException("로그인 사용자와 연결된 직원이 없습니다."));
        String title = required(request.title(), "title");
        String mode = upper(request.approvalMode(), "SEQUENTIAL");
        if (!MODES.contains(mode)) throw new CpfValidationException("approvalMode는 SEQUENTIAL/PARALLEL 이어야 합니다.");
        String payload = request.payloadJson() == null ? "{}" : request.payloadJson();
        String payloadHash = sha256(payload);
        String reason = required(request.reason(), "reason");
        if (request.dueAt() != null && !request.dueAt().isAfter(at)) {
            throw new CpfValidationException("dueAt은 현재 시각보다 뒤여야 합니다.");
        }
        Optional<Long> existing = repository.findApprovalByIdempotencyKey(idem);
        if (existing.isPresent()) {
            Map<String,Object> replay = repository.findDocument(existing.get())
                    .orElseThrow(() -> new CpfValidationException("멱등 결재 문서를 찾을 수 없습니다."));
            validateSubmitReplay(replay, request, requester, title, mode, payloadHash, resubmittedFromApprovalId);
            return detail(existing.get());
        }
        if (request.requesterEmployeeNo() != null && !request.requesterEmployeeNo().isBlank()
                && !requester.equalsIgnoreCase(request.requesterEmployeeNo().trim())) {
            throw new CpfValidationException("requesterEmployeeNo는 인증 사용자와 일치해야 합니다.");
        }
        Map<String,Object> policy = resolvePolicy(
                request.policyCode(), request.policyVersion(), request.businessDomain(), request.approvalType(), at);
        String policyCode = string(policy, "policyCode");
        int policyVersion = number(policy, "policyVersion").intValue();
        List<Map<String,Object>> steps = repository.findPolicySteps(policyCode, policyVersion);
        List<Map<String,Object>> resolved = resolveParticipants(policy, steps, requester, at);
        BackofficeApprovalDirectoryEntry requesterProfile = repository.findPrimaryAssignment(requester, at)
                .orElseThrow(() -> new CpfValidationException("요청자의 유효 직원/대표 소속 Snapshot을 찾을 수 없습니다."));

        String policySnapshot = json(Map.of("policy", policy, "steps", steps, "participants", resolved));

        Map<String,Object> doc = new LinkedHashMap<>();
        doc.put("approvalNo", "MBW-" + NO_DATE.format(at) + "-" + UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT));
        doc.put("approvalType", string(policy, "approvalType"));
        doc.put("businessDomain", string(policy, "businessDomain"));
        doc.put("policyCode", policyCode);
        doc.put("policyVersion", policyVersion);
        doc.put("policySnapshotJson", policySnapshot);
        doc.put("title", title);
        doc.put("requesterEmployeeNo", requester);
        doc.put("requesterOrganizationCode", requesterProfile.organizationCode());
        doc.put("requesterPositionCode", requesterProfile.positionCode());
        doc.put("requesterJobTitleCode", requesterProfile.jobTitleCode());
        doc.put("approvalMode", mode);
        doc.put("dueAt", request.dueAt() == null ? null : Timestamp.from(request.dueAt()));
        doc.put("payloadJson", payload);
        doc.put("payloadHash", payloadHash);
        doc.put("requestIdempotencyKey", idem);
        doc.put("attachmentGroupId", blankToNull(request.attachmentGroupId()));
        doc.put("resubmittedFromApprovalId", resubmittedFromApprovalId);
        doc.put("transactionId", CpfContexts.transactionId());
        doc.put("operatorId", normalizedOperator);
        long approvalId = repository.insertPolicyApproval(doc);

        Map<String,List<Map<String,Object>>> participantsByTarget = new LinkedHashMap<>();
        for (Map<String,Object> p : resolved) {
            String k = p.get("stepNo") + "|" + p.get("targetType") + "|" + p.get("targetCode");
            participantsByTarget.computeIfAbsent(k, ignored -> new ArrayList<>()).add(p);
        }
        for (Map<String,Object> step : steps) {
            Map<String,Object> line = new LinkedHashMap<>();
            line.put("approvalId", approvalId);
            line.put("stepNo", number(step, "stepNo").intValue());
            line.put("stepType", string(step, "stepType"));
            line.put("targetType", string(step, "targetType"));
            line.put("targetCode", string(step, "targetCode"));
            line.put("targetName", string(step, "targetCode"));
            line.put("decisionRule", string(step, "decisionRule"));
            line.put("requiredCount", step.get("requiredCount"));
            line.put("requiredYn", string(step, "requiredYn"));
            line.put("directApprover", "EMPLOYEE".equals(line.get("targetType")) ? line.get("targetCode") : null);
            line.put("operatorId", operatorId);
            long lineId = repository.insertLine(line);
            String key = line.get("stepNo") + "|" + line.get("targetType") + "|" + line.get("targetCode");
            for (Map<String,Object> participant : participantsByTarget.getOrDefault(key, List.of())) {
                Map<String,Object> participantRow = new LinkedHashMap<>(participant);
                participantRow.put("approvalId", approvalId);
                participantRow.put("approvalLineId", lineId);
                participantRow.put("operatorId", operatorId);
                repository.insertParticipant(participantRow);
            }
        }
        repository.insertHistory(approvalId, "SUBMIT", requester, idem + ":submit",
                reason, "DRAFT", "IN_REVIEW", null,
                string(doc, "transactionId"), normalizedOperator);
        return detail(approvalId);
    }

    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public Map<String,Object> decide(long approvalId, DecisionRequest request, String operatorId) {
        try (var ignored = approvalContexts.bind(Long.toString(approvalId), null, null, operatorId, request.action(), "IN_REVIEW", "MBW_APPROVAL")) {

            String idem = required(request.idempotencyKey(), "idempotencyKey");
            String normalizedOperator = required(operatorId, "operatorId");
            String actor = repository.findEmployeeNoByLoginId(normalizedOperator)
                    .orElseThrow(() -> new CpfValidationException("로그인 사용자와 연결된 직원이 없습니다."));
            String action = upper(request.action(), "APPROVE");
            String participantStatus = switch (action) {
                case "APPROVE" -> "APPROVED";
                case "AGREE" -> "AGREED";
                case "REJECT" -> "REJECTED";
                default -> throw new CpfValidationException("APPROVE/AGREE/REJECT만 지원합니다.");
            };
            if (repository.participantDecisionExists(idem)) {
                validateDecisionReplay(approvalId, idem, actor, participantStatus, blankToNull(request.comment()));
                return detail(approvalId);
            }
            Map<String,Object> doc = repository.findDocument(approvalId)
                    .orElseThrow(() -> new CpfValidationException("결재 문서를 찾을 수 없습니다."));
            String beforeStatus = string(doc, "approvalStatus");
            if (!"IN_REVIEW".equals(beforeStatus)) throw new CpfValidationException("진행 중인 결재만 결정할 수 있습니다.");
            int currentStep = number(doc, "currentStepNo").intValue();
            String mode = string(doc, "approvalMode");
            Map<String,Object> participant = repository.findWaitingParticipant(approvalId, actor, mode, currentStep)
                    .orElseThrow(() -> new CpfValidationException("현재 결정 가능한 결재 참여자가 아닙니다."));
            if (repository.decideParticipant(number(participant, "participantId").longValue(), participantStatus,
                    idem, blankToNull(request.comment()), normalizedOperator) != 1) {
                throw new CpfValidationException("결재 참여자 상태가 동시에 변경되었습니다.");
            }

            long lineId = number(participant, "lineId").longValue();
            Map<String,Object> counts = repository.participantCounts(lineId);
            BackofficeApprovalDecisionRule rule = BackofficeApprovalDecisionRule.valueOf(string(participant, "decisionRule"));
            Integer requiredCount = participant.get("requiredCount") == null ? null : number(participant, "requiredCount").intValue();
            BackofficeApprovalStepStatus lineDecision = BackofficeApprovalDecisionEvaluator.evaluate(
                    rule, number(counts, "participantCount").intValue(),
                    number(counts, "approvedCount").intValue(),
                    number(counts, "rejectedCount").intValue(), requiredCount);
            String lineStatus = switch (lineDecision) {
                case APPROVED -> "AGREEMENT".equals(string(participant, "stepType")) ? "AGREED" : "APPROVED";
                case REJECTED -> "REJECTED";
                case WAITING -> "WAITING";
            };
            repository.updateLineStatus(lineId, lineStatus, blankToNull(request.comment()), normalizedOperator);

            List<Map<String,Object>> lines = repository.findLineStatuses(approvalId);
            String afterStatus = "IN_REVIEW";
            int nextStep = currentStep;
            boolean completed = false;
            boolean rejected = lines.stream().anyMatch(line ->
                    "Y".equals(string(line, "requiredYn")) && "REJECTED".equals(string(line, "decisionStatus")));
            if (rejected) {
                afterStatus = "REJECTED";
                completed = true;
            } else if ("PARALLEL".equals(mode)) {
                boolean allDone = lines.stream().filter(line -> "Y".equals(string(line, "requiredYn")))
                        .allMatch(BackofficeApprovalPolicyService::isApprovedLine);
                if (allDone) { afterStatus = "APPROVED"; completed = true; }
            } else {
                boolean currentDone = lines.stream()
                        .filter(line -> number(line, "stepNo").intValue() == currentStep)
                        .filter(line -> "Y".equals(string(line, "requiredYn")))
                        .allMatch(BackofficeApprovalPolicyService::isApprovedLine);
                if (currentDone) {
                    OptionalInt later = lines.stream().mapToInt(line -> number(line, "stepNo").intValue())
                            .filter(step -> step > currentStep).min();
                    if (later.isPresent()) nextStep = later.getAsInt();
                    else { afterStatus = "APPROVED"; completed = true; }
                }
            }
            long version = number(doc, "versionNo").longValue();
            if (repository.updateDocumentStatus(approvalId, version, afterStatus, nextStep, completed, normalizedOperator) != 1) {
                throw new CpfValidationException("결재 문서가 동시에 변경되었습니다. 최신 상태를 다시 조회하세요.");
            }
            repository.insertHistory(approvalId, action, actor, idem + ":history",
                    required(request.reason(), "reason"), beforeStatus, afterStatus,
                    blankToNull(request.comment()), string(doc, "transactionId"), normalizedOperator);
            return detail(approvalId);

        }
    }

    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public Map<String,Object> withdraw(long approvalId, LifecycleRequest request, String operatorId) {
        try (var ignored = approvalContexts.bind(Long.toString(approvalId), null, operatorId, null, "WITHDRAW", "WITHDRAWN", "MBW_APPROVAL")) {
            return requesterLifecycle(approvalId, request, operatorId, "WITHDRAW", "WITHDRAWN");
        }
    }

    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public Map<String,Object> cancel(long approvalId, LifecycleRequest request, String operatorId) {
        try (var ignored = approvalContexts.bind(Long.toString(approvalId), null, operatorId, null, "CANCEL", "CANCELED", "MBW_APPROVAL")) {
            return requesterLifecycle(approvalId, request, operatorId, "CANCEL", "CANCELED");
        }
    }

    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public List<Long> expireDue(Instant now, int limit, String operatorId) {
        Instant effectiveNow = now == null ? Instant.now() : now;
        int bounded = Math.max(1, Math.min(limit <= 0 ? 100 : limit, 1000));
        List<Long> expired = new ArrayList<>();
        for (Long approvalId : repository.findDueApprovalIds(effectiveNow, bounded)) {
            Map<String,Object> doc = repository.findDocument(approvalId).orElse(null);
            if (doc == null || !"IN_REVIEW".equals(string(doc, "approvalStatus"))) continue;
            long version = number(doc, "versionNo").longValue();
            if (repository.updateDocumentStatus(approvalId, version, "EXPIRED",
                    number(doc, "currentStepNo").intValue(), true, operatorId) == 1) {
                String idem = "expire:" + approvalId + ":" + version;
                if (!repository.historyActionExists(idem)) {
                    repository.insertHistory(approvalId, "EXPIRE", "SYSTEM", idem,
                            "dueAt 경과 자동 만료", "IN_REVIEW", "EXPIRED", null,
                            string(doc, "transactionId"), operatorId);
                }
                expired.add(approvalId);
            }
        }
        return List.copyOf(expired);
    }

    private Map<String,Object> requesterLifecycle(long approvalId, LifecycleRequest request, String operatorId,
                                                   String action, String targetStatus) {
        String idem = required(request.idempotencyKey(), "idempotencyKey");
        if (repository.historyActionExists(idem)) {
            throw new CpfValidationException("멱등 이력 Key의 원 결재 문서·행위를 확인할 수 없어 안전하게 재사용을 거부합니다.");
        }
        String actor = repository.findEmployeeNoByLoginId(required(operatorId, "operatorId"))
                .orElseThrow(() -> new CpfValidationException("로그인 사용자와 연결된 직원이 없습니다."));
        Map<String,Object> doc = repository.findDocument(approvalId)
                .orElseThrow(() -> new CpfValidationException("결재 문서를 찾을 수 없습니다."));
        if (!actor.equals(string(doc, "requesterEmployeeNo"))) {
            throw new CpfValidationException("결재 요청자 본인만 철회/취소할 수 있습니다.");
        }
        String before = string(doc, "approvalStatus");
        if (!"IN_REVIEW".equals(before)) throw new CpfValidationException("진행 중인 결재만 철회/취소할 수 있습니다.");
        long version = number(doc, "versionNo").longValue();
        if (repository.updateDocumentStatus(approvalId, version, targetStatus,
                number(doc, "currentStepNo").intValue(), true, operatorId) != 1) {
            throw new CpfValidationException("결재 문서가 동시에 변경되었습니다. 최신 상태를 다시 조회하세요.");
        }
        repository.insertHistory(approvalId, action, actor, idem, required(request.reason(), "reason"),
                before, targetStatus, blankToNull(request.comment()), string(doc, "transactionId"), operatorId);
        return detail(approvalId);
    }

    private void validateSubmitReplay(Map<String,Object> replay, SubmitRequest request, String requester,
                                      String title, String mode, String payloadHash,
                                      Long resubmittedFromApprovalId) {
        List<String> conflicts = new ArrayList<>();
        compare(conflicts, "requesterEmployeeNo", requester, string(replay, "requesterEmployeeNo"));
        compare(conflicts, "title", title, string(replay, "title"));
        compare(conflicts, "approvalMode", mode, string(replay, "approvalMode"));
        compare(conflicts, "payloadHash", payloadHash, string(replay, "payloadHash"));
        compare(conflicts, "attachmentGroupId", blankToNull(request.attachmentGroupId()), nullableString(replay.get("attachmentGroupId")));
        compare(conflicts, "resubmittedFromApprovalId", resubmittedFromApprovalId,
                replay.get("resubmittedFromApprovalId") == null ? null : number(replay, "resubmittedFromApprovalId").longValue());
        if (blankToNull(request.policyCode()) != null) compare(conflicts, "policyCode", request.policyCode().trim(), string(replay, "policyCode"));
        if (request.policyVersion() != null) compare(conflicts, "policyVersion", request.policyVersion(), number(replay, "policyVersion").intValue());
        if (blankToNull(request.businessDomain()) != null) compare(conflicts, "businessDomain", request.businessDomain().trim(), string(replay, "businessDomain"));
        if (blankToNull(request.approvalType()) != null) compare(conflicts, "approvalType", request.approvalType().trim(), string(replay, "approvalType"));
        Instant existingDueAt = timestampInstant(replay.get("dueAt"));
        if (!Objects.equals(request.dueAt(), existingDueAt)) conflicts.add("dueAt");
        if (!conflicts.isEmpty()) {
            throw new CpfValidationException("동일 requestIdempotencyKey가 다른 결재 요청에 재사용되었습니다. conflicts=" + String.join(",", conflicts));
        }
    }

    private void validateDecisionReplay(long approvalId, String idempotencyKey, String actor,
                                        String decisionStatus, String comment) {
        Map<String,Object> replay = repository.findParticipants(approvalId).stream()
                .filter(row -> idempotencyKey.equals(nullableString(row.get("decisionIdempotencyKey"))))
                .findFirst()
                .orElseThrow(() -> new CpfValidationException(
                        "동일 idempotencyKey가 다른 결재 또는 행위에 사용되었습니다."));
        List<String> conflicts = new ArrayList<>();
        compare(conflicts, "approvalId", approvalId, number(replay, "approvalId").longValue());
        compare(conflicts, "participantEmployeeNo", actor, string(replay, "participantEmployeeNo"));
        compare(conflicts, "decisionStatus", decisionStatus, string(replay, "decisionStatus"));
        compare(conflicts, "decisionComment", comment, nullableString(replay.get("decisionComment")));
        if (!conflicts.isEmpty()) {
            throw new CpfValidationException("동일 idempotencyKey의 결정 내용이 일치하지 않습니다. conflicts=" + String.join(",", conflicts));
        }
    }

    private static void compare(List<String> conflicts, String field, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) conflicts.add(field);
    }

    private static Instant timestampInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        return Instant.parse(String.valueOf(value));
    }

    private static String nullableString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public Map<String,Object> detail(long approvalId) {
        Map<String,Object> doc = new LinkedHashMap<>(repository.findDocument(approvalId)
                .orElseThrow(() -> new CpfValidationException("결재 문서를 찾을 수 없습니다.")));
        doc.put("participants", repository.findParticipants(approvalId));
        doc.put("lines", repository.findLineStatuses(approvalId));
        return doc;
    }

    private String employeeNo(String operatorId) {
        return repository.findEmployeeNoByLoginId(required(operatorId, "operatorId"))
                .orElseThrow(() -> new CpfValidationException("로그인 사용자와 연결된 직원이 없습니다."));
    }

    private static int boundedLimit(int limit) {
        return Math.max(1, Math.min(limit <= 0 ? 100 : limit, 1000));
    }

    private static String upperOrNull(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private Map<String,Object> resolvePolicy(String policyCode, Integer policyVersion,
            String businessDomain, String approvalType, Instant at) {
        if (policyCode != null && !policyCode.isBlank()) {
            if (policyVersion == null) throw new CpfValidationException("policyVersion이 필요합니다.");
            return repository.findPolicy(policyCode.trim(), policyVersion)
                    .orElseThrow(() -> new CpfValidationException("결재 정책을 찾을 수 없습니다."));
        }
        return repository.findActivePolicy(required(businessDomain, "businessDomain"),
                required(approvalType, "approvalType"), at)
                .orElseThrow(() -> new CpfValidationException("적용 가능한 활성 결재 정책이 없습니다."));
    }

    private List<Map<String,Object>> resolveParticipants(
            Map<String,Object> policy, List<Map<String,Object>> steps, String requester, Instant at) {
        boolean selfAllowed = "Y".equals(string(policy, "selfApprovalAllowedYn"));
        String businessDomain = string(policy, "businessDomain");
        String approvalType = string(policy, "approvalType");
        List<Map<String,Object>> result = new ArrayList<>();
        for (Map<String,Object> step : steps) {
            BackofficeApprovalTargetType targetType = BackofficeApprovalTargetType.valueOf(string(step, "targetType"));
            String targetCode = string(step, "targetCode");
            List<BackofficeApprovalDirectoryEntry> candidates = repository.resolve(targetType, targetCode, at);
            LinkedHashMap<String,Map<String,Object>> unique = new LinkedHashMap<>();
            for (BackofficeApprovalDirectoryEntry candidate : candidates) {
                if (!selfAllowed && requester != null && requester.equals(candidate.employeeNo())) continue;
                String delegatedFrom = null;
                String resolutionSource = sourceName(targetType);
                BackofficeApprovalDirectoryEntry effective = candidate;
                Optional<String> delegate = repository.findActiveDelegate(candidate.employeeNo(), businessDomain, approvalType, at);
                if (delegate.isPresent()) {
                    Optional<BackofficeApprovalDirectoryEntry> delegateProfile =
                            repository.findPrimaryAssignment(delegate.get(), at);
                    if (delegateProfile.isPresent()) {
                        delegatedFrom = candidate.employeeNo();
                        effective = delegateProfile.get();
                        resolutionSource = "DELEGATION";
                    }
                }
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("stepNo", number(step, "stepNo").intValue());
                row.put("targetType", targetType.name());
                row.put("targetCode", targetCode);
                row.put("approverEmployeeNo", effective.employeeNo());
                row.put("organizationCode", effective.organizationCode());
                row.put("positionCode", effective.positionCode());
                row.put("jobTitleCode", effective.jobTitleCode());
                row.put("delegatedFrom", delegatedFrom);
                row.put("resolutionSource", resolutionSource);
                unique.putIfAbsent(effective.employeeNo(), row);
            }
            int participantCount = unique.size();
            if (participantCount == 0 && "Y".equals(string(step, "requiredYn"))) {
                throw new CpfValidationException("필수 결재 Target의 유효 참여자가 0명입니다: "
                        + targetType + "/" + targetCode);
            }
            BackofficeApprovalDecisionRule rule = BackofficeApprovalDecisionRule.valueOf(string(step, "decisionRule"));
            if (participantCount > 0) {
                BackofficeApprovalDecisionEvaluator.evaluate(rule, participantCount, 0, 0,
                        step.get("requiredCount") == null ? null : number(step, "requiredCount").intValue());
            }
            result.addAll(unique.values());
        }
        return result;
    }

    private static boolean isApprovedLine(Map<String,Object> line) {
        String status = string(line, "decisionStatus");
        return status.equals("APPROVED") || status.equals("AGREED") || status.equals("SKIPPED");
    }
    private static String sourceName(BackofficeApprovalTargetType type) {
        return switch (type) {
            case EMPLOYEE -> "DIRECT";
            case ROLE -> "ROLE";
            case ORGANIZATION -> "ORG";
            case ORG_MANAGER -> "ORG_MANAGER";
            case POSITION -> "POSITION";
        };
    }
    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException e) { throw new CpfValidationException("정책 Snapshot JSON 생성에 실패했습니다."); }
    }
    private static String sha256(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) { throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e); }
    }
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new CpfValidationException(field + "는 필수입니다.");
        return value.trim();
    }
    private static <T> T required(T value, String field) {
        if (value == null) throw new CpfValidationException(field + "는 필수입니다.");
        return value;
    }
    private static String yn(String value, String defaultValue) {
        String v = upper(value, defaultValue);
        if (!v.equals("Y") && !v.equals("N")) throw new CpfValidationException("Y/N 값이 필요합니다.");
        return v;
    }
    private static String upper(String value, String defaultValue) {
        return (value == null || value.isBlank() ? defaultValue : value.trim()).toUpperCase(Locale.ROOT);
    }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static String string(Map<String,?> map, String key) {
        Object value = map.get(key); return value == null ? "" : String.valueOf(value);
    }
    private static Number number(Map<String,?> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number n) return n;
        if (value == null) return 0;
        return Long.parseLong(String.valueOf(value));
    }

    public record PolicyRequest(String policyCode, Integer policyVersion, String policyName,
            String businessDomain, String approvalType, Instant effectiveFrom, Instant effectiveTo,
            String enabledYn, String selfApprovalAllowedYn, String description,
            List<PolicyStepRequest> steps, String reason) {}
    public record PolicyStepRequest(Integer stepNo, String stepType, String targetType, String targetCode,
            String decisionRule, Integer requiredCount, String requiredYn, Integer sortOrder) {}
    public record DelegationRequest(Long delegationId, String delegatorEmployeeNo, String delegateEmployeeNo,
            String businessDomain, String approvalType, Instant validFrom, Instant validTo,
            String reason, String useYn) {}
    public record SubmitRequest(String policyCode, Integer policyVersion, String businessDomain, String approvalType,
            String requesterEmployeeNo, String title, String approvalMode, Instant dueAt,
            String payloadJson, String attachmentGroupId, String requestIdempotencyKey, String reason) {}
    public record DecisionRequest(String action, String idempotencyKey, String reason, String comment) {}
    public record LifecycleRequest(String idempotencyKey, String reason, String comment) {}
}
