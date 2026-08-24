package com.cpf.batch.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;

class CpfBatchDynamicManagerFlowLifecycleTest {
    @Test
    void startsLateRegisteredFlowAndDestroysItWithItsOwner() {
        try (AnnotationConfigApplicationContext context = context()) {
            IntegrationFlowContext flows = context.getBean(IntegrationFlowContext.class);
            CpfBatchDynamicManagerFlowLifecycle lifecycle =
                    new CpfBatchDynamicManagerFlowLifecycle(context.getBeanFactory(), flows);
            QueueChannel input = new QueueChannel();
            QueueChannel output = new QueueChannel();

            String value = lifecycle.materialize("plan@1:hash", 1, () -> {
                flows.registration(IntegrationFlow.from(input).channel(output).get())
                        .autoStartup(false)
                        .register();
                return "materialized";
            });

            assertThat(value).isEqualTo("materialized");
            assertThat(lifecycle.ownedFlowCount()).isEqualTo(1);
            assertThat(input.send(MessageBuilder.withPayload("reply").build())).isTrue();
            assertThat(output.receive(1_000)).extracting(message -> message.getPayload())
                    .isEqualTo("reply");

            lifecycle.release("plan@1:hash");
            assertThat(lifecycle.ownedFlowCount()).isZero();
            assertThat(flows.getRegistry()).isEmpty();
            assertThat(input.send(MessageBuilder.withPayload("after-release").build())).isTrue();
            assertThat(output.receive(100)).isNull();
        }
    }

    @Test
    void rollsBackLateRegistrationWhenExpectedFlowCountDoesNotMatch() {
        try (AnnotationConfigApplicationContext context = context()) {
            IntegrationFlowContext flows = context.getBean(IntegrationFlowContext.class);
            CpfBatchDynamicManagerFlowLifecycle lifecycle =
                    new CpfBatchDynamicManagerFlowLifecycle(context.getBeanFactory(), flows);
            QueueChannel input = new QueueChannel();
            QueueChannel output = new QueueChannel();

            assertThatThrownBy(() -> lifecycle.materialize("plan@2:hash", 2, () -> {
                flows.registration(IntegrationFlow.from(input).channel(output).get())
                        .autoStartup(false)
                        .register();
                return "invalid";
            })).isInstanceOf(IllegalStateException.class)
                    .hasMessage("BATCH_DYNAMIC_MANAGER_FLOW_COUNT_MISMATCH:expected=2:actual=1");

            assertThat(lifecycle.ownedFlowCount()).isZero();
            assertThat(flows.getRegistry()).isEmpty();
            assertThat(input.send(MessageBuilder.withPayload("after-rollback").build())).isTrue();
            assertThat(output.receive(100)).isNull();
        }
    }

    private static AnnotationConfigApplicationContext context() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(IntegrationConfiguration.class);
        context.refresh();
        return context;
    }

    @EnableIntegration
    static class IntegrationConfiguration {
        @Bean(name = PollerMetadata.DEFAULT_POLLER)
        PollerMetadata defaultPoller() {
            PollerMetadata poller = new PollerMetadata();
            poller.setTrigger(new PeriodicTrigger(Duration.ofMillis(10)));
            poller.setMaxMessagesPerPoll(10);
            poller.setReceiveTimeout(10);
            return poller;
        }
    }
}
