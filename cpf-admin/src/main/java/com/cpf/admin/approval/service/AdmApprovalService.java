package com.cpf.admin.approval.service;

import com.cpf.admin.approval.api.*;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.cpf.admin.approval.spi.AdmApprovalDirectoryEntry;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.admin.common.base.AdmBaseService;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ADM 위험조치 Approval Engine.
 *
 * <p>정책 Target을 요청 시 실제 운영자 Snapshot으로 고정하고 ALL/ANY/N_OF_M을 평가합니다.
 * 승인 완료 후 실제 변경은 ADM DB가 아니라 Owner Command Port를 통해 실행합니다.</p>
 */
@Service
public class AdmApprovalService extends AdmBaseService {
    private final AdmApprovalRepository repository;
    private final ObjectMapper objectMapper;
    private final AdmApprovalSnapshotIntegrity snapshotIntegrity;
    private final Map<String, AdmApprovalOwnerCommandPort> ownerPorts;

    @Autowired
    public AdmApprovalService(
            AdmApprovalRepository repository,
            ObjectMapper objectMapper,
            AdmApprovalSnapshotIntegrity snapshotIntegrity,
            Map<String, AdmApprovalOwnerCommandPort> ownerPorts) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.snapshotIntegrity = snapshotIntegrity;
        this.ownerPorts = ownerPorts;
    }

    /** Test/source migration constructor; production wiring uses the @Autowired integrity constructor. */
    @Deprecated
    public AdmApprovalService(
            AdmApprovalRepository repository,
            ObjectMapper objectMapper,
            Map<String, AdmApprovalOwnerCommandPort> ownerPorts) {
        this(repository, objectMapper, new AdmApprovalSnapshotIntegrity(objectMapper), ownerPorts);
    }

    public List<Map<String,Object>> findPolicies(String actionType) {
        return repository.findPolicies(actionType);
    }

    public Map<String,Object> findPolicy(String code,int version) {
        Map<String,Object> p=new LinkedHashMap<>(repository.findPolicy(code,version)
                .orElseThrow(()->new CpfValidationException("ADM 승인 정책을 찾을 수 없습니다.")));
        p.put("steps",repository.findPolicySteps(code,version));
        return p;
    }

    @Transactional(transactionManager = "admTransactionManager")
    public Map<String,Object> savePolicy(PolicyRequest request,String operatorId) {
        int version=request.policyVersion()==null?1:request.policyVersion();
        if(version<1) throw new CpfValidationException("policyVersion은 1 이상이어야 합니다.");
        Instant from=required(request.effectiveFrom(),"effectiveFrom");
        Instant to=request.effectiveTo();
        if(to!=null&&!to.isAfter(from)) throw new CpfValidationException("effectiveTo는 effectiveFrom보다 뒤여야 합니다.");
        if(request.steps()==null||request.steps().isEmpty()) throw new CpfValidationException("승인 정책 단계가 필요합니다.");
        Map<String,Object> p=new LinkedHashMap<>();
        p.put("policyCode",required(request.policyCode(),"policyCode"));p.put("policyVersion",version);
        p.put("policyName",required(request.policyName(),"policyName"));p.put("actionType",required(request.actionType(),"actionType"));
        p.put("effectiveFrom",Timestamp.from(from));p.put("effectiveTo",to==null?null:Timestamp.from(to));
        p.put("enabledYn",yn(request.enabledYn(),"Y"));p.put("selfApprovalAllowedYn",yn(request.selfApprovalAllowedYn(),"N"));
        p.put("breakGlassAllowedYn",yn(request.breakGlassAllowedYn(),"N"));p.put("description",blank(request.description()));
        p.put("operatorId",required(operatorId,"operatorId"));

        List<Map<String,Object>> steps=new ArrayList<>();Set<String> uniq=new HashSet<>();
        for(PolicyStepRequest s:request.steps()){
            int no=s.stepNo()==null?1:s.stepNo();if(no<1)throw new CpfValidationException("stepNo는 1 이상이어야 합니다.");
            String type=upper(s.stepType(),"APPROVAL");if(!Set.of("APPROVAL","REVIEW").contains(type))throw new CpfValidationException("지원하지 않는 stepType");
            AdmApprovalTargetType target;
            try{target=AdmApprovalTargetType.valueOf(required(s.targetType(),"targetType").toUpperCase(Locale.ROOT));}
            catch(IllegalArgumentException e){throw new CpfValidationException("지원하지 않는 targetType");}
            AdmApprovalDecisionRule rule;
            try{rule=AdmApprovalDecisionRule.valueOf(upper(s.decisionRule(),"ALL"));}
            catch(IllegalArgumentException e){throw new CpfValidationException("지원하지 않는 decisionRule");}
            Integer requiredCount=s.requiredCount();
            if(rule==AdmApprovalDecisionRule.N_OF_M&&(requiredCount==null||requiredCount<1))throw new CpfValidationException("N_OF_M requiredCount 필요");
            if(rule!=AdmApprovalDecisionRule.N_OF_M)requiredCount=null;
            String targetCode=required(s.targetCode(),"targetCode");
            if(!uniq.add(no+"|"+target+"|"+targetCode))throw new CpfValidationException("중복 정책 Target");
            Map<String,Object> row=new LinkedHashMap<>(p);
            row.put("stepNo",no);row.put("stepType",type);row.put("targetType",target.name());row.put("targetCode",targetCode);
            row.put("decisionRule",rule.name());row.put("requiredCount",requiredCount);row.put("requiredYn",yn(s.requiredYn(),"Y"));
            steps.add(row);
        }
        String changeReason=bounded(request.reason(),"reason",8,500);
        p.put("changeReason",changeReason);
        p.put("policyHash",snapshotIntegrity.sha256Canonical(json(Map.of(
                "policy",p,"steps",steps.stream().map(step->new TreeMap<>(step)).toList()))));
        try {
            repository.insertPolicy(p,steps);
        } catch (DataIntegrityViolationException duplicate) {
            throw new AdmApprovalConflictException("동일 policyCode/version은 변경할 수 없습니다. 새 version을 생성하십시오.", duplicate);
        }
        return findPolicy(String.valueOf(p.get("policyCode")),version);
    }

    @Transactional(transactionManager = "admTransactionManager")
    public ApprovalMutationResult requestApprovalResult(CreateRequest request,String operatorId) {
        AtomicBoolean replay = new AtomicBoolean(false);
        Map<String,Object> body=requestApprovalInternal(request,operatorId,replay);
        return new ApprovalMutationResult(!replay.get(),replay.get(),body);
    }

    @Transactional(transactionManager = "admTransactionManager")
    public Map<String,Object> requestApproval(CreateRequest request,String operatorId) {
        return requestApprovalInternal(request, operatorId, new AtomicBoolean(false));
    }

    private Map<String,Object> requestApprovalInternal(CreateRequest request,String operatorId,AtomicBoolean replayFlag) {
        String requestKey=required(request.requestKey(),"requestKey");
        String requester=required(operatorId,"operatorId");
        String snapshot=snapshotIntegrity.canonicalPayload(request.payloadSnapshot());
        String ownerModule=required(request.ownerModule(),"ownerModule");
        String ownerCommand=required(request.ownerCommand(),"ownerCommand");
        String actionType=required(request.actionType(),"actionType");
        String targetType=required(request.targetType(),"targetType");
        bounded(requestKey,"requestKey",8,128);
        bounded(required(request.reason(),"reason"),"reason",8,500);
        resolveOwnerPort(ownerModule,ownerCommand,actionType,targetType);
        Instant now=Instant.now();
        Instant canonicalExpireAt=canonicalInstant(request.expireAt());
        if(canonicalExpireAt!=null&&!canonicalExpireAt.isAfter(now))
            throw new CpfValidationException("expireAt은 현재 시각보다 뒤여야 합니다.");
        if(isBat(ownerModule)&&canonicalExpireAt==null)
            throw new CpfValidationException("BAT 위험조치는 expireAt이 필요합니다.");
        Optional<Long> existing=repository.findRequestIdByKey(requestKey);
        if(existing.isPresent()){
            replayFlag.set(true);
            Map<String,Object> replay=repository.findRequest(existing.get())
                    .orElseThrow(()->new CpfValidationException("멱등 승인 요청을 찾을 수 없습니다."));
            validateRequestReplay(replay,request,requester);
            String replaySnapshot=isBat(ownerModule)
                    ? json(batchRiskSnapshot(batRiskCommand(existing.get(),requestKey,requester,request,snapshot)))
                    : snapshot;
            Map<String,Object> candidate=new LinkedHashMap<>(replay);
            candidate.put("payloadSnapshot",replaySnapshot);
            String expectedHash=snapshotIntegrity.hash(candidate);
            if(!snapshotIntegrity.constantTimeEquals(string(replay,"payloadHash"),expectedHash))
                throw new CpfValidationException("requestKey가 다른 승인 명령 Snapshot에 이미 사용되었습니다.");
            return detail(existing.get());
        }
        Map<String,Object> canonicalPolicy=repository.findActivePolicy(actionType,now)
                .orElseThrow(()->new CpfValidationException("활성 ADM 승인 정책이 없습니다."));
        validatePolicyActive(canonicalPolicy, actionType, now);
        Map<String,Object> policy;
        if(request.policyCode()!=null&&!request.policyCode().isBlank()){
            if(request.policyVersion()==null)throw new CpfValidationException("policyVersion이 필요합니다.");
            policy=repository.findPolicy(request.policyCode(),request.policyVersion())
                    .orElseThrow(()->new CpfValidationException("ADM 승인 정책을 찾을 수 없습니다."));
            validatePolicyActive(policy, actionType, now);
            if(!string(canonicalPolicy,"policyCode").equals(string(policy,"policyCode"))
                    || number(canonicalPolicy,"policyVersion").intValue()!=number(policy,"policyVersion").intValue())
                throw new AdmApprovalConflictException("명시 정책은 현재 서버 Registry의 canonical active policy와 일치해야 합니다.");
        }else{
            policy=canonicalPolicy;
        }
        String code=string(policy,"policyCode");int ver=number(policy,"policyVersion").intValue();
        List<Map<String,Object>> steps=repository.findPolicySteps(code,ver);
        if(steps.isEmpty())throw new CpfValidationException("승인 정책 단계가 없습니다.");
        boolean selfAllowed="Y".equals(string(policy,"selfApprovalAllowedYn"));
        List<Resolved> resolved=new ArrayList<>();
        int minStep=Integer.MAX_VALUE;
        for(Map<String,Object>s:steps){
            int step=number(s,"stepNo").intValue();minStep=Math.min(minStep,step);
            AdmApprovalTargetType target=AdmApprovalTargetType.valueOf(string(s,"targetType"));
            List<AdmApprovalDirectoryEntry> entries=repository.resolve(target,string(s,"targetCode"),now);
            if(!selfAllowed)entries=entries.stream().filter(e->!requester.equals(e.operatorId())).toList();
            if(entries.isEmpty()&&"Y".equals(string(s,"requiredYn")))
                throw new CpfValidationException("필수 승인 Target 참여자가 0명입니다: "+target+"/"+string(s,"targetCode"));
            Integer req=s.get("requiredCount")==null?null:number(s,"requiredCount").intValue();
            if(!entries.isEmpty()) AdmApprovalDecisionEvaluator.evaluate(
                    AdmApprovalDecisionRule.valueOf(string(s,"decisionRule")),entries.size(),0,0,req);
            for(AdmApprovalDirectoryEntry e:entries)resolved.add(new Resolved(step,target.name(),string(s,"targetCode"),e));
        }
        Map<String,Object> v=new LinkedHashMap<>();
        v.put("requestKey",requestKey);v.put("policyCode",code);v.put("policyVersion",ver);
        v.put("actionType",string(policy,"actionType"));v.put("ownerModule",ownerModule);
        v.put("ownerCommand",ownerCommand);v.put("targetType",targetType);
        v.put("targetId",required(request.targetId(),"targetId"));v.put("requestedBy",requester);
        v.put("requestReason",required(request.reason(),"reason"));
        v.put("payloadSnapshot",snapshot);v.put("currentStepNo",minStep);
        v.put("expireAt",canonicalExpireAt==null?null:Timestamp.from(canonicalExpireAt));
        v.put("transactionId",CpfTransactionContext.transactionId());v.put("operatorId",requester);
        v.put("payloadHash",snapshotIntegrity.hash(v));
        long id;
        try {
            id=repository.insertRequest(v);
        } catch (DataIntegrityViolationException duplicate) {
            replayFlag.set(true);
            Long raced=repository.findRequestIdByKey(requestKey)
                    .orElseThrow(() -> new AdmApprovalConflictException("승인 요청 unique conflict를 수렴할 수 없습니다.", duplicate));
            Map<String,Object> replay=repository.findRequest(raced)
                    .orElseThrow(() -> new AdmApprovalConflictException("승인 요청 unique conflict 결과가 없습니다.", duplicate));
            validateRequestReplay(replay,request,requester);
            String replaySnapshot=isBat(ownerModule)
                    ? json(batchRiskSnapshot(batRiskCommand(raced,requestKey,requester,request,snapshot)))
                    : snapshot;
            Map<String,Object> candidate=new LinkedHashMap<>(replay);
            candidate.put("payloadSnapshot",replaySnapshot);
            if(!snapshotIntegrity.constantTimeEquals(string(replay,"payloadHash"),snapshotIntegrity.hash(candidate)))
                throw new AdmApprovalConflictException("requestKey unique conflict가 다른 승인 Snapshot과 충돌했습니다.", duplicate);
            return detail(raced);
        }
        if(isBat(ownerModule)){
            CpfBatchRiskCommand risk=batRiskCommand(id,requestKey,requester,request,snapshot);
            String riskSnapshot=json(batchRiskSnapshot(risk));
            Map<String,Object> riskEnvelope=new LinkedHashMap<>(v);
            riskEnvelope.put("payloadSnapshot",riskSnapshot);
            String riskHash=snapshotIntegrity.hash(riskEnvelope);
            if(repository.updateCommandSnapshot(id,0L,riskHash,riskSnapshot,requester)!=1)
                throw new CpfValidationException("BAT 승인 명령 Snapshot 확정에 실패했습니다.");
        }
        for(Resolved r:resolved)repository.insertParticipant(id,r.stepNo(),r.entry(),r.targetType(),r.targetCode(),requester);
        repository.history(id,"REQUEST",requester,null,"PENDING",required(request.reason(),"reason"),
                json(Map.of("policyCode",code,"policyVersion",ver,"participants",resolved.size())),string(v,"transactionId"));
        return detail(id);
    }

    @Transactional(transactionManager = "admTransactionManager")
    public Map<String,Object> decide(long id,DecisionRequest request,String operatorId) {
        String key=required(request.idempotencyKey(),"idempotencyKey");
        String operator=required(operatorId,"operatorId");
        String reason=required(request.reason(),"reason");
        bounded(key,"idempotencyKey",8,128);
        bounded(reason,"reason",8,500);
        String action=upper(request.action(),"APPROVE");
        String status=switch(action){case"APPROVE"->"APPROVED";case"REJECT"->"REJECTED";default->throw new CpfValidationException("APPROVE/REJECT만 지원");};
        Optional<Map<String,Object>> previous=repository.findDecisionByKey(key);
        if(previous.isPresent()){
            validateDecisionReplay(previous.get(),id,operator,status,reason);
            return detail(id);
        }
        Map<String,Object> doc=repository.findRequest(id).orElseThrow(()->new CpfValidationException("승인 요청 없음"));
        ensureNotExpired(doc);
        Map<String,Object> decisionPolicy=repository.findPolicy(string(doc,"policyCode"),number(doc,"policyVersion").intValue())
                .orElseThrow(() -> new AdmApprovalConflictException("승인 요청의 정책 Version을 찾을 수 없습니다."));
        if(Boolean.TRUE.equals(request.breakGlass())&&!"Y".equals(string(decisionPolicy,"breakGlassAllowedYn")))
            throw new CpfValidationException("이 정책은 break-glass 결정을 허용하지 않습니다.");
        String before=string(doc,"approvalStatus");if(!"PENDING".equals(before))throw new AdmApprovalConflictException("PENDING 요청만 승인/반려 가능합니다.");
        int step=number(doc,"currentStepNo").intValue();
        if(string(doc,"requestedBy").equals(operatorId)) {
            Map<String,Object> policy=repository.findPolicy(string(doc,"policyCode"),number(doc,"policyVersion").intValue()).orElse(Map.of());
            if(!"Y".equals(string(policy,"selfApprovalAllowedYn")))throw new CpfValidationException("자기승인이 금지된 정책입니다.");
        }
        Map<String,Object> actor=repository.findWaitingParticipant(id,step,operator)
                .orElseThrow(()->new CpfValidationException("현재 단계 승인 참여자가 아닙니다."));
        int decisionChanged;
        try {
            decisionChanged=repository.decideParticipant(number(actor,"participantId").longValue(),status,key,reason,operator);
        } catch (DataIntegrityViolationException duplicateDecision) {
            Map<String,Object> raced=repository.findDecisionByKey(key)
                    .orElseThrow(()->new AdmApprovalConflictException("승인 결정 unique conflict를 수렴할 수 없습니다.",duplicateDecision));
            validateDecisionReplay(raced,id,operator,status,reason);
            return detail(id);
        }
        if(decisionChanged!=1){
            Optional<Map<String,Object>> raced=repository.findDecisionByKey(key);
            if(raced.isPresent()){validateDecisionReplay(raced.get(),id,operator,status,reason);return detail(id);}
            throw new AdmApprovalConflictException("승인 참여자 상태가 동시에 변경되었습니다.");
        }

        List<Map<String,Object>> steps=repository.findPolicySteps(string(doc,"policyCode"),number(doc,"policyVersion").intValue());
        List<Map<String,Object>> participants=repository.findParticipants(id);
        boolean rejected=false;boolean stepDone=true;
        for(Map<String,Object>s:steps){
            if(number(s,"stepNo").intValue()!=step)continue;
            String targetType=string(s,"targetType"),targetCode=string(s,"targetCode");
            List<Map<String,Object>> targetParticipants=participants.stream()
                    .filter(p->number(p,"stepNo").intValue()==step)
                    .filter(p->targetType.equals(string(p,"sourceTargetType"))&&targetCode.equals(string(p,"sourceTargetCode"))).toList();
            if(targetParticipants.isEmpty()){if("Y".equals(string(s,"requiredYn")))stepDone=false;continue;}
            int approved=(int)targetParticipants.stream().filter(p->"APPROVED".equals(string(p,"decisionStatus"))).count();
            int rejects=(int)targetParticipants.stream().filter(p->"REJECTED".equals(string(p,"decisionStatus"))).count();
            Integer rc=s.get("requiredCount")==null?null:number(s,"requiredCount").intValue();
            AdmApprovalDecisionStatus eval=AdmApprovalDecisionEvaluator.evaluate(
                    AdmApprovalDecisionRule.valueOf(string(s,"decisionRule")),targetParticipants.size(),approved,rejects,rc);
            if("Y".equals(string(s,"requiredYn"))&&eval==AdmApprovalDecisionStatus.REJECTED)rejected=true;
            if("Y".equals(string(s,"requiredYn"))&&eval!=AdmApprovalDecisionStatus.APPROVED)stepDone=false;
        }
        String after="PENDING";int next=step;
        if(rejected)after="REJECTED";
        else if(stepDone){
            OptionalInt later=steps.stream().mapToInt(s->number(s,"stepNo").intValue()).filter(x->x>step).min();
            if(later.isPresent())next=later.getAsInt();else after="APPROVED";
        }
        long version=number(doc,"versionNo").longValue();
        if(repository.updateRequest(id,version,after,next,operator)!=1)throw new AdmApprovalConflictException("승인 요청 동시 변경 감지");
        repository.history(id,Boolean.TRUE.equals(request.breakGlass())?"BREAK_GLASS_"+action:action,operator,before,after,reason,
                json(Map.of("breakGlass",Boolean.TRUE.equals(request.breakGlass()),"policyCode",string(doc,"policyCode"),
                        "policyVersion",number(doc,"policyVersion"))),string(doc,"transactionId"));
        return detail(id);
    }

    /**
     * 승인 완료 Command를 실제 Owner에게 전달합니다.
     * 외부 호출 구간은 DB transaction으로 감싸지 않아 장기 lock과 결과불명 오판을 피합니다.
     */
    public Map<String,Object> execute(long id,String reason,String operatorId) {
        String executionReason=bounded(reason,"reason",8,500);
        String operator=required(operatorId,"operatorId");
        Map<String,Object> doc=repository.findRequest(id).orElseThrow(()->new CpfValidationException("승인 요청 없음"));
        Optional<Map<String,Object>> existingExecution=repository.findExecution(id);
        if(existingExecution.isPresent())return detail(id);
        ensureNotExpired(doc);
        if(!"APPROVED".equals(string(doc,"approvalStatus")))throw new CpfValidationException("APPROVED 요청만 실행할 수 있습니다.");
        verifySnapshotOrAudit(id,doc,operator,"APPROVED");

        String ownerModule=string(doc,"ownerModule");
        String ownerCommand=string(doc,"ownerCommand");
        AdmApprovalOwnerCommandPort port=resolveOwnerPort(ownerModule,ownerCommand,string(doc,"actionType"),string(doc,"targetType"));
        long approvedVersion=number(doc,"versionNo").longValue();
        String commandRequestId="ADM-APP-"+id+"-"+UUID.randomUUID();
        if(!repository.reserveExecution(id,approvedVersion,commandRequestId,operator)){
            if(repository.findExecution(id).isPresent())return detail(id);
            throw new AdmApprovalConflictException("승인 실행이 다른 요청에 의해 선점되었거나 상태가 변경되었습니다.");
        }
        Map<String,Object> reserved=repository.findReservedExecutionCommand(id,commandRequestId)
                .orElseThrow(()->new CpfValidationException("서버가 예약한 승인 실행 Snapshot을 찾을 수 없습니다."));
        AdmApprovalSnapshotIntegrity.Verification reservedVerification=snapshotIntegrity.verify(reserved);
        if(!reservedVerification.valid()){
            repository.recordExecutionIntegrityFailure(id,"ADM-SNAPSHOT-HASH-MISMATCH",
                    "승인 Snapshot 무결성 검증 실패",operator,executionReason,
                    json(Map.of("storedHash",reservedVerification.storedHash(),"calculatedHash",reservedVerification.calculatedHash())),
                    string(reserved,"transactionId"));
            return detail(id);
        }

        AdmApprovedOperationResult result;
        try{
            result=port.execute(approvedCommand(id,commandRequestId,reserved,operator,executionReason));
            if(result==null)
                result=new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                        "ADM-OWNER-NULL","Owner가 실행 결과를 반환하지 않았습니다.");
        }catch(RuntimeException e){
            result=new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "ADM-OWNER-EXCEPTION","Owner 결과를 확정할 수 없습니다.");
        }

        String finalRequestStatus=switch(result.status()){
            case SUCCEEDED,RECOVERED->"COMPLETED";
            case FAILED->"FAILED";
            case UNKNOWN,PENDING,RUNNING->"UNKNOWN";
        };
        boolean recovery=result.status()==AdmApprovalExecutionStatus.UNKNOWN
                ||result.status()==AdmApprovalExecutionStatus.PENDING
                ||result.status()==AdmApprovalExecutionStatus.RUNNING;
        String eventData=json(Map.of(
                "executionStatus",result.status().name(),
                "resultCode",Objects.toString(result.resultCode(),"")));
        try{
            repository.finishExecutionAndRequest(id,approvedVersion+1,result.status().name(),
                    finalRequestStatus,result.resultCode(),mask(result.maskedMessage()),recovery,operator,
                    executionReason,eventData,string(doc,"transactionId"));
        }catch(RuntimeException finalizationFailure){
            repository.markExecutionUnknown(id,"ADM-FINALIZATION-UNKNOWN",
                    "Owner 호출 후 결과 저장을 확정할 수 없습니다.",operator);
            repository.history(id,"RESULT_UNKNOWN",operator,"EXECUTING","UNKNOWN",executionReason,
                    json(Map.of("failure","FINALIZATION","commandRequestId",commandRequestId)),
                    string(doc,"transactionId"));
        }
        return detail(id);
    }


    /**
     * Resolves an UNKNOWN result by querying the Owner state only; it never invokes the mutation again.
     */
    public Map<String,Object> reconcile(long id,String reason,String operatorId) {
        String reconciliationReason=bounded(reason,"reason",8,500);
        String operator=required(operatorId,"operatorId");
        Map<String,Object> doc=repository.findRequest(id)
                .orElseThrow(()->new CpfValidationException("승인 요청 없음"));
        Map<String,Object> execution=repository.findExecution(id)
                .orElseThrow(()->new CpfValidationException("승인 실행 없음"));
        if(!"UNKNOWN".equals(string(doc,"approvalStatus"))
                ||!"UNKNOWN".equals(string(execution,"executionStatus"))) {
            return detail(id);
        }
        verifySnapshotOrAudit(id,doc,operator,"UNKNOWN");
        String ownerModule=string(doc,"ownerModule");
        String ownerCommand=string(doc,"ownerCommand");
        AdmApprovalOwnerCommandPort port=resolveOwnerPort(ownerModule,ownerCommand,string(doc,"actionType"),string(doc,"targetType"));
        long unknownVersion=number(doc,"versionNo").longValue();
        String commandRequestId=required(string(execution,"commandRequestId"),"commandRequestId");
        if(!repository.reserveReconcile(id,unknownVersion,operator)) {
            return detail(id);
        }
        Map<String,Object> reserved=repository.findReservedExecutionCommand(id,commandRequestId)
                .orElseThrow(()->new CpfValidationException("서버가 예약한 Reconcile Snapshot을 찾을 수 없습니다."));
        AdmApprovalSnapshotIntegrity.Verification verification=snapshotIntegrity.verify(reserved);
        if(!verification.valid()) {
            repository.recordExecutionIntegrityFailure(id,"ADM-SNAPSHOT-HASH-MISMATCH",
                    "Reconcile Snapshot 무결성 검증 실패",operator,reconciliationReason,
                    json(Map.of("storedHash",verification.storedHash(),"calculatedHash",verification.calculatedHash())),
                    string(reserved,"transactionId"));
            return detail(id);
        }
        AdmApprovedOperationResult result;
        try {
            result=port.reconcile(approvedCommand(id,commandRequestId,reserved,operator,reconciliationReason));
            if(result==null) result=new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "ADM-RECONCILE-NULL","Owner가 Reconcile 결과를 반환하지 않았습니다.");
        } catch(RuntimeException failure) {
            result=new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,
                    "ADM-RECONCILE-EXCEPTION","Owner Reconcile 결과를 확정할 수 없습니다.");
        }
        String finalRequestStatus=switch(result.status()) {
            case SUCCEEDED,RECOVERED->"COMPLETED";
            case FAILED->"FAILED";
            case UNKNOWN,PENDING,RUNNING->"UNKNOWN";
        };
        boolean recovery=Set.of(AdmApprovalExecutionStatus.UNKNOWN,AdmApprovalExecutionStatus.PENDING,
                AdmApprovalExecutionStatus.RUNNING).contains(result.status());
        String eventData=json(Map.of("executionStatus",result.status().name(),
                "resultCode",Objects.toString(result.resultCode(),""),"reconciliation",true));
        try {
            repository.finishExecutionAndRequest(id,unknownVersion+1,result.status().name(),finalRequestStatus,
                    result.resultCode(),mask(result.maskedMessage()),recovery,operator,
                    reconciliationReason,eventData,string(doc,"transactionId"));
        } catch(RuntimeException finalizationFailure) {
            repository.markExecutionUnknown(id,"ADM-RECONCILE-FINALIZATION-UNKNOWN",
                    "Reconcile 후 결과 저장을 확정할 수 없습니다.",operator);
            repository.history(id,"RECONCILE_RESULT_UNKNOWN",operator,"EXECUTING","UNKNOWN",
                    reconciliationReason,json(Map.of("failure","FINALIZATION","commandRequestId",commandRequestId)),
                    string(doc,"transactionId"));
        }
        return detail(id);
    }

    private static AdmApprovedOperationCommand approvedCommand(long id,String commandRequestId,
            Map<String,Object> reserved,String operator,String reason) {
        return new AdmApprovedOperationCommand(id,commandRequestId,string(reserved,"actionType"),
                string(reserved,"ownerModule"),string(reserved,"ownerCommand"),string(reserved,"targetType"),
                string(reserved,"targetId"),string(reserved,"payloadHash"),string(reserved,"payloadSnapshot"),
                string(reserved,"requestedBy"),operator,reason,string(reserved,"transactionId"));
    }

    public Map<String,Object> detail(long id){
        return sanitizeDetail(detailInternal(id));
    }

    /** Internal command path only. Never return this map through a controller. */
    Map<String,Object> detailInternal(long id){
        Map<String,Object> d=new LinkedHashMap<>(repository.findRequest(id)
                .orElseThrow(()->new CpfValidationException("승인 요청 없음")));
        d.put("participants",repository.findParticipants(id));
        d.put("execution",repository.findExecution(id).orElse(null));
        return d;
    }

    private static Map<String,Object> sanitizeDetail(Map<String,Object> source){
        Map<String,Object> safe=new LinkedHashMap<>();
        for(Map.Entry<String,Object> entry:source.entrySet()){
            String key=entry.getKey();String lower=key.toLowerCase(Locale.ROOT);
            if(lower.contains("payloadsnapshot")||lower.contains("secret")||lower.contains("password")||lower.contains("token")) continue;
            if("participants".equals(key)&&entry.getValue() instanceof Iterable<?> rows){
                List<Map<String,Object>> participants=new ArrayList<>();
                for(Object item:rows) if(item instanceof Map<?,?> row){
                    Map<String,Object> selected=new LinkedHashMap<>();
                    for(String field:List.of("participantId","stepNo","sourceTargetType","sourceTargetCode","operatorId","decisionStatus","decisionAt"))
                        if(row.containsKey(field)) selected.put(field,row.get(field));
                    participants.add(Collections.unmodifiableMap(selected));
                }
                safe.put(key,List.copyOf(participants));
            }else if("execution".equals(key)&&entry.getValue() instanceof Map<?,?> row){
                Map<String,Object> selected=new LinkedHashMap<>();
                for(String field:List.of("commandRequestId","executionStatus","ownerResultCode","ownerResultMessage","startedAt","completedAt","recoveryRequiredYn"))
                    if(row.containsKey(field)) selected.put(field,mask(Objects.toString(row.get(field),null)));
                safe.put(key,Collections.unmodifiableMap(selected));
            }else safe.put(key,entry.getValue());
        }
        return Collections.unmodifiableMap(safe);
    }

    private AdmApprovalOwnerCommandPort resolveOwnerPort(String ownerModule,String ownerCommand,
                                                           String actionType,String targetType){
        List<AdmApprovalOwnerCommandPort> matches=ownerPorts.values().stream()
                .filter(Objects::nonNull)
                .filter(port->port.supports(ownerModule,ownerCommand,actionType,targetType))
                .distinct()
                .toList();
        if(matches.isEmpty())
            throw new CpfValidationException("Owner/Command를 지원하는 ADM Command Port가 없습니다: "
                    +ownerModule+"/"+ownerCommand);
        if(matches.size()>1)
            throw new CpfValidationException("Owner/Command에 대응하는 ADM Command Port가 둘 이상입니다: "
                    +ownerModule+"/"+ownerCommand);
        return matches.getFirst();
    }

    private static void validateRequestReplay(Map<String,Object> existing,CreateRequest request,
                                              String requester){
        requireReplayMatch(existing,"ownerModule",required(request.ownerModule(),"ownerModule"));
        requireReplayMatch(existing,"ownerCommand",required(request.ownerCommand(),"ownerCommand"));
        requireReplayMatch(existing,"targetType",required(request.targetType(),"targetType"));
        requireReplayMatch(existing,"targetId",required(request.targetId(),"targetId"));
        requireReplayMatch(existing,"requestedBy",requester);
        requireReplayMatch(existing,"requestReason",required(request.reason(),"reason"));
        if(request.actionType()!=null&&!request.actionType().isBlank())
            requireReplayMatch(existing,"actionType",request.actionType().trim());
        if(request.policyCode()!=null&&!request.policyCode().isBlank())
            requireReplayMatch(existing,"policyCode",request.policyCode().trim());
        if(request.policyVersion()!=null&&number(existing,"policyVersion").intValue()!=request.policyVersion())
            throw new CpfValidationException("requestKey가 다른 policyVersion 요청에 이미 사용되었습니다.");
        Instant requestedExpiry=canonicalInstant(request.expireAt());
        Instant existingExpiry=canonicalInstant(instant(existing.get("expireAt")));
        if(!Objects.equals(existingExpiry,requestedExpiry))
            throw new CpfValidationException("requestKey가 다른 expireAt 요청에 이미 사용되었습니다.");
    }

    private static void validateDecisionReplay(Map<String,Object> previous,long requestId,String operator,
                                               String status,String reason){
        if(number(previous,"approvalRequestId").longValue()!=requestId
                ||!operator.equals(string(previous,"operatorId"))
                ||!status.equals(string(previous,"decisionStatus"))
                ||!reason.equals(string(previous,"decisionReason")))
            throw new CpfValidationException("idempotencyKey가 다른 승인 결정에 이미 사용되었습니다.");
    }

    private static void requireReplayMatch(Map<String,Object> existing,String field,String expected){
        if(!expected.equals(string(existing,field)))
            throw new CpfValidationException("requestKey가 다른 "+field+" 요청에 이미 사용되었습니다.");
    }

    private static void ensureNotExpired(Map<String,Object> request){
        Instant expiry=instant(request.get("expireAt"));
        if(expiry!=null&&!expiry.isAfter(Instant.now()))
            throw new CpfValidationException("만료된 승인 요청입니다.");
    }

    private static Instant canonicalInstant(Instant value){
        return value==null?null:value.truncatedTo(ChronoUnit.MILLIS);
    }

    private static Instant instant(Object value){
        if(value==null)return null;
        if(value instanceof Instant instant)return instant;
        if(value instanceof Timestamp timestamp)return timestamp.toInstant();
        if(value instanceof Date date)return date.toInstant();
        return Instant.parse(String.valueOf(value));
    }

    private void verifySnapshotOrAudit(long id,Map<String,Object> request,String actor,String beforeStatus){
        AdmApprovalSnapshotIntegrity.Verification verification=snapshotIntegrity.verify(request);
        if(verification.valid())return;
        repository.recordIntegrityFailure(id,actor,beforeStatus,
                "승인 Snapshot 무결성 검증 실패",
                json(Map.of("storedHash",verification.storedHash(),"calculatedHash",verification.calculatedHash())),
                string(request,"transactionId"));
        throw new CpfValidationException("승인 Snapshot 무결성 검증에 실패했습니다.");
    }
    private CpfBatchRiskCommand batRiskCommand(long requestId,String requestKey,String requester,
                                                CreateRequest request,String snapshot){
        Map<String,Object> payload;
        try{payload=objectMapper.readValue(snapshot,new TypeReference<>(){});}
        catch(Exception invalid){throw new CpfValidationException("BAT payloadSnapshot을 해석할 수 없습니다.");}
        Long expectedVersion=nullableLong(payload.get("expectedVersion"));
        String operation=required(request.ownerCommand(),"ownerCommand");
        String action=required(request.actionType(),"actionType").toUpperCase(Locale.ROOT);
        String commandPayload=switch(operation){
            case "actGhostExecution" -> action.startsWith("BATCH_GHOST_")
                    ? action.substring("BATCH_GHOST_".length()) : optional(payload.get("actionType"));
            case "updateScheduleEnabled" -> action.endsWith("ENABLE")?"enabled=true":"enabled=false";
            case "requestRun" -> jsonValue(payload.get("jobParameters"));
            default -> "";
        };
        return new CpfBatchRiskCommand(operation,required(request.targetType(),"targetType"),
                required(request.targetId(),"targetId"),action,requester,required(request.reason(),"reason"),
                String.valueOf(requestId),requestKey,expectedVersion,commandPayload);
    }
    private static Map<String,Object> batchRiskSnapshot(CpfBatchRiskCommand risk){
        Map<String,Object> snapshot=new LinkedHashMap<>();
        snapshot.put("operation",risk.operation());snapshot.put("targetType",risk.targetType());
        snapshot.put("targetId",risk.targetId());snapshot.put("actionType",risk.actionType());
        snapshot.put("requestUser",risk.requestUser());snapshot.put("reason",risk.reason());
        snapshot.put("approvalRequestId",risk.approvalRequestId());snapshot.put("idempotencyKey",risk.idempotencyKey());
        snapshot.put("expectedVersion",risk.expectedVersion());snapshot.put("payload",risk.payload());
        return snapshot;
    }
    private static boolean isBat(String owner){return "BAT".equalsIgnoreCase(owner.trim());}
    private static Long nullableLong(Object value){
        if(value==null||String.valueOf(value).isBlank())return null;
        if(value instanceof Number number)return number.longValue();
        try{return Long.valueOf(String.valueOf(value));}catch(NumberFormatException invalid){throw new CpfValidationException("expectedVersion은 숫자여야 합니다.");}
    }
    private String jsonValue(Object value){
        if(value==null)return "";
        if(value instanceof String text)return text;
        return json(value);
    }
    private static String optional(Object value){return value==null?"":String.valueOf(value).trim();}

    private static void validatePolicyActive(Map<String,Object> policy,String requestedAction,Instant now){
        if(!"Y".equalsIgnoreCase(string(policy,"enabledYn")))
            throw new CpfValidationException("비활성 승인 정책은 사용할 수 없습니다.");
        if(!requestedAction.equals(string(policy,"actionType")))
            throw new CpfValidationException("승인 정책 actionType이 Owner 명령과 일치하지 않습니다.");
        Instant from=instant(policy.get("effectiveFrom"));Instant to=instant(policy.get("effectiveTo"));
        if(from!=null&&from.isAfter(now))throw new CpfValidationException("아직 유효하지 않은 승인 정책입니다.");
        if(to!=null&&!to.isAfter(now))throw new CpfValidationException("만료된 승인 정책입니다.");
    }
    private static String bounded(String value,String field,int min,int max){
        String result=required(value,field);
        if(result.length()<min||result.length()>max)
            throw new CpfValidationException(field+" 길이는 "+min+"~"+max+"자여야 합니다.");
        return result;
    }

    private String json(Object v){try{return objectMapper.writeValueAsString(v);}catch(JsonProcessingException e){throw new CpfValidationException("승인 Snapshot JSON 생성 실패");}}
    private static String mask(String v){if(v==null)return null;return v.replaceAll("(?i)(password|secret|token)\\s*[:=]\\s*[^,\\s]+","$1=***");}
    private static String required(String v,String f){if(v==null||v.isBlank())throw new CpfValidationException(f+"는 필수입니다.");return v.trim();}
    private static <T>T required(T v,String f){if(v==null)throw new CpfValidationException(f+"는 필수입니다.");return v;}
    private static String blank(String v){return v==null||v.isBlank()?null:v.trim();}
    private static String upper(String v,String d){return(v==null||v.isBlank()?d:v.trim()).toUpperCase(Locale.ROOT);}
    private static String yn(String v,String d){String x=upper(v,d);if(!Set.of("Y","N").contains(x))throw new CpfValidationException("Y/N 값 필요");return x;}
    private static String string(Map<String,?>m,String k){Object v=m.get(k);return v==null?"":String.valueOf(v);}
    private static Number number(Map<String,?>m,String k){Object v=m.get(k);if(v instanceof Number n)return n;return v==null?0:Long.parseLong(String.valueOf(v));}
    private record Resolved(int stepNo,String targetType,String targetCode,AdmApprovalDirectoryEntry entry){}

    public record ApprovalMutationResult(boolean created,boolean replayed,Map<String,Object> body){}
    public record PolicyRequest(
      @NotBlank @Size(max=80) String policyCode, @Min(1) Integer policyVersion,
      @NotBlank @Size(max=150) String policyName, @NotBlank @Size(max=80) String actionType,
      Instant effectiveFrom,Instant effectiveTo,String enabledYn,String selfApprovalAllowedYn,String breakGlassAllowedYn,
      @Size(max=1000) String description,@NotEmpty List<@Valid PolicyStepRequest> steps,
      @NotBlank @Size(min=8,max=500) String reason){}
    public record PolicyStepRequest(@Min(1) Integer stepNo,@Size(max=30) String stepType,
      @NotBlank @Size(max=30) String targetType,@NotBlank @Size(max=100) String targetCode,
      @Size(max=20) String decisionRule,@Min(1) Integer requiredCount,String requiredYn){}
    public record CreateRequest(@NotBlank @Size(min=8,max=128) String requestKey,
      @Size(max=80) String policyCode,@Min(1) Integer policyVersion,@NotBlank @Size(max=80) String actionType,
      @NotBlank @Size(max=30) String ownerModule,@NotBlank @Size(max=120) String ownerCommand,
      @NotBlank @Size(max=80) String targetType,@NotBlank @Size(max=200) String targetId,
      @NotBlank String payloadSnapshot,Instant expireAt,@NotBlank @Size(min=8,max=500) String reason){}
    public record DecisionRequest(@NotBlank @Size(max=20) String action,
      @NotBlank @Size(min=8,max=128) String idempotencyKey,
      @NotBlank @Size(min=8,max=500) String reason,Boolean breakGlass){
        public DecisionRequest(String action,String idempotencyKey,String reason){this(action,idempotencyKey,reason,Boolean.FALSE);}
    }
}
