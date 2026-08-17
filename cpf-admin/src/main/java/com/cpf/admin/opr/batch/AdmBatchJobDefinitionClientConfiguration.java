package com.cpf.admin.opr.batch;

import com.cpf.platform.operations.api.runtime.CpfInstanceIdentity;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.batch.api.BatchJobDefinitionControlPort;
import com.cpf.integration.api.servicecall.CpfServiceCaller;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/** Local BAT Port가 없을 때 Registry 기반 Remote Job Definition Port를 구성합니다. */
@Configuration(proxyBeanMethods=false)
public class AdmBatchJobDefinitionClientConfiguration {
    @Bean
    @ConditionalOnMissingBean(BatchJobDefinitionControlPort.class)
    public BatchJobDefinitionControlPort remoteBatchJobDefinitionControlPort(
            CpfServiceCaller caller,WebClient.Builder builder,AdmAuthenticatedOperatorContext actorContext,
            ObjectMapper mapper) {
        return new RemoteBatchJobDefinitionControlAdapter(caller,builder,actorContext,mapper,CpfInstanceIdentity.current().instanceId());
    }
}
