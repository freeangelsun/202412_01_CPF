package com.cpf.batch.execution;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

/** Manager별 전용 Reply Topic을 소비하고 Durable Ledger 완료 후 ACK합니다. */
public final class CpfBatchKafkaManagerListener {
    private final CpfBatchKafkaInboundBridge bridge;
    public CpfBatchKafkaManagerListener(CpfBatchKafkaInboundBridge bridge){this.bridge=bridge;}
    @KafkaListener(topics="#{@cpfBatchManagerReplyTopic}",groupId="#{@cpfBatchManagerReplyGroup}",containerFactory="cpfBatchKafkaManualAckContainerFactory")
    public void reply(String json,Acknowledgment acknowledgment){bridge.reply(json);acknowledgment.acknowledge();}
}
