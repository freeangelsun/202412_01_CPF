package com.cpf.starter.security.secret;

import com.cpf.core.api.security.secret.CpfSecretProvider;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfSecretAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfSecretProviderRegistry cpfSecretProviderRegistry(List<CpfSecretProvider> providers) {
        if (providers.isEmpty()) throw new IllegalStateException("CPF Product runtime requires an approved customer-managed SecretProvider.");
        return new CpfSecretProviderRegistry(providers);
    }
}
