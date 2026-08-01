package com.cpf.reference.optional.gateway.config;
import com.cpf.reference.edu.runtime.consumer.jdbc.JdbcEduBusinessConsumer;
import com.cpf.reference.optional.gateway.runtime.ReferenceGatewayBusinessConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(name="cpf.reference.features.gateway.enabled",havingValue="true",matchIfMissing=true)
public class ReferenceGatewayRuntimeConfiguration {
 @Bean ReferenceGatewayBusinessConsumer referenceGatewayBusinessConsumer(JdbcEduBusinessConsumer jdbc){return new ReferenceGatewayBusinessConsumer(jdbc);}
}
