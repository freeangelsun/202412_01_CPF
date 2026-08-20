package com.cpf.messaging.kafka;

import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.messaging.context.CpfMessageBridgeContextSupport;

import com.cpf.messaging.api.CpfBrokerBridgePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Apache Kafka를 CPF Public Messaging Publisher/Listener 계약에 연결하는 Starter 자동구성입니다.
 * <p>Kafka client 세부 설정을 업무 코드에 노출하지 않고 CPF 표준 Retry/Trace/Error 흐름과 결합합니다.
 */
@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(CpfKafkaProperties.class)
public class CpfKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    CpfMessageBridgeContextSupport cpfMessageBridgeContextSupport(CpfExecutionIdGenerator executionIds) { return new CpfMessageBridgeContextSupport(executionIds); }

    @Bean
    @ConditionalOnMissingBean(KafkaCpfMessagingTemplate.class)
    KafkaCpfMessagingTemplate cpfKafkaBrokerClient(KafkaTemplate<String,byte[]> kafkaTemplate,CpfKafkaProperties properties){return new KafkaCpfMessagingTemplate(kafkaTemplate,properties);}

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(CpfBrokerBridgePort.class)
    KafkaCpfBrokerBridgeAdapter cpfKafkaBrokerBridgePort(KafkaTemplate<String,byte[]> kafkaTemplate,
            ObjectProvider<ConsumerFactory<String,byte[]>> consumerFactory,CpfKafkaProperties properties,ObjectMapper mapper, CpfMessageBridgeContextSupport contextSupport){
        return new KafkaCpfBrokerBridgeAdapter(kafkaTemplate,consumerFactory.getIfAvailable(),properties,mapper,contextSupport);
    }
}
