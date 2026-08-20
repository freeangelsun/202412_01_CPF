package com.cpf.messaging.jms;

import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.messaging.context.CpfMessageBridgeContextSupport;

import com.cpf.messaging.api.CpfBrokerBridgePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * 표준 JMS Broker를 CPF Public Messaging 계약에 연결하는 Starter 자동구성입니다.
 * <p>JMS provider 세부 구현 대신 CPF Messaging API를 소비하도록 Bean 구성을 제공합니다.
 */
@AutoConfiguration
@EnableConfigurationProperties(CpfJmsProperties.class)
@ConditionalOnProperty(prefix = "cpf.messaging.jms", name = "enabled", havingValue = "true")
public class CpfJmsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    CpfMessageBridgeContextSupport cpfMessageBridgeContextSupport(CpfExecutionIdGenerator executionIds) { return new CpfMessageBridgeContextSupport(executionIds); }

    @Bean
    JmsTemplate cpfJmsTemplate(ConnectionFactory connectionFactory, CpfJmsProperties properties) {
        properties.validate();
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setPubSubDomain(properties.isPubSubDomain());
        template.setSessionTransacted(properties.isSessionTransacted());
        template.setSessionAcknowledgeMode(properties.getAcknowledgementMode());
        return template;
    }

    @Bean
    DefaultJmsListenerContainerFactory cpfJmsListenerContainerFactory(
            ConnectionFactory connectionFactory, CpfJmsProperties properties) {
        var factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(properties.isPubSubDomain());
        factory.setSessionTransacted(properties.isSessionTransacted());
        factory.setSessionAcknowledgeMode(properties.getAcknowledgementMode());
        return factory;
    }

    @Bean
    CpfJmsBrokerClient cpfJmsBrokerClient(JmsTemplate template, CpfJmsProperties properties) {
        return new CpfJmsBrokerClient(template, properties);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(CpfBrokerBridgePort.class)
    JmsCpfBrokerBridgeAdapter cpfJmsBrokerBridgeAdapter(
            JmsTemplate template,
            DefaultJmsListenerContainerFactory factory,
            CpfJmsProperties properties,
            ObjectMapper mapper, CpfMessageBridgeContextSupport contextSupport) {
        return new JmsCpfBrokerBridgeAdapter(template, factory, properties, mapper, contextSupport);
    }


}
