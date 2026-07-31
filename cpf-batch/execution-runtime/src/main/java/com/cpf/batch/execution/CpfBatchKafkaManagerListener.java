package com.cpf.batch.execution;

import org.springframework.kafka.annotation.KafkaListener;

/** Manager 역할에서만 Worker 응답 Topic을 소비합니다. */
public final class CpfBatchKafkaManagerListener {
    private final CpfBatchKafkaInboundBridge bridge;
    public CpfBatchKafkaManagerListener(CpfBatchKafkaInboundBridge bridge) { this.bridge = bridge; }
    @KafkaListener(topics = "${cpf.batch.remote.kafka.reply-topic:cpf.batch.remote.replies.v1}",
            groupId = "${cpf.batch.remote.kafka.reply-group:cpf-batch-remote-managers}")
    public void reply(String json) { bridge.reply(json); }
}
