package com.cpf.starter.kafka;
import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.starter.base.CpfCapabilityBinding;
import com.cpf.starter.base.CpfCapabilityBindingRegistry;
import com.cpf.starter.messaging.reliability.CpfNamedBrokerClient;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
@AutoConfiguration(after=CpfKafkaAutoConfiguration.class)
public class CpfKafkaBindingAutoConfiguration {
 @Bean CpfNamedBrokerClient cpfKafkaNamedBrokerClient(CpfBrokerClient client,CpfKafkaProperties p,CpfCapabilityBindingRegistry registry){var named=new CpfNamedBrokerClient("kafka","kafka",true,client);registry.register(new CpfCapabilityBinding("messaging","kafka","kafka",true,Map.of()));return named;}
}
