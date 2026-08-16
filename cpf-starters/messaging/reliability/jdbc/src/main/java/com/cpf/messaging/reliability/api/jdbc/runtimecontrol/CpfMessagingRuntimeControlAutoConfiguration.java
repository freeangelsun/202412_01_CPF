package com.cpf.messaging.reliability.api.jdbc.runtimecontrol;

import com.cpf.messaging.api.CpfBrokerConsumerControlPort;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.messaging.reliability.api.jdbc.internal.CpfBrokerConsumerRuntimePolicy;
import com.cpf.messaging.reliability.api.jdbc.internal.CpfBrokerConsumerWorker;
import com.cpf.messaging.spi.broker.CpfBrokerDlqPort;
import com.cpf.messaging.spi.broker.CpfBrokerInboxPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
/** CpfMessagingRuntimeControlAutoConfiguration는 메시징 신뢰성 경계에서 중복 방지·재시도·결과불명 복구 책임을 명확히 수행합니다. */
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
