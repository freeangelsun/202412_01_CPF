package com.cpf.batch.worker;

import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.batch.api.RuntimeRole;
import com.cpf.batch.runtime.BatDataSourceConfiguration;
import com.cpf.batch.runtime.JobPackCatalog;
import com.cpf.batch.runtime.RuntimeCommonConfiguration;
import com.cpf.batch.runtime.RuntimeIdentityFactory;
import com.cpf.batch.centercut.runtime.CenterCutWorkRuntimeConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

@EnableConfigurationProperties(WorkerOperationalProperties.class)
@SpringBootApplication(scanBasePackages = "com.cpf.batch.worker")
@Import({RuntimeCommonConfiguration.class, BatDataSourceConfiguration.class,
        CenterCutWorkRuntimeConfiguration.class})
public class BatchWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchWorkerApplication.class, args);
    }

    @Bean
    JobPackReporter jobPackReporter(
            JobPackCatalog catalog,
            RestClient.Builder builder,
            @Value("${cpf.batch.control.base-url:${CPF_BATCH_CONTROL_BASE_URL:http://127.0.0.1:8180}}")
            String controlBaseUrl) {
        return new JobPackReporter(catalog, builder, controlBaseUrl);
    }

    @Bean
    RuntimeRegistration runtimeRegistration(Environment environment) {
        return RuntimeIdentityFactory.fromBatchEnvironment(
                environment, RuntimeRole.WORKER, 8182);
    }
}
