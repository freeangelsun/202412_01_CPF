package com.cpf.integration.resilience.runtime;

import com.cpf.core.api.context.*;
import com.cpf.integration.api.annotation.*;
import com.cpf.integration.resilience.api.*;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.Callable;

/** {@code @CpfClient}/{@code @CpfRetry}/{@code @CpfTimeout}를 기존 Resilience Engine과 Core Context에 연결하는 순수 Runtime Coordinator입니다. */
@SuppressWarnings("deprecation")
public final class CpfIntegrationClientCoordinator {
    private final CpfIntegrationAnnotationProperties properties;
    private final CpfIntegrationAnnotationPolicyFactory policyFactory;
    private final CpfAnnotationResiliencePolicyRegistry annotationPolicies;
    private final CpfResilienceExecutor executor;
    private final Clock clock;
    public CpfIntegrationClientCoordinator(CpfIntegrationAnnotationProperties properties,CpfIntegrationAnnotationPolicyFactory policyFactory,
            CpfAnnotationResiliencePolicyRegistry annotationPolicies,CpfResilienceExecutor executor,Clock clock){
        this.properties=properties;this.policyFactory=policyFactory;this.annotationPolicies=annotationPolicies;this.executor=executor;this.clock=clock;
    }
    public Object execute(Method method,Object[] args,CpfClient client,CpfRetry retry,CpfTimeout timeout,CpfTimeLimiter legacyTimeout,Callable<Object> action) throws Exception{
        if(!properties.isEnabled())return action.call();
        CpfContext current=client.contextRequired()?CpfContexts.requireCurrent():CpfContexts.current();
        if(current==null)return action.call();
        CpfResiliencePolicy policy=policyFactory.create(method,client,retry,timeout,legacyTimeout); annotationPolicies.register(policy);
        String key=current.idempotencyKey();
        Map<String,String> attrs=Map.of(
                CpfResilienceCallContext.OPERATION_KIND_ATTRIBUTE,client.sideEffecting()?"WRITE":"READ",
                CpfResilienceCallContext.TIMEOUT_RETRY_ATTRIBUTE,"false");
        CpfResilienceCallContext call=CpfResilienceCallContext.current(policy.operationId(),key,attrs,clock);
        CpfResilienceOutcome<Object> result=executor.execute(call,()->{
            try{return action.call();}catch(RuntimeException e){throw e;}catch(Exception e){throw new CpfCheckedIntegrationException(e);}
        });
        if(result.status()==CpfResilienceOutcome.Status.SUCCESS)return result.value();
        throw new CpfIntegrationCallException(policy.operationId(),result.status(),result.reasonCode());
    }
    private static final class CpfCheckedIntegrationException extends RuntimeException{CpfCheckedIntegrationException(Exception cause){super(cause);}}
}
