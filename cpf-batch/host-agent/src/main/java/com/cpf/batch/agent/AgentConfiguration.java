package com.cpf.batch.agent;

import com.cpf.batch.agent.internal.ArtifactInstaller;
import com.cpf.batch.agent.internal.ArtifactStateStore;
import com.cpf.batch.agent.internal.ArtifactVerifier;
import com.cpf.batch.agent.internal.LogArchiveService;
import com.cpf.batch.agent.internal.RuntimeControlProxy;
import com.cpf.batch.agent.internal.ServiceManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AgentConfiguration {
    @Bean
    ArtifactVerifier artifactVerifier(AgentProperties properties) {
        return new ArtifactVerifier(properties);
    }

    @Bean
    ArtifactStateStore artifactStateStore(AgentProperties properties) {
        return new ArtifactStateStore(properties);
    }

    @Bean
    ArtifactInstaller artifactInstaller(
            AgentProperties properties, ArtifactVerifier verifier, ArtifactStateStore stateStore) {
        return new ArtifactInstaller(properties, verifier, stateStore);
    }

    @Bean
    ServiceManager serviceManager(AgentProperties properties) {
        return new ServiceManager(properties);
    }

    @Bean
    RuntimeControlProxy runtimeControlProxy(AgentProperties properties, RestClient.Builder builder) {
        return new RuntimeControlProxy(properties, builder);
    }

    @Bean
    LogArchiveService logArchiveService(AgentProperties properties) {
        return new LogArchiveService(properties);
    }

    @Bean
    AgentCommandLedger agentCommandLedger(AgentProperties properties, ObjectMapper mapper) {
        return new AgentCommandLedger(properties, mapper);
    }

    @Bean
    AgentProductionSafetyValidator safety(
            org.springframework.core.env.Environment environment, AgentProperties properties) {
        return new AgentProductionSafetyValidator(environment, properties);
    }
}
