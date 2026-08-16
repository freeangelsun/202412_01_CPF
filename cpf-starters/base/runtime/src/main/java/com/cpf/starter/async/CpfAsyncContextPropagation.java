package com.cpf.starter.async;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Callable;

/** Async 경계에서 parent Context를 캡처하고 child execution으로 전환합니다. */
public final class CpfAsyncContextPropagation {
    private final CpfExecutionIdGenerator ids;
    private final String executorName;
    public CpfAsyncContextPropagation(CpfExecutionIdGenerator ids, String executorName) {
        this.ids=Objects.requireNonNull(ids,"ids"); this.executorName=executorName;
    }
    public Runnable wrap(Runnable task, CpfAsyncForkType type) {
        Objects.requireNonNull(task,"task"); CpfContextSnapshot parent=CpfContexts.requireSnapshot();
        return () -> CpfContexts.run(childSnapshot(parent,type),task);
    }
    public <T> Callable<T> wrap(Callable<T> task, CpfAsyncForkType type) {
        Objects.requireNonNull(task,"task"); CpfContextSnapshot parent=CpfContexts.requireSnapshot();
        return () -> CpfContexts.call(childSnapshot(parent,type),task);
    }
    private CpfContextSnapshot childSnapshot(CpfContextSnapshot parent, CpfAsyncForkType type) {
        var p=parent.execution(); Instant now=Instant.now(); String executionId=ids.newExecutionId(); String segmentId=ids.newSegmentId();
        var childExecution=new CpfContext.CpfExecutionContext(null,executionId,p.rootExecutionId(),p.executionId(),segmentId,p.segmentId(),
                CpfContext.CpfExecutionType.ASYNC,Math.max(1,p.attempt()),p.callDepth()+1,now,p.deadline(),p.cancellationMode());
        // Async 전용 executor/fork 정보는 Foundation runtime metadata이며 Core Context에 삽입하지 않습니다.
        new CpfAsyncContext(p.executionId(),executionId,type,executorName,childExecution.attempt());
        return CpfContextSnapshot.capture(new CpfContext(parent.transaction(),childExecution,parent.operation(),parent.identity(),parent.tenant()));
    }
}
