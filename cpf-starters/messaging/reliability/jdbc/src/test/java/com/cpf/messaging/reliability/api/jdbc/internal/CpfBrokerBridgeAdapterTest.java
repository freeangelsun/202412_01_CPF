package com.cpf.messaging.reliability.api.jdbc.internal;

import com.cpf.messaging.spi.broker.*;
import com.cpf.messaging.api.CpfBrokerBridgeMessage;
import com.cpf.messaging.api.CpfBrokerBridgeResult;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.messaging.context.CpfMessageBridgeContextSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
class CpfBrokerBridgeAdapterTest {
    private static final Instant NOW = Instant.parse("2026-08-22T07:00:00Z");
    private AutoCloseable contextScope;
    private CpfMessageBridgeContextSupport contextSupport;

    @BeforeEach
    void bindContext() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        CpfExecutionIdGenerator ids = new CpfExecutionIdGenerator() {
            private int sequence;
            @Override public String newExecutionId() { return "bridge-execution-" + (++sequence); }
            @Override public String newSegmentId() { return "bridge-segment-1"; }
        };
        CpfContextExecutionFactory factory = new CpfContextExecutionFactory(
                () -> "bridge-transaction-1", ids, () -> LocalDate.of(2026, 8, 22), clock);
        contextScope = CpfContexts.bind(CpfContextSnapshot.capture(
                factory.newRoot(null, "messaging.bridge.test", null, null, NOW.plusSeconds(60)), NOW));
        contextSupport = new CpfMessageBridgeContextSupport(ids, clock);
    }

    @AfterEach
    void clearContext() throws Exception {
        if (contextScope != null) contextScope.close();
        assertThat(CpfContexts.current()).isNull();
    }

    @Test
    void 인메모리_발행은_구독자와_최근이력에_같은_봉투를_전달한다() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.broker.type", "IN_MEMORY")
                .withProperty("cpf.broker.enabled", "true");
        CpfBrokerBridgeAdapter adapter = new CpfBrokerBridgeAdapter(environment, contextSupport);
        List<CpfBrokerBridgeMessage> consumed = new CopyOnWriteArrayList<>();
        adapter.subscribe("cpf.edu", consumed::add);

        CpfBrokerBridgeResult result = adapter.publish(
                "cpf.edu",
                "KEY-1",
                Map.of("status", "READY"),
                Map.of("X-Cpf-Ext-Edu", "Y"));

        assertThat(result.accepted()).isTrue();
        assertThat(result.transport()).isEqualTo("IN_MEMORY");
        assertThat(consumed).hasSize(1);
        assertThat(consumed.getFirst().headers()).containsEntry("X-Cpf-Ext-Edu", "Y");
        assertThat(adapter.findRecent("cpf.edu", 10)).hasSize(1);
    }

    @Test
    void 비활성화된_bridge는_발행과_이력기록을_수행하지_않는다() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.broker.type", "IN_MEMORY")
                .withProperty("cpf.broker.enabled", "false");
        CpfBrokerBridgeAdapter adapter = new CpfBrokerBridgeAdapter(environment, contextSupport);

        CpfBrokerBridgeResult result = adapter.publish("cpf.edu", "KEY-2", Map.of(), Map.of());

        assertThat(result.accepted()).isFalse();
        assertThat(adapter.findRecent(null, 10)).isEmpty();
    }
}
