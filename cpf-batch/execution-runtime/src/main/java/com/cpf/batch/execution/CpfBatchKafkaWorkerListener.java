package com.cpf.batch.execution;

import org.springframework.kafka.annotation.KafkaListener;

/** Worker 역할에서만 Remote Step/Partition/Chunk 요청 Topic을 소비합니다. */
public final class CpfBatchKafkaWorkerListener {
    private final CpfBatchKafkaInboundBridge bridge;
    public CpfBatchKafkaWorkerListener(CpfBatchKafkaInboundBridge bridge) { this.bridge = bridge; }
    @KafkaListener(topics = "${cpf.batch.remote.kafka.request-topic:cpf.batch.remote.requests.v1}",
            groupId = "${cpf.batch.remote.kafka.consumer-group:cpf-batch-remote-workers}")
    public void request(String json) { bridge.request(json); }
}
