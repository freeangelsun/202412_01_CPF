package com.cpf.messaging.ibmmq;

import com.cpf.messaging.api.*;
import com.cpf.messaging.context.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/** IBM MQ JMS bridge with CPF Message Context restore/bind/clear semantics. */
public final class IbmMqCpfBrokerBridgeAdapter implements CpfBrokerBridgePort, AutoCloseable {
    private static final Map<String,String> CTX = Map.ofEntries(
            Map.entry(CpfMessageHeaderNames.TRANSACTION_ID,"cpfCtxTransactionId"),
            Map.entry(CpfMessageHeaderNames.ROOT_TRANSACTION_ID,"cpfCtxRootTransactionId"),
            Map.entry(CpfMessageHeaderNames.CORRELATION_ID,"cpfCtxCorrelationId"),
            Map.entry(CpfMessageHeaderNames.BUSINESS_DATE,"cpfCtxBusinessDate"),
            Map.entry(CpfMessageHeaderNames.PARENT_EXECUTION_ID,"cpfCtxParentExecutionId"),
            Map.entry(CpfMessageHeaderNames.ROOT_EXECUTION_ID,"cpfCtxRootExecutionId"),
            Map.entry(CpfMessageHeaderNames.PARENT_SEGMENT_ID,"cpfCtxParentSegmentId"),
            Map.entry(CpfMessageHeaderNames.IDEMPOTENCY_KEY,"cpfCtxIdempotencyKey"),
            Map.entry(CpfMessageHeaderNames.DEADLINE,"cpfCtxDeadline"));
    private final JmsTemplate template; private final DefaultJmsListenerContainerFactory factory;
    private final CpfIbmMqProperties properties; private final ObjectMapper mapper;
    private final CpfMessageBridgeContextSupport contextSupport;
    private final Map<String,CopyOnWriteArrayList<CpfBrokerBridgeHandler>> handlers=new ConcurrentHashMap<>();
    private final Map<String,String> consumerGroups=new ConcurrentHashMap<>();
    private final Map<String,DefaultMessageListenerContainer> containers=new ConcurrentHashMap<>();

    public IbmMqCpfBrokerBridgeAdapter(JmsTemplate template, DefaultJmsListenerContainerFactory factory,
            CpfIbmMqProperties properties, ObjectMapper mapper, CpfMessageBridgeContextSupport contextSupport) {
        this.template=Objects.requireNonNull(template);this.factory=Objects.requireNonNull(factory);
        this.properties=Objects.requireNonNull(properties);this.mapper=Objects.requireNonNull(mapper);
        this.contextSupport=Objects.requireNonNull(contextSupport,"contextSupport");
    }
    @Override public CpfBrokerBridgeResult publish(String destination,String key,Object payload,Map<String,String> additionalHeaders){
        String d=required(destination,"destination");if(!d.equals(properties.getDestination()))throw new IllegalArgumentException("IBM MQ destination must match configured destination");
        var out=contextSupport.prepareOutbound("IBM_MQ",d,key,additionalHeaders);
        var bridge=new CpfBrokerBridgeMessage("IBM_MQ",d,out.messageId(),payload,out.headers(),java.time.Instant.now());
        final byte[] body;try{body=mapper.writeValueAsBytes(bridge);}catch(Exception e){throw new IllegalArgumentException("IBM MQ bridge payload cannot be serialized",e);}
        if(body.length>properties.getMaxPayloadBytes())throw new IllegalArgumentException("IBM MQ bridge payload exceeds maximum size");
        try{template.send(d,session->{BytesMessage m=session.createBytesMessage();m.writeBytes(body);m.setJMSCorrelationID(out.headers().get(CpfMessageHeaderNames.TRANSACTION_ID));m.setStringProperty("cpfBridgeDestination",d);m.setStringProperty("cpfBridgeKey",out.messageId());write(m,out.headers());return m;});}
        catch(RuntimeException e){throw new java.lang.IllegalStateException("IBM MQ publish result is UNKNOWN; reconcile before retry",e);}
        return new CpfBrokerBridgeResult(true,"IBM_MQ",d,out.messageId(),out.headers().get(CpfMessageHeaderNames.TRANSACTION_ID),"JMS transaction accepted");
    }
    @Override public void subscribe(String destination,CpfBrokerBridgeHandler handler){String d=required(destination,"destination");if(!d.equals(properties.getDestination()))throw new IllegalArgumentException("IBM MQ subscription destination mismatch");Objects.requireNonNull(handler,"handler");String group=resolveGroup(d,handler.consumerGroup());String existing=consumerGroups.putIfAbsent(d,group);if(existing!=null&&!existing.equals(group))throw new java.lang.IllegalStateException("IBM MQ destination already bound to different CPF consumerGroup: "+d);handlers.computeIfAbsent(d,x->new CopyOnWriteArrayList<>()).add(handler);containers.computeIfAbsent(d,this::start);}
    private DefaultMessageListenerContainer start(String d){
        SimpleJmsListenerEndpoint endpoint=new SimpleJmsListenerEndpoint();
        endpoint.setId("cpf-ibm-mq-"+Integer.toUnsignedString(d.hashCode()));
        endpoint.setDestination(d);
        endpoint.setMessageListener((MessageListener)this::consume);
        var c=factory.createListenerContainer(endpoint);
        c.afterPropertiesSet();c.start();return c;
    }
    private void consume(Message provider){try{byte[] body=body(provider);if(body.length>properties.getMaxPayloadBytes())throw new IllegalArgumentException("IBM MQ bridge consumer payload exceeds maximum size");var message=mapper.readValue(body,CpfBrokerBridgeMessage.class);String pd=provider.getStringProperty("cpfBridgeDestination");if(pd!=null&&!pd.equals(message.destination()))throw new SecurityException("IBM MQ destination metadata mismatch");Map<String,String> wire=read(provider,message.headers());if(provider.getJMSCorrelationID()!=null&&!provider.getJMSCorrelationID().equals(wire.get(CpfMessageHeaderNames.TRANSACTION_ID)))throw new SecurityException("IBM MQ correlation/context mismatch");int attempt=provider.propertyExists("JMSXDeliveryCount")?Math.max(1,provider.getIntProperty("JMSXDeliveryCount")):1;String group=consumerGroups.get(message.destination());if(group==null||group.isBlank())throw new java.lang.IllegalStateException("IBM MQ CPF consumerGroup is not registered");var bundle=contextSupport.extractInbound("IBM_MQ",message.key(),message.destination(),null,group,null,null,attempt,provider.getJMSRedelivered(),null,null,wire,null);var targets=handlers.getOrDefault(message.destination(),new CopyOnWriteArrayList<>());if(targets.isEmpty())throw new java.lang.IllegalStateException("No IBM MQ bridge consumer registered");contextSupport.consume(bundle,()->targets.forEach(h->h.handle(message)));provider.acknowledge();}catch(Exception e){throw new java.lang.IllegalStateException("IBM MQ bridge consumer rejected message for redelivery/DLQ",e);}}
    private static void write(Message m,Map<String,String> h)throws JMSException{for(var e:CTX.entrySet()){String v=h.get(e.getKey());if(v!=null&&!v.isBlank())m.setStringProperty(e.getValue(),v);}}
    private static Map<String,String> read(Message m,Map<String,String> body)throws JMSException{Map<String,String> r=new LinkedHashMap<>();if(body!=null)r.putAll(body);for(var e:CTX.entrySet()){String v=m.getStringProperty(e.getValue());if(v!=null&&!v.isBlank()){String old=r.put(e.getKey(),v);if(old!=null&&!old.equals(v))throw new SecurityException("IBM MQ body/wire context mismatch: "+e.getKey());}}return Map.copyOf(r);}
    private static byte[] body(Message m)throws Exception{if(m instanceof BytesMessage b){long len=b.getBodyLength();if(len<0||len>Integer.MAX_VALUE)throw new IllegalArgumentException("invalid IBM MQ body length");byte[] out=new byte[(int)len];int pos=0;while(pos<out.length){int n=b.readBytes(out,out.length-pos);if(n<0)break;pos+=n;}if(pos!=out.length)throw new IllegalArgumentException("truncated IBM MQ body");return out;}if(m instanceof TextMessage t)return t.getText().getBytes(StandardCharsets.UTF_8);throw new IllegalArgumentException("unsupported IBM MQ message type");}
    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
    private static String resolveGroup(String destination,String requested){return requested!=null&&!requested.isBlank()?requested.trim():destination;}
    @Override public List<CpfBrokerBridgeMessage> findRecent(String destination,int limit){return List.of();}
    @Override public void close(){containers.values().forEach(c->{try{c.stop();}finally{c.destroy();}});containers.clear();handlers.clear();consumerGroups.clear();}
}
