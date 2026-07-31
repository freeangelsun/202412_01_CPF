package com.cpf.starter.kafka;

import com.cpf.core.api.broker.CpfBrokerBridgePort;
import com.cpf.core.api.broker.CpfBrokerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(CpfKafkaProperties.class)
public class CpfKafkaAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfBrokerClient.class)
    CpfBrokerClient cpfKafkaBrokerClient(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            CpfKafkaProperties properties) {
        return new KafkaCpfBrokerClient(kafkaTemplate, properties);
    }

    @Bean
    @ConditionalOnProperty(name = "cpf.broker.type", havingValue = "KAFKA")
    @ConditionalOnMissingBean(CpfBrokerBridgePort.class)
    CpfBrokerBridgePort cpfKafkaBrokerBridgePort(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            CpfKafkaProperties properties,
            ObjectMapper mapper) {
        return new KafkaCpfBrokerBridgeAdapter(kafkaTemplate, properties, mapper);
    }
}
