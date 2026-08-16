package com.cpf.batch.agent;

import com.cpf.batch.agent.internal.RuntimeControlProxy;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuntimeControlProxyTest {
    @Test
    void missingRuntimeControlEndpointFailsClosed() {
        AgentProperties properties = new AgentProperties();
        AgentProperties.ServiceDefinition service = new AgentProperties.ServiceDefinition();
        service.setServiceId("cpf-batch-worker");
        properties.setServices(Map.of("worker", service));
        RuntimeControlProxy proxy = new RuntimeControlProxy(properties, RestClient.builder());

        assertThatThrownBy(() -> proxy.invoke("cpf-batch-worker", "drain"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void nonLoopbackRuntimeControlEndpointIsRejected() {
        AgentProperties properties = new AgentProperties();
        AgentProperties.ServiceDefinition service = new AgentProperties.ServiceDefinition();
        service.setServiceId("cpf-batch-worker");
        service.setRuntimeControlUrl("https://remote.example:18092");
        properties.setServices(Map.of("worker", service));
        RuntimeControlProxy proxy = new RuntimeControlProxy(properties, RestClient.builder());

        assertThatThrownBy(() -> proxy.invoke("cpf-batch-worker", "drain"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("loopback");
    }
}
