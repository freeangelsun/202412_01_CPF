package com.cpf.batch.execution;

import org.springframework.batch.integration.chunk.ChunkRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

/** Kafka payload를 Spring Batch 표준 Remote Channel로 분류합니다. Listener 생명주기는 역할별 Bean이 소유합니다. */
public final class CpfBatchKafkaInboundBridge {
    private final CpfBatchRemoteCodec codec;
    private final MessageChannel stepRequests;
    private final MessageChannel chunkRequests;
    private final PollableChannel replies;

    public CpfBatchKafkaInboundBridge(CpfBatchRemoteCodec codec, MessageChannel stepRequests,
            MessageChannel chunkRequests, PollableChannel replies) {
        this.codec = codec; this.stepRequests = stepRequests;
        this.chunkRequests = chunkRequests; this.replies = replies;
    }
    public void request(String json) {
        Message<?> message = codec.decodeJson(json);
        boolean accepted = message.getPayload() instanceof ChunkRequest<?>
                ? chunkRequests.send(message) : stepRequests.send(message);
        if (!accepted) throw new IllegalStateException("BATCH_REMOTE_WORKER_CHANNEL_REJECTED");
    }
    public void reply(String json) {
        if (!replies.send(codec.decodeJson(json)))
            throw new IllegalStateException("BATCH_REMOTE_MANAGER_REPLY_REJECTED");
    }
}
