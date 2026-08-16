package com.cpf.integration.realtime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

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
