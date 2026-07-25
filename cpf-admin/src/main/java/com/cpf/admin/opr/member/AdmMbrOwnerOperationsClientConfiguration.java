package com.cpf.admin.opr.member;

import com.cpf.core.api.admin.CpfOwnerAdminOperationsPort;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/** 동일 JVM MBR Port가 없을 때만 MBR Remote Adapter를 구성합니다. */
@Configuration(proxyBeanMethods = false)
public class AdmMbrOwnerOperationsClientConfiguration {
    @Bean(name = "mbrOwnerAdminOperationsPort")
    @ConditionalOnMissingBean(name = "mbrOwnerAdminOperationsPort")
    public CpfOwnerAdminOperationsPort remoteMbrOwnerAdminOperationsPort(
            CpfServiceCaller caller,
            WebClient.Builder webClientBuilder) {
        return new RemoteMbrOwnerAdminOperationsAdapter(caller, webClientBuilder);
    }
}
