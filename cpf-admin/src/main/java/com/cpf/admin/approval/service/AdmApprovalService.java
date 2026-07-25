package com.cpf.admin.approval.service;

import com.cpf.admin.approval.api.*;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.spi.AdmApprovalDirectoryEntry;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.core.common.exception.CpfValidationException;
import com.cpf.core.common.logging.TransactionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/**
 * ADM 위험조치 Approval Engine.
 *
 * <p>정책 Target을 요청 시 실제 운영자 Snapshot으로 고정하고 ALL/ANY/N_OF_M을 평가합니다.
 * 승인 완료 후 실제 변경은 ADM DB가 아니라 Owner Command Port를 통해 실행합니다.</p>
 */
@Service
public class AdmApprovalService {
    private final AdmApprovalRepository repository;
    private final ObjectMapper objectMapper;
    private final Map<String, AdmApprovalOwnerCommandPort> ownerPorts;

    public AdmApprovalService(
            AdmApprovalRepository repository,
            ObjectMapper objectMapper,
            Map<String, AdmApprovalOwnerCommandPort> ownerPorts) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.ownerPorts = ownerPorts;
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
        repository.replacePolicy(p,steps);
        return findPolicy(String.valueOf(p.get("policyCode")),version);
    }

    @Transactional(transactionManager = "admTransactionManager")
    public Map<String,Object> requestApproval(CreateRequest request,String operatorId) {
        String requestKey=required(request.requestKey(),"requestKey");
        Optional<Long> existing=repository.findRequestIdByKey(requestKey);
        if(existing.isPresent())return detail(existing.get());
        Instant now=Instant.now();
        Map<String,Object> policy;
        if(request.policyCode()!=null&&!request.policyCode().isBlank()){
            if(request.policyVersion()==null)throw new CpfValidationException("policyVersion이 필요합니다.");
            policy=repository.findPolicy(request.policyCode(),request.policyVersion())
                    .orElseThrow(()->new CpfValidationException("ADM 승인 정책을 찾을 수 없습니다."));
        }else{
            policy=repository.findActivePolicy(required(request.actionType(),"actionType"),now)
                    .orElseThrow(()->new CpfValidationException("활성 ADM 승인 정책이 없습니다."));
        }
        String code=string(policy,"policyCode");int ver=number(policy,"policyVersion").intValue();
        List<Map<String,Object>> steps=repository.findPolicySteps(code,ver);
        if(steps.isEmpty())throw new CpfValidationException("승인 정책 단계가 없습니다.");
        String requester=required(operatorId,"operatorId");
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
        String snapshot=request.payloadSnapshot()==null?"{}":request.payloadSnapshot();
        Map<String,Object> v=new LinkedHashMap<>();
        v.put("requestKey",requestKey);v.put("policyCode",code);v.put("policyVersion",ver);
        v.put("actionType",string(policy,"actionType"));v.put("ownerModule",required(request.ownerModule(),"ownerModule"));
        v.put("ownerCommand",required(request.ownerCommand(),"ownerCommand"));v.put("targetType",required(request.targetType(),"targetType"));
        v.put("targetId",required(request.targetId(),"targetId"));v.put("requestedBy",requester);
        v.put("requestReason",required(request.reason(),"reason"));v.put("payloadHash",sha256(snapshot));
        v.put("payloadSnapshot",snapshot);v.put("currentStepNo",minStep);
        v.put("expireAt",request.expireAt()==null?null:Timestamp.from(request.expireAt()));
        v.put("transactionId",TransactionContext.getOrCreateTransactionId());v.put("operatorId",requester);
        long id=repository.insertRequest(v);
        for(Resolved r:resolved)repository.insertParticipant(id,r.stepNo(),r.entry(),r.targetType(),r.targetCode(),requester);
        repository.history(id,"REQUEST",requester,null,"PENDING",required(request.reason(),"reason"),
                json(Map.of("policyCode",code,"policyVersion",ver,"participants",resolved.size())),string(v,"transactionId"));
        return detail(id);
    }

    @Transactional(transactionManager = "admTransactionManager")
    public Map<String,Object> decide(long id,DecisionRequest request,String operatorId) {
        String key=required(request.idempotencyKey(),"idempotencyKey");
        if(repository.decisionKeyExists(key))return detail(id);
        Map<String,Object> doc=repository.findRequest(id).orElseThrow(()->new CpfValidationException("승인 요청 없음"));
        String before=string(doc,"approvalStatus");if(!"PENDING".equals(before))throw new CpfValidationException("PENDING 요청만 승인/반려 가능");
        int step=number(doc,"currentStepNo").intValue();
        if(string(doc,"requestedBy").equals(operatorId)) {
            Map<String,Object> policy=repository.findPolicy(string(doc,"policyCode"),number(doc,"policyVersion").intValue()).orElse(Map.of());
            if(!"Y".equals(string(policy,"selfApprovalAllowedYn")))throw new CpfValidationException("자기승인이 금지된 정책입니다.");
        }
        Map<String,Object> actor=repository.findWaitingParticipant(id,step,required(operatorId,"operatorId"))
                .orElseThrow(()->new CpfValidationException("현재 단계 승인 참여자가 아닙니다."));
        String action=upper(request.action(),"APPROVE");
        String status=switch(action){case"APPROVE"->"APPROVED";case"REJECT"->"REJECTED";default->throw new CpfValidationException("APPROVE/REJECT만 지원");};
        if(repository.decideParticipant(number(actor,"participantId").longValue(),status,key,required(request.reason(),"reason"),operatorId)!=1)
            throw new CpfValidationException("승인 참여자 상태가 동시에 변경되었습니다.");

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
        if(repository.updateRequest(id,version,after,next,operatorId)!=1)throw new CpfValidationException("승인 요청 동시 변경 감지");
        repository.history(id,action,operatorId,before,after,required(request.reason(),"reason"),null,string(doc,"transactionId"));
        return detail(id);
    }

    /**
     * 승인 완료 Command를 실제 Owner에게 전달합니다.
     * 외부 호출 구간은 DB transaction으로 감싸지 않아 장기 lock과 결과불명 오판을 피합니다.
     */
    public Map<String,Object> execute(long id,String reason,String operatorId) {
        Map<String,Object> doc=repository.findRequest(id).orElseThrow(()->new CpfValidationException("승인 요청 없음"));
        if(repository.findExecution(id).isPresent())return detail(id);
        if(!"APPROVED".equals(string(doc,"approvalStatus")))throw new CpfValidationException("APPROVED 요청만 실행할 수 있습니다.");
        String commandRequestId="ADM-APP-"+id+"-"+UUID.randomUUID();
        repository.startExecution(id,commandRequestId,operatorId);
        if(repository.updateRequest(id,number(doc,"versionNo").longValue(),"EXECUTING",number(doc,"currentStepNo").intValue(),operatorId)!=1)
            throw new CpfValidationException("실행 시작 중 승인 요청 동시 변경 감지");
        AdmApprovedOperationResult result;
        try{
            AdmApprovalOwnerCommandPort port=resolveOwnerPort(string(doc,"ownerModule"));
            result=port.execute(new AdmApprovedOperationCommand(id,commandRequestId,string(doc,"actionType"),
                    string(doc,"ownerModule"),string(doc,"ownerCommand"),string(doc,"targetType"),string(doc,"targetId"),
                    string(doc,"payloadHash"),string(doc,"transactionId")));
        }catch(RuntimeException e){
            result=new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,"ADM-OWNER-EXCEPTION","Owner 결과를 확정할 수 없습니다.");
        }
        String finalRequestStatus=switch(result.status()){
            case SUCCEEDED,RECOVERED->"COMPLETED";
            case FAILED->"FAILED";
            case UNKNOWN,PENDING,RUNNING->"UNKNOWN";
        };
        boolean recovery=result.status()==AdmApprovalExecutionStatus.UNKNOWN||result.status()==AdmApprovalExecutionStatus.PENDING||result.status()==AdmApprovalExecutionStatus.RUNNING;
        repository.finishExecution(id,result.status().name(),result.resultCode(),mask(result.maskedMessage()),recovery,operatorId);
        Map<String,Object> executing=repository.findRequest(id).orElseThrow();
        repository.updateRequest(id,number(executing,"versionNo").longValue(),finalRequestStatus,
                number(executing,"currentStepNo").intValue(),operatorId);
        repository.history(id,"RESULT",operatorId,"EXECUTING",finalRequestStatus,required(reason,"reason"),
                json(Map.of("executionStatus",result.status().name(),"resultCode",Objects.toString(result.resultCode(),""))),
                string(doc,"transactionId"));
        return detail(id);
    }

    public Map<String,Object> detail(long id){
        Map<String,Object> d=new LinkedHashMap<>(repository.findRequest(id).orElseThrow(()->new CpfValidationException("승인 요청 없음")));
        d.put("participants",repository.findParticipants(id));d.put("execution",repository.findExecution(id).orElse(null));return d;
    }

    private AdmApprovalOwnerCommandPort resolveOwnerPort(String ownerModule){
        if(ownerPorts.isEmpty())throw new CpfValidationException("등록된 ADM Owner Command Port가 없습니다.");
        for(Map.Entry<String,AdmApprovalOwnerCommandPort>e:ownerPorts.entrySet()){
            String k=e.getKey().replace("-","").replace("_","").toLowerCase(Locale.ROOT);
            String o=ownerModule.replace("-","").replace("_","").toLowerCase(Locale.ROOT);
            if(k.equals(o)||k.startsWith(o))return e.getValue();
        }
        if(ownerPorts.size()==1)return ownerPorts.values().iterator().next();
        throw new CpfValidationException("Owner Module에 대응하는 Command Port를 결정할 수 없습니다: "+ownerModule);
    }

    private String json(Object v){try{return objectMapper.writeValueAsString(v);}catch(JsonProcessingException e){throw new CpfValidationException("승인 Snapshot JSON 생성 실패");}}
    private static String sha256(String t){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(t.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    private static String mask(String v){if(v==null)return null;return v.replaceAll("(?i)(password|secret|token)\\s*[:=]\\s*[^,\\s]+","$1=***");}
    private static String required(String v,String f){if(v==null||v.isBlank())throw new CpfValidationException(f+"는 필수입니다.");return v.trim();}
    private static <T>T required(T v,String f){if(v==null)throw new CpfValidationException(f+"는 필수입니다.");return v;}
    private static String blank(String v){return v==null||v.isBlank()?null:v.trim();}
    private static String upper(String v,String d){return(v==null||v.isBlank()?d:v.trim()).toUpperCase(Locale.ROOT);}
    private static String yn(String v,String d){String x=upper(v,d);if(!Set.of("Y","N").contains(x))throw new CpfValidationException("Y/N 값 필요");return x;}
    private static String string(Map<String,?>m,String k){Object v=m.get(k);return v==null?"":String.valueOf(v);}
    private static Number number(Map<String,?>m,String k){Object v=m.get(k);if(v instanceof Number n)return n;return v==null?0:Long.parseLong(String.valueOf(v));}
    private record Resolved(int stepNo,String targetType,String targetCode,AdmApprovalDirectoryEntry entry){}

    public record PolicyRequest(String policyCode,Integer policyVersion,String policyName,String actionType,
      Instant effectiveFrom,Instant effectiveTo,String enabledYn,String selfApprovalAllowedYn,String breakGlassAllowedYn,
      String description,List<PolicyStepRequest> steps,String reason){}
    public record PolicyStepRequest(Integer stepNo,String stepType,String targetType,String targetCode,
      String decisionRule,Integer requiredCount,String requiredYn){}
    public record CreateRequest(String requestKey,String policyCode,Integer policyVersion,String actionType,
      String ownerModule,String ownerCommand,String targetType,String targetId,String payloadSnapshot,Instant expireAt,String reason){}
    public record DecisionRequest(String action,String idempotencyKey,String reason){}
}
