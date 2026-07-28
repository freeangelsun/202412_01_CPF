package com.cpf.batch.runtime;

import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.batch.spi.BusinessJobProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
@EnableScheduling
@Import({DefaultBusinessCalendarConfiguration.class, CenterCutParameterProtector.class})
public class RuntimeCommonConfiguration {
    @Bean
    @ConditionalOnMissingBean
    RuntimeStateProvider runtimeStateProvider() {
        return new RuntimeStateProvider() {};
    }

    @Bean
    RuntimeSmokeController runtimeSmokeController(RuntimeRegistration registration, RuntimeStateProvider state) {
        return new RuntimeSmokeController(registration, state);
    }

    @Bean
    RuntimeReporter runtimeReporter(
            RuntimeRegistration registration,
            RuntimeStateProvider state,
            RestClient.Builder builder,
            @Value("${cpf.batch.control.base-url:${CPF_BATCH_CONTROL_BASE_URL:http://127.0.0.1:8180}}")
            String controlBaseUrl) {
        return new RuntimeReporter(registration, state, builder, controlBaseUrl);
    }

    @Bean
    @ConditionalOnMissingBean
    BatchRuntimePolicy batchRuntimePolicy() {
        return new BatchRuntimePolicy();
    }

    @Bean
    JobPackCatalog jobPackCatalog(List<BusinessJobProvider> providers) {
        return new JobPackCatalog(providers);
    }
}
