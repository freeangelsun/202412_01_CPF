package com.cpf.security;

import com.cpf.security.api.CpfPasswordRuntimePolicy;
import com.cpf.security.api.password.CpfPasswordEncoder;
import com.cpf.security.internal.password.CpfPasswordHashingPort;
import com.cpf.security.internal.password.CpfPasswordEncoderAdapter;
import com.cpf.security.internal.password.CpfPbkdf2PasswordHasher;
import java.util.Arrays;
import java.util.Set;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;

/**
 * ADM/MBW와 일반 BFF가 동일한 Public 비밀번호 서비스를 안전하게 조립합니다.
 * Secret pepper는 환경/Secret provider에서만 읽고 설정 객체나 로그/Evidence에 원문을 보관하지 않습니다.
 */
@AutoConfiguration
@EnableConfigurationProperties(CpfPasswordHashingProperties.class)
@ConditionalOnProperty(prefix = "cpf.security.password-hashing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CpfPasswordAutoConfiguration {
    private static final Set<String> PRODUCT_PROFILES = Set.of("prod", "stg", "qa", "dr");

    @Bean
    @ConditionalOnMissingBean
    CpfPasswordRuntimePolicy cpfPasswordRuntimePolicy() {
        return new CpfPasswordRuntimePolicy();
    }

    @Bean
    @ConditionalOnMissingBean(CpfPasswordHashingPort.class)
    CpfPasswordHashingPort cpfPasswordHashingPort(CpfPasswordHashingProperties properties, Environment environment) {
        properties.validate();
        String secret = environment.getProperty(properties.getPepperEnvironmentVariable());
        boolean productProfile = Arrays.stream(environment.getActiveProfiles())
                .map(String::toLowerCase)
                .anyMatch(PRODUCT_PROFILES::contains);
        if ((properties.isRequirePepper() || productProfile) && (secret == null || secret.isBlank())) {
            throw new IllegalStateException("CPF password pepper secret is required for this runtime profile");
        }
        char[] pepper = secret == null ? new char[0] : secret.toCharArray();
        try {
            return new CpfPbkdf2PasswordHasher(properties.getIterations(), properties.getKeyLengthBits(), pepper);
        } finally {
            Arrays.fill(pepper, '\0');
        }
    }

    @Bean
    @ConditionalOnMissingBean(CpfPasswordEncoder.class)
    CpfPasswordEncoder cpfPasswordEncoder(
            CpfPasswordHashingPort hashingPort,
            ObjectProvider<CpfPasswordRuntimePolicy> runtimePolicyProvider) {
        return new CpfPasswordEncoderAdapter(hashingPort, runtimePolicyProvider);
    }
}
