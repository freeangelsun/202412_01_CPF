package com.cpf.batch.execution;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.batch.remote.kafka")
public record CpfBatchKafkaRemoteProperties(
        String requestTopic,
        String replyTopic,
        String consumerGroup,
        String replyGroup,
        Role role,
        Duration sendTimeout) {
    public enum Role { MANAGER, WORKER, ALL }
    public CpfBatchKafkaRemoteProperties {
        requestTopic = blank(requestTopic) ? "cpf.batch.remote.requests.v1" : requestTopic.trim();
        replyTopic = blank(replyTopic) ? "cpf.batch.remote.replies.v1" : replyTopic.trim();
        consumerGroup = blank(consumerGroup) ? "cpf-batch-remote-workers" : consumerGroup.trim();
        replyGroup = blank(replyGroup) ? "cpf-batch-remote-managers" : replyGroup.trim();
        role = role == null ? Role.ALL : role;
        sendTimeout = sendTimeout == null || sendTimeout.isNegative() || sendTimeout.isZero()
                ? Duration.ofSeconds(30) : sendTimeout;
    }
    public boolean managerEnabled() { return role == Role.MANAGER || role == Role.ALL; }
    public boolean workerEnabled() { return role == Role.WORKER || role == Role.ALL; }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
