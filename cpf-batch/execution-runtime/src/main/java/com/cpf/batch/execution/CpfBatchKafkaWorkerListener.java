package com.cpf.batch.execution;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

/** Worker가 Durable Ledger 완료 후에만 Kafka offset을 manual ACK합니다. */
public final class CpfBatchKafkaWorkerListener {
    private final CpfBatchInboundHandler bridge;
    public CpfBatchKafkaWorkerListener(CpfBatchInboundHandler bridge){this.bridge=bridge;}
    @KafkaListener(topics="#{@cpfBatchRequestTopic}",groupId="#{@cpfBatchWorkerGroupId}",containerFactory="cpfBatchKafkaManualAckContainerFactory")
    public void request(String json,Acknowledgment acknowledgment){bridge.request(json);acknowledgment.acknowledge();}
}
