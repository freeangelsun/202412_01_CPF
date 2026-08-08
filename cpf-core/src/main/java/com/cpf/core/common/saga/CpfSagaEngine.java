package com.cpf.core.common.saga;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 기술 공통 Saga Runtime. 정방향 완료 Step만 역순 보상하며, 보상 실패를 성공으로 숨기지 않습니다.
 */
public class CpfSagaEngine {
    private final CpfSagaStateStore store;
    private final CpfSagaDefinitionRegistry registry;

    public CpfSagaEngine(CpfSagaStateStore store,CpfSagaDefinitionRegistry registry){this.store=store;this.registry=registry;}

    public CpfSagaSnapshot execute(CpfSagaDefinition definition,String businessKey,String transactionId,java.util.Map<String,Object> attributes){
        registry.register(definition);
        String sagaId="SAGA-"+UUID.randomUUID();
        CpfSagaContext context=new CpfSagaContext(sagaId,definition.sagaType(),businessKey,transactionId,attributes);
        store.create(context);
        store.markSaga(sagaId,CpfSagaStatus.STARTED,null);
        store.markSaga(sagaId,CpfSagaStatus.RUNNING,null);
        List<Completed> completed=new ArrayList<>();
        for(int i=0;i<definition.steps().size();i++){
            CpfSagaStep step=definition.steps().get(i);int stepNo=i+1;
            store.markStep(sagaId,stepNo,step.stepId(),CpfSagaStepStatus.RUNNING,null,null,false);
            try{
                CpfSagaStepResult result=step.execute(context);
                CpfSagaStepResult safe=result==null?CpfSagaStepResult.success(null):result;
                store.markStep(sagaId,stepNo,step.stepId(),CpfSagaStepStatus.COMPLETED,safe,null,false);
                completed.add(new Completed(stepNo,step,safe));
            }catch(CpfSagaUnknownOutcomeException ex){
                store.markStep(sagaId,stepNo,step.stepId(),CpfSagaStepStatus.FAILED,null,safe(ex),false);
                store.markSaga(sagaId,CpfSagaStatus.UNKNOWN,safe(ex));
                return store.find(sagaId).orElseThrow();
            }catch(Exception ex){
                store.markStep(sagaId,stepNo,step.stepId(),CpfSagaStepStatus.FAILED,null,safe(ex),false);
                store.markSaga(sagaId,CpfSagaStatus.FAILED,safe(ex));
                compensate(context,completed,ex);
                return store.find(sagaId).orElseThrow();
            }
        }
        store.markSaga(sagaId,CpfSagaStatus.COMPLETED,null);
        return store.find(sagaId).orElseThrow();
    }

    private void compensate(CpfSagaContext context,List<Completed> completed,Exception cause){
        store.markSaga(context.sagaId(),CpfSagaStatus.COMPENSATING,safe(cause));
        boolean failed=false;
        for(int i=completed.size()-1;i>=0;i--){
            Completed c=completed.get(i);
            store.markStep(context.sagaId(),c.stepNo,c.step.stepId(),CpfSagaStepStatus.COMPENSATING,c.result,null,true);
            try{c.step.compensate(context,c.result);store.markStep(context.sagaId(),c.stepNo,c.step.stepId(),CpfSagaStepStatus.COMPENSATED,c.result,null,true);}
            catch(Exception ex){failed=true;store.markStep(context.sagaId(),c.stepNo,c.step.stepId(),CpfSagaStepStatus.COMPENSATION_FAILED,c.result,safe(ex),true);}
        }
        store.markSaga(context.sagaId(),failed?CpfSagaStatus.MANUAL_REVIEW:CpfSagaStatus.COMPENSATED,failed?"일부 Compensation 실패 - 수동 복구 필요":safe(cause));
    }

    private static String safe(Exception ex){String m=ex==null?null:ex.getMessage();return m==null?ex==null?null:ex.getClass().getSimpleName():m.substring(0,Math.min(1000,m.length()));}
    private record Completed(int stepNo,CpfSagaStep step,CpfSagaStepResult result){}
}
