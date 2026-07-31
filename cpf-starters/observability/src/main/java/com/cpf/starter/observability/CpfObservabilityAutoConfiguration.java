package com.cpf.starter.observability;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ObservationRegistry.class)
public class CpfObservabilityAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfObservationSupport cpfObservationSupport(ObservationRegistry registry) { return new CpfObservationSupport(registry); }
}
