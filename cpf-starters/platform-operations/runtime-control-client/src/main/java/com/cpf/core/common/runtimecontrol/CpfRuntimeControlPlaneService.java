package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.*;
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
        validate(command,true);
        String operationId=require(command.operationId(),"operationId");
        String changeType=normalizeChangeType(command.changeType());
        String requestedBy=require(command.requestedBy(),"requestedBy");
        String reason=require(command.reason(),"reason");
        String approvalId=trimToNull(command.approvalId());
        String breakGlassId=trimToNull(command.breakGlassId());
        Map<String,Object> fingerprint=new LinkedHashMap<>();
        fingerprint.put("changeType",changeType);
        fingerprint.put("payloadSchemaVersion",command.payloadSchemaVersion());
        fingerprint.put("target",command.target());
        fingerprint.put("payload",command.payload());
        fingerprint.put("expectedVersion",command.expectedVersion());
        fingerprint.put("rolloutMode",command.rolloutMode());
        fingerprint.put("waveSize",command.waveSize());
        fingerprint.put("quorumPercent",command.quorumPercent());
        fingerprint.put("scheduledAt",command.scheduledAt());
        fingerprint.put("expiresAt",command.expiresAt());
        fingerprint.put("requestedBy",requestedBy);
        fingerprint.put("reason",reason);
        fingerprint.put("approvalId",approvalId);
        fingerprint.put("breakGlassId",breakGlassId);
        String requestHash=CpfRuntimeCanonicalHash.sha256(fingerprint);

        var existing=repository.findOperation(operationId);
        if(existing.isPresent()) {
            assertOperationIdentity(operationId,"RUNTIME_CHANGE",requestHash,existing.get());
            return replay(operationId,requestHash,existing.get());
        }
        if(!repository.insertOperation(operationId,"RUNTIME_CHANGE",requestHash,operationExpiry(command))) {
            Map<String,Object> winner=repository.findOperation(operationId).orElseThrow();
            assertOperationIdentity(operationId,"RUNTIME_CHANGE",requestHash,winner);
            return replay(operationId,requestHash,winner);
        }
        repository.consumeRateLimit(requestedBy,60);

        List<String> targets=repository.resolveTargets(changeType,command.payloadSchemaVersion(),command.target());
        if(targets.isEmpty()) throw new IllegalArgumentException("Runtime Change 대상 instance가 없거나 capability/schema가 일치하지 않습니다.");
        long version=repository.lockAndNextVersion(command.expectedVersion());
        String changeId=UUID.randomUUID().toString();
        CpfRuntimePayload rollbackPayload=CpfRuntimePayloadJson.objectField(command.payload(),"_rollback");
        CpfRuntimePayload effectivePayload=CpfRuntimePayloadJson.without(command.payload(),"_rollback");
        String payloadHash=CpfRuntimeCanonicalHash.sha256(effectivePayload);
        repository.insertChange(changeId,operationId,changeType,command.payloadSchemaVersion(),
                requestHash,payloadHash,effectivePayload.canonicalJson(),
                rollbackPayload==null?null:rollbackPayload.canonicalJson(),repository.json(targets),
                version,command.rolloutMode(),command.waveSize(),command.quorumPercent(),command.scheduledAt(),command.expiresAt(),
                reason,approvalId,breakGlassId,requestedBy,targets);
        CpfRuntimeChangeResult result=getChange(changeId);
        // cpf_control_operation은 명령 수락/생성 결과 Ledger이고 Runtime Change lifecycle은
        // cpf_runtime_change.change_state가 소유합니다. 두 상태 Catalog를 혼용하지 않습니다.
        repository.completeOperation(operationId,changeId,"SUCCESS",
                repository.json(Map.of("changeId",changeId,"state",result.state())));
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
        String normalizedChangeId=requireMax(changeId,"changeId",80);
        String normalizedOperationId=requireMax(operationId,"operationId",100);
        String normalizedOperator=requireMax(operatorId,"operatorId",100);
        String normalizedReason=requireMax(reason,"reason",1000);
        OperationClaim claim=claimControlOperation(normalizedOperationId,"RUNTIME_CANCEL",
                Map.of("changeId",normalizedChangeId,"reason",normalizedReason,"operatorId",normalizedOperator),Instant.now().plusSeconds(86400));
        if(!claim.owner()) return replayControlChange(claim.operation(),normalizedChangeId,normalizedOperationId);
        repository.consumeRateLimit(normalizedOperator,60);
        repository.cancel(normalizedChangeId,normalizedOperator,normalizedReason);
        var result=getChange(normalizedChangeId);
        repository.completeOperation(normalizedOperationId,normalizedChangeId,"SUCCESS",repository.json(Map.of("state",result.state())));
        return result;
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeChangeResult rollback(String changeId,String operationId,String reason,String operatorId){
        String normalizedChangeId=requireMax(changeId,"changeId",80);
        String normalizedOperationId=requireMax(operationId,"operationId",100);
        String normalizedOperator=requireMax(operatorId,"operatorId",100);
        String normalizedReason=requireMax(reason,"reason",1000);
        CpfRuntimeChangeResult original=getChange(normalizedChangeId);
        Map<String,Object> row=repository.findChange("change_id",normalizedChangeId).orElseThrow();
        String rollbackJson=row.get("rollback_payload_json")==null?null:String.valueOf(row.get("rollback_payload_json"));
        if(rollbackJson==null||rollbackJson.isBlank()) throw new IllegalStateException("Rollback snapshot이 없는 Change입니다. changeId="+changeId);
        List<String> targets=repository.acknowledgedTargets(normalizedChangeId);
        if(targets.isEmpty()) throw new IllegalStateException("실제로 ACKED된 대상이 없어 Rollback할 내용이 없습니다. changeId="+normalizedChangeId);
        CpfRuntimeChangeCommand rollback=new CpfRuntimeChangeCommand(normalizedOperationId,"ROLLBACK:"+original.changeType(),
                original.changeType()==null?1:((Number)row.getOrDefault("payload_schema_version",1)).intValue(),
                new com.cpf.core.api.runtimecontrol.CpfRuntimeTargetSelector(null,null,null,targets,List.of(),Map.of(),null,null,true,true,false),
                CpfRuntimePayload.parse(rollbackJson),null,"ALL_AT_ONCE",1,100,null,null,normalizedReason,
                nullableString(row.get("approval_id")),nullableString(row.get("break_glass_id")),normalizedOperator);
        var existingOperation=repository.findOperation(normalizedOperationId);
        if(existingOperation.isEmpty()) {
            repository.markRollbackPending(normalizedChangeId,normalizedOperator,normalizedReason);
        } else if(!"ROLLBACK_PENDING".equals(original.state()) && !"ROLLED_BACK".equals(original.state())) {
            throw new IllegalStateException("Rollback operationId가 존재하지만 원본 Change 상태가 일치하지 않습니다. changeId="+normalizedChangeId);
        }
        CpfRuntimeChangeResult rollbackResult=createChange(rollback);
        repository.linkRollbackChange(normalizedChangeId,rollbackResult.changeId(),normalizedOperator,normalizedReason);
        return rollbackResult;
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public com.cpf.core.api.runtimecontrol.CpfRuntimeGroupResult saveGroup(com.cpf.core.api.runtimecontrol.CpfRuntimeGroupCommand command){
        validateGroup(command);
        String operationId=require(command.operationId(),"operationId");
        String groupId=require(command.groupId(),"groupId");
        String groupName=require(command.groupName(),"groupName");
        String parentGroupId=trimToNull(command.parentGroupId());
        String environment=trimToNull(command.environment());
        String description=trimToNull(command.description());
        String requestedBy=require(command.requestedBy(),"requestedBy");
        String reason=require(command.reason(),"reason");
        Map<String,Object> fp=new LinkedHashMap<>();fp.put("groupId",groupId);fp.put("groupName",groupName);fp.put("parentGroupId",safe(parentGroupId));fp
                .put("environment",safe(environment));fp.put("description",safe(description));fp.put("expectedVersion",command.expectedVersion());fp.put("active",command.active());
                fp.put("requestedBy",requestedBy);fp.put("reason",reason);
        OperationClaim claim=claimControlOperation(operationId,"RUNTIME_GROUP_SAVE",fp,Instant.now().plusSeconds(86400*7L));
        if(!claim.owner()) return replayGroupOperation(claim.operation(),groupId,operationId);
        repository.consumeRateLimit(requestedBy,60);
        var result=groupResult(repository.saveGroup(groupId,groupName,parentGroupId,environment,description,command.expectedVersion(),command
                .active(),requestedBy));
        repository.completeOperation(operationId,groupId,"SUCCESS",repository.json(result)); return result;
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public com.cpf.core.api.runtimecontrol.CpfRuntimeGroupResult changeGroupMember(com.cpf.core.api.runtimecontrol.CpfRuntimeGroupMemberCommand command){
        validateGroupMember(command);
        String operationId=require(command.operationId(),"operationId");
        String groupId=require(command.groupId(),"groupId");
        String instanceId=require(command.instanceId(),"instanceId");
        String requestedBy=require(command.requestedBy(),"requestedBy");
        String reason=require(command.reason(),"reason");
        Map<String,Object> fp=Map.of("groupId",groupId,"instanceId",instanceId,"active",command.active(),"requestedBy",requestedBy,"reason",reason);
        OperationClaim claim=claimControlOperation(operationId,"RUNTIME_GROUP_MEMBER",fp,Instant.now().plusSeconds(86400*7L));
        if(!claim.owner())return replayGroupOperation(claim.operation(),groupId,operationId);
        repository.consumeRateLimit(requestedBy,60);
        var result=groupResult(repository.changeGroupMember(groupId,instanceId,command.active(),requestedBy));repository.completeOperation(operationId,
                groupId,"SUCCESS",repository.json(result));return result;
    }

    @Override

    public com.cpf.core.api.runtimecontrol.CpfRuntimeGroupResult getGroup(String groupId){return groupResult(repository.findGroup(require(groupId,"groupId")).orElseThrow(()->new
            IllegalArgumentException("Runtime Group을 찾을 수 없습니다: "+groupId)));}

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void deleteGroup(String groupId,String operationId,Long expectedVersion,String reason,String operatorId){
        String normalizedGroupId=requireMax(groupId,"groupId",80);
        String normalizedOperationId=requireMax(operationId,"operationId",100);
        String normalizedReason=requireMax(reason,"reason",1000);
        String normalizedOperator=requireMax(operatorId,"operatorId",100);
        if(expectedVersion!=null&&expectedVersion<0L)throw new IllegalArgumentException("expectedVersion은 0 이상이어야 합니다.");
        Map<String,Object> fp=new LinkedHashMap<>();fp.put("groupId",normalizedGroupId);fp.put("expectedVersion",expectedVersion);fp.put("operatorId",normalizedOperator);fp.put("reason",normalizedReason);
        OperationClaim claim=claimControlOperation(normalizedOperationId,"RUNTIME_GROUP_DELETE",fp,Instant.now().plusSeconds(86400*7L));
        if(!claim.owner()){ replayVoidOperation(claim.operation(),normalizedGroupId,normalizedOperationId); return; }
        repository.consumeRateLimit(normalizedOperator,60);repository.deleteGroup(normalizedGroupId,expectedVersion,normalizedOperator);repository
                .completeOperation(normalizedOperationId,normalizedGroupId,"SUCCESS",repository.json(Map.of("deleted",true)));
    }

    private com.cpf.core.api.runtimecontrol.CpfRuntimeGroupResult groupResult(Map<String,Object> row){
        @SuppressWarnings("unchecked") List<String> members=(List<String>)row.getOrDefault("instance_ids",List.of());
        return new com.cpf.core.api.runtimecontrol.CpfRuntimeGroupResult(String.valueOf(row.get("group_id")),String.valueOf(row.get("group_name")),nullableString(row.get("parent_group_id")),
                nullableString(row.get("environment_code")),nullableString(row.get("description")),"Y".equalsIgnoreCase(String.valueOf(row.get("active_yn"))),number(row.get("row_version")),members);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration registration){
        validateRegistration(registration);
        return repository.register(registration);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeInstanceLease heartbeat(String instanceId,long fencingToken,String actualHash,long actualVersion){
        validateHeartbeat(instanceId,fencingToken,actualHash,actualVersion);
        return repository.heartbeat(requireMax(instanceId,"instanceId",120),fencingToken,trimToNull(actualHash),actualVersion,60,Instant.now());
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeInstanceLease heartbeat(String instanceId,long fencingToken,String actualHash,long actualVersion,Instant agentTime){
        validateHeartbeat(instanceId,fencingToken,actualHash,actualVersion);
        return repository.heartbeat(requireMax(instanceId,"instanceId",120),fencingToken,trimToNull(actualHash),actualVersion,60,agentTime);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void deregister(String instanceId,long fencingToken,String reason){
        if(fencingToken<=0L)throw new IllegalArgumentException("fencingToken은 양수여야 합니다.");
        repository.deregister(requireMax(instanceId,"instanceId",120),fencingToken,truncateInput(reason,500));
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public void reconcileActualState(String instanceId,long fencingToken,List<com.cpf.core.api.runtimecontrol.CpfRuntimeActualState> states){
        String normalizedInstanceId=requireMax(instanceId,"instanceId",120);
        if(fencingToken<=0L)throw new IllegalArgumentException("fencingToken은 양수여야 합니다.");
        List<CpfRuntimeActualState> safeStates=states==null?List.of():List.copyOf(states);
        if(safeStates.size()>1000)throw new IllegalArgumentException("actual state 목록은 최대 1000개입니다.");
        for(CpfRuntimeActualState state:safeStates)validateActualState(state);
        repository.reconcileActualState(normalizedInstanceId,fencingToken,safeStates);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public List<CpfRuntimeDelivery> claim(String instanceId,long fencingToken,int limit){
        if(fencingToken<=0L)throw new IllegalArgumentException("fencingToken은 양수여야 합니다.");
        if(limit<1||limit>100)throw new IllegalArgumentException("claim limit은 1..100 범위여야 합니다.");
        return repository.claim(requireMax(instanceId,"instanceId",120),fencingToken,limit);
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack){
        if(ack==null)throw new IllegalArgumentException("ACK가 필요합니다.");
        if(ack.fencingToken()<=0L)throw new IllegalArgumentException("fencingToken은 양수여야 합니다.");
        if(ack.attempt()<0)throw new IllegalArgumentException("attempt는 0 이상이어야 합니다.");
        if(ack.appliedVersion()<0L)throw new IllegalArgumentException("appliedVersion은 0 이상이어야 합니다.");
        String deliveryId=requireMax(ack.deliveryId(),"deliveryId",80);
        String changeId=requireMax(ack.changeId(),"changeId",80);
        String instanceId=requireMax(ack.instanceId(),"instanceId",120);
        String actualHash=trimToNull(ack.actualHash());
        optionalMax(actualHash,"actualHash",64);
        String state=requireMax(ack.state(),"state",30).toUpperCase(java.util.Locale.ROOT);
        String errorCode=trimToNull(ack.errorCode());
        optionalMax(errorCode,"errorCode",80);
        optionalMax(ack.message(),"message",4000);
        repository.acknowledge(deliveryId,changeId,instanceId,ack.fencingToken(),ack.attempt(),ack.appliedVersion(),actualHash,state,errorCode,ack.message(),
                ack.acknowledgedAt()==null?Instant.now():ack.acknowledgedAt());
        return getChange(changeId);
    }

    @Override

    public CpfRuntimeStatus status(String environment,String serviceId){
        String normalizedEnvironment=trimToNull(environment);
        String normalizedServiceId=trimToNull(serviceId);
        optionalMax(normalizedEnvironment,"environment",40);
        optionalMax(normalizedServiceId,"serviceId",40);
        return repository.status(normalizedEnvironment,normalizedServiceId);
    }

    @Override
    public com.cpf.core.api.runtimecontrol.CpfRuntimeControlHealth health(){
        return repository.health(60L);
    }

    @Override
    public CpfRuntimeTargetPreview previewTargets(String changeType,int payloadSchemaVersion,
                                                   CpfRuntimeTargetSelector target){
        String normalizedChangeType=normalizeChangeType(changeType);
        requireMax(normalizedChangeType,"changeType",80);
        if(payloadSchemaVersion<1)throw new IllegalArgumentException("payloadSchemaVersion은 1 이상이어야 합니다.");
        if(target==null)throw new IllegalArgumentException("target이 필요합니다.");
        validateTargetSelector(target);
        return repository.previewTargets(normalizedChangeType,payloadSchemaVersion,target);
    }

    @Override
    public CpfRuntimeChangePreview previewChange(CpfRuntimeChangeCommand command){
        validate(command,false);
        String changeType=normalizeChangeType(command.changeType());
        CpfRuntimeTargetPreview targetPreview=repository.previewTargets(
                changeType,command.payloadSchemaVersion(),command.target());
        List<CpfRuntimeTargetPreviewItem> targets=targetPreview.targets();
        List<String> eligible=targets.stream().filter(CpfRuntimeTargetPreviewItem::eligible)
                .map(CpfRuntimeTargetPreviewItem::instanceId).toList();
        CpfRuntimePayload payload=CpfRuntimePayloadJson.without(command.payload(),"_rollback");
        String payloadHash=CpfRuntimeCanonicalHash.sha256(payload);
        List<CpfRuntimeFeatureStatus> current=repository.featureStates(eligible,changeType);
        ArrayList<CpfRuntimeInstanceDiff> diff=new ArrayList<>();
        LinkedHashMap<String,Integer> restartImpact=new LinkedHashMap<>();
        for(CpfRuntimeTargetPreviewItem target:targets){
            if(!target.eligible())continue;
            String instanceId=target.instanceId();
            CpfRuntimeFeatureStatus state=current.stream().filter(row->instanceId.equals(row.instanceId()))
                    .findFirst().orElse(new CpfRuntimeFeatureStatus(instanceId,target.serviceId(),changeType,
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
        String state=String.valueOf(op.get("result_state"));
        if(!"SUCCESS".equals(state)) throw new IllegalStateException("동일 operationId 처리 상태를 확인하십시오: "+operationId+", state="+state);
        Object entity=op.get("entity_id");
        if(entity==null||String.valueOf(entity).isBlank()) throw new IllegalStateException("성공 operation에 결과 entity가 없습니다: "+operationId);
        return getChange(String.valueOf(entity));
    }

    private OperationClaim claimControlOperation(String operationId,String type,Map<String,Object> payload,Instant expiresAt){
        String normalizedOperationId=require(operationId,"operationId");
        String hash=CpfRuntimeCanonicalHash.sha256(payload);
        var existing=repository.findOperation(normalizedOperationId);
        if(existing.isPresent()){
            assertOperationIdentity(normalizedOperationId,type,hash,existing.get());
            return new OperationClaim(false,existing.get());
        }
        if(repository.insertOperation(normalizedOperationId,type,hash,expiresAt)){
            return new OperationClaim(true,Map.of("operation_id",normalizedOperationId,"request_hash",hash,"result_state","PROCESSING"));
        }
        Map<String,Object> winner=repository.findOperation(normalizedOperationId).orElseThrow(()->new IllegalStateException(
                "operationId 동시 등록 결과를 조회할 수 없습니다: "+normalizedOperationId));
        assertOperationIdentity(normalizedOperationId,type,hash,winner);
        return new OperationClaim(false,winner);
    }

    private void assertOperationIdentity(String operationId,String expectedType,String requestHash,Map<String,Object> operation){
        String actualType=String.valueOf(operation.get("command_type"));
        if(!expectedType.equals(actualType)){
            throw new IllegalStateException("operationId command type 충돌: "+operationId+", expected="+expectedType+", actual="+actualType);
        }
        if(!requestHash.equals(String.valueOf(operation.get("request_hash")))){
            throw new IllegalStateException("operationId payload fingerprint 충돌: "+operationId);
        }
    }

    private CpfRuntimeChangeResult replayControlChange(Map<String,Object> operation,String changeId,String operationId){
        String state=String.valueOf(operation.get("result_state"));
        if("SUCCESS".equals(state)) {
            assertReplayEntity(operation,changeId,operationId);
            return getChange(changeId);
        }
        throw new IllegalStateException("동일 operationId 처리 상태를 확인하십시오: "+operationId+", state="+state);
    }

    private CpfRuntimeGroupResult replayGroupOperation(Map<String,Object> operation,String groupId,String operationId){
        String state=String.valueOf(operation.get("result_state"));
        if("SUCCESS".equals(state)) {
            assertReplayEntity(operation,groupId,operationId);
            Object stored=operation.get("result_json");
            if(stored!=null&&!String.valueOf(stored).isBlank())
                return repository.readJson(String.valueOf(stored),CpfRuntimeGroupResult.class);
            return getGroup(groupId);
        }
        throw new IllegalStateException("동일 operationId 처리 상태를 확인하십시오: "+operationId+", state="+state);
    }

    private void replayVoidOperation(Map<String,Object> operation,String expectedEntityId,String operationId){
        String state=String.valueOf(operation.get("result_state"));
        if(!"SUCCESS".equals(state)){
            throw new IllegalStateException("동일 operationId 처리 상태를 확인하십시오: "+operationId+", state="+state);
        }
        assertReplayEntity(operation,expectedEntityId,operationId);
    }

    private void assertReplayEntity(Map<String,Object> operation,String expectedEntityId,String operationId){
        Object entity=operation.get("entity_id");
        if(entity==null||!expectedEntityId.equals(String.valueOf(entity))){
            throw new IllegalStateException("operationId 결과 entity 불일치: "+operationId+", expected="+expectedEntityId+", actual="+entity);
        }
    }

    private record OperationClaim(boolean owner,Map<String,Object> operation) { }

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

    private void validateRegistration(CpfRuntimeInstanceRegistration r){
        if(r==null)throw new IllegalArgumentException("Runtime instance registration이 필요합니다.");
        requireMax(r.instanceId(),"instanceId",120);requireMax(r.serviceId(),"serviceId",40);
        requireMax(r.endpointCode(),"endpointCode",80);optionalMax(r.environment(),"environment",40);
        optionalMax(r.zone(),"zone",60);optionalMax(r.cell(),"cell",60);requireMax(r.baseUrl(),"baseUrl",500);
        optionalMax(r.artifactVersion(),"artifactVersion",100);optionalMax(r.artifactCommit(),"artifactCommit",64);
        optionalMax(r.runtimeRole(),"runtimeRole",40);requireMax(r.registrationSource(),"registrationSource",120);
        optionalMax(r.schemaVersion(),"schemaVersion",100);optionalMax(r.configHash(),"configHash",64);
        if(r.agentTime()==null)throw new IllegalArgumentException("agentTime이 필요합니다.");
        if(r.leaseSeconds()<10||r.leaseSeconds()>3600)throw new IllegalArgumentException("leaseSeconds는 10..3600 범위여야 합니다.");
        validateStringMap(r.capabilities(),"capabilities",200,120,500);
        validateStringMap(r.labels(),"labels",200,120,500);
    }
    private void validateHeartbeat(String instanceId,long fencingToken,String actualHash,long actualVersion){
        requireMax(instanceId,"instanceId",120);
        if(fencingToken<=0L)throw new IllegalArgumentException("fencingToken은 양수여야 합니다.");
        if(actualVersion<0L)throw new IllegalArgumentException("actualVersion은 0 이상이어야 합니다.");
        optionalMax(actualHash,"actualHash",64);
    }
    private void validateActualState(CpfRuntimeActualState state){
        if(state==null)throw new IllegalArgumentException("actual state 항목이 null일 수 없습니다.");
        requireMax(normalizeChangeType(state.changeType()),"actualState.changeType",80);
        if(state.actualVersion()<0L)throw new IllegalArgumentException("actualState.actualVersion은 0 이상이어야 합니다.");
        requireMax(state.actualHash(),"actualState.actualHash",64);
        requireMax(state.sourceDeliveryId(),"actualState.sourceDeliveryId",80);
    }
    private void validateStringMap(Map<String,String> values,String name,int maxEntries,int maxKey,int maxValue){
        Map<String,String> safeValues=values==null?Map.of():values;
        if(safeValues.size()>maxEntries)throw new IllegalArgumentException(name+"는 최대 "+maxEntries+"개입니다.");
        for(Map.Entry<String,String> entry:safeValues.entrySet()){
            requireMax(entry.getKey(),name+".key",maxKey);requireMax(entry.getValue(),name+".value",maxValue);
        }
    }
    private String truncateInput(String value,int max){
        String safe=value==null?"":value.trim();return safe.length()>max?safe.substring(0,max):safe;
    }

    private void validateGroup(CpfRuntimeGroupCommand c){
        if(c==null)throw new IllegalArgumentException("Runtime Group command가 필요합니다.");
        requireMax(c.operationId(),"operationId",100);requireMax(c.groupId(),"groupId",80);
        requireMax(c.groupName(),"groupName",150);optionalMax(c.parentGroupId(),"parentGroupId",80);
        optionalMax(c.environment(),"environment",40);optionalMax(c.description(),"description",500);
        requireMax(c.requestedBy(),"requestedBy",100);requireMax(c.reason(),"reason",1000);
        if(c.expectedVersion()!=null&&c.expectedVersion()<0L)
            throw new IllegalArgumentException("expectedVersion은 0 이상이어야 합니다.");
    }
    private void validateGroupMember(CpfRuntimeGroupMemberCommand c){
        if(c==null)throw new IllegalArgumentException("Runtime Group member command가 필요합니다.");
        requireMax(c.operationId(),"operationId",100);requireMax(c.groupId(),"groupId",80);
        requireMax(c.instanceId(),"instanceId",120);requireMax(c.requestedBy(),"requestedBy",100);
        requireMax(c.reason(),"reason",1000);
    }

    private void validate(CpfRuntimeChangeCommand c,boolean requireCas){
        if(c==null)throw new IllegalArgumentException("Runtime Change command가 필요합니다.");
        requireMax(c.operationId(),"operationId",100);
        String changeType=normalizeChangeType(c.changeType());
        requireMax(changeType,"changeType",80);
        if(c.payloadSchemaVersion()<1)throw new IllegalArgumentException("payloadSchemaVersion은 1 이상이어야 합니다.");
        requireMax(c.requestedBy(),"requestedBy",100);
        requireMax(c.reason(),"reason",1000);
        optionalMax(c.approvalId(),"approvalId",100);
        optionalMax(c.breakGlassId(),"breakGlassId",100);
        if(c.target()==null)throw new IllegalArgumentException("target이 필요합니다.");
        validateTargetSelector(c.target());
        int payloadBytes=c.payload().canonicalJson().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        if(payloadBytes>1_048_576)throw new IllegalArgumentException("Runtime payload는 최대 1MiB입니다.");
        if(!java.util.Set.of("ALL_AT_ONCE","CANARY","WAVE").contains(c.rolloutMode()))
            throw new IllegalArgumentException("지원하지 않는 rolloutMode입니다: "+c.rolloutMode());
        if(c.waveSize()==null||c.waveSize()<1||c.waveSize()>100000)
            throw new IllegalArgumentException("waveSize는 1..100000 범위여야 합니다.");
        if(c.quorumPercent()==null||c.quorumPercent()<1||c.quorumPercent()>100)
            throw new IllegalArgumentException("quorumPercent는 1..100 범위여야 합니다.");
        if(requireCas&&!changeType.startsWith("ROLLBACK:")&&c.expectedVersion()==null)
            throw new IllegalArgumentException("Runtime Change에는 expectedVersion 기반 CAS가 필요합니다.");
        if(c.expectedVersion()!=null&&c.expectedVersion()<0L)
            throw new IllegalArgumentException("expectedVersion은 0 이상이어야 합니다.");
        Instant now=Instant.now();
        if(c.expiresAt()!=null&&!c.expiresAt().isAfter(now))
            throw new IllegalArgumentException("이미 만료된 Runtime Change입니다.");
        if(c.scheduledAt()!=null&&c.expiresAt()!=null&&!c.expiresAt().isAfter(c.scheduledAt()))
            throw new IllegalArgumentException("expiresAt은 scheduledAt 이후여야 합니다.");
    }
    private void validateTargetSelector(CpfRuntimeTargetSelector target){
        optionalMax(target.environment(),"target.environment",40);optionalMax(target.serviceId(),"target.serviceId",40);
        optionalMax(target.groupId(),"target.groupId",80);optionalMax(target.zone(),"target.zone",60);
        optionalMax(target.cell(),"target.cell",60);
        if(target.instanceIds().size()>100000||target.excludeInstanceIds().size()>100000)
            throw new IllegalArgumentException("Runtime target instance 목록은 최대 100000개입니다.");
        for(String id:target.instanceIds()) requireMax(id,"target.instanceId",120);
        for(String id:target.excludeInstanceIds()) requireMax(id,"target.excludeInstanceId",120);
        if(target.labels().size()>100)throw new IllegalArgumentException("Runtime target label은 최대 100개입니다.");
        for(Map.Entry<String,String> entry:target.labels().entrySet()){
            requireMax(entry.getKey(),"target.label.key",120);requireMax(entry.getValue(),"target.label.value",500);
        }
    }

    private Instant operationExpiry(CpfRuntimeChangeCommand c){return c.expiresAt()!=null?c.expiresAt():Instant.now().plusSeconds(86400*7L);}
    private String require(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+"가 필요합니다.");return v.trim();}
    private String requireMax(String v,String n,int max){String value=require(v,n);if(value.length()>max)throw new IllegalArgumentException(n+"는 최대 "+max+"자입니다.");return value;}
    private void optionalMax(String v,String n,int max){if(v!=null&&!v.isBlank()&&v.trim().length()>max)throw new IllegalArgumentException(n+"는 최대 "+max+"자입니다.");}
    private String normalizeChangeType(String v){return require(v,"changeType").toUpperCase(java.util.Locale.ROOT);}
    private String trimToNull(String v){return v==null||v.isBlank()?null:v.trim();}
    private String safe(String v){return v==null?"":v;}
    private String nullableString(Object v){return v==null?null:String.valueOf(v);}
    private long number(Object value) {
        if (!(value instanceof Number number)) {
            throw new IllegalStateException("Runtime Change 필수 숫자 값이 누락되었거나 타입이 올바르지 않습니다.");
        }
        return number.longValue();
    }

    private Instant instant(Object value) {
        if (value == null) return null;
        if (value instanceof Instant instant) return instant;
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof java.util.Date date) return date.toInstant();
        if (value instanceof CharSequence text) {
            try {
                return Instant.parse(text.toString().trim());
            } catch (java.time.format.DateTimeParseException ex) {
                throw new IllegalStateException("Runtime Change 시간 값을 해석할 수 없습니다: " + text, ex);
            }
        }
        throw new IllegalStateException(
                "Runtime Change 시간 값의 타입이 올바르지 않습니다: " + value.getClass().getName());
    }
    private List<?> readList(String json){try{return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json,List.class);}catch(Exception ex){throw new IllegalStateException("target snapshot 파싱 실패",ex);}}
}
