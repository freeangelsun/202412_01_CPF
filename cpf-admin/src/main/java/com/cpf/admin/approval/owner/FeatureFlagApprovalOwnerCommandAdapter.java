package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.*;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagOperations;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagResult;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

/** Feature Flag Kill Switch를 Approval Engine Owner Command로만 변경합니다. */
@Component("cpfFeatureFlagApprovalOwnerCommandPort")
public final class FeatureFlagApprovalOwnerCommandAdapter implements AdmApprovalOwnerCommandPort {
    public static final String OWNER_MODULE="CPF-PLATFORM-OPERATIONS";
    public static final String COMMAND="FEATURE_FLAG_KILL_SWITCH";
    public static final String TARGET_TYPE="FEATURE_FLAG";
    private final CpfFeatureFlagOperations operations;
    private final ObjectMapper mapper;
    public FeatureFlagApprovalOwnerCommandAdapter(CpfFeatureFlagOperations operations,ObjectMapper mapper){this.operations=Objects.requireNonNull(operations);this.mapper=Objects.requireNonNull(mapper);}
    @Override public boolean supports(String ownerModule,String ownerCommand){return OWNER_MODULE.equals(text(ownerModule))&&COMMAND.equals(text(ownerCommand));}
    @Override public boolean supports(String ownerModule,String ownerCommand,String actionType,String targetType){return supports(ownerModule,ownerCommand)&&COMMAND.equals(text(actionType))&&TARGET_TYPE.equals(text(targetType));}
    @Override public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command){
        if(command==null||!supports(command.ownerModule(),command.ownerCommand(),command.actionType(),command.targetType()))return failed("FEATURE_FLAG_OWNER_MISMATCH","Feature Flag 승인 Owner 조합이 올바르지 않습니다.");
        if(command.requestedBy().equals(command.approvedBy()))return failed("FEATURE_FLAG_SELF_APPROVAL","Kill Switch 요청자와 승인자는 달라야 합니다.");
        try{
            JsonNode payload=mapper.readTree(command.payloadSnapshot());
            boolean enabled=requiredBoolean(payload,"enabled");
            long expectedRevision=requiredLong(payload,"expectedRevision");
            CpfFeatureFlagResult<CpfFeatureFlagValue> before=operations.find(command.targetId());
            if(before.revision()!=expectedRevision)return failed("FEATURE_FLAG_VERSION_CONFLICT","승인 Snapshot 이후 Feature Flag revision이 변경되었습니다.");
            operations.setKillSwitch(command.targetId(),enabled,command.approvedBy(),command.reason());
            return observe(command.targetId(),enabled,expectedRevision,false);
        }catch(IllegalArgumentException|IllegalStateException rejected){return failed("FEATURE_FLAG_COMMAND_REJECTED",safe(rejected.getMessage()));}
        catch(RuntimeException uncertain){return unknown("FEATURE_FLAG_COMMAND_UNKNOWN","Kill Switch 변경 결과를 확정할 수 없습니다.");}
        catch(Exception invalid){return failed("FEATURE_FLAG_APPROVED_PAYLOAD_INVALID","Kill Switch 승인 Payload를 해석할 수 없습니다.");}
    }
    @Override public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command){
        if(command==null||!supports(command.ownerModule(),command.ownerCommand(),command.actionType(),command.targetType()))return failed("FEATURE_FLAG_OWNER_MISMATCH","Feature Flag 승인 Owner 조합이 올바르지 않습니다.");
        try{JsonNode payload=mapper.readTree(command.payloadSnapshot());return observe(command.targetId(),requiredBoolean(payload,"enabled"),requiredLong(payload,"expectedRevision"),true);}
        catch(Exception failure){return unknown("FEATURE_FLAG_RECONCILE_PENDING","Kill Switch 현재 상태를 확정하지 못해 UNKNOWN을 유지합니다.");}
    }
    private AdmApprovedOperationResult observe(String flagKey,boolean enabled,long expectedRevision,boolean reconcile){
        CpfFeatureFlagResult<CpfFeatureFlagValue> current=operations.find(flagKey);
        boolean desired=enabled?current.source()==CpfFeatureFlagResult.Source.KILL_SWITCH:current.source()!=CpfFeatureFlagResult.Source.KILL_SWITCH;
        if(desired&&current.revision()>=expectedRevision)return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,reconcile?"FEATURE_FLAG_KILL_SWITCH_RECONCILED":"FEATURE_FLAG_KILL_SWITCH_CHANGED","Kill Switch 목표 상태가 확인되었습니다.");
        return unknown("FEATURE_FLAG_KILL_SWITCH_PENDING","Kill Switch 목표 상태가 아직 확인되지 않았습니다.");
    }
    private static boolean requiredBoolean(JsonNode n,String f){if(n==null||!n.has(f)||!n.get(f).isBoolean())throw new IllegalArgumentException(f+"가 필요합니다.");return n.get(f).asBoolean();}
    private static long requiredLong(JsonNode n,String f){if(n==null||!n.hasNonNull(f)||!n.get(f).canConvertToLong())throw new IllegalArgumentException(f+"가 필요합니다.");long v=n.get(f).asLong();if(v<0)throw new IllegalArgumentException(f+"는 0 이상이어야 합니다.");return v;}
    private static String text(String v){return Objects.toString(v,"").trim().toUpperCase(java.util.Locale.ROOT);}
    private static String safe(String v){return v==null||v.isBlank()?"Feature Flag 요청이 유효하지 않습니다.":v;}
    private static AdmApprovedOperationResult failed(String c,String m){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED,c,m);}
    private static AdmApprovedOperationResult unknown(String c,String m){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,c,m);}
}
