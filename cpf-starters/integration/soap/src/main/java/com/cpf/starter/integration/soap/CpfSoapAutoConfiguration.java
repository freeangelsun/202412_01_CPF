package com.cpf.starter.integration.soap;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.webservices.client.WebServiceMessageSenderFactory;
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.ws.client.core.WebServiceTemplate;
/** SOAP starter. Module을 선택하지 않으면 관련 dependency/bean이 전혀 존재하지 않습니다. */
@AutoConfiguration
@ConditionalOnClass(WebServiceTemplate.class)
@EnableConfigurationProperties(CpfSoapProperties.class)
public class CpfSoapAutoConfiguration {
 @Bean @ConditionalOnMissingBean(CpfSoapClient.class)
 CpfSoapClient cpfSoapClient(WebServiceTemplateBuilder builder,CpfSoapProperties p){
   HttpClientSettings settings=HttpClientSettings.defaults().withConnectTimeout(p.getConnectTimeout()).withReadTimeout(p.getReadTimeout());
   return new CpfSoapClient(builder.httpMessageSenderFactory(WebServiceMessageSenderFactory.http(settings)).build());
 }
}
