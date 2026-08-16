package com.cpf.integration.ai;

import com.cpf.integration.ai.api.CpfAiOperations;
import com.cpf.integration.ai.api.CpfAiPolicy;
import com.cpf.integration.ai.api.CpfAiProvider;
import com.cpf.integration.ai.api.CpfAiTelemetry;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** AI capability를 활성화했을 때만 provider-neutral router를 조립합니다. */
@AutoConfiguration
@EnableConfigurationProperties(CpfAiProperties.class)
@ConditionalOnProperty(prefix = "cpf.integration.ai", name = "enabled", havingValue = "true")
public class CpfAiAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfAiOperations.class)
    CpfAiOperations cpfAiOperations(
            List<CpfAiProvider> providers,
            CpfAiPolicy policy,
            CpfAiProperties properties,
            CpfContextExecutionFactory contextFactory,
            CpfAiTelemetry telemetry) {
        return new CpfAiRouter(providers, policy, properties, contextFactory,
                new CpfAiResourceLimiter(properties, java.time.Clock.systemUTC()), telemetry);
    }
    @Bean
    @ConditionalOnMissingBean(CpfAiTelemetry.class)
    CpfAiTelemetry cpfAiTelemetry() { return CpfAiTelemetry.NOOP; }

}
