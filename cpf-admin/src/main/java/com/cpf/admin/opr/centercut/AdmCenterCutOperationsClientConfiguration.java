package com.cpf.admin.opr.centercut;
import com.cpf.core.api.batch.CpfCenterCutOperationsPort;
import com.cpf.core.common.servicecall.CpfServiceCallEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration(proxyBeanMethods=false)
public class AdmCenterCutOperationsClientConfiguration {
 @Bean @ConditionalOnMissingBean(CpfCenterCutOperationsPort.class) CpfCenterCutOperationsPort remoteCenterCutPort(CpfServiceCallEngine e,WebClient.Builder b){return new RemoteCpfCenterCutOperationsAdapter(e,b);}
}
