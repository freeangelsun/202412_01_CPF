package com.cpf.batch.agent;

import com.cpf.batch.api.ActualState;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRuntimeStateProviderTest {
    @Test
    void emptyApprovedCatalogIsFailClosed() {
        AgentProperties properties = new AgentProperties();
        AgentRuntimeStateProvider provider = new AgentRuntimeStateProvider(properties);

        assertThat(provider.actualState()).isEqualTo(ActualState.DEGRADED);
        assertThat(provider.ready()).isFalse();
        assertThat(provider.availableCapacity()).isZero();
    }

    @Test
    void configuredApprovedCatalogIsReady() {
        AgentProperties properties = new AgentProperties();
        AgentProperties.ServiceDefinition service = new AgentProperties.ServiceDefinition();
        service.setServiceId("cpf-batch-worker");
        properties.setServices(Map.of("worker", service));
        AgentRuntimeStateProvider provider = new AgentRuntimeStateProvider(properties);

        assertThat(provider.actualState()).isEqualTo(ActualState.READY);
        assertThat(provider.ready()).isTrue();
        assertThat(provider.availableCapacity()).isEqualTo(1);
    }
}
