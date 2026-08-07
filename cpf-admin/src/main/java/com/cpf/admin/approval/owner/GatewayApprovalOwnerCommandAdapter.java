package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.*;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

/** ADM 승인 완료 Snapshot을 Gateway Owner Command로 실행하는 Local/Remote 공통 Adapter입니다. */
@Component("cpfGatewayApprovalOwnerCommandPort")
public final class GatewayApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    private final CpfGatewayRegistryPort registry;

    public GatewayApprovalOwnerCommandAdapter(CpfGatewayRegistryPort registry) {
        this.registry = registry;
    }

    private static final java.util.Set<OwnerTuple> ALLOWED = java.util.Set.of(
            tuple("GATEWAY_BINDING_APPROVE"), tuple("GATEWAY_BINDING_ACTIVATE"),
            tuple("GATEWAY_BINDING_BLOCK"), tuple("GATEWAY_BINDING_RETIRE"));

    public boolean supports(String ownerModule, String ownerCommand) {
        String owner=canonical(ownerModule), command=canonical(ownerCommand);
        return ALLOWED.stream().anyMatch(tuple -> tuple.ownerModule().equals(owner) && tuple.ownerCommand().equals(command));
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType) {
        return ALLOWED.contains(new OwnerTuple(canonical(ownerModule),canonical(ownerCommand),canonical(actionType),canonical(targetType)));
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("GATEWAY_OWNER_MISMATCH", "Gateway Owner/Command/Action/Target 조합이 일치하지 않습니다.");
        }
        if (command.requestedBy().equals(command.approvedBy())) {
            return failed("GATEWAY_SELF_APPROVAL", "요청자와 승인 실행자는 달라야 합니다.");
        }
        CpfGatewayRegistryPort.GatewayBinding binding = registry.findBindings(null, null, null, 10_000).stream()
                .filter(item -> item.bindingId().equals(command.targetId()))
                .findFirst().orElse(null);
        if (binding == null) return failed("GATEWAY_BINDING_NOT_FOUND", "Gateway Binding을 찾을 수 없습니다.");
        if (!Objects.equals(binding.bindingChecksum(), command.payloadHash())) {
            return failed("GATEWAY_APPROVAL_HASH_MISMATCH", "승인 Snapshot 이후 Binding이 변경되었습니다.");
        }
        String targetState = switch (command.ownerCommand().toUpperCase(Locale.ROOT)) {
            case "GATEWAY_BINDING_APPROVE" -> "APPROVED";
            case "GATEWAY_BINDING_ACTIVATE" -> "ACTIVE";
            case "GATEWAY_BINDING_BLOCK" -> "BLOCKED";
            case "GATEWAY_BINDING_RETIRE" -> "RETIRED";
            default -> null;
        };
        if (targetState == null) return failed("GATEWAY_COMMAND_UNSUPPORTED", "지원하지 않는 Gateway 승인 Command입니다.");
        try {
            CpfGatewayRegistryPort.MutationResult result = registry.changeBindingState(
                    new CpfGatewayRegistryPort.BindingStateCommand(
                            command.commandRequestId(), binding.bindingId(), targetState, binding.version(),
                            String.valueOf(command.approvalRequestId()), command.reason(), command.approvedBy()));
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.SUCCEEDED, "GATEWAY_" + result.status(), "Gateway 상태 전환 완료");
        } catch (IllegalStateException conflict) {
            return new AdmApprovedOperationResult(
                    AdmApprovalExecutionStatus.UNKNOWN, "GATEWAY_STATE_UNKNOWN", "동시 변경으로 결과 재확인이 필요합니다.");
        } catch (RuntimeException ex) {
            return failed("GATEWAY_STATE_FAILED", "Gateway 상태 전환이 거부되었습니다.");
        }
    }

    @Override
    public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("GATEWAY_RECONCILE_UNSUPPORTED", "Gateway Owner/Command/Action/Target 조합이 일치하지 않습니다.");
        }
        try {
            CpfGatewayRegistryPort.GatewayBinding binding = registry.findBindings(null, null, null, 10_000).stream()
                    .filter(item -> item.bindingId().equals(command.targetId()))
                    .findFirst().orElse(null);
            if (binding == null) {
                return failed("GATEWAY_RECONCILE_NOT_FOUND", "Gateway Binding을 찾을 수 없습니다.");
            }
            if (!Objects.equals(binding.bindingChecksum(), command.payloadHash())) {
                return failed("GATEWAY_RECONCILE_HASH_MISMATCH", "현재 Binding checksum이 승인 Snapshot과 다릅니다.");
            }
            String expected = switch (command.ownerCommand().toUpperCase(Locale.ROOT)) {
                case "GATEWAY_BINDING_APPROVE" -> "APPROVED";
                case "GATEWAY_BINDING_ACTIVATE" -> "ACTIVE";
                case "GATEWAY_BINDING_BLOCK" -> "BLOCKED";
                case "GATEWAY_BINDING_RETIRE" -> "RETIRED";
                default -> "";
            };
            if (!expected.isBlank() && expected.equalsIgnoreCase(binding.status())) {
                return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                        "GATEWAY_RECONCILED_" + expected,
                        "Gateway Owner 상태 조회로 상태 전환 완료를 확인했습니다.");
            }
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "GATEWAY_RECONCILE_PENDING", "Gateway Owner 상태가 아직 성공을 확정하지 못했습니다.");
        } catch (RuntimeException observationFailure) {
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "GATEWAY_RECONCILE_OBSERVATION_FAILED", "Gateway Owner 상태 조회 실패로 UNKNOWN을 유지합니다.");
        }
    }

    private static AdmApprovedOperationResult failed(String code,String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED,code,message);
    }
    private static OwnerTuple tuple(String command) { return new OwnerTuple("CPF-GATEWAY",command,command,"GATEWAY_BINDING"); }
    private static String canonical(String value) { return Objects.toString(value,"").trim().toUpperCase(Locale.ROOT); }
    private record OwnerTuple(String ownerModule,String ownerCommand,String actionType,String targetType) { }
}
