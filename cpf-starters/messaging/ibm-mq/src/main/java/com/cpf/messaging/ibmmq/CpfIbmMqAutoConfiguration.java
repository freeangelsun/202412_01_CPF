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
