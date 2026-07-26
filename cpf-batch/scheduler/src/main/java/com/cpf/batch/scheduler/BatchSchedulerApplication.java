package com.cpf.batch.scheduler;

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

@SpringBootApplication(scanBasePackages = "com.cpf.batch.scheduler")
@Import({RuntimeCommonConfiguration.class, BatDataSourceConfiguration.class})
public class BatchSchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchSchedulerApplication.class, args);
    }

    @Bean
    RuntimeRegistration runtimeRegistration(Environment environment) {
        return RuntimeIdentityFactory.fromEnvironment(
                environment, RuntimeRole.SCHEDULER, "cpf-batch-scheduler", 8181);
    }
}
