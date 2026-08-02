package com.cpf.starter.observability.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.common.logging.CpfTraceSamplingPolicy;
import com.cpf.core.common.logging.DynamicTransactionLogLevelService;
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
            DynamicTransactionLogLevelService service) {
        return new CpfDynamicLogLevelRuntimeApplier(service);
    }

    @Bean(name = "cpfMaskingPolicyRuntimeApplier")
    @ConditionalOnMissingBean(name = "cpfMaskingPolicyRuntimeApplier")
    CpfRuntimeChangeApplier maskingPolicyRuntimeApplier() {
        return new CpfMaskingPolicyRuntimeApplier();
    }
}
