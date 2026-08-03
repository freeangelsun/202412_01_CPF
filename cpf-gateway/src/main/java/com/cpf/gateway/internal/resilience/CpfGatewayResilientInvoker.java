package com.cpf.gateway.internal.resilience;

import com.cpf.core.api.resilience.*;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Gateway adapter that applies the same operation policy used by HTTP and TCP clients. */
public final class CpfGatewayResilientInvoker {
    private final CpfResilienceExecutor executor;
    public CpfGatewayResilientInvoker(CpfResilienceExecutor executor){this.executor=Objects.requireNonNull(executor);}
    public <T> CpfResilienceOutcome<T> invoke(String routeId,String transactionId,String idempotencyKey,Supplier<T> downstream){
        return executor.execute(new CpfResilienceCallContext("gateway."+routeId,transactionId,idempotencyKey,Instant.now(),Map.of("consumer","gateway","route",routeId)),downstream);
    }
}
