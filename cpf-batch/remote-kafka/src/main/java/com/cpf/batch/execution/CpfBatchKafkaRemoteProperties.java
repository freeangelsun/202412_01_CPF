package com.cpf.batch.execution;

import java.time.Duration;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("cpf.batch.remote.kafka")
public record CpfBatchKafkaRemoteProperties(
        String requestTopic,
        String replyTopicPrefix,
        String consumerGroup,
        String managerInstanceId,
        String producerId,
        String environment,
        String tenantId,
        Role role,
        Duration sendTimeout,
        Duration messageTtl,
        int maxEnvelopeBytes,
        int maxPayloadBytes,
        int maxHeaders,
        int maxHeaderValueLength,
        long retryBackoffMillis,
        int maxDeliveryAttempts,
        Set<String> allowedProducerIds,
        int maxPayloadDepth,
        int replyQueueCapacity) {
    public enum Role { MANAGER, WORKER, ALL }

    /** QA32 생성자와 Source 호환을 유지합니다. */
    public CpfBatchKafkaRemoteProperties(
            String requestTopic,String replyTopicPrefix,String consumerGroup,String managerInstanceId,
            String producerId,String environment,String tenantId,Role role,Duration sendTimeout,
            Duration messageTtl,int maxEnvelopeBytes,int maxPayloadBytes,int maxHeaders,
            int maxHeaderValueLength,long retryBackoffMillis,int maxDeliveryAttempts){
        this(requestTopic,replyTopicPrefix,consumerGroup,managerInstanceId,producerId,environment,tenantId,
                role,sendTimeout,messageTtl,maxEnvelopeBytes,maxPayloadBytes,maxHeaders,maxHeaderValueLength,
                retryBackoffMillis,maxDeliveryAttempts,null,32,1000);
    }

    @ConstructorBinding
    public CpfBatchKafkaRemoteProperties {
        requestTopic = blank(requestTopic) ? "cpf.batch.remote.requests.v2" : requestTopic.trim();
        replyTopicPrefix = blank(replyTopicPrefix) ? "cpf.batch.remote.replies.v2" : replyTopicPrefix.trim();
        consumerGroup = blank(consumerGroup) ? "cpf-batch-remote-workers-v2" : consumerGroup.trim();
        managerInstanceId = blank(managerInstanceId) ? instanceId() : managerInstanceId.trim();
        producerId = blank(producerId) ? managerInstanceId : producerId.trim();
        environment = blank(environment) ? "local" : environment.trim();
        tenantId = blank(tenantId) ? "default" : tenantId.trim();
        role = role == null ? Role.ALL : role;
        sendTimeout = positive(sendTimeout, Duration.ofSeconds(30));
        messageTtl = positive(messageTtl, Duration.ofMinutes(5));
        maxEnvelopeBytes = maxEnvelopeBytes <= 0 ? 2 * 1024 * 1024 : maxEnvelopeBytes;
        maxPayloadBytes = maxPayloadBytes <= 0 ? 1024 * 1024 : maxPayloadBytes;
        maxHeaders = maxHeaders <= 0 ? 32 : maxHeaders;
        maxHeaderValueLength = maxHeaderValueLength <= 0 ? 512 : maxHeaderValueLength;
        retryBackoffMillis = retryBackoffMillis <= 0 ? 1000L : retryBackoffMillis;
        maxDeliveryAttempts = maxDeliveryAttempts <= 0 ? 5 : maxDeliveryAttempts;
        allowedProducerIds = allowedProducerIds == null || allowedProducerIds.isEmpty()
                ? Set.of(producerId)
                : allowedProducerIds.stream().filter(v -> !blank(v)).map(String::trim).collect(java.util.stream.Collectors.toUnmodifiableSet());
        maxPayloadDepth = maxPayloadDepth <= 0 ? 32 : maxPayloadDepth;
        replyQueueCapacity = replyQueueCapacity <= 0 ? 1000 : replyQueueCapacity;
        validateTopic(requestTopic,"requestTopic");
        validateTopic(replyTopicPrefix+"."+managerInstanceId,"managerReplyTopic");
        validateIdentifier(consumerGroup,255,"consumerGroup");
        validateIdentifier(managerInstanceId,128,"managerInstanceId");
        validateIdentifier(producerId,128,"producerId");
        validateIdentifier(environment,64,"environment");
        validateIdentifier(tenantId,64,"tenantId");
        allowedProducerIds.forEach(value->validateIdentifier(value,128,"allowedProducerId"));
        if (messageTtl.compareTo(Duration.ofHours(1)) > 0) throw new IllegalArgumentException("remote message TTL exceeds one hour");
        if (maxPayloadBytes > maxEnvelopeBytes) throw new IllegalArgumentException("payload limit exceeds envelope limit");
        if (maxPayloadDepth > 128) throw new IllegalArgumentException("payload depth exceeds 128");
        if (replyQueueCapacity > 100000) throw new IllegalArgumentException("reply queue capacity exceeds 100000");
    }
    public boolean managerEnabled(){return role==Role.MANAGER||role==Role.ALL;}
    public boolean workerEnabled(){return role==Role.WORKER||role==Role.ALL;}
    public String managerReplyTopic(){return replyTopicPrefix+"."+managerInstanceId;}
    private static void validateTopic(String value,String name){
        if(value.length()>249||value.startsWith(".")||value.endsWith(".")||value.contains("..")
                ||!value.matches("[A-Za-z0-9._-]+"))throw new IllegalArgumentException(name+" is invalid");
    }
    private static void validateIdentifier(String value,int max,String name){
        if(value.length()>max||!value.matches("[A-Za-z0-9._:-]+"))throw new IllegalArgumentException(name+" is invalid");
    }
    private static Duration positive(Duration value,Duration fallback){return value==null||value.isZero()||value.isNegative()?fallback:value;}
    private static boolean blank(String value){return value==null||value.isBlank();}
    private static String instanceId(){return com.cpf.foundation.runtime.CpfInstanceIdentity.current().instanceId();}
}
