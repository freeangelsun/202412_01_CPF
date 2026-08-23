package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.platform.operations.runtimecontrol.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Runtime Control 위험 변경을 Approval Engine의 단회 Owner Command로만 실행합니다. */
@Component("cpfRuntimeControlApprovalOwnerCommandPort")
public final class RuntimeControlApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    public static final String OWNER_MODULE="cpf-starter-platform-operations-runtime-control";
    public static final String ACTION="RUNTIME_CONFIG_CHANGE";
    public static final String TARGET_TYPE="OPS_RUNTIME_CHANGE";
    public static final String CREATE="RUNTIME_CONTROL_CREATE";
    public static final String CANCEL="RUNTIME_CONTROL_CANCEL";
    public static final String ROLLBACK="RUNTIME_CONTROL_ROLLBACK";
    private static final Set<String> COMMANDS=Set.of(CREATE,CANCEL,ROLLBACK);

    private final CpfRuntimeControlPlane control;
    private final ObjectMapper mapper;

    public RuntimeControlApprovalOwnerCommandAdapter(CpfRuntimeControlPlane control,ObjectMapper mapper){
        this.control=Objects.requireNonNull(control,"control");this.mapper=Objects.requireNonNull(mapper,"mapper");
    }

    @Override public boolean supports(String ownerModule,String ownerCommand){
        return OWNER_MODULE.equals(text(ownerModule))&&COMMANDS.contains(text(ownerCommand));
    }
    @Override public boolean supports(String ownerModule,String ownerCommand,String actionType,String targetType){
        return supports(ownerModule,ownerCommand)&&ACTION.equals(text(actionType))&&TARGET_TYPE.equals(text(targetType));
    }

    @Override public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command){
        if(command==null||!supports(command.ownerModule(),command.ownerCommand(),command.actionType(),command.targetType()))
            return failed("RUNTIME_OWNER_MISMATCH","Runtime Control 승인 Owner Command가 아닙니다.");
        if(command.requestedBy().equals(command.approvedBy()))
            return failed("RUNTIME_SELF_APPROVAL","Runtime 위험 변경 요청자와 실행 승인자는 달라야 합니다.");
        try{
            JsonNode payload=mapper.readTree(command.payloadSnapshot());
            return switch(command.ownerCommand()){
                case CREATE -> executeCreate(command,payload);
                case CANCEL -> terminal(control.cancel(required(payload,"changeId"),required(payload,"commandId"),required(payload,"reason"),command.approvedBy()),"CANCEL");
                case ROLLBACK -> terminal(control.rollback(required(payload,"changeId"),required(payload,"commandId"),required(payload,"reason"),command.approvedBy()),"ROLLBACK");
                default -> failed("RUNTIME_COMMAND_UNSUPPORTED","지원하지 않는 Runtime Control 명령입니다.");
            };
        }catch(IllegalArgumentException invalid){return failed("RUNTIME_APPROVED_PAYLOAD_INVALID",safe(invalid.getMessage()));}
        catch(RuntimeException uncertain){return unknown("RUNTIME_OWNER_UNKNOWN","Runtime Owner 실행 결과를 확정할 수 없습니다.");}
        catch(Exception invalid){return failed("RUNTIME_APPROVED_PAYLOAD_INVALID","승인 Payload를 해석할 수 없습니다.");}
    }

    @Override public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command){
        if(command==null||!supports(command.ownerModule(),command.ownerCommand(),command.actionType(),command.targetType()))
            return failed("RUNTIME_OWNER_MISMATCH","Runtime Control 승인 Owner Command가 아닙니다.");
        try{
            JsonNode payload=mapper.readTree(command.payloadSnapshot());
            String operationId=required(payload,"commandId");
            return terminal(control.getByCommandId(operationId),"RECONCILE");
        }catch(RuntimeException notObserved){return unknown("RUNTIME_RECONCILE_PENDING","Runtime Owner에서 최종 상태를 아직 관측하지 못했습니다.");}
        catch(Exception invalid){return failed("RUNTIME_APPROVED_PAYLOAD_INVALID","승인 Payload를 해석할 수 없습니다.");}
    }

    private AdmApprovedOperationResult executeCreate(AdmApprovedOperationCommand approved,JsonNode n) throws Exception{
        String commandId=required(n,"commandId");
        String changeType=required(n,"changeType");
        JsonNode targetNode=n.path("target");
        CpfRuntimeTargetSelector target=mapper.treeToValue(targetNode,CpfRuntimeTargetSelector.class);
        CpfRuntimePayload payload=CpfRuntimePayload.parse(n.path("payload").isMissingNode()?"{}":n.path("payload").toString());
        Long expectedVersion=n.hasNonNull("expectedVersion")?n.get("expectedVersion").asLong():null;
        CpfRuntimeChangeCommand command=new CpfRuntimeChangeCommand(commandId,changeType,n.path("payloadSchemaVersion").asInt(1),target,payload,
                expectedVersion,textOr(n,"rolloutMode","ALL_AT_ONCE"),n.path("waveSize").asInt(1),n.path("quorumPercent").asInt(100),
                instant(n,"scheduledAt"),instant(n,"expiresAt"),required(n,"reason"),String.valueOf(approved.approvalRequestId()),null,approved.requestedBy());
        return terminal(control.createChange(command),"CREATE");
    }

    private AdmApprovedOperationResult terminal(CpfRuntimeChangeResult r,String operation){
        String state=r==null?"":String.valueOf(r.state()).toUpperCase(Locale.ROOT);
        if(Set.of("FAILED","ROLLED_BACK","CANCELLED","CANCELED").contains(state))
            return failed("RUNTIME_"+operation+"_"+state,"Runtime Control이 실패/종료 상태를 반환했습니다.");
        if(Set.of("UNKNOWN","UNKNOWN_RESULT","PARTIAL","PENDING","RUNNING","APPLYING","CREATED","SCHEDULED").contains(state))
            return unknown("RUNTIME_"+operation+"_PENDING","Runtime Control 결과가 아직 최종 확정되지 않았습니다.");
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,"RUNTIME_"+operation+"_"+(state.isBlank()?"ACCEPTED":state),"Runtime Control Owner 결과가 확인되었습니다.");
    }
    private static String required(JsonNode n,String field){if(n==null||!n.hasNonNull(field)||n.get(field).asText().isBlank())throw new IllegalArgumentException(field+"가 필요합니다.");return n.get(field).asText().trim();}
    private static String textOr(JsonNode n,String f,String d){return n!=null&&n.hasNonNull(f)&&!n.get(f).asText().isBlank()?n.get(f).asText().trim():d;}
    private static Instant instant(JsonNode n,String f){return n!=null&&n.hasNonNull(f)&&!n.get(f).asText().isBlank()?Instant.parse(n.get(f).asText()):null;}
    private static String text(String v){return v==null?"":v.trim();}
    private static String safe(String v){return v==null||v.isBlank()?"Runtime Control 요청이 유효하지 않습니다.":v;}
    private static AdmApprovedOperationResult failed(String c,String m){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED,c,m);}
    private static AdmApprovedOperationResult unknown(String c,String m){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,c,m);}
}
