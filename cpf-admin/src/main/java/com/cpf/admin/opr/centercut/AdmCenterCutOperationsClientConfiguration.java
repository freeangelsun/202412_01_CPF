package com.cpf.admin.opr.centercut;

import com.cpf.foundation.runtime.CpfInstanceIdentity;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.batch.api.CpfCenterCutOperationsPort;
import com.cpf.integration.api.servicecall.CpfServiceCaller;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
public class AdmCenterCutOperationsClientConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfCenterCutOperationsPort.class)
    CpfCenterCutOperationsPort remoteCenterCutPort(
            CpfServiceCaller serviceCaller,
            WebClient.Builder webClientBuilder,
            AdmAuthenticatedOperatorContext operatorContext) {
        return new RemoteCpfCenterCutOperationsAdapter(
                serviceCaller,
                webClientBuilder,
                operatorContext,
                CpfInstanceIdentity.current().instanceId());
    }
}
