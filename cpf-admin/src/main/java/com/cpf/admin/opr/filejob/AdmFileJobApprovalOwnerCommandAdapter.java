package com.cpf.admin.opr.filejob;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * ADM 대량파일 위험조치를 Approval Engine의 불변 Snapshot에서만 실행하는 Owner Command Adapter입니다.
 *
 * <p>브라우저가 임의 approvalId를 직접 전달해 실행할 수 없으며, 요청자/승인자 분리와
 * 승인 Snapshot의 expectedState를 실제 Job 상태와 다시 대조한 뒤 단 한 번 상태 전이를 수행합니다.</p>
 */
@Component("cpfFileJobApprovalOwnerCommandPort")
public final class AdmFileJobApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    public static final String OWNER_MODULE="ADM";
    public static final String TARGET_TYPE="FILE_JOB";
    public static final String APPLY="FILE_JOB_APPLY";
    public static final String RETRY="FILE_JOB_RETRY";
    public static final String CANCEL="FILE_JOB_CANCEL";
    public static final String ROLLBACK="FILE_JOB_ROLLBACK";
    public static final String RESOLVE_UNKNOWN="FILE_JOB_RESOLVE_UNKNOWN";
    private static final Set<String> COMMANDS=Set.of(APPLY,RETRY,CANCEL,ROLLBACK,RESOLVE_UNKNOWN);

    private final AdmFileJobService service;
    private final ObjectMapper mapper;

    public AdmFileJobApprovalOwnerCommandAdapter(AdmFileJobService service,ObjectMapper mapper){
        this.service=Objects.requireNonNull(service,"service");
        this.mapper=Objects.requireNonNull(mapper,"mapper");
    }

    @Override public boolean supports(String ownerModule,String ownerCommand){
        return OWNER_MODULE.equals(canonical(ownerModule))&&COMMANDS.contains(canonical(ownerCommand));
    }
    @Override public boolean supports(String ownerModule,String ownerCommand,String actionType,String targetType){
        String command=canonical(ownerCommand);
        return supports(ownerModule,command)&&command.equals(canonical(actionType))&&TARGET_TYPE.equals(canonical(targetType));
    }

    @Override public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command){
        if(command==null||!supports(command.ownerModule(),command.ownerCommand(),command.actionType(),command.targetType()))
            return failed("FILE_JOB_OWNER_MISMATCH","File Job 승인 Owner/Command/Action/Target 조합이 올바르지 않습니다.");
        if(command.requestedBy().equals(command.approvedBy()))
            return failed("FILE_JOB_SELF_APPROVAL","File Job 위험조치 요청자와 승인자는 달라야 합니다.");
        try{
            JsonNode payload=mapper.readTree(command.payloadSnapshot());
            AdmFileJobResponse before=service.get(command.targetId());
            String expectedState=required(payload,"expectedState");
            if(!before.state().name().equals(expectedState))
                return failed("FILE_JOB_STATE_CHANGED","승인 Snapshot 이후 File Job 상태가 변경되었습니다.");
            String approvalId=String.valueOf(command.approvalRequestId());
            AdmFileJobResponse after=switch(canonical(command.ownerCommand())){
                case APPLY -> service.apply(command.targetId(),command.approvedBy(),command.reason(),approvalId);
                case RETRY -> service.retry(command.targetId(),command.approvedBy(),command.reason(),approvalId);
                case CANCEL -> service.cancel(command.targetId(),command.approvedBy(),command.reason(),approvalId);
                case ROLLBACK -> service.rollback(command.targetId(),command.approvedBy(),command.reason(),approvalId);
                case RESOLVE_UNKNOWN -> service.resolveUnknown(command.targetId(),payload.path("rowNumber").asLong(-1),
                        AdmFileJobService.UnknownResolution.valueOf(required(payload,"resolution")),
                        textOrNull(payload,"businessKey"),textOrNull(payload,"rollbackToken"),
                        command.approvedBy(),command.reason(),approvalId);
                default -> throw new IllegalArgumentException("지원하지 않는 File Job 승인 명령입니다.");
            };
            return observed(command,after,false);
        }catch(IllegalArgumentException|IllegalStateException rejected){
            return failed("FILE_JOB_COMMAND_REJECTED",safe(rejected.getMessage()));
        }catch(RuntimeException uncertain){
            return unknown("FILE_JOB_COMMAND_UNKNOWN","File Job Owner 실행 결과를 확정할 수 없습니다.");
        }catch(Exception invalid){
            return failed("FILE_JOB_APPROVED_PAYLOAD_INVALID","File Job 승인 Payload를 해석할 수 없습니다.");
        }
    }

    @Override public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command){
        if(command==null||!supports(command.ownerModule(),command.ownerCommand(),command.actionType(),command.targetType()))
            return failed("FILE_JOB_OWNER_MISMATCH","File Job 승인 Owner/Command/Action/Target 조합이 올바르지 않습니다.");
        try{
            AdmFileJobResponse current=service.get(command.targetId());
            String expectedApproval=String.valueOf(command.approvalRequestId());
            if(!Objects.equals(expectedApproval,current.approvalId()))
                return unknown("FILE_JOB_RECONCILE_DIFFERENT_COMMAND","현재 File Job은 이 승인 요청의 Owner Command 결과로 확인되지 않습니다.");
            return observed(command,current,true);
        }catch(RuntimeException observationFailure){
            return unknown("FILE_JOB_RECONCILE_OBSERVATION_FAILED","File Job 상태 조회 실패로 UNKNOWN을 유지합니다.");
        }
    }

    private AdmApprovedOperationResult observed(AdmApprovedOperationCommand command,AdmFileJobResponse job,boolean reconcile){
        String state=job.state().name();
        String prefix=reconcile?"FILE_JOB_RECONCILED_":"FILE_JOB_";
        return switch(canonical(command.ownerCommand())){
            case CANCEL -> state.equals("CANCELLED")?success(prefix+state):pending(prefix+state);
            case ROLLBACK -> state.equals("ROLLED_BACK")?success(prefix+state):
                    Set.of("PARTIAL_FAILED","FAILED").contains(state)?failed(prefix+state,"File Job Rollback이 실패/부분실패로 확정되었습니다."):pending(prefix+state);
            case APPLY,RETRY -> state.equals("COMPLETED")?success(prefix+state):
                    Set.of("FAILED","PARTIAL_FAILED","CANCELLED").contains(state)?failed(prefix+state,"File Job 실행이 실패/부분실패로 확정되었습니다."):pending(prefix+state);
            case RESOLVE_UNKNOWN -> state.equals("UNKNOWN_RESULT")?unknown(prefix+state,"결과 불명 행이 남아 있습니다."):success(prefix+state);
            default -> failed("FILE_JOB_COMMAND_UNSUPPORTED","지원하지 않는 File Job 승인 명령입니다.");
        };
    }

    private AdmApprovedOperationResult success(String code){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,code,"File Job Owner 상태가 확인되었습니다.");}
    private AdmApprovedOperationResult pending(String code){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.RUNNING,code,"File Job 비동기 처리가 진행 중이며 Reconcile이 필요합니다.");}
    private static String required(JsonNode n,String field){if(n==null||!n.hasNonNull(field)||n.get(field).asText().isBlank())throw new IllegalArgumentException(field+"가 필요합니다.");return n.get(field).asText().trim();}
    private static String textOrNull(JsonNode n,String field){return n!=null&&n.hasNonNull(field)&&!n.get(field).asText().isBlank()?n.get(field).asText().trim():null;}
    private static String canonical(String value){return Objects.toString(value,"").trim().toUpperCase(Locale.ROOT);}
    private static String safe(String value){return value==null||value.isBlank()?"File Job 요청이 유효하지 않습니다.":value;}
    private static AdmApprovedOperationResult failed(String c,String m){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED,c,m);}
    private static AdmApprovedOperationResult unknown(String c,String m){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,c,m);}
}
