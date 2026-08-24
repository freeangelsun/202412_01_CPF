package com.cpf.batch.centercut.runner;

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

@SpringBootApplication(scanBasePackages = "com.cpf.batch.centercut.runner")
@Import({RuntimeCommonConfiguration.class, BatDataSourceConfiguration.class})
public class CenterCutApplication {
    public static void main(String[] args) {
        SpringApplication.run(CenterCutApplication.class, args);
    }

    @Bean
    RuntimeRegistration runtimeRegistration(Environment environment) {
        return RuntimeIdentityFactory.fromBatchEnvironment(
                environment, RuntimeRole.CENTER_CUT, 8183);
    }
}
