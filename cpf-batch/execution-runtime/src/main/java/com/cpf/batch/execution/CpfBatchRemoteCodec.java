package com.cpf.batch.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.integration.chunk.ChunkRequest;
import org.springframework.batch.integration.chunk.ChunkResponse;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/** 허용된 Remote Batch Payload만 Version 2 Envelope로 bounded 직렬화합니다. */
public final class CpfBatchRemoteCodec {
    public static final String ATTEMPT = "cpfBatchAttempt";
    public static final String CORRELATION_ID = "cpfBatchCorrelationId";
    public static final String MESSAGE_ID = "cpfBatchRemoteMessageId";
    public static final String REPLY_TOPIC = "cpfBatchReplyTopic";
    private final ObjectMapper mapper;
    private final Map<String,JavaType> allowedTypes;
    private final CpfBatchKafkaRemoteProperties properties;
    private final Clock clock;

    public CpfBatchRemoteCodec(ObjectMapper mapper,CpfBatchKafkaRemoteProperties properties){this(mapper,properties,Clock.systemUTC());}
    CpfBatchRemoteCodec(ObjectMapper mapper,CpfBatchKafkaRemoteProperties properties,Clock clock){
        this.mapper=mapper;this.properties=properties;this.clock=clock;
        this.allowedTypes=Map.of(
                StepExecutionRequest.class.getName(),mapper.constructType(StepExecutionRequest.class),
                ChunkRequest.class.getName(),mapper.constructType(ChunkRequest.class),
                ChunkResponse.class.getName(),mapper.constructType(ChunkResponse.class),
                StepExecution.class.getName(),mapper.constructType(StepExecution.class));
    }

    public CpfBatchRemoteEnvelope encode(Message<?> message){
        Object payload=message.getPayload();String type=payload.getClass().getName();requireAllowed(type);
        try{
            String payloadJson=mapper.writeValueAsString(payload);requireBytes(payloadJson,properties.maxPayloadBytes(),"BATCH_REMOTE_PAYLOAD_TOO_LARGE");
            int attempt=positiveInt(message.getHeaders().get(ATTEMPT),1);
            String payloadHash=sha256(payloadJson);
            String stable=properties.environment()+"\n"+properties.tenantId()+"\n"+type+"\n"+payloadHash;
            String messageId=text(message.getHeaders().get(MESSAGE_ID));if(messageId==null)messageId=sha256(stable);
            String correlation=text(message.getHeaders().get(CORRELATION_ID));if(correlation==null)correlation=messageId;
            LinkedHashMap<String,Object> headers=copyHeaders(message);
            String replyTopic=text(message.getHeaders().get(REPLY_TOPIC));if(replyTopic==null)replyTopic=properties.managerReplyTopic();
            Instant created=clock.instant();
            return new CpfBatchRemoteEnvelope(CpfBatchRemoteEnvelope.CURRENT_SCHEMA_VERSION,messageId,correlation,
                    properties.producerId(),properties.environment(),properties.tenantId(),attempt,
                    replyTopic,type,payloadJson,payloadHash,Map.copyOf(headers),created,created.plus(properties.messageTtl()));
        }catch(JsonProcessingException failure){throw new IllegalArgumentException("BATCH_REMOTE_PAYLOAD_SERIALIZATION_FAILED",failure);}
    }

    public String encodeJson(CpfBatchRemoteEnvelope envelope){
        validate(envelope);
        try{String json=mapper.writeValueAsString(envelope);requireBytes(json,properties.maxEnvelopeBytes(),"BATCH_REMOTE_ENVELOPE_TOO_LARGE");return json;}
        catch(JsonProcessingException failure){throw new IllegalArgumentException("BATCH_REMOTE_ENVELOPE_SERIALIZATION_FAILED",failure);}
    }
    public CpfBatchRemoteEnvelope readEnvelope(String json){requireBytes(json,properties.maxEnvelopeBytes(),"BATCH_REMOTE_ENVELOPE_TOO_LARGE");try{CpfBatchRemoteEnvelope envelope=mapper.readValue(json,CpfBatchRemoteEnvelope.class);validate(envelope);return envelope;}catch(JsonProcessingException failure){throw new IllegalArgumentException("BATCH_REMOTE_ENVELOPE_DESERIALIZATION_FAILED",failure);}}
    public Message<?> decodeJson(String json){return decode(readEnvelope(json));}
    public Message<?> decode(CpfBatchRemoteEnvelope envelope){
        validate(envelope);JavaType type=requireAllowed(envelope.payloadType());
        try{
            Object payload=mapper.readValue(envelope.payloadJson(),type);MessageBuilder<Object> builder=MessageBuilder.withPayload(payload);
            envelope.headers().forEach((name,value)->{if(safe(value))builder.setHeaderIfAbsent(name,value);});
            builder.setHeader(MESSAGE_ID,envelope.messageId());builder.setHeader(CORRELATION_ID,envelope.correlationId());
            builder.setHeader(ATTEMPT,envelope.attempt());builder.setHeader(REPLY_TOPIC,envelope.replyTopic());
            builder.setHeader("cpfBatchProducerId",envelope.producerId());builder.setHeader("cpfBatchEnvironment",envelope.environment());builder.setHeader("cpfBatchTenantId",envelope.tenantId());
            return builder.build();
        }catch(JsonProcessingException failure){throw new IllegalArgumentException("BATCH_REMOTE_PAYLOAD_DESERIALIZATION_FAILED",failure);}
    }
    private void validate(CpfBatchRemoteEnvelope e){
        if(e.schemaVersion()!=CpfBatchRemoteEnvelope.CURRENT_SCHEMA_VERSION)throw new SecurityException("BATCH_REMOTE_SCHEMA_DENIED");
        requireIdentifier(e.messageId(), 128, "BATCH_REMOTE_MESSAGE_ID_DENIED");
        requireIdentifier(e.correlationId(), 128, "BATCH_REMOTE_CORRELATION_ID_DENIED");
        requireIdentifier(e.producerId(), 128, "BATCH_REMOTE_PRODUCER_ID_DENIED");
        requireIdentifier(e.environment(), 64, "BATCH_REMOTE_ENVIRONMENT_FORMAT_DENIED");
        requireIdentifier(e.tenantId(), 64, "BATCH_REMOTE_TENANT_FORMAT_DENIED");
        requireTopic(e.replyTopic(), "BATCH_REMOTE_REPLY_TOPIC_DENIED");
        if(e.attempt()>1000)throw new SecurityException("BATCH_REMOTE_ATTEMPT_EXCEEDED");
        if(!properties.environment().equals(e.environment()))throw new SecurityException("BATCH_REMOTE_ENVIRONMENT_DENIED");
        if(!properties.tenantId().equals(e.tenantId()))throw new SecurityException("BATCH_REMOTE_TENANT_DENIED");
        if(!properties.allowedProducerIds().contains(e.producerId()))throw new SecurityException("BATCH_REMOTE_PRODUCER_DENIED");
        if(!e.replyTopic().startsWith(properties.replyTopicPrefix()+"."))throw new SecurityException("BATCH_REMOTE_REPLY_TOPIC_DENIED");
        Instant now=clock.instant();
        Duration lifetime=Duration.between(e.createdAt(),e.expiresAt());
        if(e.createdAt().isAfter(now.plusSeconds(30))||!e.expiresAt().isAfter(now)
                ||lifetime.isNegative()||lifetime.isZero()||lifetime.compareTo(properties.messageTtl().plusSeconds(30))>0)
            throw new SecurityException("BATCH_REMOTE_MESSAGE_EXPIRED");
        requireBytes(e.payloadJson(),properties.maxPayloadBytes(),"BATCH_REMOTE_PAYLOAD_TOO_LARGE");
        requireJsonDepth(e.payloadJson(),properties.maxPayloadDepth());
        if(!sha256(e.payloadJson()).equals(e.payloadSha256()))throw new SecurityException("BATCH_REMOTE_PAYLOAD_HASH_MISMATCH");
        if(e.headers().size()>properties.maxHeaders())throw new SecurityException("BATCH_REMOTE_HEADER_COUNT_EXCEEDED");
        for(var entry:e.headers().entrySet()){if(entry.getKey().length()>128||String.valueOf(entry.getValue()).length()>properties.maxHeaderValueLength()||!safe(entry.getValue()))throw new SecurityException("BATCH_REMOTE_HEADER_DENIED:"+entry.getKey());}
    }
    private LinkedHashMap<String,Object> copyHeaders(Message<?> message){LinkedHashMap<String,Object> headers=new LinkedHashMap<>();message.getHeaders().forEach((name,value)->{if(name.startsWith("cpfBatch")&&safe(value)&&String.valueOf(value).length()<=properties.maxHeaderValueLength()&&headers.size()<properties.maxHeaders())headers.put(name,value);});return headers;}
    private JavaType requireAllowed(String type){JavaType value=allowedTypes.get(type);if(value==null)throw new SecurityException("BATCH_REMOTE_PAYLOAD_TYPE_DENIED:"+type);return value;}
    private static boolean safe(Object value){return value instanceof String||value instanceof Number||value instanceof Boolean;}

    private static void requireJsonDepth(String json,int maxDepth){
        int depth=0;boolean quoted=false;boolean escaped=false;
        for(int i=0;i<json.length();i++){
            char c=json.charAt(i);
            if(quoted){if(escaped){escaped=false;}else if(c=='\\'){escaped=true;}else if(c=='"'){quoted=false;}continue;}
            if(c=='"'){quoted=true;continue;}
            if(c=='{'||c=='['){depth++;if(depth>maxDepth)throw new SecurityException("BATCH_REMOTE_PAYLOAD_DEPTH_EXCEEDED");}
            else if(c=='}'||c==']'){depth--;if(depth<0)throw new SecurityException("BATCH_REMOTE_PAYLOAD_JSON_INVALID");}
        }
        if(quoted||depth!=0)throw new SecurityException("BATCH_REMOTE_PAYLOAD_JSON_INVALID");
    }
    private static void requireIdentifier(String value,int max,String code){
        if(value==null||value.length()>max||!value.matches("[A-Za-z0-9._:-]+"))throw new SecurityException(code);
    }
    static void requireTopic(String value,String code){
        if(value==null||value.isBlank()||value.length()>249||value.startsWith(".")||value.endsWith(".")
                ||value.contains("..")||!value.matches("[A-Za-z0-9._-]+"))throw new SecurityException(code);
    }
    private static int positiveInt(Object value,int fallback){if(value instanceof Number n&&n.intValue()>0)return n.intValue();if(value instanceof String s)try{int v=Integer.parseInt(s);return v>0?v:fallback;}catch(NumberFormatException ignored){}return fallback;}
    private static String text(Object value){return value instanceof String s&&!s.isBlank()?s:null;}
    private static void requireBytes(String value,int limit,String code){if(value==null||value.getBytes(StandardCharsets.UTF_8).length>limit)throw new IllegalArgumentException(code);}
    private static String sha256(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
