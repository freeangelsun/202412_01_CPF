package com.cpf.testkit.fault;

import com.cpf.platform.operations.api.featureflag.CpfFeatureFlags;
import com.cpf.integration.resilience.api.CpfFaultInjector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * test/verification/chaos profile에서만 CPF Fault Injector를 활성화하는 Testkit 구성입니다.
 * <p>명시적인 {@code cpf.fault-injection.enabled=true}가 없으면 실제 장애 주입 Bean을 만들지 않습니다.
 */
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
