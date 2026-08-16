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
public class BatchAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchAgentApplication.class, args);
    }

    @Bean
    RuntimeRegistration runtimeRegistration(Environment environment) {
        return RuntimeIdentityFactory.fromEnvironment(
                environment, RuntimeRole.AGENT, "cpf-batch-agent", 8184);
    }

    @Bean
    ApprovedCommandCatalog approvedCommandCatalog() {
        return new ApprovedCommandCatalog();
    }
}
