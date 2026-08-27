package com.cpf.messaging.kafka;

import com.cpf.messaging.api.*;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;

import com.cpf.messaging.context.*;
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
    private final CpfMessageBridgeContextSupport contextSupport;
    private final ConcurrentLinkedDeque<CpfBrokerBridgeMessage> recent=new ConcurrentLinkedDeque<>();
    private final Map<Subscription,List<CpfBrokerBridgeHandler>> handlers=new ConcurrentHashMap<>();
    private final Map<Subscription,KafkaMessageListenerContainer<String,byte[]>> containers=new ConcurrentHashMap<>();

    public KafkaCpfBrokerBridgeAdapter(KafkaTemplate<String,byte[]> kafka,CpfKafkaProperties properties,ObjectMapper mapper){this(kafka,null,properties,mapper,Clock.systemUTC(),defaultContextSupport());}
    public KafkaCpfBrokerBridgeAdapter(KafkaTemplate<String,byte[]> kafka,CpfKafkaProperties properties,ObjectMapper mapper,CpfMessageBridgeContextSupport contextSupport){this(kafka,null,properties,mapper,Clock.systemUTC(),contextSupport);}
    public KafkaCpfBrokerBridgeAdapter(KafkaTemplate<String,byte[]> kafka,ConsumerFactory<String,byte[]> consumerFactory,CpfKafkaProperties properties,ObjectMapper mapper){this(kafka,consumerFactory,properties,mapper,Clock.systemUTC(),defaultContextSupport());}
    public KafkaCpfBrokerBridgeAdapter(KafkaTemplate<String,byte[]> kafka,ConsumerFactory<String,byte[]> consumerFactory,CpfKafkaProperties properties,ObjectMapper mapper,CpfMessageBridgeContextSupport contextSupport){this(kafka,consumerFactory,properties,mapper,Clock.systemUTC(),contextSupport);}
    KafkaCpfBrokerBridgeAdapter(KafkaTemplate<String,byte[]> kafka,ConsumerFactory<String,byte[]> consumerFactory,CpfKafkaProperties properties,ObjectMapper mapper,Clock clock){this(kafka,consumerFactory,properties,mapper,clock,defaultContextSupport());}
    KafkaCpfBrokerBridgeAdapter(KafkaTemplate<String,byte[]> kafka,ConsumerFactory<String,byte[]> consumerFactory,CpfKafkaProperties properties,ObjectMapper mapper,Clock clock,CpfMessageBridgeContextSupport contextSupport){
        this.kafka=Objects.requireNonNull(kafka,"kafka");this.consumerFactory=consumerFactory;this.properties=Objects.requireNonNull(properties,"properties");this.mapper=Objects.requireNonNull(mapper,"mapper");this.clock=Objects.requireNonNull(clock,"clock");this.contextSupport=Objects.requireNonNull(contextSupport,"contextSupport");}

    @Override public CpfBrokerBridgeResult publish(String destination,String key,Object payload,Map<String,String> additionalHeaders){
        String topic=required(destination,"destination");
        CpfMessageBridgeContextSupport.Outbound outbound=contextSupport.prepareOutbound("KAFKA",topic,key,additionalHeaders);
        String resolvedKey=outbound.messageId(); Map<String,String> headers=outbound.headers();
        CpfBrokerBridgeMessage message=new CpfBrokerBridgeMessage("KAFKA",topic,resolvedKey,payload,headers,clock.instant());
        byte[] body;try{body=mapper.writeValueAsBytes(message);}catch(JsonProcessingException e){throw new IllegalArgumentException("Kafka bridge payload cannot be serialized",e);}
        if(body.length>properties.maximumPayloadBytes())throw new IllegalArgumentException("Kafka bridge payload exceeds maximumPayloadBytes");
        ProducerRecord<String,byte[]> record=new ProducerRecord<>(topic,resolvedKey,body);headers.forEach((n,v)->addHeader(record,n,v));
        try{var result=kafka.send(record).get(properties.acknowledgementTimeout().toMillis(),TimeUnit.MILLISECONDS);remember(message);return new CpfBrokerBridgeResult(true,"KAFKA",topic,resolvedKey,headers.get(CpfMessageHeaderNames.TRANSACTION_ID),"partition="+result.getRecordMetadata().partition()+", offset="+result.getRecordMetadata().offset());}
        catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("Kafka publish was interrupted; result is UNKNOWN",e);}catch(Exception e){throw new IllegalStateException("Kafka broker acknowledgement was not confirmed; result is UNKNOWN",e);}
    }

    @Override public void subscribe(String destination,CpfBrokerBridgeHandler handler){
        String topic=required(destination,"destination");Objects.requireNonNull(handler,"handler");
        if(consumerFactory==null)throw new UnsupportedOperationException("Kafka ConsumerFactory is required for bridge subscription");
        Subscription subscription=new Subscription(topic,resolveGroup(topic,handler.consumerGroup()));
        handlers.computeIfAbsent(subscription,k->new java.util.concurrent.CopyOnWriteArrayList<>()).add(handler);
        containers.computeIfAbsent(subscription,this::startContainer);
    }
    private KafkaMessageListenerContainer<String,byte[]> startContainer(Subscription subscription){
        ContainerProperties cp=new ContainerProperties(subscription.destination());cp.setGroupId(subscription.consumerGroup());
        cp.setMessageListener((MessageListener<String,byte[]>)record->consumeRecord(subscription,record));
        KafkaMessageListenerContainer<String,byte[]> c=new KafkaMessageListenerContainer<>(consumerFactory,cp);c.start();return c;
    }
    private void consumeRecord(Subscription subscription,ConsumerRecord<String,byte[]> record){
        try{
            CpfBrokerBridgeMessage message=mapper.readValue(record.value(),CpfBrokerBridgeMessage.class);
            if(!record.topic().equals(message.destination()))throw new IllegalArgumentException("Kafka bridge destination mismatch");
            Map<String,String> transportHeaders=new LinkedHashMap<>();
            record.headers().forEach(h->transportHeaders.put(h.key(),new String(h.value(),StandardCharsets.UTF_8)));
            String tx=transportHeaders.get(CpfMessageHeaderNames.TRANSACTION_ID);
            if(!Objects.equals(tx,message.headers().get(CpfMessageHeaderNames.TRANSACTION_ID)))throw new SecurityException("Kafka transport/body Context mismatch");
            if(!subscription.destination().equals(record.topic()))throw new SecurityException("Kafka subscription/topic mismatch");
            var bundle=contextSupport.extractInbound("KAFKA",required(message.key(),"message key"),record.topic(),null,subscription.consumerGroup(),Integer.toString(record.partition()),Long.toString(record.offset()),1,false,null,null,transportHeaders,null);
            remember(message);
            for(CpfBrokerBridgeHandler h:handlers.getOrDefault(subscription,List.of()))contextSupport.consume(bundle,()->h.handle(message));
        }catch(Exception e){throw new IllegalStateException("Kafka bridge consumer rejected message",e);}
    }
    @Override public List<CpfBrokerBridgeMessage> findRecent(String destination,int limit){String topic=hasText(destination)?destination:null;int bounded=Math.max(1,Math.min(limit<=0?50:limit,RECENT_LIMIT));return recent.stream().filter(m->topic==null||topic.equals(m.destination())).sorted(Comparator.comparing((CpfBrokerBridgeMessage value) -> value.createdAt()).reversed()).limit(bounded).toList();}
    private static void addHeader(ProducerRecord<String,byte[]> r,String n,String v){if(!hasText(v))return;validateName(n);r.headers().add(new RecordHeader(n,v.getBytes(StandardCharsets.UTF_8)));}
    private static void validateName(String n){if(!n.matches("[A-Za-z0-9._-]{1,128}"))throw new IllegalArgumentException("Invalid Kafka header name: "+n);}
    private void remember(CpfBrokerBridgeMessage m){recent.addFirst(m);while(recent.size()>RECENT_LIMIT)recent.pollLast();}
    private static String required(String v,String f){if(!hasText(v))throw new IllegalArgumentException(f+" is required");return v.trim();}
    private static String resolveGroup(String destination,String requested){return hasText(requested)?requested.trim():"cpf-bridge-"+Integer.toUnsignedString(destination.hashCode(),36);}
    private static boolean hasText(String v){return v!=null&&!v.isBlank();}
    private record Subscription(String destination,String consumerGroup){}

    private static CpfMessageBridgeContextSupport defaultContextSupport() {
        CpfExecutionIdGenerator ex = new CpfExecutionIdGenerator() {
            public String newExecutionId() { return "EX-" + UUID.randomUUID(); }
            public String newSegmentId() { return "SG-" + UUID.randomUUID(); }
        };
        return new CpfMessageBridgeContextSupport(ex);
    }

    @Override public void close(){containers.values().forEach(value -> value.stop());containers.clear();handlers.clear();}
}
