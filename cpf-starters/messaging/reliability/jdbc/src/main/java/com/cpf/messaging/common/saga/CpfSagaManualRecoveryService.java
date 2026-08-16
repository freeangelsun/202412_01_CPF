package com.cpf.messaging.common.saga;

import com.cpf.messaging.reliability.saga.*;

import java.util.HashMap;
import java.util.Map;

/** 운영자 사유를 필수로 하는 Saga Compensation 수동 복구 Runtime. */
public class CpfSagaManualRecoveryService {
    private final CpfSagaStateStore store;private final CpfSagaDefinitionRegistry registry;
    public CpfSagaManualRecoveryService(CpfSagaStateStore store,CpfSagaDefinitionRegistry registry){this.store=store;this.registry=registry;}

    public CpfSagaSnapshot retryCompensation(String sagaId,String operatorId,String reason){
        requireAudit(operatorId,reason);CpfSagaSnapshot snap=store.find(sagaId).orElseThrow(()->new IllegalArgumentException("Saga를 찾을 수 없습니다: "+sagaId));
        if(snap.status()!=CpfSagaStatus.MANUAL_REVIEW&&snap.status()!=CpfSagaStatus.MANUAL_INTERVENTION_REQUIRED&&snap.status()!=CpfSagaStatus.COMPENSATION_FAILED)throw new IllegalStateException("수동 Compensation 대상 상태가 아닙니다: "+snap.status());
        CpfSagaDefinition def=registry.find(snap.sagaType()).orElseThrow(()->new IllegalStateException("실행 중인 Saga 정의를 Registry에서 찾을 수 없습니다: "+snap.sagaType()));
        Map<String,CpfSagaStep> byId=new HashMap<>();def.steps().forEach(s->byId.put(s.stepId(),s));
        CpfSagaContext ctx=new CpfSagaContext(snap.sagaId(),snap.sagaType(),snap.businessKey(),snap.transactionId(),Map.of());
        String before=snap.status().name();boolean failed=false;
        java.util.List<CpfSagaStepSnapshot> steps=snap.steps();
        for(int i=steps.size()-1;i>=0;i--){CpfSagaStepSnapshot ss=steps.get(i);if(ss.status()!=CpfSagaStepStatus.COMPENSATION_FAILED)continue;
            CpfSagaStep step=byId.get(ss.stepId());if(step==null){failed=true;continue;}
            CpfSagaStepResult prior=new CpfSagaStepResult(ss.resultCode(),ss.resultSnapshot());
            store.markStep(sagaId,ss.stepNo(),ss.stepId(),CpfSagaStepStatus.COMPENSATING,prior,null,true);
            try{step.compensate(ctx,prior);store.markStep(sagaId,ss.stepNo(),ss.stepId(),CpfSagaStepStatus.COMPENSATED,prior,null,true);}
            catch(Exception ex){failed=true;store.markStep(sagaId,ss.stepNo(),ss.stepId(),CpfSagaStepStatus.COMPENSATION_FAILED,prior,ex.getMessage(),true);}
        }
        CpfSagaStatus after=failed?CpfSagaStatus.MANUAL_REVIEW:CpfSagaStatus.COMPENSATED;store.markSaga(sagaId,after,failed?"수동 Compensation 재시도 일부 실패":null);store.auditManualAction(sagaId,"RETRY_COMPENSATION",operatorId,reason,before,after.name());return store.find(sagaId).orElseThrow();
    }

    public CpfSagaSnapshot resolveManually(String sagaId,String operatorId,String reason){
        requireAudit(operatorId,reason);CpfSagaSnapshot before=store.find(sagaId).orElseThrow();
        if(before.status()!=CpfSagaStatus.MANUAL_REVIEW&&before.status()!=CpfSagaStatus.MANUAL_INTERVENTION_REQUIRED)throw new IllegalStateException("수동 확정 가능 상태가 아닙니다.");
        store.markSaga(sagaId,CpfSagaStatus.MANUALLY_RESOLVED,"운영자 수동 확정");store.auditManualAction(sagaId,"MANUAL_RESOLVE",operatorId,reason,before.status().name(),CpfSagaStatus.MANUALLY_RESOLVED.name());return store.find(sagaId).orElseThrow();
    }
    private static void requireAudit(String o,String r){if(o==null||o.isBlank())throw new IllegalArgumentException("operatorId는 필수입니다.");if(r==null||r.isBlank())throw new IllegalArgumentException("reason은 필수입니다.");}
}
