
package com.cpf.admin.config;

import com.cpf.admin.approval.owner.DataQualityCorrectionApprovalOwnerCommandAdapter;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalCapabilityNonceRepository;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.admin.opr.controller.AdmIntegrationClosureController;
import com.cpf.admin.opr.integration.AdmIntegrationClosureService;
import com.cpf.data.api.quality.CpfDataQualityOperations;
import com.cpf.security.api.crypto.CpfCryptoOperations;
import com.cpf.data.spi.quality.CpfDataQualityCorrectionPort;
import com.cpf.foundation.time.CpfTimeOperations;
import com.cpf.integration.api.webhook.CpfWebhookOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdmIntegrationClosureConfigurationTest {
    private static final String PROOF_KEY = "cpf.adm.integration-closure.approval-proof-key-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(context -> context.getEnvironment().setActiveProfiles("local"))
            .withUserConfiguration(
                    AdmIntegrationClosureConfiguration.class,
                    AdmIntegrationClosureController.class,
                    Dependencies.class);

    @Test
    void disabledFeatureDoesNotCreateOperationalBeans() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(AdmIntegrationClosureService.class);
            assertThat(context).doesNotHaveBean(AdmIntegrationClosureController.class);
            assertThat(context).doesNotHaveBean(CpfDataQualityOperations.class);
            assertThat(context).doesNotHaveBean(CpfWebhookOperations.class);
        });
    }

    @Test
    void ephemeralLocalModeCreatesSingleDefaultProviderSetAndController() {
        contextRunner.withPropertyValues(
                        "cpf.adm.integration-closure.enabled=true",
                        PROOF_KEY,
                        "cpf.adm.integration-closure.ephemeral-providers-enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(CpfDataQualityOperations.class);
                    assertThat(context).hasSingleBean(CpfTimeOperations.class);
                    assertThat(context).hasSingleBean(CpfWebhookOperations.class);
                    assertThat(context).hasSingleBean(AdmIntegrationClosureService.class);
                    assertThat(context).hasSingleBean(AdmIntegrationClosureController.class);
                    assertThat(context).hasSingleBean(DataQualityCorrectionApprovalOwnerCommandAdapter.class);
                    assertThat(context.getBean(AdmIntegrationClosureService.class).cryptoStatus())
                            .containsEntry("configured", false)
                            .containsEntry("plaintextKeyExposed", false);
                });
    }

    @Test
    void enabledFeatureFailsClosedWhenRequiredProvidersAreMissing() {
        contextRunner.withPropertyValues("cpf.adm.integration-closure.enabled=true", PROOF_KEY)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void customerOverridesWinOverDefaultProviders() {
        CpfDataQualityOperations customQuality = mock(CpfDataQualityOperations.class);
        CpfDataQualityCorrectionPort customCorrection = mock(CpfDataQualityCorrectionPort.class);
        CpfWebhookOperations customWebhook = mock(CpfWebhookOperations.class);
        contextRunner.withBean("customerQuality", CpfDataQualityOperations.class, () -> customQuality)
                .withBean("customerCorrection", CpfDataQualityCorrectionPort.class, () -> customCorrection)
                .withBean("customerWebhook", CpfWebhookOperations.class, () -> customWebhook)
                .withPropertyValues(
                        "cpf.adm.integration-closure.enabled=true",
                        PROOF_KEY,
                        "cpf.adm.integration-closure.ephemeral-providers-enabled=true")
                .run(context -> {
                    assertThat(context.getBean(CpfDataQualityOperations.class)).isSameAs(customQuality);
                    assertThat(context.getBean(CpfWebhookOperations.class)).isSameAs(customWebhook);
                    assertThat(context).hasSingleBean(AdmIntegrationClosureService.class);
                });
    }

    @Test
    void partialCustomerQueryProviderFailsClosedInsteadOfCreatingAnUnpairedCorrectionProvider() {
        contextRunner.withBean("customerQuality", CpfDataQualityOperations.class, () -> mock(CpfDataQualityOperations.class))
                .withBean("customerWebhook", CpfWebhookOperations.class, () -> mock(CpfWebhookOperations.class))
                .withPropertyValues(
                        "cpf.adm.integration-closure.enabled=true",
                        PROOF_KEY,
                        "cpf.adm.integration-closure.ephemeral-providers-enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void partialCustomerCorrectionProviderFailsClosedInsteadOfCreatingADuplicateCorrectionProvider() {
        contextRunner.withBean("customerCorrection", CpfDataQualityCorrectionPort.class, () -> mock(CpfDataQualityCorrectionPort.class))
                .withBean("customerWebhook", CpfWebhookOperations.class, () -> mock(CpfWebhookOperations.class))
                .withPropertyValues(
                        "cpf.adm.integration-closure.enabled=true",
                        PROOF_KEY,
                        "cpf.adm.integration-closure.ephemeral-providers-enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void duplicateCustomerProvidersFailInsteadOfSelectingArbitrarily() {
        contextRunner.withBean("qualityOne", CpfDataQualityOperations.class, () -> mock(CpfDataQualityOperations.class))
                .withBean("qualityTwo", CpfDataQualityOperations.class, () -> mock(CpfDataQualityOperations.class))
                .withBean("correction", CpfDataQualityCorrectionPort.class, () -> mock(CpfDataQualityCorrectionPort.class))
                .withBean("webhook", CpfWebhookOperations.class, () -> mock(CpfWebhookOperations.class))
                .withPropertyValues("cpf.adm.integration-closure.enabled=true", PROOF_KEY)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void customerCryptoOverrideIsUsedWithoutReadingAPlaintextKeyProperty() {
        CpfCryptoOperations customCrypto = mock(CpfCryptoOperations.class);
        when(customCrypto.activeKeyVersion()).thenReturn("kms-v9");
        contextRunner.withBean(CpfCryptoOperations.class, () -> customCrypto)
                .withPropertyValues(
                        "cpf.adm.integration-closure.enabled=true",
                        PROOF_KEY,
                        "cpf.adm.integration-closure.ephemeral-providers-enabled=true")
                .run(context -> assertThat(context.getBean(AdmIntegrationClosureService.class).cryptoStatus())
                        .containsEntry("configured", true)
                        .containsEntry("activeKeyVersion", "kms-v9")
                        .containsEntry("plaintextKeyExposed", false));
    }

    @Test
    void localJceCryptoFailsClosedWhenEnabledWithoutExternalKey() {
        contextRunner.withPropertyValues(
                        "cpf.adm.integration-closure.enabled=true",
                        PROOF_KEY,
                        "cpf.adm.integration-closure.ephemeral-providers-enabled=true",
                        "cpf.adm.integration-closure.crypto.enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration(proxyBeanMethods = false)
    static class Dependencies {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        AdmApprovalService admApprovalService() {
            return mock(AdmApprovalService.class);
        }

        @Bean
        AdmApprovalCapabilityNonceRepository admApprovalCapabilityNonceRepository() {
            return mock(AdmApprovalCapabilityNonceRepository.class);
        }

        @Bean
        AdmApprovalRepository admApprovalRepository() {
            return mock(AdmApprovalRepository.class);
        }

        @Bean
        AdmApprovalSnapshotIntegrity admApprovalSnapshotIntegrity(ObjectMapper objectMapper) {
            return new AdmApprovalSnapshotIntegrity(objectMapper);
        }
    }
}
