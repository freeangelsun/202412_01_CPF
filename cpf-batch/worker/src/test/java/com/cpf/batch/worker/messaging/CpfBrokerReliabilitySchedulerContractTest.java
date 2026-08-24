package com.cpf.batch.worker.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

class CpfBrokerReliabilitySchedulerContractTest {
    @Test
    void schedulerExistsOnlyWhenTheIndependentReliabilityCapabilityIsEnabled() {
        ConditionalOnProperty condition = CpfBrokerReliabilityScheduler.class
                .getAnnotation(ConditionalOnProperty.class);

        assertThat(condition).isNotNull();
        assertThat(condition.prefix()).isEqualTo("cpf.messaging.reliability");
        assertThat(condition.name()).containsExactly("enabled");
        assertThat(condition.havingValue()).isEqualTo("true");
        assertThat(condition.matchIfMissing()).isTrue();
    }
}
