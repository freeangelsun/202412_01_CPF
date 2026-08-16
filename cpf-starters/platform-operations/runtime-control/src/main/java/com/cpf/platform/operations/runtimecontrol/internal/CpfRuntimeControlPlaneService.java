package com.cpf.platform.operations.runtimecontrol.internal;

import com.cpf.platform.operations.runtimecontrol.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** DB 정본, operationId fingerprint, CAS, durable delivery, ACK/drift를 구현한 Runtime Control Plane입니다. */
public class CpfRuntimeControlPlaneService implements CpfRuntimeControlPlane {
    private final CpfRuntimeControlPlaneRepository repository;

    public CpfRuntimeControlPlaneService(CpfRuntimeControlPlaneRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeChangeResult createChange(CpfRuntimeChangeCommand command) {
        validate(command);
        Map<String,Object> fingerprint=new LinkedHashMap<>();
        fingerprint.put("changeType",command.changeType());
        fingerprint.put("payloadSchemaVersion",command.payloadSchemaVersion());
        fingerprint.put("target",command.target());
        fingerprint.put("payload",command.payload());
        fingerprint.put("expectedVersion",command.expectedVersion());
        fingerprint.put("rolloutMode",command.rolloutMode());
        fingerprint.put("waveSize",command.waveSize());
        fingerprint.put("quorumPercent",command.quorumPercent());
        fingerprint.put("scheduledAt",command.scheduledAt());
        fingerprint.put("expiresAt",command.expiresAt());
        fingerprint.put("reason",command.reason());
        fingerprint.put("approvalId",command.approvalId());
        fingerprint.put("breakGlassId",command.breakGlassId());
        String requestHash=CpfRuntimeCanonicalHash.sha256(fingerprint);

        var existing=repository.findOperation(command.operationId());
        if(existing.isPresent()) return replay(command.operationId(),requestHash,existing.get());
        repository.consumeRateLimit(command.requestedBy(),60);
        if(!repository.insertOperation(command.operationId(),"RUNTIME_CHANGE",requestHash,operationExpiry(command))) {
            return replay(command.operationId(),requestHash,repository.findOperation(command.operationId()).orElseThrow());
        }

        List<String> targets=repository.resolveTargets(command.changeType(),command.payloadSchemaVersion(),command.target());
        if(targets.isEmpty()) throw new IllegalArgumentException("Runtime Change 대상 instance가 없거나 capability/schema가 일치하지 않습니다.");
        long version=repository.lockAndNextVersion(command.expectedVersion());
        String changeId=UUID.randomUUID().toString();
        CpfRuntimePayload rollbackPayload=CpfRuntimePayloadJson.objectField(command.payload(),"_rollback");
        CpfRuntimePayload effectivePayload=CpfRuntimePayloadJson.without(command.payload(),"_rollback");
        String payloadHash=CpfRuntimeCanonicalHash.sha256(effectivePayload);
        repository.insertChange(changeId,command.operationId(),command.changeType(),command.payloadSchemaVersion(),
                requestHash,payloadHash,effectivePayload.canonicalJson(),
                rollbackPayload==null?null:rollbackPayload.canonicalJson(),repository.json(targets),
                version,command.rolloutMode(),command.waveSize(),command.quorumPercent(),command.scheduledAt(),command.expiresAt(),
                command.reason(),command.approvalId(),command.breakGlassId(),command.requestedBy(),targets);
        CpfRuntimeChangeResult result=getChange(changeId);
        repository.completeOperation(command.operationId(),changeId,result.state(),repository.json(Map.of("changeId",changeId,"state",result.state())));
        return result;
    }

    @Override

    public CpfRuntimeChangeResult getChange(String changeId) { return toResult(repository.findChange("change_id",require(changeId,"changeId")).orElseThrow(() -> new
            IllegalArgumentException("Runtime Change를 찾을 수 없습니다: "+changeId))); }
    @Override
    public CpfRuntimeChangeResult getByOperationId(String operationId) { return toResult(repository.findChange("operation_id",require(operationId,"operationId")).orElseThrow(() -> new
            IllegalArgumentException("Runtime operation을 찾을 수 없습니다: "+operationId))); }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeChangeResult cancel(String changeId,String operationId,String reason,String operatorId){
        repository.consumeRateLimit(require(operatorId,"operatorId"),60);
        idempotentControlOperation(operationId,"RUNTIME_CANCEL",Map.of("changeId",changeId,"reason",safe(reason)),changeId);
        var existing=repository.findOperation(operationId).orElseThrow();
        if("SUCCESS".equals(String.valueOf(existing.get("result_state")))) return getChange(changeId);
        repository.cancel(require(changeId,"changeId"),require(operatorId,"operatorId"),require(reason,"reason"));
        var result=getChange(changeId); repository.completeOperation(operationId,changeId,"SUCCESS",repository.json(Map.of("state",result.state()))); return result;
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeChangeResult rollback(String changeId,String operationId,String reason,String operatorId){
        repository.consumeRateLimit(require(operatorId,"operatorId"),60);
        CpfRuntimeChangeResult original=getChange(changeId);
        Map<String,Object> row=repository.findChange("change_id",changeId).orElseThrow();
        String rollbackJson=row.get("rollback_payload_json")==null?null:String.valueOf(row.get("rollback_payload_json"));
        if(rollbackJson==null||rollbackJson.isBlank()) throw new IllegalStateException("Rollback snapshot이 없는 Change입니다. changeId="+changeId);
        repository.markRollbackPending(changeId,require(operatorId,"operatorId"),require(reason,"reason"));
        List<String> targets=repository.acknowledgedTargets(changeId);
        if(targets.isEmpty()) throw new IllegalStateException("실제로 ACKED된 대상이 없어 Rollback할 내용이 없습니다. changeId="+changeId);
        CpfRuntimeChangeCommand rollback=new CpfRuntimeChangeCommand(operationId,"ROLLBACK:"+original.changeType(),
                original.changeType()==null?1:((Number)row.getOrDefault("payload_schema_version",1)).intValue(),
                new com.cpf.platform.operations.runtimecontrol.CpfRuntimeTargetSelector(null,null,null,targets,List.of(),Map.of(),null,null,true,true,false),
                CpfRuntimePayload.parse(rollbackJson),null,"ALL_AT_ONCE",1,100,null,null,reason,null,null,operatorId);
        return createChange(rollback);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public com.cpf.platform.operations.runtimecontrol.CpfRuntimeGroupResult saveGroup(com.cpf.platform.operations.runtimecontrol.CpfRuntimeGroupCommand command){
        if(command==null)throw new IllegalArgumentException("Runtime Group command가 필요합니다.");
        require(command.operationId(),"operationId"); require(command.groupId(),"groupId"); require(command.groupName(),"groupName"); require(command.requestedBy(),"requestedBy"); require(command.reason(),"reason");
        repository.consumeRateLimit(command.requestedBy(),60);
        Map<String,Object> fp=new LinkedHashMap<>();fp.put("groupId",command.groupId());fp.put("groupName",command.groupName());fp.put("parentGroupId",safe(command.parentGroupId()));fp
                .put("environment",safe(command.environment()));fp.put("description",safe(command.description()));fp.put("expectedVersion",command.expectedVersion());fp.put("active",command.active());
                fp.put("reason",command.reason());
        String hash=CpfRuntimeCanonicalHash.sha256(fp); var existing=repository.findOperation(command.operationId());
        if(existing.isPresent()) { if(!hash.equals(String.valueOf(existing.get().get("request_hash"))))throw new IllegalStateException("operationId payload fingerprint 충돌: "+command.operationId());
                return getGroup(command.groupId()); }
        if(!repository.insertOperation(command.operationId(),"RUNTIME_GROUP_SAVE",hash,Instant.now().plusSeconds(86400*7L))) return getGroup(command.groupId());
        var result=groupResult(repository.saveGroup(command.groupId(),command.groupName(),command.parentGroupId(),command.environment(),command.description(),command.expectedVersion(),command
                .active(),command.requestedBy()));
        repository.completeOperation(command.operationId(),command.groupId(),"SUCCESS",repository.json(result)); return result;
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public com.cpf.platform.operations.runtimecontrol.CpfRuntimeGroupResult changeGroupMember(com.cpf.platform.operations.runtimecontrol.CpfRuntimeGroupMemberCommand command){
        if(command==null)throw new IllegalArgumentException("Runtime Group member command가 필요합니다."); require(command.operationId(),"operationId");require(command.groupId(),"groupId");require(command
                .instanceId(),"instanceId");require(command.requestedBy(),"requestedBy");require(command.reason(),"reason");
        repository.consumeRateLimit(command.requestedBy(),60);
        Map<String,Object> fp=Map.of("groupId",command.groupId(),"instanceId",command.instanceId(),"active",command.active(),"reason",command.reason());String hash=CpfRuntimeCanonicalHash.sha256(fp);
        var existing=repository.findOperation(command.operationId()); if(existing.isPresent()){if(!hash.equals(String.valueOf(existing.get().get("request_hash"))))throw new
                IllegalStateException("operationId payload fingerprint 충돌: "+command.operationId());return getGroup(command.groupId());}
        if(!repository.insertOperation(command.operationId(),"RUNTIME_GROUP_MEMBER",hash,Instant.now().plusSeconds(86400*7L)))return getGroup(command.groupId());
        var result=groupResult(repository.changeGroupMember(command.groupId(),command.instanceId(),command.active(),command.requestedBy()));repository.completeOperation(command.operationId(),command
                .groupId(),"SUCCESS",repository.json(result));return result;
    }

    @Override

    public com.cpf.platform.operations.runtimecontrol.CpfRuntimeGroupResult getGroup(String groupId){return groupResult(repository.findGroup(require(groupId,"groupId")).orElseThrow(()->new
            IllegalArgumentException("Runtime Group을 찾을 수 없습니다: "+groupId)));}

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void deleteGroup(String groupId,String operationId,Long expectedVersion,String reason,String operatorId){
        require(groupId,"groupId");require(operationId,"operationId");require(reason,"reason");require(operatorId,"operatorId");repository.consumeRateLimit(operatorId,60);Map<String,Object> fp=new
                LinkedHashMap<>();fp.put("groupId",groupId);fp.put("expectedVersion",expectedVersion);fp.put("reason",reason);String hash=CpfRuntimeCanonicalHash.sha256(fp);
        var existing=repository.findOperation(operationId);if(existing.isPresent()){if(!hash.equals(String.valueOf(existing.get().get("request_hash"))))throw new
                IllegalStateException("operationId payload fingerprint 충돌: "+operationId);return;}
        if(!repository.insertOperation(operationId,"RUNTIME_GROUP_DELETE",hash,Instant.now().plusSeconds(86400*7L)))return;repository.deleteGroup(groupId,expectedVersion,operatorId);repository
                .completeOperation(operationId,groupId,"SUCCESS",repository.json(Map.of("deleted",true)));
    }

    private com.cpf.platform.operations.runtimecontrol.CpfRuntimeGroupResult groupResult(Map<String,Object> row){
        @SuppressWarnings("unchecked") List<String> members=(List<String>)row.getOrDefault("instance_ids",List.of());
        return new com.cpf.platform.operations.runtimecontrol.CpfRuntimeGroupResult(String.valueOf(row.get("group_id")),String.valueOf(row.get("group_name")),nullableString(row.get("parent_group_id")),
                nullableString(row.get("environment_code")),nullableString(row.get("description")),"Y".equalsIgnoreCase(String.valueOf(row.get("active_yn"))),number(row.get("row_version")),members);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration registration){return repository.register(registration);}

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeInstanceLease heartbeat(String instanceId,long fencingToken,String actualHash,long actualVersion){
        return repository.heartbeat(require(instanceId,"instanceId"),fencingToken,actualHash,actualVersion,60,Instant.now());
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeInstanceLease heartbeat(String instanceId,long fencingToken,String actualHash,long actualVersion,Instant agentTime){
        return repository.heartbeat(require(instanceId,"instanceId"),fencingToken,actualHash,actualVersion,60,agentTime);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void deregister(String instanceId,long fencingToken,String reason){
        repository.deregister(require(instanceId,"instanceId"),fencingToken,safe(reason));
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void reconcileActualState(String instanceId,long fencingToken,List<com.cpf.platform.operations.runtimecontrol.CpfRuntimeActualState> states){
        repository.reconcileActualState(require(instanceId,"instanceId"),fencingToken,states);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public List<CpfRuntimeDelivery> claim(String instanceId,long fencingToken,int limit){return repository.claim(require(instanceId,"instanceId"),fencingToken,limit);}

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack){
        if(ack==null)throw new IllegalArgumentException("ACK가 필요합니다.");
        repository.acknowledge(require(ack.deliveryId(),"deliveryId"),require(ack.changeId(),"changeId"),require(ack.instanceId(),"instanceId"),
                ack.fencingToken(),ack.appliedVersion(),ack.actualHash(),safe(ack.state()).toUpperCase(),ack.errorCode(),ack.message(),ack.acknowledgedAt()==null?Instant.now():ack.acknowledgedAt());
        return getChange(ack.changeId());
    }

    @Override

    public CpfRuntimeStatus status(String environment,String serviceId){return repository.status(environment,serviceId);}

    @Override
    public com.cpf.platform.operations.runtimecontrol.CpfRuntimeControlHealth health(){
        return repository.health(60L);
    }

    @Override
    public CpfRuntimeTargetPreview previewTargets(String changeType,int payloadSchemaVersion,
                                                   CpfRuntimeTargetSelector target){
        require(changeType,"changeType");
        if(target==null)throw new IllegalArgumentException("target이 필요합니다.");
        return repository.previewTargets(changeType,payloadSchemaVersion,target);
    }

    @Override
    public CpfRuntimeChangePreview previewChange(CpfRuntimeChangeCommand command){
        validate(command);
        CpfRuntimeTargetPreview targetPreview=repository.previewTargets(
                command.changeType(),command.payloadSchemaVersion(),command.target());
        List<CpfRuntimeTargetPreviewItem> targets=targetPreview.targets();
        List<String> eligible=targets.stream().filter(CpfRuntimeTargetPreviewItem::eligible)
                .map(CpfRuntimeTargetPreviewItem::instanceId).toList();
        CpfRuntimePayload payload=CpfRuntimePayloadJson.without(command.payload(),"_rollback");
        String payloadHash=CpfRuntimeCanonicalHash.sha256(payload);
        List<CpfRuntimeFeatureStatus> current=repository.featureStates(eligible,command.changeType());
        ArrayList<CpfRuntimeInstanceDiff> diff=new ArrayList<>();
        LinkedHashMap<String,Integer> restartImpact=new LinkedHashMap<>();
        for(CpfRuntimeTargetPreviewItem target:targets){
            if(!target.eligible())continue;
            String instanceId=target.instanceId();
            CpfRuntimeFeatureStatus state=current.stream().filter(row->instanceId.equals(row.instanceId()))
                    .findFirst().orElse(new CpfRuntimeFeatureStatus(instanceId,target.serviceId(),command.changeType(),
                            0L,0L,null,null,"UNKNOWN",null,null));
            String capability=target.capability()==null?"":target.capability();
            String[] parts=capability.split("\\|");
            String impact=parts.length>1?parts[1]:"HOT_APPLY";
            restartImpact.merge(impact,1,Integer::sum);
            diff.add(new CpfRuntimeInstanceDiff(instanceId,state.desiredVersion(),state.actualVersion(),
                    state.desiredHash(),state.actualHash(),state.driftState(),payloadHash,
                    !payloadHash.equals(state.desiredHash()),impact));
        }
        List<CpfRuntimeImpactCount> impactSummary=restartImpact.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry->new CpfRuntimeImpactCount(entry.getKey(),entry.getValue())).toList();
        List<String> affectedServices=targets.stream().filter(CpfRuntimeTargetPreviewItem::eligible)
                .map(CpfRuntimeTargetPreviewItem::serviceId).filter(java.util.Objects::nonNull)
                .distinct().sorted().toList();
        return new CpfRuntimeChangePreview(targetPreview,payloadHash,impactSummary,diff,affectedServices);
    }

    @Override
    public CpfRuntimeAuditVerification verifyAudit(String changeId){
        return repository.verifyAudit(require(changeId,"changeId"));
    }

    private CpfRuntimeChangeResult replay(String operationId,String requestHash,Map<String,Object> op){
        if(!requestHash.equals(String.valueOf(op.get("request_hash")))) throw new IllegalStateException("같은 operationId가 다른 Runtime 요청 payload에 사용되었습니다: "+operationId);
        Object entity=op.get("entity_id");
        if(entity==null||String.valueOf(entity).isBlank()) throw new IllegalStateException("동일 operationId 처리 중입니다. 결과를 조회하십시오: "+operationId);
        return getChange(String.valueOf(entity));
    }

    private void idempotentControlOperation(String operationId,String type,Map<String,Object> payload,String entityId){
        require(operationId,"operationId"); String hash=CpfRuntimeCanonicalHash.sha256(payload);
        var existing=repository.findOperation(operationId);
        if(existing.isPresent()){
            if(!hash.equals(String.valueOf(existing.get().get("request_hash"))))throw new IllegalStateException("operationId payload fingerprint 충돌: "+operationId);
            return;
        }
        repository.insertOperation(operationId,type,hash,Instant.now().plusSeconds(86400));
    }

    private CpfRuntimeChangeResult toResult(Map<String,Object> row){
        String id=String.valueOf(row.get("change_id")); Map<String,Number> counts=repository.deliveryCounts(id);
        int total=counts.values().stream().mapToInt(Number::intValue).sum();
        int ack=counts.getOrDefault("ACKED",0).intValue();
        int failed=counts.getOrDefault("FAILED",0).intValue()
                + counts.getOrDefault("POISONED",0).intValue()
                + counts.getOrDefault("UNKNOWN_RESULT",0).intValue();
        int drift=repository.driftCount(id);
        return new CpfRuntimeChangeResult(id,String.valueOf(row.get("operation_id")),String.valueOf(row.get("change_type")),String.valueOf(row.get("change_state")),
                number(row.get("desired_version")),String.valueOf(row.get("request_hash")),total,ack,failed,drift,
                instant(row.get("scheduled_at")),instant(row.get("expires_at")),instant(row.get("created_at")),instant(row.get("updated_at")),null);
    }

    private void validate(CpfRuntimeChangeCommand c){if(c==null)throw new IllegalArgumentException("Runtime Change command가 필요합니다.");require(c.operationId(),"operationId");require(c.changeType(),
            "changeType");require(c.requestedBy(),"requestedBy");require(c.reason(),"reason");if(c.target()==null)throw new IllegalArgumentException("target이 필요합니다.");if(c.expiresAt()!=null&&c
            .expiresAt().isBefore(Instant.now()))throw new IllegalArgumentException("이미 만료된 Runtime Change입니다.");}
    private Instant operationExpiry(CpfRuntimeChangeCommand c){return c.expiresAt()!=null?c.expiresAt():Instant.now().plusSeconds(86400*7L);}
    private String require(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+"가 필요합니다.");return v.trim();}
    private String safe(String v){return v==null?"":v;}
    private String nullableString(Object v){return v==null?null:String.valueOf(v);}
    private long number(Object v){return v==null?0L:((Number)v).longValue();}
    private Instant instant(Object v){if(v==null)return null;if(v instanceof java.sql.Timestamp t)return t.toInstant();if(v instanceof java.util.Date d)return d.toInstant();try{return Instant
            .parse(String.valueOf(v));}catch(Exception ignored){return null;}}
    private List<?> readList(String json){try{return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json,List.class);}catch(Exception ex){throw new IllegalStateException("target snapshot 파싱 실패",ex);}}
}
