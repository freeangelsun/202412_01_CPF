package com.cpf.batch.control;

import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.batch.api.RuntimeRole;
import com.cpf.batch.runtime.BatDataSourceConfiguration;
import com.cpf.batch.runtime.RuntimeCommonConfiguration;
import com.cpf.batch.runtime.RuntimeIdentityFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@SpringBootApplication(scanBasePackages = "com.cpf.batch.control")
@Import({RuntimeCommonConfiguration.class, BatDataSourceConfiguration.class})
public class BatchControlPlaneApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchControlPlaneApplication.class, args);
    }

    @Bean
    RuntimeRegistration runtimeRegistration(Environment environment) {
        return RuntimeIdentityFactory.fromEnvironment(
                environment, RuntimeRole.CONTROL_PLANE, "cpf-batch-control-plane", 8180);
    }
}
