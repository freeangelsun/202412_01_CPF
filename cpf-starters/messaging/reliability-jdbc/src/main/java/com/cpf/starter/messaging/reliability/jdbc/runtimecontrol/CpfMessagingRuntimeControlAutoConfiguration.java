package com.cpf.starter.messaging.reliability.jdbc.runtimecontrol;

import com.cpf.core.api.broker.CpfBrokerConsumerControlPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.starter.messaging.reliability.jdbc.internal.CpfBrokerConsumerRuntimePolicy;
import com.cpf.starter.messaging.reliability.jdbc.internal.CpfBrokerConsumerWorker;
import com.cpf.core.common.broker.CpfBrokerDlqPort;
import com.cpf.core.common.broker.CpfBrokerInboxPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfMessagingRuntimeControlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfBrokerConsumerRuntimePolicy brokerConsumerRuntimePolicy() {
        return new CpfBrokerConsumerRuntimePolicy();
    }

    @Bean
    @ConditionalOnBean({CpfBrokerInboxPort.class, CpfBrokerDlqPort.class})
    @ConditionalOnMissingBean
    CpfBrokerConsumerWorker brokerConsumerWorker(
            CpfBrokerInboxPort inbox,
            CpfBrokerDlqPort dlq,
            CpfBrokerConsumerRuntimePolicy policy) {
        return new CpfBrokerConsumerWorker(inbox, dlq, policy);
    }

    @Bean(name = "cpfBrokerConsumerRuntimeApplier")
    @ConditionalOnBean(CpfBrokerConsumerWorker.class)
    @ConditionalOnMissingBean(name = "cpfBrokerConsumerRuntimeApplier")
    CpfRuntimeChangeApplier brokerConsumerRuntimeApplier(
            CpfBrokerConsumerRuntimePolicy policy,
            ObjectProvider<CpfBrokerConsumerControlPort> controls) {
        return new CpfBrokerConsumerRuntimeApplier(policy, controls.getIfAvailable());
    }

    @Bean(name = "cpfBrokerRetryDlqRuntimeApplier")
    @ConditionalOnBean(CpfBrokerConsumerWorker.class)
    @ConditionalOnMissingBean(name = "cpfBrokerRetryDlqRuntimeApplier")
    CpfRuntimeChangeApplier brokerRetryDlqRuntimeApplier(
            CpfBrokerConsumerRuntimePolicy policy) {
        return new CpfBrokerRetryDlqRuntimeApplier(policy);
    }
}
