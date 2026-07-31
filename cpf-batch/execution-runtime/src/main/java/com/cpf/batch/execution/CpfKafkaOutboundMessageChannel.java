package com.cpf.batch.execution;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

/** Broker ACK를 확인하고 승인된 Manager별 Reply Topic만 허용하는 Kafka MessageChannel입니다. */
public final class CpfKafkaOutboundMessageChannel implements MessageChannel {
    private final KafkaTemplate<String,String> kafka; private final CpfBatchRemoteCodec codec;
    private final String defaultTopic; private final String dynamicTopicPrefix; private final Duration timeout;
    public CpfKafkaOutboundMessageChannel(KafkaTemplate<String,String> kafka,CpfBatchRemoteCodec codec,String defaultTopic,String dynamicTopicPrefix,Duration timeout){this.kafka=kafka;this.codec=codec;this.defaultTopic=defaultTopic;this.dynamicTopicPrefix=dynamicTopicPrefix;this.timeout=timeout;}
    @Override public boolean send(Message<?> message){return send(message,timeout.toMillis());}
    @Override public boolean send(Message<?> message,long timeoutMillis){
        CpfBatchRemoteEnvelope envelope=codec.encode(message);String json=codec.encodeJson(envelope);String topic=resolveTopic(message,envelope);
        try{kafka.send(topic,envelope.messageId(),json).get(Math.max(1L,timeoutMillis),TimeUnit.MILLISECONDS);return true;}
        catch(InterruptedException interrupted){Thread.currentThread().interrupt();throw new IllegalStateException("BATCH_REMOTE_KAFKA_INTERRUPTED",interrupted);}
        catch(Exception failure){throw new IllegalStateException("BATCH_REMOTE_KAFKA_ACK_FAILED",failure);}
    }
    private String resolveTopic(Message<?> message,CpfBatchRemoteEnvelope envelope){
        Object requested=message.getHeaders().get(CpfBatchRemoteCodec.REPLY_TOPIC);
        if(requested==null){
            if(defaultTopic==null)throw new SecurityException("BATCH_REMOTE_REPLY_TOPIC_REQUIRED");
            CpfBatchRemoteCodec.requireTopic(defaultTopic,"BATCH_REMOTE_DEFAULT_TOPIC_DENIED");
            return defaultTopic;
        }
        if(dynamicTopicPrefix==null)throw new SecurityException("BATCH_REMOTE_DYNAMIC_TOPIC_DENIED");
        String topic=String.valueOf(requested);
        CpfBatchRemoteCodec.requireTopic(topic,"BATCH_REMOTE_REPLY_TOPIC_DENIED");
        if(!topic.startsWith(dynamicTopicPrefix+"."))throw new SecurityException("BATCH_REMOTE_REPLY_TOPIC_DENIED");
        return topic;
    }
}
