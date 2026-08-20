package com.cpf.backoffice.web.shared.config;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BackofficeWebProperties.class)
public class BackofficeWebConfiguration {
    @Bean
    HttpClient BACKOFFICE_WEB_HTTP_CLIENT(BackofficeWebProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }
}
