package com.cpf.core.service.reliability;

import com.cpf.core.api.feature.CpfFeatureFlags;
import com.cpf.core.api.reliability.CpfFaultInjector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
public class CpfFaultInjectionConfiguration {
    @Bean
    @Profile({"test", "verification", "chaos"})
    @ConditionalOnProperty(name = "cpf.fault-injection.enabled", havingValue = "true")
    CpfFaultInjector cpfControlledFaultInjector(CpfFeatureFlags flags, Environment environment) {
        return new CpfControlledFaultInjector(
                flags,
                environment.getProperty("cpf.fault-injection.targets", ""),
                environment.getProperty("cpf.fault-injection.delay-millis", Long.class, 0L),
                environment.getProperty("cpf.fault-injection.throw", Boolean.class, false));
    }

    @Bean
    @ConditionalOnMissingBean(CpfFaultInjector.class)
    CpfFaultInjector cpfNoopFaultInjector() {
        return new CpfNoopFaultInjector();
    }
}
