package com.cpf.starter.kafka;

import com.cpf.core.api.broker.*;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.util.CpfHeaders;
import com.cpf.core.api.workflow.CpfWorkflow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

/** Kafka Profile의 publish/subscribe {@link CpfBrokerBridgePort} Product Adapter입니다. */
public final class KafkaCpfBrokerBridgeAdapter implements CpfBrokerBridgePort, AutoCloseable {
    private static final int RECENT_LIMIT=200;
    private final KafkaTemplate<String,byte[]> kafka;
    private final ConsumerFactory<String,byte[]> consumerFactory;
    private final CpfKafkaProperties properties;
    private final ObjectMapper mapper;
    private final Clock clock;
    private final ConcurrentLinkedDeque<CpfBrokerBridgeMessage> recent=new ConcurrentLinkedDeque<>();
    private final Map<String,List<CpfBrokerBridgeHandler>> handlers=new ConcurrentHashMap<>();
    private final Map<String,KafkaMessageListenerContainer<String,byte[]>> containers=new ConcurrentHashMap<>();

    public KafkaCpfBrokerBridgeAdapter(KafkaTemplate<String,byte[]> kafka,CpfKafkaProperties properties,ObjectMapper mapper){this(kafka,null,properties,mapper,Clock.systemUTC());}
    public KafkaCpfBrokerBridgeAdapter(KafkaTemplate<String,byte[]> kafka,ConsumerFactory<String,byte[]> consumerFactory,CpfKafkaProperties properties,ObjectMapper mapper){this(kafka,consumerFactory,properties,mapper,Clock.systemUTC());}
    KafkaCpfBrokerBridgeAdapter(KafkaTemplate<String,byte[]> kafka,ConsumerFactory<String,byte[]> consumerFactory,CpfKafkaProperties properties,ObjectMapper mapper,Clock clock){
        this.kafka=Objects.requireNonNull(kafka,"kafka");this.consumerFactory=consumerFactory;this.properties=Objects.requireNonNull(properties,"properties");this.mapper=Objects.requireNonNull(mapper,"mapper");this.clock=Objects.requireNonNull(clock,"clock");}

    @Override public CpfBrokerBridgeResult publish(String destination,String key,Object payload,Map<String,String> additionalHeaders){
        String topic=required(destination,"destination");String resolvedKey=hasText(key)?key:CpfTransactionContext.transactionId();
        Map<String,String> headers=propagationHeaders(additionalHeaders);
        CpfBrokerBridgeMessage message=new CpfBrokerBridgeMessage("KAFKA",topic,resolvedKey,payload,headers,clock.instant());
        byte[] body;try{body=mapper.writeValueAsBytes(message);}catch(JsonProcessingException e){throw new IllegalArgumentException("Kafka bridge payload cannot be serialized",e);}
        if(body.length>properties.maximumPayloadBytes())throw new IllegalArgumentException("Kafka bridge payload exceeds maximumPayloadBytes");
        ProducerRecord<String,byte[]> record=new ProducerRecord<>(topic,resolvedKey,body);headers.forEach((n,v)->addHeader(record,n,v));
        try{var result=kafka.send(record).get(properties.acknowledgementTimeout().toMillis(),TimeUnit.MILLISECONDS);remember(message);return new CpfBrokerBridgeResult(true,"KAFKA",topic,resolvedKey,headers.get(CpfHeaders.transactionId()),"partition="+result.getRecordMetadata().partition()+", offset="+result.getRecordMetadata().offset());}
        catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("Kafka publish was interrupted; result is UNKNOWN",e);}catch(Exception e){throw new IllegalStateException("Kafka broker acknowledgement was not confirmed; result is UNKNOWN",e);}
    }

    @Override public void subscribe(String destination,CpfBrokerBridgeHandler handler){
        String topic=required(destination,"destination");Objects.requireNonNull(handler,"handler");
        if(consumerFactory==null)throw new UnsupportedOperationException("Kafka ConsumerFactory is required for bridge subscription");
        handlers.computeIfAbsent(topic,k->new java.util.concurrent.CopyOnWriteArrayList<>()).add(handler);
        containers.computeIfAbsent(topic,this::startContainer);
    }
    private KafkaMessageListenerContainer<String,byte[]> startContainer(String topic){
        ContainerProperties cp=new ContainerProperties(topic);cp.setGroupId("cpf-bridge-"+Integer.toUnsignedString(topic.hashCode(),36));
        cp.setMessageListener((MessageListener<String,byte[]>)this::consumeRecord);
        KafkaMessageListenerContainer<String,byte[]> c=new KafkaMessageListenerContainer<>(consumerFactory,cp);c.start();return c;
    }
    private void consumeRecord(ConsumerRecord<String,byte[]> record){
        try{
            CpfBrokerBridgeMessage message=mapper.readValue(record.value(),CpfBrokerBridgeMessage.class);
            if(!record.topic().equals(message.destination()))throw new IllegalArgumentException("Kafka bridge destination mismatch");
            remember(message);
            for(CpfBrokerBridgeHandler h:handlers.getOrDefault(record.topic(),List.of()))h.handle(message);
        }catch(Exception e){throw new IllegalStateException("Kafka bridge consumer rejected message",e);}
    }
    @Override public List<CpfBrokerBridgeMessage> findRecent(String destination,int limit){String topic=hasText(destination)?destination:null;int bounded=Math.max(1,Math.min(limit<=0?50:limit,RECENT_LIMIT));return recent.stream().filter(m->topic==null||topic.equals(m.destination())).sorted(Comparator.comparing(CpfBrokerBridgeMessage::createdAt).reversed()).limit(bounded).toList();}
    private Map<String,String> propagationHeaders(Map<String,String> additional){Map<String,String> h=new LinkedHashMap<>();h.putAll(CpfTransactionContext.propagationHeaders());h.putAll(CpfWorkflow.propagationHeaders());Set<String> reserved=new HashSet<>();h.keySet().forEach(k->reserved.add(k.toLowerCase(Locale.ROOT)));if(additional!=null)for(var e:additional.entrySet()){String n=required(e.getKey(),"header name");String v=Objects.requireNonNull(e.getValue(),"header value");if(reserved.contains(n.toLowerCase(Locale.ROOT)))throw new SecurityException("CPF propagation header cannot be overridden: "+n);validateName(n);h.put(n,v);}return Map.copyOf(h);}
    private static void addHeader(ProducerRecord<String,byte[]> r,String n,String v){if(!hasText(v))return;validateName(n);r.headers().add(new RecordHeader(n,v.getBytes(StandardCharsets.UTF_8)));}
    private static void validateName(String n){if(!n.matches("[A-Za-z0-9._-]{1,128}"))throw new IllegalArgumentException("Invalid Kafka header name: "+n);}
    private void remember(CpfBrokerBridgeMessage m){recent.addFirst(m);while(recent.size()>RECENT_LIMIT)recent.pollLast();}
    private static String required(String v,String f){if(!hasText(v))throw new IllegalArgumentException(f+" is required");return v.trim();}
    private static boolean hasText(String v){return v!=null&&!v.isBlank();}
    @Override public void close(){containers.values().forEach(KafkaMessageListenerContainer::stop);containers.clear();handlers.clear();}
}
