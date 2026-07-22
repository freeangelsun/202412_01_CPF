package com.cpf.core.common.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CpfBrokerBridgeAdapterTest {

    @Test
    void 인메모리_발행은_구독자와_최근이력에_같은_봉투를_전달한다() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.broker.type", "IN_MEMORY")
                .withProperty("cpf.broker.enabled", "true");
        CpfBrokerBridgeAdapter adapter = new CpfBrokerBridgeAdapter(
                environment,
                new ObjectMapper(),
                provider(),
                provider());
        List<CpfBrokerBridgeMessage> consumed = new CopyOnWriteArrayList<>();
        adapter.subscribe("cpf.edu", consumed::add);

        CpfBrokerBridgeResult result = adapter.publish(
                "cpf.edu",
                "KEY-1",
                Map.of("status", "READY"),
                Map.of("X-Cpf-Ext-Edu", "Y"));

        assertThat(result.success()).isTrue();
        assertThat(result.broker()).isEqualTo("IN_MEMORY");
        assertThat(consumed).hasSize(1);
        assertThat(consumed.getFirst().headers()).containsEntry("X-Cpf-Ext-Edu", "Y");
        assertThat(adapter.findRecent("cpf.edu", 10)).hasSize(1);
    }

    @Test
    void 비활성화된_bridge는_발행과_이력기록을_수행하지_않는다() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.broker.type", "IN_MEMORY")
                .withProperty("cpf.broker.enabled", "false");
        CpfBrokerBridgeAdapter adapter = new CpfBrokerBridgeAdapter(
                environment,
                new ObjectMapper(),
                provider(),
                provider());

        CpfBrokerBridgeResult result = adapter.publish("cpf.edu", "KEY-2", Map.of(), Map.of());

        assertThat(result.success()).isFalse();
        assertThat(adapter.findRecent(null, 10)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectProvider<T> provider() {
        return mock(ObjectProvider.class);
    }
}
