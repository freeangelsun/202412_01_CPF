package com.cpf.security.secret;

import com.cpf.security.api.audit.CpfTamperAuditOperations;
import com.cpf.security.api.audit.CpfTamperAuditStore;
import com.cpf.security.api.crypto.CpfDigitalSignatureOperations;
import com.cpf.security.api.crypto.CpfKeyManagementOperations;
import com.cpf.security.api.crypto.CpfKeyProvider;
import com.cpf.security.api.secret.CpfSecretProvider;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/** Security secret/crypto auto-configuration. Optional crypto capabilities remain zero-footprint without providers. */
@AutoConfiguration
@EnableConfigurationProperties(CpfKeyOperationProperties.class)
public class CpfSecretAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfSecretProviderRegistry cpfSecretProviderRegistry(List<CpfSecretProvider> providers) {
        if (providers.isEmpty()) throw new IllegalStateException("CPF Product runtime requires an approved customer-managed SecretProvider.");
        return new CpfSecretProviderRegistry(providers);
    }

    @Bean(destroyMethod = "close") @ConditionalOnBean(CpfKeyProvider.class) @ConditionalOnMissingBean
    CpfKeyTimeoutExecutor cpfKeyTimeoutExecutor(CpfKeyOperationProperties properties) { return new CpfKeyTimeoutExecutor(properties.getOperationTimeout()); }

    @Bean @ConditionalOnBean(CpfKeyProvider.class) @ConditionalOnMissingBean(CpfDigitalSignatureOperations.class)
    CpfDigitalSignatureOperations cpfDigitalSignatureOperations(ApplicationContext context, CpfKeyTimeoutExecutor timeout) {
        return new CpfDigitalSignatureService(keyProviders(context), timeout, java.time.Clock.systemUTC());
    }

    @Bean @ConditionalOnBean(CpfKeyProvider.class) @ConditionalOnMissingBean(CpfKeyManagementOperations.class)
    CpfKeyManagementOperations cpfKeyManagementOperations(ApplicationContext context, CpfKeyTimeoutExecutor timeout) {
        return new CpfKeyManagementService(keyProviders(context), timeout);
    }

    @Bean
    @ConditionalOnBean({CpfTamperAuditStore.class, CpfDigitalSignatureOperations.class})
    @ConditionalOnMissingBean(CpfTamperAuditOperations.class)
    @ConditionalOnProperty(prefix = "cpf.security.audit", name = "tamper-evident-enabled", havingValue = "true")
    CpfTamperAuditOperations cpfTamperAuditOperations(CpfTamperAuditStore store, CpfDigitalSignatureOperations signatures,
        @Value("${cpf.security.audit.signing-key-id}") String keyId,
        @Value("${cpf.security.audit.signature-algorithm:SHA256withRSA}") String algorithm) {
        if (keyId == null || keyId.isBlank()) throw new IllegalStateException("cpf.security.audit.signing-key-id is required");
        return new CpfTamperAuditService(store, signatures, keyId, algorithm);
    }

    private static Map<String,CpfKeyProvider> keyProviders(ApplicationContext context) {
        Map<String,CpfKeyProvider> providers=new LinkedHashMap<>(context.getBeansOfType(CpfKeyProvider.class));
        if(providers.isEmpty())throw new IllegalStateException("No CpfKeyProvider is available");
        return providers;
    }
}
