package com.cpf.integration.context;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.integration.internal.context.CpfIntegrationContextRuntime;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 외부/타 시스템 호출의 Core child execution과 Integration 전문 Context lifecycle을 함께 관리합니다.
 *
 * <p>Provider/연계 Runtime이 사용하며 Application 업무 코드는 ID SPI나 ThreadLocal을 직접 조립하지 않습니다.
 * retry/UNKNOWN/reconcile은 같은 transaction/root execution 계보를 유지하면서 새로운 executionId를 발급합니다.</p>
 */
public final class CpfIntegrationContextRunner {
    private final CpfExecutionIdGenerator executionIds;

    public CpfIntegrationContextRunner(CpfExecutionIdGenerator executionIds) {
        this.executionIds=Objects.requireNonNull(executionIds,"executionIds");
    }

    public <T> T call(String partnerSystemCode,String logicalEndpointId,String idempotencyKey,int attempt,
                      Instant deadline,Callable<T> work) throws Exception {
        return call(partnerSystemCode,logicalEndpointId,idempotencyKey,attempt,null,null,deadline,work);
    }

    public <T> T call(String partnerSystemCode,String logicalEndpointId,String idempotencyKey,int attempt,
                      String unknownOutcomeId,String recoveryId,Instant deadline,Callable<T> work) throws Exception {
        Objects.requireNonNull(work,"work");
        CpfContext parent=CpfContexts.requireCurrent();
        Instant now=Instant.now();
        CpfContext.CpfExecutionContext p=parent.execution();
        CpfContext.CpfExecutionContext child=p.child(
                logicalEndpointId,executionIds.newExecutionId(),executionIds.newSegmentId(),
                CpfContext.CpfExecutionType.INTEGRATION,Math.max(1,attempt),now,deadline==null?p.deadline():deadline);
        CpfContext childContext=parent.child(child,parent.operation());
        CpfIntegrationContext integration=new CpfIntegrationContext(
                partnerSystemCode,logicalEndpointId,child.executionId(),Math.max(1,attempt),
                idempotencyKey,unknownOutcomeId,recoveryId,now);
        try(AutoCloseable _=CpfContexts.bind(CpfContextSnapshot.capture(childContext));
            AutoCloseable _=CpfIntegrationContextRuntime.bind(integration)){
            return work.call();
        }
    }

    public <T> T reconcile(String partnerSystemCode,String logicalEndpointId,String idempotencyKey,int attempt,
                           String unknownOutcomeId,String recoveryId,Callable<T> work) throws Exception {
        if(unknownOutcomeId==null||unknownOutcomeId.isBlank())throw new IllegalArgumentException("unknownOutcomeId is required");
        if(recoveryId==null||recoveryId.isBlank())throw new IllegalArgumentException("recoveryId is required");
        return call(partnerSystemCode,logicalEndpointId,idempotencyKey,attempt,unknownOutcomeId,recoveryId,null,work);
    }
}
