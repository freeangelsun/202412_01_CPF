package com.cpf.reference.edu.counterparty.application;

import com.cpf.reference.edu.counterparty.model.ReferenceCounterpartyExchange;
import com.cpf.reference.edu.counterparty.persistence.ReferenceCounterpartyStore;
import com.cpf.reference.edu.runtime.application.EduPayloadHasher;
import java.time.*;
import java.util.*;

/** Deterministic REF-owned external counterparty simulator with durable idempotency/reconciliation. */
public final class ReferenceCounterpartyService {
    private static final Set<Integer> ALLOWED=Set.of(200,202,409,429,503);
    private final ReferenceCounterpartyStore store; private final Clock clock;
    public ReferenceCounterpartyService(ReferenceCounterpartyStore store,Clock clock){this.store=Objects.requireNonNull(store);this.clock=Objects.requireNonNull(clock);}
    public ReferenceCounterpartyResult exchange(String family,String scenario,String requirementId,String idempotencyKey,String traceId,Map<String,Object> request){
        family=code(family,"family");scenario=code(scenario,"scenario");requirementId=text(requirementId,"requirementId");idempotencyKey=text(idempotencyKey,"idempotencyKey");traceId=text(traceId,"traceId");request=Map.copyOf(request==null?Map.of():request);
        String requestHash=EduPayloadHasher.hash(request);
        var existing=store.find(requirementId,idempotencyKey);
        if(existing.isPresent())return replay(existing.get(),requestHash);
        Instant now=clock.instant();String requestId=UUID.randomUUID().toString();String businessKey=text(String.valueOf(request.getOrDefault("businessKey","unknown")),"businessKey");
        var received=new ReferenceCounterpartyExchange(requestId,requirementId,idempotencyKey,requestHash,businessKey,family,scenario,"RECEIVED",0,Map.of(),1,traceId,now,now,null);
        if(!store.insert(received)){return replay(store.find(requirementId,idempotencyKey).orElseThrow(),requestHash);}
        int requested=status(request);boolean responseLoss=bool(request,"simulateResponseLoss");
        String state=responseLoss||requested==202?"UNKNOWN_RESULT":requested==200?"COMPLETED":requested==409?"CONFLICT":requested==429?"RETRY_WAIT":"FAILED_RETRYABLE";
        int responseStatus=responseLoss?202:requested;
        Map<String,Object> response=Map.of("counterpartyRequestId",requestId,"requirementId",requirementId,"businessKey",businessKey,"family",family,"scenario",scenario,"state",state,"traceId",traceId);
        var completed=new ReferenceCounterpartyExchange(requestId,requirementId,idempotencyKey,requestHash,businessKey,family,scenario,state,responseStatus,response,1,traceId,now,clock.instant(),terminal(state)?clock.instant():null);
        store.update(completed);
        return new ReferenceCounterpartyResult(responseStatus,false,response);
    }
    private ReferenceCounterpartyResult replay(ReferenceCounterpartyExchange old,String requestHash){
        if(!old.requestHash().equals(requestHash))return new ReferenceCounterpartyResult(409,true,Map.of("state","IDEMPOTENCY_CONFLICT","counterpartyRequestId",old.requestId()));
        int status=old.responseStatus()==0?202:old.responseStatus();return new ReferenceCounterpartyResult(status,true,old.response());
    }
    private static int status(Map<String,Object> request){Object payload=request.get("payload");Object raw=payload instanceof Map<?,?> m?m.get("simulateStatus"):request.get("simulateStatus");int v=200;try{if(raw!=null)v=Integer.parseInt(String.valueOf(raw));}catch(NumberFormatException ignored){}return ALLOWED.contains(v)?v:200;}
    private static boolean bool(Map<String,Object> r,String key){Object p=r.get("payload");Object v=p instanceof Map<?,?> m?m.get(key):r.get(key);return Boolean.parseBoolean(String.valueOf(v));}
    private static boolean terminal(String state){return Set.of("COMPLETED","CONFLICT").contains(state);}
    private static String code(String v,String n){v=text(v,n);if(!v.matches("[a-zA-Z0-9_-]{1,40}"))throw new IllegalArgumentException(n+" contains unsafe characters");return v;}
    private static String text(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
}
