package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.admin.opr.dto.AdmCacheControlResponse;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmCacheOperationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Cache Refresh/Evict/Reconcile 위험조치를 Approval Engine의 불변 Snapshot으로만 실행합니다. */
@Component("cpfCacheApprovalOwnerCommandPort")
public final class CacheApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    public static final String OWNER_MODULE = "CPF-DATA-CACHE";
    public static final String TARGET_TYPE = "CACHE";
    public static final String REFRESH = "CACHE_REFRESH";
    public static final String EVICT_KEY = "CACHE_EVICT_KEY";
    public static final String EVICT_NAMESPACE = "CACHE_EVICT_NAMESPACE";
    public static final String RECONCILE = "CACHE_RECONCILE";
    private static final Set<String> COMMANDS = Set.of(REFRESH, EVICT_KEY, EVICT_NAMESPACE, RECONCILE);

    private final AdmCacheOperationService service;
    private final AdmAuditLogService audit;
    private final ObjectMapper mapper;

    public CacheApprovalOwnerCommandAdapter(AdmCacheOperationService service, AdmAuditLogService audit, ObjectMapper mapper) {
        this.service = Objects.requireNonNull(service, "service");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand) {
        return OWNER_MODULE.equals(text(ownerModule)) && COMMANDS.contains(text(ownerCommand));
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType) {
        String command = text(ownerCommand);
        return supports(ownerModule, ownerCommand)
                && command.equals(text(actionType))
                && TARGET_TYPE.equals(text(targetType));
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("CACHE_OWNER_MISMATCH", "Cache 승인 Owner 조합이 올바르지 않습니다.");
        }
        if (command.requestedBy().equals(command.approvedBy())) {
            return failed("CACHE_SELF_APPROVAL", "Cache 위험조치 요청자와 승인자는 달라야 합니다.");
        }
        try {
            JsonNode payload = mapper.readTree(command.payloadSnapshot());
            AdmCacheControlResponse response = switch (text(command.ownerCommand())) {
                case REFRESH -> service.refresh(required(payload, "target"), command.approvedBy(), command.reason());
                case EVICT_KEY -> service.evictKey(
                        required(payload, "tenantId"), required(payload, "namespace"), required(payload, "key"),
                        nonNegativeLong(payload, "version"), command.approvedBy(), command.reason());
                case EVICT_NAMESPACE -> service.evictNamespace(
                        required(payload, "tenantId"), required(payload, "namespace"),
                        nonNegativeLong(payload, "version"), command.approvedBy(), command.reason());
                case RECONCILE -> service.reconcile(command.approvedBy(), command.reason());
                default -> throw new IllegalArgumentException("지원하지 않는 Cache 명령입니다.");
            };
            if (response == null || !response.accepted()) {
                return failed("CACHE_OWNER_REJECTED", "Cache Owner가 조치를 수락하지 않았습니다.");
            }
            audit.record(command.transactionId(), command.approvedBy(), command.ownerCommand(), "cache",
                    command.targetId(), command.reason(), "approval-engine");
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                    "CACHE_" + response.operation() + "_SUCCEEDED", "Cache Owner 조치가 완료되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            return failed("CACHE_COMMAND_REJECTED", safe(rejected.getMessage()));
        } catch (RuntimeException uncertain) {
            return unknown("CACHE_COMMAND_UNKNOWN", "Cache Owner 조치 결과를 확정할 수 없습니다.");
        } catch (Exception invalid) {
            return failed("CACHE_APPROVED_PAYLOAD_INVALID", "Cache 승인 Payload를 해석할 수 없습니다.");
        }
    }

    @Override
    public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("CACHE_OWNER_MISMATCH", "Cache 승인 Owner 조합이 올바르지 않습니다.");
        }
        if (!RECONCILE.equals(text(command.ownerCommand()))) {
            return unknown("CACHE_RECONCILE_OBSERVATION_REQUIRED",
                    "Refresh/Evict는 동일 mutation을 재실행하지 않으며 Owner 상태를 안전하게 증명할 수 없어 UNKNOWN을 유지합니다.");
        }
        try {
            var summary = service.summary();
            if (summary.durableBacklog() == 0) {
                return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                        "CACHE_RECONCILED", "Durable Cache backlog가 0으로 수렴한 상태를 확인했습니다.");
            }
            return unknown("CACHE_RECONCILE_PENDING", "Durable Cache backlog가 남아 있어 UNKNOWN을 유지합니다.");
        } catch (RuntimeException failure) {
            return unknown("CACHE_RECONCILE_OBSERVATION_FAILED", "Cache 상태 조회 실패로 UNKNOWN을 유지합니다.");
        }
    }

    private static String required(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field) || node.get(field).asText().isBlank()) {
            throw new IllegalArgumentException(field + "가 필요합니다.");
        }
        return node.get(field).asText().trim();
    }

    private static long nonNegativeLong(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field) || !node.get(field).canConvertToLong()) {
            throw new IllegalArgumentException(field + "가 필요합니다.");
        }
        long value = node.get(field).asLong();
        if (value < 0) throw new IllegalArgumentException(field + "는 0 이상이어야 합니다.");
        return value;
    }

    private static String text(String value) {
        return Objects.toString(value, "").trim().toUpperCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "Cache 위험조치 요청이 유효하지 않습니다." : value;
    }

    private static AdmApprovedOperationResult failed(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED, code, message);
    }

    private static AdmApprovedOperationResult unknown(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN, code, message);
    }
}
