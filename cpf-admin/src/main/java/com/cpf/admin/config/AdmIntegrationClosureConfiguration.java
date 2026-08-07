package com.cpf.admin.config;

import com.cpf.admin.approval.owner.DataQualityCorrectionApprovalOwnerCommandAdapter;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.cpf.admin.approval.security.AdmDataQualityApprovalProofService;
import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.admin.opr.integration.AdmIntegrationClosureService;
import com.cpf.common.data.quality.InMemoryCpfDataQualityOperations;
import com.cpf.common.security.crypto.JceCpfCryptoOperations;
import com.cpf.common.time.SystemCpfTimeOperations;
import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;
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
import org.springframework.context.annotation.Configuration;

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
    AdmDataQualityApprovalProofService admDataQualityApprovalProofService(AdmIntegrationClosureProperties properties) {
        return new AdmDataQualityApprovalProofService(
                require(properties.getApprovalProofKeyBase64(), "approval-proof-key-base64"));
    }

    @Bean
    @ConditionalOnMissingBean(CpfTimeOperations.class)
    CpfTimeOperations cpfTimeOperations() {
        return new SystemCpfTimeOperations();
    }

    @Bean
    @ConditionalOnMissingBean(value = {CpfDataQualityOperations.class, CpfDataQualityCorrectionPort.class})
    @ConditionalOnProperty(
            prefix = "cpf.adm.integration-closure",
            name = "ephemeral-providers-enabled",
            havingValue = "true")
    InMemoryCpfDataQualityOperations cpfDataQualityOperations(AdmDataQualityApprovalProofService proofService) {
        return new InMemoryCpfDataQualityOperations(proofService::verify);
    }

    @Bean
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
    CpfCryptoOperations cpfCryptoOperations(AdmIntegrationClosureProperties properties) {
        String version = require(properties.getCrypto().getActiveKeyVersion(), "crypto.active-key-version");
        String encoded = require(properties.getCrypto().getActiveKeyBase64(), "crypto.active-key-base64");
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
    @ConditionalOnBean(CpfDataQualityCorrectionPort.class)
    @ConditionalOnMissingBean(DataQualityCorrectionApprovalOwnerCommandAdapter.class)
    DataQualityCorrectionApprovalOwnerCommandAdapter dataQualityCorrectionApprovalOwnerCommandAdapter(
            CpfDataQualityCorrectionPort correctionPort,
            CpfDataQualityOperations quality,
            ObjectMapper objectMapper,
            AdmApprovalRepository repository,
            AdmApprovalSnapshotIntegrity snapshotIntegrity,
            AdmDataQualityApprovalProofService proofService) {
        return new DataQualityCorrectionApprovalOwnerCommandAdapter(
                correctionPort, quality, objectMapper, repository, snapshotIntegrity, proofService);
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
            CpfDataQualityCorrectionPort correctionPort,
            CpfTimeOperations time,
            CpfWebhookOperations webhook,
            AdmApprovalService approvals,
            ObjectMapper objectMapper,
            AdmIntegrationClosureProperties properties) {
        java.util.Objects.requireNonNull(correctionPort, "data-quality correction provider");
        return new AdmIntegrationClosureService(
                crypto.getIfAvailable(),
                quality,
                time,
                webhook,
                approvals,
                objectMapper,
                properties.getCorrectionApprovalTtl());
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalStateException(field + " is required");
        return value.trim();
    }
}
