package com.cpf.bizadmin.approval.service;

import com.cpf.bizadmin.approval.api.BzaApprovalDecisionEvaluator;
import com.cpf.bizadmin.approval.api.BzaApprovalDecisionRule;
import com.cpf.bizadmin.approval.api.BzaApprovalStepStatus;
import com.cpf.bizadmin.approval.api.BzaApprovalTargetType;
import com.cpf.bizadmin.approval.repository.BzaApprovalPolicyRepository;
import com.cpf.bizadmin.approval.spi.BzaApprovalDirectoryEntry;
import com.cpf.bizadmin.common.base.BzaBaseService;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * BZA Versioned Approval Policy Engine.
 *
 * <p>정책 Target은 상신 시점에 실제 참여자로 해석하여 Snapshot하고 ALL/ANY/N_OF_M 결정을
 * Snapshot 분모로 평가합니다. 진행 중 조직/Role 변경은 이미 상신된 결재의 참여자를 바꾸지 않습니다.</p>
 */
@Service
public class BzaApprovalPolicyService extends BzaBaseService {
    private static final Set<String> STEP_TYPES = Set.of("APPROVAL", "AGREEMENT", "REVIEW");
    private static final Set<String> MODES = Set.of("SEQUENTIAL", "PARALLEL");
    private static final DateTimeFormatter NO_DATE =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);

    private final BzaApprovalPolicyRepository repository;
    private final ObjectMapper objectMapper;

    public BzaApprovalPolicyService(BzaApprovalPolicyRepository repository, ObjectMapper objectMapper) {
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

    @Transactional(transactionManager = "bzaTransactionManager")
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
            BzaApprovalTargetType targetType;
            try { targetType = BzaApprovalTargetType.valueOf(required(step.targetType(), "targetType").toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ex) { throw new CpfValidationException("지원하지 않는 targetType입니다."); }
            BzaApprovalDecisionRule rule;
            try { rule = BzaApprovalDecisionRule.valueOf(upper(step.decisionRule(), "ALL")); }
            catch (IllegalArgumentException ex) { throw new CpfValidationException("지원하지 않는 decisionRule입니다."); }
            Integer requiredCount = step.requiredCount();
            if (rule == BzaApprovalDecisionRule.N_OF_M && (requiredCount == null || requiredCount < 1)) {
                throw new CpfValidationException("N_OF_M에는 requiredCount가 필요합니다.");
            }
            if (rule != BzaApprovalDecisionRule.N_OF_M) requiredCount = null;
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

    @Transactional(transactionManager = "bzaTransactionManager")
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

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String,Object> submit(SubmitRequest request, String operatorId) {
        return submitInternal(request, operatorId, null);
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String,Object> resubmit(long previousApprovalId, SubmitRequest request, String operatorId) {
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

    private Map<String,Object> submitInternal(SubmitRequest request, String operatorId, Long resubmittedFromApprovalId) {
        String idem = required(request.requestIdempotencyKey(), "requestIdempotencyKey");
        Optional<Long> existing = repository.findApprovalByIdempotencyKey(idem);
        if (existing.isPresent()) return detail(existing.get());

        Instant at = Instant.now();
        String requester = repository.findEmployeeNoByLoginId(required(operatorId, "operatorId"))
                .orElseThrow(() -> new CpfValidationException("로그인 사용자와 연결된 직원이 없습니다."));
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
        BzaApprovalDirectoryEntry requesterProfile = repository.findPrimaryAssignment(requester, at)
                .orElseThrow(() -> new CpfValidationException("요청자의 유효 직원/대표 소속 Snapshot을 찾을 수 없습니다."));

        String mode = upper(request.approvalMode(), "SEQUENTIAL");
        if (!MODES.contains(mode)) throw new CpfValidationException("approvalMode는 SEQUENTIAL/PARALLEL 이어야 합니다.");
        String payload = request.payloadJson() == null ? "{}" : request.payloadJson();
        String policySnapshot = json(Map.of("policy", policy, "steps", steps, "participants", resolved));

        Map<String,Object> doc = new LinkedHashMap<>();
        doc.put("approvalNo", "BZA-" + NO_DATE.format(at) + "-" + UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT));
        doc.put("approvalType", string(policy, "approvalType"));
        doc.put("businessDomain", string(policy, "businessDomain"));
        doc.put("policyCode", policyCode);
        doc.put("policyVersion", policyVersion);
        doc.put("policySnapshotJson", policySnapshot);
        doc.put("title", required(request.title(), "title"));
        doc.put("requesterEmployeeNo", requester);
        doc.put("requesterOrganizationCode", requesterProfile.organizationCode());
        doc.put("requesterPositionCode", requesterProfile.positionCode());
        doc.put("requesterJobTitleCode", requesterProfile.jobTitleCode());
        doc.put("approvalMode", mode);
        doc.put("dueAt", request.dueAt() == null ? null : Timestamp.from(request.dueAt()));
        doc.put("payloadJson", payload);
        doc.put("payloadHash", sha256(payload));
        doc.put("requestIdempotencyKey", idem);
        doc.put("attachmentGroupId", blankToNull(request.attachmentGroupId()));
        doc.put("resubmittedFromApprovalId", resubmittedFromApprovalId);
        doc.put("transactionId", CpfTransactionContext.transactionId());
        doc.put("operatorId", required(operatorId, "operatorId"));
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
                required(request.reason(), "reason"), "DRAFT", "IN_REVIEW", null,
                string(doc, "transactionId"), operatorId);
        return detail(approvalId);
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String,Object> decide(long approvalId, DecisionRequest request, String operatorId) {
        String idem = required(request.idempotencyKey(), "idempotencyKey");
        if (repository.participantDecisionExists(idem)) return detail(approvalId);
        String actor = repository.findEmployeeNoByLoginId(required(operatorId, "operatorId"))
                .orElseThrow(() -> new CpfValidationException("로그인 사용자와 연결된 직원이 없습니다."));
        Map<String,Object> doc = repository.findDocument(approvalId)
                .orElseThrow(() -> new CpfValidationException("결재 문서를 찾을 수 없습니다."));
        String beforeStatus = string(doc, "approvalStatus");
        if (!"IN_REVIEW".equals(beforeStatus)) throw new CpfValidationException("진행 중인 결재만 결정할 수 있습니다.");
        int currentStep = number(doc, "currentStepNo").intValue();
        String mode = string(doc, "approvalMode");
        Map<String,Object> participant = repository.findWaitingParticipant(approvalId, actor, mode, currentStep)
                .orElseThrow(() -> new CpfValidationException("현재 결정 가능한 결재 참여자가 아닙니다."));
        String action = upper(request.action(), "APPROVE");
        String participantStatus = switch (action) {
            case "APPROVE" -> "APPROVED";
            case "AGREE" -> "AGREED";
            case "REJECT" -> "REJECTED";
            default -> throw new CpfValidationException("APPROVE/AGREE/REJECT만 지원합니다.");
        };
        if (repository.decideParticipant(number(participant, "participantId").longValue(), participantStatus,
                idem, blankToNull(request.comment()), operatorId) != 1) {
            throw new CpfValidationException("결재 참여자 상태가 동시에 변경되었습니다.");
        }

        long lineId = number(participant, "lineId").longValue();
        Map<String,Object> counts = repository.participantCounts(lineId);
        BzaApprovalDecisionRule rule = BzaApprovalDecisionRule.valueOf(string(participant, "decisionRule"));
        Integer requiredCount = participant.get("requiredCount") == null ? null : number(participant, "requiredCount").intValue();
        BzaApprovalStepStatus lineDecision = BzaApprovalDecisionEvaluator.evaluate(
                rule, number(counts, "participantCount").intValue(),
                number(counts, "approvedCount").intValue(),
                number(counts, "rejectedCount").intValue(), requiredCount);
        String lineStatus = switch (lineDecision) {
            case APPROVED -> "AGREEMENT".equals(string(participant, "stepType")) ? "AGREED" : "APPROVED";
            case REJECTED -> "REJECTED";
            case WAITING -> "WAITING";
        };
        repository.updateLineStatus(lineId, lineStatus, blankToNull(request.comment()), operatorId);

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
                    .allMatch(BzaApprovalPolicyService::isApprovedLine);
            if (allDone) { afterStatus = "APPROVED"; completed = true; }
        } else {
            boolean currentDone = lines.stream()
                    .filter(line -> number(line, "stepNo").intValue() == currentStep)
                    .filter(line -> "Y".equals(string(line, "requiredYn")))
                    .allMatch(BzaApprovalPolicyService::isApprovedLine);
            if (currentDone) {
                OptionalInt later = lines.stream().mapToInt(line -> number(line, "stepNo").intValue())
                        .filter(step -> step > currentStep).min();
                if (later.isPresent()) nextStep = later.getAsInt();
                else { afterStatus = "APPROVED"; completed = true; }
            }
        }
        long version = number(doc, "versionNo").longValue();
        if (repository.updateDocumentStatus(approvalId, version, afterStatus, nextStep, completed, operatorId) != 1) {
            throw new CpfValidationException("결재 문서가 동시에 변경되었습니다. 최신 상태를 다시 조회하세요.");
        }
        repository.insertHistory(approvalId, action, actor, idem + ":history",
                required(request.reason(), "reason"), beforeStatus, afterStatus,
                blankToNull(request.comment()), string(doc, "transactionId"), operatorId);
        return detail(approvalId);
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String,Object> withdraw(long approvalId, LifecycleRequest request, String operatorId) {
        return requesterLifecycle(approvalId, request, operatorId, "WITHDRAW", "WITHDRAWN");
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String,Object> cancel(long approvalId, LifecycleRequest request, String operatorId) {
        return requesterLifecycle(approvalId, request, operatorId, "CANCEL", "CANCELED");
    }

    @Transactional(transactionManager = "bzaTransactionManager")
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
        if (repository.historyActionExists(idem)) return detail(approvalId);
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

    public Map<String,Object> detail(long approvalId) {
        Map<String,Object> doc = new LinkedHashMap<>(repository.findDocument(approvalId)
                .orElseThrow(() -> new CpfValidationException("결재 문서를 찾을 수 없습니다.")));
        doc.put("participants", repository.findParticipants(approvalId));
        doc.put("lines", repository.findLineStatuses(approvalId));
        return doc;
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
            BzaApprovalTargetType targetType = BzaApprovalTargetType.valueOf(string(step, "targetType"));
            String targetCode = string(step, "targetCode");
            List<BzaApprovalDirectoryEntry> candidates = repository.resolve(targetType, targetCode, at);
            LinkedHashMap<String,Map<String,Object>> unique = new LinkedHashMap<>();
            for (BzaApprovalDirectoryEntry candidate : candidates) {
                if (!selfAllowed && requester != null && requester.equals(candidate.employeeNo())) continue;
                String delegatedFrom = null;
                String resolutionSource = sourceName(targetType);
                BzaApprovalDirectoryEntry effective = candidate;
                Optional<String> delegate = repository.findActiveDelegate(candidate.employeeNo(), businessDomain, approvalType, at);
                if (delegate.isPresent()) {
                    Optional<BzaApprovalDirectoryEntry> delegateProfile =
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
            BzaApprovalDecisionRule rule = BzaApprovalDecisionRule.valueOf(string(step, "decisionRule"));
            if (participantCount > 0) {
                BzaApprovalDecisionEvaluator.evaluate(rule, participantCount, 0, 0,
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
    private static String sourceName(BzaApprovalTargetType type) {
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
