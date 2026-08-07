package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.*;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.batch.api.BatchJobDefinitionControlPort;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

/** 승인 Snapshot을 BAT Published Definition Owner Command로 실행합니다. */
@Component("cpfBatchJobDefinitionApprovalOwnerCommandPort")
public final class BatchJobDefinitionApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    private final BatchJobDefinitionControlPort port;
    public BatchJobDefinitionApprovalOwnerCommandAdapter(BatchJobDefinitionControlPort port){this.port=port;}

    private static final java.util.Set<OwnerTuple> ALLOWED = java.util.Set.of(
            new OwnerTuple("BAT", "BAT_JOB_PUBLISH", "BAT_JOB_PUBLISH", "BAT_JOB_DEFINITION"),
            new OwnerTuple("BAT", "BAT_JOB_PUBLISH", "BAT_JOB_PUBLISH", "BAT_JOB"));

    public boolean supports(String ownerModule, String ownerCommand) {
        String owner = canonical(ownerModule);
        String command = canonical(ownerCommand);
        return ALLOWED.stream().anyMatch(tuple -> tuple.ownerModule().equals(owner) && tuple.ownerCommand().equals(command));
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType) {
        return ALLOWED.contains(new OwnerTuple(canonical(ownerModule), canonical(ownerCommand),
                canonical(actionType), canonical(targetType)));
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if(command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType()))
            return failed("BAT_COMMAND_UNSUPPORTED","지원하지 않는 BAT 승인 Owner/Command/Action/Target 조합입니다.");
        if(command.requestedBy().equals(command.approvedBy()))return failed("BAT_SELF_APPROVAL","요청자와 승인자는 달라야 합니다.");
        Target target=parse(command.targetId());
        try {
            BatchJobDefinitionControlPort.DefinitionState current=port.state(target.jobId(),target.version());
            if(!Objects.equals(current.checksum(),command.payloadHash()))return failed("BAT_APPROVAL_HASH_MISMATCH","승인 Snapshot 이후 Definition이 변경되었습니다.");
            BatchJobDefinitionControlPort.PublishResult result=port.publishApproved(new BatchJobDefinitionControlPort.PublishCommand(
                    command.commandRequestId(),target.jobId(),target.version(),current.rowVersion(),command.approvalRequestId(),
                    command.payloadHash(),command.requestedBy(),command.approvedBy(),command.reason()));
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,"BAT_"+result.state(),"BAT Job Definition 배포 완료");
        } catch(IllegalStateException failure) {
            String message=Objects.toString(failure.getMessage(),"").toUpperCase(Locale.ROOT);
            if(message.contains("UNKNOWN")||message.contains("RECONCILIATION REQUIRED")
                    ||message.contains("RESULT PERSISTENCE IS UNRESOLVED")) {
                return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                        "BAT_PUBLISH_UNKNOWN","BAT 배포 결과 재확인이 필요합니다.");
            }
            return failed("BAT_PUBLISH_REJECTED","BAT Job Definition 배포가 상태·버전·승인 검증에서 거부되었습니다.");
        } catch(RuntimeException ex) {
            return failed("BAT_PUBLISH_FAILED","BAT Job Definition 배포가 실패했습니다.");
        }
    }
    @Override
    public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("BAT_RECONCILE_UNSUPPORTED", "지원하지 않는 BAT 승인 Owner/Command/Action/Target 조합입니다.");
        }
        final Target target;
        try {
            target = parse(command.targetId());
        } catch (RuntimeException invalidTarget) {
            return failed("BAT_RECONCILE_TARGET_INVALID", "BAT targetId를 해석할 수 없습니다.");
        }
        try {
            BatchJobDefinitionControlPort.DefinitionState current = port.state(target.jobId(), target.version());
            if (!Objects.equals(current.checksum(), command.payloadHash())) {
                return failed("BAT_RECONCILE_HASH_MISMATCH", "현재 Definition checksum이 승인 Snapshot과 다릅니다.");
            }
            if ("PUBLISHED".equalsIgnoreCase(current.state()) || "ACTIVE".equalsIgnoreCase(current.state())) {
                return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,
                        "BAT_RECONCILED_" + current.state().toUpperCase(Locale.ROOT),
                        "BAT Owner 상태 조회로 배포 완료를 확인했습니다.");
            }
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "BAT_RECONCILE_PENDING", "BAT Owner 상태가 아직 성공을 확정하지 못했습니다.");
        } catch (RuntimeException observationFailure) {
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "BAT_RECONCILE_OBSERVATION_FAILED", "BAT Owner 상태 조회 실패로 UNKNOWN을 유지합니다.");
        }
    }

    private static Target parse(String value){String[] p=Objects.toString(value,"").split("@",2);if(p.length!=2||p[0].isBlank())throw new IllegalArgumentException("BAT targetId는 jobId@definitionVersion 형식이어야 합니다.");return new Target(p[0],Long.parseLong(p[1]));}
    private static AdmApprovedOperationResult failed(String c,String m){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED,c,m);}
    private static String canonical(String v){return Objects.toString(v,"").trim().toUpperCase(Locale.ROOT);}
    private record OwnerTuple(String ownerModule,String ownerCommand,String actionType,String targetType){}
    private record Target(String jobId,long version){}
}
