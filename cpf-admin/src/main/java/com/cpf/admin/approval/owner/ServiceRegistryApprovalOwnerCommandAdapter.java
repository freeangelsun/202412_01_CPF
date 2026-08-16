package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.admin.opr.service.AdmControlPlaneService;
import com.cpf.integration.api.servicecall.CpfServiceRegistryControlPort;
import com.cpf.integration.api.servicecall.CpfServiceRegistryQueryPort;
import com.cpf.integration.api.servicecall.CpfServiceRegistryView;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

/** 승인 완료된 Service Registry Instance 위험조치만 Owner Port로 실행하는 Adapter입니다. */
@Component("cpfServiceRegistryApprovalOwnerCommandPort")
public final class ServiceRegistryApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    private static final String OWNER = "CPF-INTEGRATION";
    private static final String TARGET = "SERVICE_INSTANCE";
    private static final java.util.Set<String> STATE_COMMANDS = java.util.Set.of(
            "SERVICE_INSTANCE_DRAIN", "SERVICE_INSTANCE_DISABLE", "SERVICE_INSTANCE_RESUME");
    private static final java.util.Set<String> DELETE_COMMANDS = java.util.Set.of(
            "SERVICE_REGISTRY_SERVICE_DELETE", "SERVICE_REGISTRY_ENDPOINT_DELETE", "SERVICE_REGISTRY_INSTANCE_DELETE");
    private static final java.util.Set<String> COMMANDS = java.util.stream.Stream.concat(
            STATE_COMMANDS.stream(), DELETE_COMMANDS.stream()).collect(java.util.stream.Collectors.toUnmodifiableSet());

    private final CpfServiceRegistryQueryPort queryPort;
    private final AdmControlPlaneService controlPlane;

    /** ServiceRegistryApprovalOwnerCommandAdapter 작업을 CPF 표준 계약에 따라 수행한다. */
    public ServiceRegistryApprovalOwnerCommandAdapter(
            CpfServiceRegistryQueryPort queryPort, AdmControlPlaneService controlPlane) {
        this.queryPort = queryPort;
        this.controlPlane = controlPlane;
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand) {
        return OWNER.equals(canonical(ownerModule)) && COMMANDS.contains(canonical(ownerCommand));
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType) {
        String command = canonical(ownerCommand);
        if (!OWNER.equals(canonical(ownerModule)) || !COMMANDS.contains(command) || !command.equals(canonical(actionType))) return false;
        if (STATE_COMMANDS.contains(command)) return TARGET.equals(canonical(targetType));
        return deleteTargetType(command).equals(canonical(targetType));
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("SERVICE_REGISTRY_COMMAND_UNSUPPORTED", "지원하지 않는 Service Registry 승인 Command입니다.");
        }
        if (Objects.equals(command.requestedBy(), command.approvedBy())) {
            return failed("SERVICE_REGISTRY_SELF_APPROVAL", "요청자와 승인자는 달라야 합니다.");
        }
        if (DELETE_COMMANDS.contains(canonical(command.ownerCommand()))) return executeDelete(command);
        Target target;
        try { target = parse(command.targetId()); }
        catch (RuntimeException invalid) { return failed("SERVICE_REGISTRY_TARGET_INVALID", "targetId 형식이 올바르지 않습니다."); }
        CpfServiceRegistryView.Instance current = current(target);
        if (current == null) return failed("SERVICE_REGISTRY_INSTANCE_NOT_FOUND", "Service Instance를 찾을 수 없습니다.");
        if (current.version() != target.version()) {
            return failed("SERVICE_REGISTRY_VERSION_CONFLICT", "승인 Snapshot 이후 Instance version이 변경되었습니다.");
        }
        CpfServiceRegistryControlPort.InstanceCommand action = action(command.ownerCommand());
        try {
            CpfServiceRegistryView.MutationResult result = controlPlane.executeApprovedMaintenance(
                    command.commandRequestId(), target.serviceId(), target.endpointCode(), target.instanceId(),
                    action, target.version(), command.reason(), command.approvedBy());
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                    "SERVICE_REGISTRY_" + result.status(), "Service Instance 상태 변경 완료");
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IllegalStateException conflict) {
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "SERVICE_REGISTRY_STATE_UNKNOWN", "동시 변경 또는 결과 저장 불확실성으로 재확인이 필요합니다.");
        } catch (RuntimeException failure) {
            return failed("SERVICE_REGISTRY_STATE_FAILED", "Service Instance 상태 변경이 실패했습니다.");
        }
    }

    @Override
    public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("SERVICE_REGISTRY_RECONCILE_UNSUPPORTED", "지원하지 않는 Service Registry 승인 Command입니다.");
        }
        if (DELETE_COMMANDS.contains(canonical(command.ownerCommand()))) return reconcileDelete(command);
        final Target target;
        try { target = parse(command.targetId()); }
        catch (RuntimeException invalid) { return failed("SERVICE_REGISTRY_RECONCILE_TARGET_INVALID", "targetId 형식이 올바르지 않습니다."); }
        try {
            CpfServiceRegistryView.Instance current = current(target);
            if (current == null) return failed("SERVICE_REGISTRY_RECONCILE_NOT_FOUND", "Service Instance를 찾을 수 없습니다.");
            boolean applied = switch (action(command.ownerCommand())) {
                case DRAIN -> current.draining() || "DRAINING".equalsIgnoreCase(current.status());
                case DISABLE -> !current.active() || "DISABLED".equalsIgnoreCase(current.status());
                case RESUME -> current.active() && !current.draining() && !"DISABLED".equalsIgnoreCase(current.status());
            };
            if (applied) {
                return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                        "SERVICE_REGISTRY_RECONCILED", "Owner 상태 조회로 승인 조치 완료를 확인했습니다.");
            }
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "SERVICE_REGISTRY_RECONCILE_PENDING", "Owner 상태가 아직 성공을 확정하지 못했습니다.");
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (RuntimeException observationFailure) {
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "SERVICE_REGISTRY_RECONCILE_OBSERVATION_FAILED", "Owner 상태 조회 실패로 UNKNOWN을 유지합니다.");
        }
    }

    private AdmApprovedOperationResult executeDelete(AdmApprovedOperationCommand command) {
        DeleteTarget target;
        try { target = parseDelete(command.targetType(), command.targetId()); }
        catch (RuntimeException invalid) { return failed("SERVICE_REGISTRY_DELETE_TARGET_INVALID", "삭제 target Snapshot이 올바르지 않습니다."); }
        if (!deleteExists(target)) return failed("SERVICE_REGISTRY_DELETE_NOT_FOUND", "삭제 대상을 찾을 수 없습니다.");
        if (deleteVersion(target) != target.version()) return failed("SERVICE_REGISTRY_DELETE_VERSION_CONFLICT", "승인 Snapshot 이후 대상 version이 변경되었습니다.");
        try {
            CpfServiceRegistryView.MutationResult result = controlPlane.executeApprovedRegistryDelete(
                    command.commandRequestId(), target.targetType(), target.id(), target.version(), command.reason(), command.approvedBy());
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                    "SERVICE_REGISTRY_" + result.status(), "Service Registry 삭제 완료");
        } catch (IllegalStateException conflict) {
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "SERVICE_REGISTRY_DELETE_UNKNOWN", "삭제 결과 저장 불확실성으로 재확인이 필요합니다.");
        } catch (RuntimeException failure) {
            return failed("SERVICE_REGISTRY_DELETE_FAILED", "Service Registry 삭제가 실패했습니다.");
        }
    }

    private AdmApprovedOperationResult reconcileDelete(AdmApprovedOperationCommand command) {
        final DeleteTarget target;
        try { target = parseDelete(command.targetType(), command.targetId()); }
        catch (RuntimeException invalid) { return failed("SERVICE_REGISTRY_DELETE_RECONCILE_TARGET_INVALID", "삭제 target Snapshot이 올바르지 않습니다."); }
        try {
            if (!deleteExists(target)) return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                    "SERVICE_REGISTRY_DELETE_RECONCILED", "Owner 조회로 삭제 완료를 확인했습니다.");
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "SERVICE_REGISTRY_DELETE_RECONCILE_PENDING", "대상이 남아 있어 삭제 완료를 확정할 수 없습니다.");
        } catch (RuntimeException observationFailure) {
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "SERVICE_REGISTRY_DELETE_RECONCILE_OBSERVATION_FAILED", "Owner 조회 실패로 UNKNOWN을 유지합니다.");
        }
    }

    private boolean deleteExists(DeleteTarget target) {
        return switch (target.targetType()) {
            case "SERVICE_REGISTRY_SERVICE" -> queryPort.services(target.id(), null, 10).stream().anyMatch(v -> target.id().equals(v.serviceId()));
            case "SERVICE_REGISTRY_ENDPOINT" -> queryPort.endpoints(null, target.id(), null, 10).stream().anyMatch(v -> target.id().equals(v.endpointCode()));
            case "SERVICE_REGISTRY_INSTANCE" -> queryPort.instances(null, null, null, 1000).stream().anyMatch(v -> target.id().equals(v.instanceId()));
            default -> false;
        };
    }

    private long deleteVersion(DeleteTarget target) {
        return switch (target.targetType()) {
            case "SERVICE_REGISTRY_SERVICE" -> queryPort.services(target.id(), null, 10).stream().filter(v -> target.id().equals(v.serviceId())).findFirst().orElseThrow().version();
            case "SERVICE_REGISTRY_ENDPOINT" -> queryPort.endpoints(null, target.id(), null, 10).stream().filter(v -> target.id().equals(v.endpointCode())).findFirst().orElseThrow().version();
            case "SERVICE_REGISTRY_INSTANCE" -> queryPort.instances(null, null, null, 1000).stream().filter(v -> target.id().equals(v.instanceId())).findFirst().orElseThrow().version();
            default -> throw new IllegalArgumentException("지원하지 않는 삭제 대상입니다.");
        };
    }

    private static String deleteTargetType(String ownerCommand) {
        return switch (canonical(ownerCommand)) {
            case "SERVICE_REGISTRY_SERVICE_DELETE" -> "SERVICE_REGISTRY_SERVICE";
            case "SERVICE_REGISTRY_ENDPOINT_DELETE" -> "SERVICE_REGISTRY_ENDPOINT";
            case "SERVICE_REGISTRY_INSTANCE_DELETE" -> "SERVICE_REGISTRY_INSTANCE";
            default -> "";
        };
    }

    private static DeleteTarget parseDelete(String targetType, String value) {
        String type = canonical(targetType);
        if (!java.util.Set.of("SERVICE_REGISTRY_SERVICE", "SERVICE_REGISTRY_ENDPOINT", "SERVICE_REGISTRY_INSTANCE").contains(type)) {
            throw new IllegalArgumentException("unsupported targetType");
        }
        String raw = Objects.toString(value, "");
        int delimiter = raw.lastIndexOf('@');
        if (delimiter <= 0 || delimiter == raw.length() - 1) throw new IllegalArgumentException("targetId must be id@expectedVersion");
        String id = raw.substring(0, delimiter).trim();
        long version = Long.parseLong(raw.substring(delimiter + 1));
        if (id.isBlank() || version < 0) throw new IllegalArgumentException("invalid target snapshot");
        return new DeleteTarget(type, id, version);
    }

    private CpfServiceRegistryView.Instance current(Target target) {
        return queryPort.instances(target.serviceId(), target.endpointCode(), null, 1000).stream()
                .filter(instance -> target.instanceId().equals(instance.instanceId()))
                .findFirst().orElse(null);
    }

    private static CpfServiceRegistryControlPort.InstanceCommand action(String command) {
        return switch (canonical(command)) {
            case "SERVICE_INSTANCE_DRAIN" -> CpfServiceRegistryControlPort.InstanceCommand.DRAIN;
            case "SERVICE_INSTANCE_DISABLE" -> CpfServiceRegistryControlPort.InstanceCommand.DISABLE;
            case "SERVICE_INSTANCE_RESUME" -> CpfServiceRegistryControlPort.InstanceCommand.RESUME;
            default -> throw new IllegalArgumentException("지원하지 않는 Service Registry Command입니다.");
        };
    }

    /** targetId 형식: serviceId@endpointCode@instanceId@expectedVersion */
    private static Target parse(String value) {
        String[] parts = Objects.toString(value, "").split("@", 4);
        if (parts.length != 4 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            throw new IllegalArgumentException("targetId must be serviceId@endpointCode@instanceId@expectedVersion");
        }
        long version = Long.parseLong(parts[3]);
        if (version < 0) throw new IllegalArgumentException("expectedVersion must be non-negative");
        return new Target(parts[0].trim(), parts[1].trim(), parts[2].trim(), version);
    }

    private static AdmApprovedOperationResult failed(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED, code, message);
    }
    private static String canonical(String value) {
        return Objects.toString(value, "").trim().toUpperCase(Locale.ROOT);
    }
    /** Target 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    private record Target(String serviceId, String endpointCode, String instanceId, long version) { }
    private record DeleteTarget(String targetType, String id, long version) { }
}
