package com.cpf.core.config;

import com.cpf.core.common.logging.file.CpfLogPathPolicy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Logback 초기화 전에 CPF 로그 절대 root와 실행 식별값을 확정합니다.
 *
 * <p>애플리케이션 bean 생성보다 먼저 실행되므로, 공통 Logback과 CPF 파일 writer가
 * 같은 환경·모듈·인스턴스 경로를 사용합니다.</p>
 */
public class CpfLogEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final String PROPERTY_SOURCE_NAME = "cpfResolvedLogEnvironment";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        CpfLogPathPolicy policy = new CpfLogPathPolicy(environment);
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("cpf.logging.file.base-path", policy.logRoot().toString());
        properties.put("cpf.environment", policy.environmentCode());
        properties.put("cpf.framework.instance-id", policy.instanceId());
        properties.put("cpf.framework.module-id", policy.runtimeModuleCode());
        properties.put("cpf.logging.runtime-module-path",
                policy.runtimeModuleCode().toLowerCase(Locale.ROOT));
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
