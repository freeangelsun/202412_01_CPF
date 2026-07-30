package com.cpf.admin.opr.batch;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.batch.api.BatchJobDefinitionControlPort;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
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
            ObjectMapper mapper,@Value("${cpf.framework.instance-id:adm-local-01}")String callerInstanceId) {
        return new RemoteBatchJobDefinitionControlAdapter(caller,builder,actorContext,mapper,callerInstanceId);
    }
}
