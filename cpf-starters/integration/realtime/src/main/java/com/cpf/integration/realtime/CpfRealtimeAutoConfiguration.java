package com.cpf.integration.realtime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

/**
 * CPF Realtime capability의 Hub와 Web endpoint를 Spring Runtime에 연결하는 Public Starter 자동구성입니다.
 * <p>Realtime capability를 선택한 경우에만 활성화하며 별도 자체 프로토콜을 만들지 않고 CPF Context/Security 계약을 유지합니다.
 */
@AutoConfiguration
@EnableConfigurationProperties(CpfRealtimeProperties.class)
@ConditionalOnProperty(prefix = "cpf.integration.realtime", name = "enabled", havingValue = "true")
public class CpfRealtimeAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfRealtimeBackplane cpfRealtimeBackplane() { return new CpfLocalRealtimeBackplane(); }

    @Bean @ConditionalOnMissingBean
    CpfRealtimeAuthorization cpfRealtimeAuthorization() { return CpfRealtimeAuthorization.authenticatedPrincipal(); }

    @Bean(destroyMethod = "close")
    CpfRealtimeBroker cpfRealtimeBroker(CpfRealtimeProperties properties, CpfRealtimeBackplane backplane) {
        return new CpfRealtimeBroker(UUID.randomUUID().toString(), properties, backplane);
    }

    @Bean
    CpfRealtimeController cpfRealtimeController(CpfRealtimeBroker broker,
                                                 CpfRealtimeAuthorization authorization,
                                                 CpfRealtimeProperties properties) {
        return new CpfRealtimeController(broker, authorization, properties);
    }
}
