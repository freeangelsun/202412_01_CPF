package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmDynamicLogLevelBroadcastService;
import com.cpf.admin.opr.service.AdmDynamicLogLevelRuleStore;
import com.cpf.platform.operations.observability.api.logging.CpfDynamicLogLevelOperations;
import com.cpf.platform.operations.observability.api.logging.CpfLogLevel;
import com.cpf.platform.operations.observability.api.logging.DynamicLogLevelRequest;
import com.cpf.platform.operations.observability.api.logging.DynamicLogLevelRule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** 동적 로그 레벨 변경을 Approval Engine Owner Command로만 실행합니다. */
@Component("cpfDynamicLogLevelApprovalOwnerCommandPort")
public final class DynamicLogLevelApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    public static final String OWNER_MODULE = "CPF-PLATFORM-OBSERVABILITY";
    public static final String TARGET_TYPE = "DYNAMIC_LOG_LEVEL";
    public static final String REGISTER = "DYNAMIC_LOG_REGISTER";
    public static final String REMOVE = "DYNAMIC_LOG_REMOVE";
    private static final Set<String> COMMANDS = Set.of(REGISTER, REMOVE);

    private final CpfDynamicLogLevelOperations runtime;
    private final AdmDynamicLogLevelRuleStore store;
    private final AdmDynamicLogLevelBroadcastService broadcast;
    private final AdmAuditLogService audit;
    private final ObjectMapper mapper;

    public DynamicLogLevelApprovalOwnerCommandAdapter(CpfDynamicLogLevelOperations runtime,
            AdmDynamicLogLevelRuleStore store, AdmDynamicLogLevelBroadcastService broadcast,
            AdmAuditLogService audit, ObjectMapper mapper) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.store = Objects.requireNonNull(store, "store");
        this.broadcast = Objects.requireNonNull(broadcast, "broadcast");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override public boolean supports(String ownerModule, String ownerCommand) {
        return OWNER_MODULE.equals(text(ownerModule)) && COMMANDS.contains(text(ownerCommand));
    }
    @Override public boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType) {
        String command = text(ownerCommand);
        return supports(ownerModule, ownerCommand) && command.equals(text(actionType)) && TARGET_TYPE.equals(text(targetType));
    }

    @Override public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType()))
            return failed("DYNAMIC_LOG_OWNER_MISMATCH", "동적 로그 승인 Owner 조합이 올바르지 않습니다.");
        if (command.requestedBy().equals(command.approvedBy()))
            return failed("DYNAMIC_LOG_SELF_APPROVAL", "동적 로그 변경 요청자와 승인자는 달라야 합니다.");
        try {
            JsonNode payload = mapper.readTree(command.payloadSnapshot());
            if (REGISTER.equals(text(command.ownerCommand()))) {
                DynamicLogLevelRequest request = new DynamicLogLevelRequest();
                request.setBusinessTransactionId(optional(payload, "businessTransactionId"));
                request.setTransactionId(optional(payload, "transactionId"));
                if (blank(request.getBusinessTransactionId()) && blank(request.getTransactionId()))
                    return failed("DYNAMIC_LOG_TARGET_REQUIRED", "업무 거래 ID 또는 거래 ID가 필요합니다.");
                request.setModuleId("ADM");
                request.setLogLevel(CpfLogLevel.valueOf(required(payload, "logLevel").toUpperCase(Locale.ROOT)));
                long ttlSeconds = positiveLong(payload, "ttlSeconds");
                if (ttlSeconds > 3600) return failed("DYNAMIC_LOG_TTL_TOO_LONG", "동적 로그 TTL은 3600초 이하여야 합니다.");
                request.setTtl(Duration.ofSeconds(ttlSeconds));
                request.setReason(command.reason());
                request.setRequestUser(command.approvedBy());
                DynamicLogLevelRule rule = runtime.register(request);
                store.save(rule);
                broadcast.publishUpsert(rule, command.approvedBy());
                audit.record(command.transactionId(), command.approvedBy(), REGISTER, "adm_dynamic_log_level_rule",
                        rule.ruleId(), command.reason(), "approval-engine");
                return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                        "DYNAMIC_LOG_REGISTERED", "동적 로그 규칙 등록과 전파를 완료했습니다.");
            }
            String ruleId = required(payload, "ruleId");
            boolean runtimeRemoved = runtime.remove(ruleId);
            boolean persistedDisabled = store.disable(ruleId, command.approvedBy());
            broadcast.publishDelete(ruleId, command.approvedBy());
            audit.record(command.transactionId(), command.approvedBy(), REMOVE, "adm_dynamic_log_level_rule",
                    ruleId, command.reason(), "approval-engine");
            if (!runtimeRemoved && !persistedDisabled)
                return failed("DYNAMIC_LOG_RULE_NOT_FOUND", "승인 대상 동적 로그 규칙을 찾을 수 없습니다.");
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                    "DYNAMIC_LOG_REMOVED", "동적 로그 규칙 제거와 전파를 완료했습니다.");
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            return failed("DYNAMIC_LOG_COMMAND_REJECTED", safe(rejected.getMessage()));
        } catch (RuntimeException uncertain) {
            return unknown("DYNAMIC_LOG_COMMAND_UNKNOWN", "동적 로그 변경 결과를 확정할 수 없습니다.");
        } catch (Exception invalid) {
            return failed("DYNAMIC_LOG_APPROVED_PAYLOAD_INVALID", "동적 로그 승인 Payload를 해석할 수 없습니다.");
        }
    }

    @Override public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType()))
            return failed("DYNAMIC_LOG_OWNER_MISMATCH", "동적 로그 승인 Owner 조합이 올바르지 않습니다.");
        try {
            JsonNode payload = mapper.readTree(command.payloadSnapshot());
            if (REMOVE.equals(text(command.ownerCommand()))) {
                String ruleId = required(payload, "ruleId");
                boolean active = store.findActiveRules().stream().anyMatch(rule -> ruleId.equals(rule.ruleId()));
                return active ? unknown("DYNAMIC_LOG_REMOVE_PENDING", "동적 로그 규칙이 아직 활성 상태입니다.")
                        : new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                                "DYNAMIC_LOG_REMOVE_RECONCILED", "동적 로그 규칙 비활성 상태를 확인했습니다.");
            }
            String transactionId = optional(payload, "transactionId");
            String businessTransactionId = optional(payload, "businessTransactionId");
            String level = required(payload, "logLevel").toUpperCase(Locale.ROOT);
            boolean observed = store.findActiveRules().stream().anyMatch(rule ->
                    Objects.equals(trim(rule.transactionId()), trim(transactionId))
                            && Objects.equals(trim(rule.businessTransactionId()), trim(businessTransactionId))
                            && rule.logLevel().name().equals(level));
            return observed ? new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                    "DYNAMIC_LOG_REGISTER_RECONCILED", "승인 Snapshot과 일치하는 활성 규칙을 확인했습니다.")
                    : unknown("DYNAMIC_LOG_REGISTER_PENDING", "승인 Snapshot과 일치하는 활성 규칙을 아직 확인하지 못했습니다.");
        } catch (Exception failure) {
            return unknown("DYNAMIC_LOG_RECONCILE_OBSERVATION_FAILED", "동적 로그 상태 조회 실패로 UNKNOWN을 유지합니다.");
        }
    }

    private static String required(JsonNode node, String field) {
        String value = optional(node, field);
        if (blank(value)) throw new IllegalArgumentException(field + "가 필요합니다.");
        return value;
    }
    private static String optional(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) ? node.get(field).asText().trim() : null;
    }
    private static long positiveLong(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field) || !node.get(field).canConvertToLong())
            throw new IllegalArgumentException(field + "가 필요합니다.");
        long value = node.get(field).asLong();
        if (value <= 0) throw new IllegalArgumentException(field + "는 양수여야 합니다.");
        return value;
    }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static String text(String value) { return Objects.toString(value, "").trim().toUpperCase(Locale.ROOT); }
    private static String safe(String value) { return blank(value) ? "동적 로그 요청이 유효하지 않습니다." : value; }
    private static AdmApprovedOperationResult failed(String c, String m) { return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED, c, m); }
    private static AdmApprovedOperationResult unknown(String c, String m) { return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN, c, m); }
}
