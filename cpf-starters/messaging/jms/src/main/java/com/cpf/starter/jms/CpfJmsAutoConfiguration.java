package com.cpf.starter.jms;

import com.cpf.core.api.broker.CpfBrokerBridgePort;
import com.cpf.starter.base.CpfCapabilityBinding;
import com.cpf.starter.base.CpfCapabilityBindingRegistry;
import com.cpf.starter.messaging.reliability.CpfNamedBrokerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@AutoConfiguration
@EnableConfigurationProperties(CpfJmsProperties.class)
@ConditionalOnProperty(prefix = "cpf.messaging.jms", name = "enabled", havingValue = "true")
public class CpfJmsAutoConfiguration {
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
            ObjectMapper mapper) {
        return new JmsCpfBrokerBridgeAdapter(template, factory, properties, mapper);
    }

    @Bean
    CpfNamedBrokerClient cpfJmsNamedBrokerClient(
            CpfJmsBrokerClient client,
            CpfJmsProperties properties,
            CpfCapabilityBindingRegistry registry) {
        registry.register(new CpfCapabilityBinding(
                "messaging", properties.getBindingName(), "jms", properties.isDefaultBinding(),
                Map.of("destination", properties.getDestination())));
        return new CpfNamedBrokerClient(
                properties.getBindingName(), "jms", properties.isDefaultBinding(), client);
    }
}
