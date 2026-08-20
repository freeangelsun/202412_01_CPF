package com.cpf.messaging.ibmmq;

import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.messaging.api.CpfBrokerBridgePort;
import com.cpf.messaging.context.CpfMessageBridgeContextSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import java.util.Locale;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * IBM MQ를 CPF Public Messaging 계약에 연결하는 Starter 자동구성입니다.
 * <p>IBM MQ를 명시적으로 선택한 애플리케이션에서만 Broker adapter를 구성하며, 업무 코드는 CPF Messaging API를 사용합니다.
 */
@AutoConfiguration
@EnableConfigurationProperties(CpfIbmMqProperties.class)
@ConditionalOnProperty(prefix="cpf.messaging.ibm-mq", name="enabled", havingValue="true")
public class CpfIbmMqAutoConfiguration {
    @Bean
    SmartInitializingSingleton cpfIbmMqDriverBoundary(CpfIbmMqProperties properties, ConnectionFactory connectionFactory) {
        return () -> {
            properties.validate();
            String name = connectionFactory.getClass().getName();
            if (!name.toLowerCase(Locale.ROOT).contains("mq")) {
                throw new IllegalStateException("IBM MQ requires customer-provided IBM MQ JMS ConnectionFactory: " + name);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    CpfMessageBridgeContextSupport cpfMessageBridgeContextSupport(CpfExecutionIdGenerator executionIds) {
        return new CpfMessageBridgeContextSupport(executionIds);
    }

    @Bean
    JmsTemplate cpfIbmMqJmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setSessionTransacted(true);
        return template;
    }

    @Bean
    CpfIbmMqBrokerClient cpfIbmMqBrokerClient(JmsTemplate template, CpfIbmMqProperties properties) {
        return new CpfIbmMqBrokerClient(template, properties);
    }

    @Bean
    DefaultJmsListenerContainerFactory cpfIbmMqListenerContainerFactory(ConnectionFactory connectionFactory) {
        var factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(true);
        return factory;
    }

    @Bean(destroyMethod="close")
    @ConditionalOnMissingBean(CpfBrokerBridgePort.class)
    IbmMqCpfBrokerBridgeAdapter cpfIbmMqBridge(
            JmsTemplate template,
            DefaultJmsListenerContainerFactory listenerFactory,
            CpfIbmMqProperties properties,
            ObjectMapper mapper,
            CpfMessageBridgeContextSupport contextSupport) {
        return new IbmMqCpfBrokerBridgeAdapter(template, listenerFactory, properties, mapper, contextSupport);
    }
}
