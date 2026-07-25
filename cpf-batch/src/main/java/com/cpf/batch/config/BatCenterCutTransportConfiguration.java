package com.cpf.batch.config;

import com.cpf.batch.runtime.centercut.BatCenterCutRemoteTransport;
import com.cpf.batch.runtime.centercut.BatHttpCenterCutRemoteTransport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** BAT 독립 실행 시 사용할 기본 Center-Cut HTTP transport를 조립합니다. */
@Configuration
public class BatCenterCutTransportConfiguration {
    @Bean
    @ConditionalOnMissingBean(BatCenterCutRemoteTransport.class)
    BatCenterCutRemoteTransport batCenterCutRemoteTransport(RestClient.Builder builder) {
        return new BatHttpCenterCutRemoteTransport(builder);
    }
}
