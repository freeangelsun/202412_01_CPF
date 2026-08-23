package com.cpf.integration.resilience.runtime;

import com.cpf.data.lock.api.CpfLockManager;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.integration.resilience.internal.CpfResilienceEngine;
import com.cpf.integration.resilience.spi.*;
import com.cpf.platform.operations.observability.api.CpfTelemetry;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Integration Developer Annotation과 기존 Resilience Engine을 조립하는 Provider-neutral AutoConfiguration입니다. */
@AutoConfiguration
@EnableConfigurationProperties(CpfIntegrationAnnotationProperties.class)
public class CpfIntegrationResilienceAutoConfiguration {
    @Bean @ConditionalOnMissingBean CpfAnnotationResiliencePolicyRegistry cpfAnnotationResiliencePolicyRegistry(){return new CpfAnnotationResiliencePolicyRegistry();}
    @Bean @ConditionalOnMissingBean CpfIntegrationAnnotationPolicyFactory cpfIntegrationAnnotationPolicyFactory(CpfIntegrationAnnotationProperties p){return new CpfIntegrationAnnotationPolicyFactory(p);}
    @Bean @ConditionalOnMissingBean CpfResilienceFailureClassifier cpfResilienceFailureClassifier(){return new CpfDefaultResilienceFailureClassifier();}
    @Bean @ConditionalOnMissingBean CpfResilienceRuntimePolicyResolver cpfResilienceRuntimePolicyResolver(){return CpfResilienceRuntimePolicyResolver.legacyCompatible();}
    @Bean @ConditionalOnMissingBean CpfResilienceAuditSink cpfResilienceAuditSink(){return new CpfSafeResilienceAuditSink();}
    @Bean @ConditionalOnMissingBean CpfResilienceEngine cpfResilienceEngine(CpfAnnotationResiliencePolicyRegistry annotationPolicies,
            ObjectProvider<CpfResiliencePolicyStore> durablePolicies,CpfResilienceFailureClassifier classifier,
            CpfResilienceAuditSink audit,CpfResilienceRuntimePolicyResolver runtimePolicyResolver,
            ObjectProvider<CpfLockManager> locks,ObjectProvider<CpfTelemetry> telemetry,CpfContextExecutionFactory contextFactory,
            ObjectProvider<Clock> clocks,CpfIntegrationAnnotationProperties properties){
        ArrayList<CpfResiliencePolicyResolver> resolvers=new ArrayList<>();
        CpfResiliencePolicyStore durable=durablePolicies.getIfAvailable(); if(durable!=null)resolvers.add(durable);resolvers.add(annotationPolicies);
        Clock clock=clocks.getIfUnique(Clock::systemUTC);CpfTelemetry tel=telemetry.getIfAvailable(CpfTelemetry::noop);
        return new CpfResilienceEngine(new CpfCompositeResiliencePolicyResolver(resolvers),classifier,audit,runtimePolicyResolver,
                locks.getIfAvailable(),tel,contextFactory,clock,Executors.newVirtualThreadPerTaskExecutor(),Math::random,System::nanoTime,10_000,Duration.ofMinutes(30));
    }
    @Bean @ConditionalOnMissingBean CpfIntegrationClientCoordinator cpfIntegrationClientCoordinator(CpfIntegrationAnnotationProperties p,
            CpfIntegrationAnnotationPolicyFactory f,CpfAnnotationResiliencePolicyRegistry r,CpfResilienceEngine e,ObjectProvider<Clock> clocks){
        return new CpfIntegrationClientCoordinator(p,f,r,e,clocks.getIfUnique(Clock::systemUTC));
    }
    @Bean @ConditionalOnMissingBean CpfIntegrationClientAspect cpfIntegrationClientAspect(CpfIntegrationClientCoordinator c){return new CpfIntegrationClientAspect(c);}
}
