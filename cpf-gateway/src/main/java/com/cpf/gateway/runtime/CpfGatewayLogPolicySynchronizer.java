package com.cpf.gateway.runtime;

import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyResolver;
import com.cpf.platform.operations.api.runtime.CpfRuntimePolicyDistributionPort;
import com.cpf.platform.operations.api.runtime.CpfInstanceIdentity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/** Durable Log Policy Event를 현재 Gateway Instance에 적용하고 ACK합니다. */
@Component
public final class CpfGatewayLogPolicySynchronizer {
    private final CpfRuntimePolicyDistributionPort distribution;
    private final ObjectProvider<CpfLogPolicyResolver> resolvers;
    private final String instanceId;
    public CpfGatewayLogPolicySynchronizer(@Qualifier("gatewayRuntimePolicyDistributionPort") CpfRuntimePolicyDistributionPort distribution,
            ObjectProvider<CpfLogPolicyResolver> resolvers) {
        this.distribution = distribution;
        this.resolvers = resolvers;
        this.instanceId = CpfInstanceIdentity.current().instanceId();
    }

    @Scheduled(fixedDelayString="${cpf.gateway.policy-refresh:15s}")
    public void synchronize(){
        for(var event:distribution.claimPending(instanceId,List.of("LOG_POLICY"),100,60)){
            try{
                CpfLogPolicyResolver resolver=resolvers.getIfAvailable();
                if(resolver==null)throw new IllegalStateException("CpfLogPolicyResolver unavailable");
                String targetType=event.metadata().getOrDefault("targetType",event.aggregateType());
                String targetId=event.metadata().getOrDefault("targetId",event.aggregateId());
                if("*".equals(targetId)||"CACHE_CLEAR".equals(event.action()))resolver.clear();else resolver.refresh(targetType,targetId);
                distribution.acknowledge(new CpfRuntimePolicyDistributionPort.AcknowledgeCommand(event.eventId(),instanceId,event.fencingToken(),"APPLIED","","",OffsetDateTime.now()));
            }catch(RuntimeException ex){
                distribution.acknowledge(new CpfRuntimePolicyDistributionPort.AcknowledgeCommand(event.eventId(),instanceId,event.fencingToken(),"FAILED",ex.getClass().getSimpleName(),ex.getMessage(),OffsetDateTime.now()));
            }
        }
    }
}
