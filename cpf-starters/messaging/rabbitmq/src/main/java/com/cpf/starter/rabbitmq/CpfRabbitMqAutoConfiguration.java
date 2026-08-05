package com.cpf.starter.rabbitmq;

import com.cpf.core.api.broker.CpfBrokerBridgePort;
import com.cpf.starter.base.CpfCapabilityBinding;
import com.cpf.starter.base.CpfCapabilityBindingRegistry;
import com.cpf.starter.messaging.reliability.CpfNamedBrokerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(CpfRabbitMqProperties.class)
@ConditionalOnProperty(prefix = "cpf.messaging.rabbitmq", name = "enabled", havingValue = "true")
public class CpfRabbitMqAutoConfiguration {
    @Bean
    Declarables cpfRabbitDeclarables(CpfRabbitMqProperties properties) {
        properties.validate();
        Map<String, Object> arguments = properties.isQuorum()
                ? Map.of("x-queue-type", "quorum") : Map.of();
        Queue queue = new Queue(properties.getQueue(), properties.isDurable(), false, false, arguments);
        Exchange exchange = switch (properties.getExchangeType()) {
            case "direct" -> new DirectExchange(properties.getExchange(), properties.isDurable(), false);
            case "fanout" -> new FanoutExchange(properties.getExchange(), properties.isDurable(), false);
            case "headers" -> new HeadersExchange(properties.getExchange(), properties.isDurable(), false);
            default -> new TopicExchange(properties.getExchange(), properties.isDurable(), false);
        };
        Binding binding = new Binding(
                queue.getName(), Binding.DestinationType.QUEUE,
                properties.getExchange(), properties.getRoutingKey(), Map.of());
        return new Declarables(exchange, queue, binding);
    }

    @Bean
    RabbitTemplate cpfRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(true);
        return template;
    }

    @Bean
    SimpleRabbitListenerContainerFactory cpfRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, CpfRabbitMqProperties properties) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(properties.getPrefetch());
        factory.setConcurrentConsumers(properties.getConcurrency());
        return factory;
    }

    @Bean
    CpfRabbitMqBrokerClient cpfRabbitMqBrokerClient(
            RabbitTemplate template, CpfRabbitMqProperties properties) {
        return new CpfRabbitMqBrokerClient(template, properties);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(CpfBrokerBridgePort.class)
    RabbitCpfBrokerBridgeAdapter cpfRabbitMqBrokerBridgeAdapter(
            RabbitTemplate template,
            SimpleRabbitListenerContainerFactory listenerFactory,
            CpfRabbitMqProperties properties,
            ObjectMapper mapper) {
        return new RabbitCpfBrokerBridgeAdapter(template, listenerFactory, properties, mapper);
    }

    @Bean
    CpfNamedBrokerClient cpfRabbitNamedBrokerClient(
            CpfRabbitMqBrokerClient client,
            CpfRabbitMqProperties properties,
            CpfCapabilityBindingRegistry registry) {
        registry.register(new CpfCapabilityBinding(
                "messaging", properties.getBindingName(), "rabbitmq",
                properties.isDefaultBinding(), Map.of("exchange", properties.getExchange())));
        return new CpfNamedBrokerClient(
                properties.getBindingName(), "rabbitmq", properties.isDefaultBinding(), client);
    }
}
