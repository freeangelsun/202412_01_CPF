package com.cpf.starter.webhook;

import com.cpf.core.api.http.CpfWebhookSignaturePort;
import com.cpf.core.common.http.CpfWebClient;
import com.cpf.core.common.http.CpfWebhookCallbackClient;
import com.cpf.core.common.http.CpfWebhookRuntimePolicy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Legacy callback API를 Webhook leaf Starter 경계에서만 조립합니다. */
@AutoConfiguration
public class CpfWebhookCompatibilityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfWebhookRuntimePolicy cpfWebhookRuntimePolicy() { return new CpfWebhookRuntimePolicy(); }

    @Bean
    @ConditionalOnMissingBean
    CpfWebhookCallbackClient cpfWebhookCallbackClient(
            CpfWebClient webClient,
            CpfWebhookRuntimePolicy policy,
            ObjectProvider<CpfWebhookSignaturePort> signatureProvider) {
        return new CpfWebhookCallbackClient(webClient, policy, signatureProvider.getIfAvailable());
    }
}
