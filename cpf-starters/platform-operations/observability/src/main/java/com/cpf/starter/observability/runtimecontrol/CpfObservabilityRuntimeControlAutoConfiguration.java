package com.cpf.starter.observability.runtimecontrol;

import java.time.Clock;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.observability.internal.logging.CpfTraceSamplingPolicy;
import com.cpf.platform.operations.observability.internal.logging.DynamicTransactionLogLevelService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfObservabilityRuntimeControlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfTraceSamplingPolicy traceSamplingPolicy() {
        return new CpfTraceSamplingPolicy();
    }

    @Bean(name = "cpfTraceSamplingRuntimeApplier")
    @ConditionalOnBean(CpfTraceSamplingPolicy.class)
    @ConditionalOnMissingBean(name = "cpfTraceSamplingRuntimeApplier")
    CpfRuntimeChangeApplier traceSamplingRuntimeApplier(CpfTraceSamplingPolicy policy) {
        return new CpfTraceSamplingRuntimeApplier(policy);
    }

    @Bean(name = "cpfDynamicLogLevelRuntimeApplier")
    @ConditionalOnBean(DynamicTransactionLogLevelService.class)
    @ConditionalOnMissingBean(name = "cpfDynamicLogLevelRuntimeApplier")
    CpfRuntimeChangeApplier dynamicLogLevelRuntimeApplier(
            DynamicTransactionLogLevelService service,
            org.springframework.beans.factory.ObjectProvider<Clock> clockProvider) {
        return new CpfDynamicLogLevelRuntimeApplier(service, clockProvider.getIfAvailable(Clock::systemUTC));
    }

    @Bean(name = "cpfMaskingPolicyRuntimeApplier")
    @ConditionalOnMissingBean(name = "cpfMaskingPolicyRuntimeApplier")
    CpfRuntimeChangeApplier maskingPolicyRuntimeApplier() {
        return new CpfMaskingPolicyRuntimeApplier();
    }
}
