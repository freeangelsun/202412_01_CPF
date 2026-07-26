package com.cpf.batch.agent;

import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.batch.api.RuntimeRole;
import com.cpf.batch.runtime.RuntimeCommonConfiguration;
import com.cpf.batch.runtime.RuntimeIdentityFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@SpringBootApplication(scanBasePackages = "com.cpf.batch.agent")
@EnableConfigurationProperties(AgentProperties.class)
@Import(RuntimeCommonConfiguration.class)
public class BatchHostAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchHostAgentApplication.class, args);
    }

    @Bean
    RuntimeRegistration runtimeRegistration(Environment environment) {
        return RuntimeIdentityFactory.fromEnvironment(
                environment, RuntimeRole.HOST_AGENT, "cpf-batch-host-agent", 8184);
    }

    @Bean
    ApprovedCommandCatalog approvedCommandCatalog() {
        return new ApprovedCommandCatalog();
    }
}
