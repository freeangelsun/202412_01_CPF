package com.cpf.messaging.kafka;

import com.cpf.starter.runtime.CpfCapabilityBinding;
import com.cpf.starter.runtime.CpfCapabilityBindingRegistry;
import com.cpf.messaging.spi.CpfNamedBrokerClient;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Kafka topic/binding 설정을 CPF Messaging의 논리 destination 계약과 연결합니다.
 * <p>물리 topic 이름과 업무 Consumer를 분리하며 환경별 binding 설정을 Runtime에 적용합니다.
 */
@AutoConfiguration(after = CpfKafkaAutoConfiguration.class)
public class CpfKafkaBindingAutoConfiguration {
    @Bean
    CpfNamedBrokerClient cpfKafkaNamedBrokerClient(
            KafkaCpfMessagingTemplate client,
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
