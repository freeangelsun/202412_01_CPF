package com.cpf.admin.config;

import com.cpf.admin.approval.owner.DataQualityCorrectionApprovalOwnerCommandAdapter;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.cpf.admin.approval.security.AdmDataQualityApprovalProofService;
import com.cpf.admin.approval.security.AdmApprovalCapabilityNonceRepository;
import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.admin.opr.integration.AdmIntegrationClosureService;
import com.cpf.data.quality.InMemoryCpfDataQualityOperations;
import com.cpf.security.common.security.crypto.JceCpfCryptoOperations;
import com.cpf.foundation.time.SystemCpfTimeOperations;
import com.cpf.data.api.quality.CpfDataQualityOperations;
import com.cpf.admin.approval.security.AdmDataQualityCorrectionGateway;
import com.cpf.data.spi.quality.CpfDataQualityCorrectionPort;
import com.cpf.security.spi.CpfSecretProvider;
import com.cpf.security.api.crypto.CpfCryptoOperations;
import com.cpf.security.api.crypto.CpfCryptoPolicy;
import com.cpf.foundation.time.CpfTimeOperations;
import com.cpf.integration.api.webhook.CpfWebhookOperations;
import com.cpf.integration.webhook.CpfWebhookEndpointValidator;
import com.cpf.integration.webhook.InMemoryCpfWebhookOperations;
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
// Harness 26.2 — ADMUI-046(CRITICAL)은 mandatory ADM route다. 이 Configuration을 ADM
// Composition에 포함한 경우에는 항상 여덟 개의 운영 Route를 제공한다. `enabled` 같은 YAML
// switch로 Controller/Operation을 숨기면 OpenAPI·Frontend·운영 절차의 제품 계약을 축소하므로
// 허용하지 않는다. 실제 Provider가 없는 protected Runtime은 route를 삭제하지 않고 fail-closed로
// 기동을 거부해야 한다.
/** AdmIntegrationClosureConfiguration 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class AdmIntegrationClosureConfiguration {


    @Bean
    @ConditionalOnMissingBean(AdmDataQualityApprovalProofService.class)
    AdmDataQualityApprovalProofService admDataQualityApprovalProofService(
            AdmIntegrationClosureProperties properties,
            ObjectProvider<CpfSecretProvider> secretProvider,
            Environment environment,
            AdmApprovalCapabilityNonceRepository nonceRepository) {
        return new AdmDataQualityApprovalProofService(resolveApprovalProofKey(
                properties.getApprovalProofKeyBase64(), properties.getApprovalProofKeyRef(),
                secretProvider.getIfAvailable(), environment),
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
    // 이 Bean 은 이미 @Profile({"local","dev"}) 로 좁혀져 있다. 기능을 기본 제공으로 바꾼 이상
    // local/dev 에서는 임시 Provider 도 기본값이어야 한다. 그렇지 않으면 위 Javadoc 대로
    // "Provider 없음"으로 Context 생성이 실패해 1-WAS 가 아예 기동하지 못한다.
    // 고객 Provider 가 있으면 @ConditionalOnMissingBean 으로 자동 양보한다.
    @ConditionalOnProperty(
            prefix = "cpf.adm.integration-closure",
            name = "ephemeral-providers-enabled",
            havingValue = "true", matchIfMissing = true)
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
    // 이 Bean 은 이미 @Profile({"local","dev"}) 로 좁혀져 있다. 기능을 기본 제공으로 바꾼 이상
    // local/dev 에서는 임시 Provider 도 기본값이어야 한다. 그렇지 않으면 위 Javadoc 대로
    // "Provider 없음"으로 Context 생성이 실패해 1-WAS 가 아예 기동하지 못한다.
    // 고객 Provider 가 있으면 @ConditionalOnMissingBean 으로 자동 양보한다.
    @ConditionalOnProperty(
            prefix = "cpf.adm.integration-closure",
            name = "ephemeral-providers-enabled",
            havingValue = "true", matchIfMissing = true)
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
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
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

    /** 승인 증명 키 환경변수의 canonical 이름입니다. */
    private static final String APPROVAL_PROOF_KEY_ENV = "CPF_ADM_APPROVAL_PROOF_KEY_BASE64";

    /**
     * 승인 증명 키를 해석합니다. prod/stg 는 종전대로 secret-ref + provider 만 허용합니다.
     *
     * <p>기본 제공으로 전환하면서 "조립 Runtime 이 ADM 내부 secret 요구사항을 알아야 하는" 구조가
     * 되지 않도록, 비보호 profile 에서는 canonical 환경변수를 직접 읽고 그마저 없으면 이 실행
     * instance 에서만 유효한 임시 키를 만든다. 임시 키는 재기동 시 달라지므로 그 사이 발급된
     * correction approval 증명은 무효가 된다 — local/dev 의 의미로는 올바른 동작이다.</p>
     */
    private static String resolveApprovalProofKey(
            String rawValue, String secretRef, CpfSecretProvider provider, Environment environment) {
        if (isProtectedProfile(environment) || (secretRef != null && !secretRef.isBlank())
                || (rawValue != null && !rawValue.isBlank())) {
            return resolveSecret(rawValue, secretRef, provider, environment, "approval-proof-key");
        }
        String fromEnvironment = environment.getProperty(APPROVAL_PROOF_KEY_ENV);
        if (fromEnvironment != null && !fromEnvironment.isBlank()) return fromEnvironment.trim();
        byte[] ephemeral = new byte[32];
        new java.security.SecureRandom().nextBytes(ephemeral);
        return Base64.getEncoder().encodeToString(ephemeral);
    }

    private static boolean isProtectedProfile(Environment environment) {
        return Set.of(environment.getActiveProfiles()).stream().anyMatch(Set.of("prod", "stg")::contains);
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
