package com.cpf.starter.kafka;

import com.cpf.starter.base.CpfCapabilityBinding;
import com.cpf.starter.base.CpfCapabilityBindingRegistry;
import com.cpf.starter.messaging.reliability.CpfNamedBrokerClient;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = CpfKafkaAutoConfiguration.class)
public class CpfKafkaBindingAutoConfiguration {
    @Bean
    CpfNamedBrokerClient cpfKafkaNamedBrokerClient(
            KafkaCpfBrokerClient client,
            CpfKafkaProperties properties,
            CpfCapabilityBindingRegistry registry) {
        String bindingName = properties.bindingName();
        boolean defaultBinding = properties.defaultBinding();
        var named = new CpfNamedBrokerClient(bindingName, "kafka", defaultBinding, client);
        registry.register(new CpfCapabilityBinding(
                "messaging", bindingName, "kafka", defaultBinding,
                Map.of("requireIdempotence", Boolean.toString(properties.requireIdempotence()))));
        return named;
    }
}
