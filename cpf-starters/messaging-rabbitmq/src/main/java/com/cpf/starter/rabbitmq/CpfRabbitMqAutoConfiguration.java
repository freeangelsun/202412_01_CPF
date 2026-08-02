package com.cpf.starter.rabbitmq;
import com.cpf.starter.base.*;import com.cpf.starter.messaging.reliability.CpfNamedBrokerClient;import java.util.*;
import org.springframework.amqp.core.*;import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;import org.springframework.amqp.rabbit.connection.ConnectionFactory;import org.springframework.amqp.rabbit.core.RabbitTemplate;import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.boot.context.properties.EnableConfigurationProperties;import org.springframework.context.annotation.Bean;
@AutoConfiguration
@EnableConfigurationProperties(CpfRabbitMqProperties.class)
@ConditionalOnProperty(prefix="cpf.messaging.rabbitmq",name="enabled",havingValue="true")
public class CpfRabbitMqAutoConfiguration {
 @Bean Declarables cpfRabbitDeclarables(CpfRabbitMqProperties p){p.validate();Map<String,Object> args=p.isQuorum()?Map.of("x-queue-type","quorum"):Map.of();Queue q=new Queue(p.getQueue(),p.isDurable(),false,false,args);Exchange e=switch(p.getExchangeType()){case "direct"->new DirectExchange(p.getExchange(),p.isDurable(),false);case "fanout"->new FanoutExchange(p.getExchange(),p.isDurable(),false);case "headers"->new HeadersExchange(p.getExchange(),p.isDurable(),false);default->new TopicExchange(p.getExchange(),p.isDurable(),false);};Binding b=new Binding(q.getName(),Binding.DestinationType.QUEUE,p.getExchange(),p.getRoutingKey(),Map.of());return new Declarables(e,q,b);}
 @Bean RabbitTemplate cpfRabbitTemplate(ConnectionFactory cf){RabbitTemplate t=new RabbitTemplate(cf);t.setMandatory(true);return t;}
 @Bean SimpleRabbitListenerContainerFactory cpfRabbitListenerContainerFactory(ConnectionFactory cf,CpfRabbitMqProperties p){var f=new SimpleRabbitListenerContainerFactory();f.setConnectionFactory(cf);f.setAcknowledgeMode(AcknowledgeMode.MANUAL);f.setPrefetchCount(p.getPrefetch());f.setConcurrentConsumers(p.getConcurrency());return f;}
 @Bean CpfRabbitMqBrokerClient cpfRabbitMqBrokerClient(RabbitTemplate t,CpfRabbitMqProperties p){return new CpfRabbitMqBrokerClient(t,p);}
 @Bean CpfNamedBrokerClient cpfRabbitNamedBrokerClient(CpfRabbitMqBrokerClient c,CpfRabbitMqProperties p,CpfCapabilityBindingRegistry r){r.register(new CpfCapabilityBinding("messaging",p.getBindingName(),"rabbitmq",p.isDefaultBinding(),Map.of("exchange",p.getExchange())));return new CpfNamedBrokerClient(p.getBindingName(),"rabbitmq",p.isDefaultBinding(),c);}
}
