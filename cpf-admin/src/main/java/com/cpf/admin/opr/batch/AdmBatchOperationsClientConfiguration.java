package com.cpf.admin.opr.batch;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.batch.api.CpfBatchOperationsPort;
import com.cpf.integration.api.servicecall.CpfServiceCaller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/** Local BAT Port가 같은 ApplicationContext에 없을 때 Remote Adapter를 선택합니다. */
@Configuration(proxyBeanMethods=false)
public class AdmBatchOperationsClientConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfBatchOperationsPort.class)
    public CpfBatchOperationsPort remoteCpfBatchOperationsPort(
            CpfServiceCaller serviceCaller,
            WebClient.Builder webClientBuilder,
            AdmAuthenticatedOperatorContext operatorContext,
            @Value("${cpf.framework.instance-id:adm-local-01}") String callerInstanceId) {
        return new RemoteCpfBatchOperationsAdapter(
                serviceCaller,
                webClientBuilder,
                operatorContext,
                callerInstanceId);
    }
}
