package com.cpf.starter.rabbitmq;
import com.cpf.core.api.broker.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
public final class CpfRabbitMqBrokerClient implements CpfBrokerClient {
 private final RabbitTemplate template;private final CpfRabbitMqProperties p;
 public CpfRabbitMqBrokerClient(RabbitTemplate t,CpfRabbitMqProperties p){this.template=t;this.p=p;}
 public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest r){if(r.payload().length>p.getMaxPayloadBytes())throw new IllegalArgumentException("RabbitMQ payload exceeds limit");
  MessageBuilder b=MessageBuilder.withBody(r.payload()).setMessageId(r.messageId()).setContentType("application/octet-stream").setHeader("cpf-transaction-id",r.transactionId()).setHeader("cpf-idempotency-key",r.idempotencyKey());r.headers().forEach(b::setHeader);Message m=b.build();CorrelationData c=new CorrelationData(r.messageId());
  try{template.send(p.getExchange(),p.getRoutingKey(),m,c);var confirm=c.getFuture().get(p.getConfirmTimeout().toMillis(),TimeUnit.MILLISECONDS);if(!confirm.isAck())throw new IllegalStateException("RabbitMQ NACK: "+confirm.getReason());if(c.getReturned()!=null)throw new IllegalStateException("RabbitMQ returned mandatory message: "+c.getReturned().getReplyText());return new CpfBrokerPublishResult("PUBLISHED",r.messageId(),"RABBITMQ",p.getRoutingKey(),Instant.now(),"confirm=ACK");}catch(Exception ex){throw new IllegalStateException("RabbitMQ publish result is UNKNOWN and must be reconciled",ex);}}
}
