package com.cpf.starter.kafka;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;

/** Kafka broker ACK를 확인한 뒤에만 PUBLISHED를 반환하는 Product Adapter입니다. */
public final class KafkaCpfBrokerClient implements CpfBrokerClient {
    private static final Set<String> RESERVED = Set.of(
            "cpf-message-id","cpf-transaction-id","cpf-idempotency-key","cpf-content-type",
            "cpf-segment-id","cpf-producer-module","cpf-consumer-module");
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final CpfKafkaProperties properties;
    private final Clock clock;

    public KafkaCpfBrokerClient(KafkaTemplate<String, byte[]> kafkaTemplate, CpfKafkaProperties properties) {
        this(kafkaTemplate, properties, Clock.systemUTC());
    }
    KafkaCpfBrokerClient(KafkaTemplate<String, byte[]> kafkaTemplate, CpfKafkaProperties properties, Clock clock) {
        this.kafkaTemplate=java.util.Objects.requireNonNull(kafkaTemplate,"kafkaTemplate");
        this.properties=java.util.Objects.requireNonNull(properties,"properties");
        this.clock=java.util.Objects.requireNonNull(clock,"clock");
    }

    @Override
    public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
        java.util.Objects.requireNonNull(request,"request");
        String transactionId=required(request.transactionId(),"transactionId");
        String idempotencyKey=required(request.idempotencyKey(),"idempotencyKey");
        if(request.payload().length>properties.maximumPayloadBytes())throw new IllegalArgumentException("Kafka message payload exceeds CPF maximumPayloadBytes.");
        Map<String,String> userHeaders=validatedSnapshot(request.headers());
        ProducerRecord<String,byte[]> record=new ProducerRecord<>(request.topic(),request.key(),request.payload());
        addHeader(record,"cpf-message-id",request.messageId());
        addHeader(record,"cpf-transaction-id",transactionId);
        addHeader(record,"cpf-idempotency-key",idempotencyKey);
        addHeader(record,"cpf-content-type",request.contentType());
        addHeader(record,"cpf-segment-id",request.segmentId());
        addHeader(record,"cpf-producer-module",request.producerModule());
        addHeader(record,"cpf-consumer-module",request.consumerModule());
        userHeaders.forEach((n,v)->addHeader(record,n,v));
        java.util.concurrent.CompletableFuture<org.springframework.kafka.support.SendResult<String, byte[]>> acknowledgement;
        try {
            acknowledgement = java.util.Objects.requireNonNull(
                    kafkaTemplate.send(record), "KafkaTemplate.send returned null");
        } catch (SerializationException | IllegalArgumentException deterministicFailure) {
            throw failedBeforeWrite("Kafka record was rejected before provider write", deterministicFailure);
        } catch (RuntimeException uncertainProviderFailure) {
            throw unknown("Kafka provider invocation failed without a definitive acknowledgement",
                    uncertainProviderFailure);
        }
        try {
            var result=acknowledgement.get(
                    properties.acknowledgementTimeout().toMillis(),TimeUnit.MILLISECONDS);
            return new CpfBrokerPublishResult("PUBLISHED",request.messageId(),"KAFKA",
                    Integer.toString(result.getRecordMetadata().partition()),clock.instant(),
                    "offset="+result.getRecordMetadata().offset());
        } catch(InterruptedException e){Thread.currentThread().interrupt();throw unknown("Kafka acknowledgement wait was interrupted",e);}
        catch(TimeoutException e){throw unknown("Kafka acknowledgement timed out",e);}
        catch(ExecutionException e){
            Throwable cause=e.getCause()==null?e:e.getCause();
            if(cause instanceof SerializationException || cause instanceof IllegalArgumentException){
                throw failedBeforeWrite("Kafka record was rejected before provider write",cause);
            }
            throw unknown("Kafka broker failed without a definitive acknowledgement",cause);
        }
    }

    private static Map<String,String> validatedSnapshot(Map<String,String> source){
        Map<String,String> copy=new LinkedHashMap<>();
        if(source==null)return Map.of();
        for(var e:source.entrySet()){
            String name=required(e.getKey(),"header name"); String value=java.util.Objects.requireNonNull(e.getValue(),"header value");
            validateName(name); String normalized=name.toLowerCase(Locale.ROOT);
            if(RESERVED.contains(normalized))throw new SecurityException("CPF reserved Kafka header cannot be overridden: "+name);
            if(copy.keySet().stream().anyMatch(k->k.equalsIgnoreCase(name)))throw new IllegalArgumentException("Duplicate Kafka header name: "+name);
            copy.put(name,value);
        }
        return Map.copyOf(copy);
    }
    private static IllegalArgumentException failedBeforeWrite(String detail,Throwable cause){
        return new IllegalArgumentException(detail+"; publish result is FAILED.",cause);
    }
    private static IllegalStateException unknown(String d,Throwable t){return new IllegalStateException(d+"; publish result is UNKNOWN and must be reconciled.",t);}
    private static void addHeader(ProducerRecord<String,byte[]> r,String n,String v){if(v==null||v.isBlank())return;validateName(n);r.headers().add(new RecordHeader(n,v.getBytes(StandardCharsets.UTF_8)));}
    private static void validateName(String n){if(!n.matches("[A-Za-z0-9._-]{1,128}"))throw new IllegalArgumentException("Invalid Kafka header name: "+n);}
    private static String required(String v,String f){if(v==null||v.isBlank())throw new IllegalArgumentException(f+" is required");return v.trim();}
}
