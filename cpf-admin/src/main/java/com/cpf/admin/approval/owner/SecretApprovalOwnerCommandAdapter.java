package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.*;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.security.api.secret.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/** Secret Rotation을 Approval Engine의 독립 승인 Snapshot으로만 실행합니다. 원문 Secret은 취급하지 않습니다. */
@Component("cpfSecretApprovalOwnerCommandPort")
public final class SecretApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    public static final String OWNER_MODULE="CPF-SECURITY";
    public static final String COMMAND="SECRET_ROTATE";
    public static final String TARGET_TYPE="SECRET_REFERENCE";
    private final List<CpfSecretProvider> providers;
    private final AdmAuditLogService audit;
    private final ObjectMapper mapper;
    public SecretApprovalOwnerCommandAdapter(List<CpfSecretProvider> providers,AdmAuditLogService audit,ObjectMapper mapper){this.providers=List.copyOf(providers);this.audit=Objects.requireNonNull(audit);this.mapper=Objects.requireNonNull(mapper);}
    @Override public boolean supports(String ownerModule,String ownerCommand){return OWNER_MODULE.equals(text(ownerModule))&&COMMAND.equals(text(ownerCommand));}
    @Override public boolean supports(String ownerModule,String ownerCommand,String actionType,String targetType){return supports(ownerModule,ownerCommand)&&COMMAND.equals(text(actionType))&&TARGET_TYPE.equals(text(targetType));}
    @Override public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command){
        if(command==null||!supports(command.ownerModule(),command.ownerCommand(),command.actionType(),command.targetType()))return failed("SECRET_OWNER_MISMATCH","Secret 승인 Owner 조합이 올바르지 않습니다.");
        if(command.requestedBy().equals(command.approvedBy()))return failed("SECRET_SELF_APPROVAL","Secret Rotation 요청자와 승인자는 달라야 합니다.");
        try{
            JsonNode payload=mapper.readTree(command.payloadSnapshot());
            String providerId=required(payload,"provider"),key=required(payload,"key"),expectedVersion=required(payload,"expectedVersion");
            CpfSecretReference ref=new CpfSecretReference(providerId,key);
            if(!command.targetId().equals(ref.toString()))return failed("SECRET_TARGET_MISMATCH","승인 대상 Secret reference가 Payload와 다릅니다.");
            CpfSecretProvider provider=provider(providerId);
            CpfSecretMetadata before=provider.metadata(ref);
            if(!Objects.equals(expectedVersion,before.version()))return failed("SECRET_VERSION_CONFLICT","승인 Snapshot 이후 Secret version이 변경되었습니다.");
            if(!(provider instanceof CpfRotatableSecretProvider rotatable))return failed("SECRET_NOT_ROTATABLE","Rotation을 지원하지 않는 Secret Provider입니다.");
            CpfSecretMetadata after=audit.executeAudited(command.transactionId(),command.approvedBy(),COMMAND,"secret_reference",ref.toString(),command.reason(),null,null,
                    ()->rotatable.rotate(ref,command.reason(),command.approvedBy()),m->"version="+Objects.toString(m.version(),""));
            if(Objects.equals(expectedVersion,after.version()))return unknown("SECRET_ROTATION_VERSION_UNCHANGED","Rotation 응답의 version이 변경되지 않아 결과 재확인이 필요합니다.");
            return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,"SECRET_ROTATED","Secret Rotation 완료와 감사 기록을 확인했습니다.");
        }catch(IllegalArgumentException|IllegalStateException rejected){return failed("SECRET_ROTATION_REJECTED",safe(rejected.getMessage()));}
        catch(RuntimeException uncertain){return unknown("SECRET_ROTATION_UNKNOWN","Secret Provider Rotation 결과를 확정할 수 없습니다.");}
        catch(Exception invalid){return failed("SECRET_APPROVED_PAYLOAD_INVALID","Secret 승인 Payload를 해석할 수 없습니다.");}
    }
    @Override public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command){
        if(command==null||!supports(command.ownerModule(),command.ownerCommand(),command.actionType(),command.targetType()))return failed("SECRET_OWNER_MISMATCH","Secret 승인 Owner 조합이 올바르지 않습니다.");
        try{JsonNode payload=mapper.readTree(command.payloadSnapshot());String providerId=required(payload,"provider"),key=required(payload,"key"),expectedVersion=required(payload,"expectedVersion");CpfSecretMetadata current=provider(providerId).metadata(new CpfSecretReference(providerId,key));if(!Objects.equals(expectedVersion,current.version()))return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,"SECRET_ROTATION_RECONCILED","Secret metadata version 변경을 확인했습니다.");return unknown("SECRET_ROTATION_PENDING","Secret metadata version이 아직 승인 Snapshot과 같아 UNKNOWN을 유지합니다.");}
        catch(RuntimeException|java.io.IOException failure){return unknown("SECRET_RECONCILE_OBSERVATION_FAILED","Secret metadata 조회 실패로 UNKNOWN을 유지합니다.");}
    }
    private CpfSecretProvider provider(String id){return providers.stream().filter(p->p.providerId().equalsIgnoreCase(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Secret Provider를 찾을 수 없습니다: "+id));}
    private static String required(JsonNode n,String f){if(n==null||!n.hasNonNull(f)||n.get(f).asText().isBlank())throw new IllegalArgumentException(f+"가 필요합니다.");return n.get(f).asText().trim();}
    private static String text(String v){return Objects.toString(v,"").trim().toUpperCase(java.util.Locale.ROOT);}
    private static String safe(String v){return v==null||v.isBlank()?"Secret Rotation 요청이 유효하지 않습니다.":v;}
    private static AdmApprovedOperationResult failed(String c,String m){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED,c,m);}
    private static AdmApprovedOperationResult unknown(String c,String m){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,c,m);}
}
