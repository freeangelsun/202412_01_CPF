package com.cpf.admin.config;

import com.cpf.admin.approval.owner.DataQualityCorrectionApprovalOwnerCommandAdapter;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.cpf.admin.approval.security.AdmDataQualityApprovalProofService;
import com.cpf.admin.approval.security.AdmApprovalCapabilityNonceRepository;
import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.admin.opr.integration.AdmIntegrationClosureService;
import com.cpf.common.data.quality.InMemoryCpfDataQualityOperations;
import com.cpf.common.security.crypto.JceCpfCryptoOperations;
import com.cpf.common.time.SystemCpfTimeOperations;
import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.admin.approval.security.AdmDataQualityCorrectionGateway;
import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;
import com.cpf.core.spi.security.CpfSecretProvider;
import com.cpf.core.api.security.crypto.CpfCryptoOperations;
import com.cpf.core.api.security.crypto.CpfCryptoPolicy;
import com.cpf.core.api.time.CpfTimeOperations;
import com.cpf.core.api.webhook.CpfWebhookOperations;
import com.cpf.starter.integration.webhook.CpfWebhookEndpointValidator;
import com.cpf.starter.integration.webhook.InMemoryCpfWebhookOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Base64;
import java.util.Map;
import java.util.Set;

/** Explicit, fail-closed Spring wiring for the integration-closure operational surface. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AdmIntegrationClosureProperties.class)
@ConditionalOnProperty(prefix = "cpf.adm.integration-closure", name = "enabled", havingValue = "true")
public class AdmIntegrationClosureConfiguration {


    @Bean
    @ConditionalOnMissingBean(AdmDataQualityApprovalProofService.class)
    AdmDataQualityApprovalProofService admDataQualityApprovalProofService(
            AdmIntegrationClosureProperties properties,
            ObjectProvider<CpfSecretProvider> secretProvider,
            Environment environment,
            AdmApprovalCapabilityNonceRepository nonceRepository) {
        return new AdmDataQualityApprovalProofService(resolveSecret(
                properties.getApprovalProofKeyBase64(), properties.getApprovalProofKeyRef(),
                secretProvider.getIfAvailable(), environment, "approval-proof-key"),
                nonceRepository, properties.getCorrectionApprovalTtl(), java.time.Clock.systemUTC());
    }

    @Bean
    @ConditionalOnMissingBean(CpfTimeOperations.class)
    CpfTimeOperations cpfTimeOperations() {
        return new SystemCpfTimeOperations();
    }

    @Bean
    @Profile({"local", "dev"})
    @ConditionalOnMissingBean(value = {CpfDataQualityOperations.class, CpfDataQualityCorrectionPort.class})
    @ConditionalOnProperty(
            prefix = "cpf.adm.integration-closure",
            name = "ephemeral-providers-enabled",
            havingValue = "true")
    InMemoryCpfDataQualityOperations cpfDataQualityOperations(AdmDataQualityApprovalProofService proofService) {
        return new InMemoryCpfDataQualityOperations(proofService::verify);
    }

    @Bean
    @ConditionalOnBean(CpfDataQualityCorrectionPort.class)
    @ConditionalOnMissingBean(AdmDataQualityCorrectionGateway.class)
    AdmDataQualityCorrectionGateway cpfDataQualityCorrectionGateway(
            CpfDataQualityCorrectionPort provider,
            AdmDataQualityApprovalProofService proofService) {
        return new AdmDataQualityCorrectionGateway(provider, proofService::verifyAndConsume);
    }

    @Bean
    @Profile({"local", "dev"})
    @ConditionalOnMissingBean(CpfWebhookOperations.class)
    @ConditionalOnProperty(
            prefix = "cpf.adm.integration-closure",
            name = "ephemeral-providers-enabled",
            havingValue = "true")
    CpfWebhookOperations cpfWebhookOperations(AdmIntegrationClosureProperties properties) {
        return new InMemoryCpfWebhookOperations(
                new CpfWebhookEndpointValidator(properties.getWebhook().getAllowedHosts()),
                Map.of(),
                properties.getWebhook().getMaxAttempts(),
                properties.getWebhook().getBaseDelay());
    }

    @Bean
    @ConditionalOnMissingBean(CpfCryptoOperations.class)
    @ConditionalOnProperty(
            prefix = "cpf.adm.integration-closure.crypto",
            name = "enabled",
            havingValue = "true")
    CpfCryptoOperations cpfCryptoOperations(
            AdmIntegrationClosureProperties properties,
            ObjectProvider<CpfSecretProvider> secretProvider,
            Environment environment) {
        String version = require(properties.getCrypto().getActiveKeyVersion(), "crypto.active-key-version");
        String encoded = resolveSecret(properties.getCrypto().getActiveKeyBase64(), properties.getCrypto().getActiveKeyRef(),
                secretProvider.getIfAvailable(), environment, "crypto.active-key");
        byte[] key;
        try {
            key = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException invalid) {
            throw new IllegalStateException("crypto.active-key-base64 must be valid Base64", invalid);
        }
        if (key.length != 32) {
            throw new IllegalStateException("crypto.active-key-base64 must decode to 32 bytes");
        }
        return new JceCpfCryptoOperations(
                Map.of(version, key),
                version,
                new CpfCryptoPolicy(
                        Set.of("AES/GCM/NOPADDING"),
                        Set.of("SUNJCE"),
                        Set.of(),
                        256,
                        true));
    }

    @Bean
    @ConditionalOnBean(AdmDataQualityCorrectionGateway.class)
    @ConditionalOnMissingBean(DataQualityCorrectionApprovalOwnerCommandAdapter.class)
    DataQualityCorrectionApprovalOwnerCommandAdapter dataQualityCorrectionApprovalOwnerCommandAdapter(
            AdmDataQualityCorrectionGateway correctionGateway,
            CpfDataQualityOperations quality,
            ObjectMapper objectMapper,
            AdmApprovalRepository repository,
            AdmApprovalSnapshotIntegrity snapshotIntegrity,
            AdmDataQualityApprovalProofService proofService) {
        return new DataQualityCorrectionApprovalOwnerCommandAdapter(
                correctionGateway, quality, objectMapper, repository, snapshotIntegrity, proofService);
    }

    /**
     * Required operational providers are constructor dependencies. When the feature is enabled
     * without customer providers and ephemeral providers are disabled, context creation fails
     * instead of silently omitting the controller.
     */
    @Bean
    @ConditionalOnMissingBean(AdmIntegrationClosureService.class)
    AdmIntegrationClosureService admIntegrationClosureService(
            ObjectProvider<CpfCryptoOperations> crypto,
            CpfDataQualityOperations quality,
            AdmDataQualityCorrectionGateway correctionGateway,
            CpfTimeOperations time,
            CpfWebhookOperations webhook,
            AdmApprovalService approvals,
            ObjectMapper objectMapper,
            AdmIntegrationClosureProperties properties) {
        java.util.Objects.requireNonNull(correctionGateway, "data-quality correction gateway");
        return new AdmIntegrationClosureService(
                crypto.getIfAvailable(),
                quality,
                time,
                webhook,
                approvals,
                objectMapper,
                properties.getCorrectionApprovalTtl());
    }

    private static String resolveSecret(
            String rawValue, String secretRef, CpfSecretProvider provider,
            Environment environment, String field) {
        Set<String> profiles = Set.of(environment.getActiveProfiles());
        boolean protectedProfile = profiles.stream().anyMatch(Set.of("prod", "stg")::contains);
        if (protectedProfile) {
            if (rawValue != null && !rawValue.isBlank()) {
                throw new IllegalStateException(field + " raw secret property is forbidden in prod/stg");
            }
            String ref = require(secretRef, field + "-ref");
            if (provider == null) throw new IllegalStateException(field + " secret provider is required in prod/stg");
            return require(provider.resolveSecret(ref), field + " resolved secret");
        }
        if (secretRef != null && !secretRef.isBlank()) {
            if (provider == null) throw new IllegalStateException(field + " secret-ref requires a secret provider");
            return require(provider.resolveSecret(secretRef.trim()), field + " resolved secret");
        }
        return require(rawValue, field + "-base64");
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalStateException(field + " is required");
        return value.trim();
    }
}
