package com.cpf.testkit.context;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/** deterministic CPF Context fixture. Test가 runtime storage 구현을 직접 다루지 않도록 합니다. */
public final class CpfContextTestSupport {
    private final String prefix;
    private final LocalDate businessDate;
    private final AtomicInteger sequence = new AtomicInteger();

    public CpfContextTestSupport(String prefix, LocalDate businessDate) {
        this.prefix = prefix; this.businessDate = businessDate;
    }

    public AutoCloseable bindRoot(String correlationId, String idempotencyKey, String actorId) {
        int n=sequence.incrementAndGet();
        String tx=prefix+"-TX-"+n; String ex=prefix+"-EX-"+sequence.incrementAndGet(); String sg=prefix+"-SG-"+sequence.incrementAndGet();
        CpfContext.CpfOperationContext operation=idempotencyKey==null?null:new CpfContext.CpfOperationContext(
                "OP-"+sequence.incrementAndGet(),"TEST",null,idempotencyKey,
                CpfContext.CpfIdempotencyScope.CURRENT_OPERATION,CpfContext.CpfIdempotencyMode.REQUIRED,null,null);
        CpfContext.CpfIdentityContext identity=actorId==null?null:new CpfContext.CpfIdentityContext(actorId,null,CpfContext.CpfPrincipalType.USER);
        Instant now=Instant.now();
        CpfContext root=new CpfContext(
                new CpfContext.CpfTransactionContext(tx,tx,null,correlationId,businessDate,now,CpfContext.CpfTransactionOriginKind.INTERNAL,"testkit",null),
                new CpfContext.CpfExecutionContext("TEST",ex,ex,null,sg,null,CpfContext.CpfExecutionType.INTERNAL,1,0,now,null,CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                operation,identity,null);
        return CpfContexts.bind(CpfContextSnapshot.capture(root));
    }

    public void assertClear() { if (CpfContexts.snapshot()!=null) throw new AssertionError("CPF context leak"); }
}
