package com.cpf.batch.execution;

import org.springframework.batch.integration.chunk.ChunkRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

/** Kafka Envelope를 Durable claim 후 Spring Batch Remote Channel로 전달합니다. */
public final class CpfBatchKafkaInboundBridge {
    private final CpfBatchRemoteCodec codec; private final MessageChannel stepRequests; private final MessageChannel chunkRequests;
    private final PollableChannel replies; private final CpfBatchRemoteMessageLedger ledger; private final String ownerId;
    public CpfBatchKafkaInboundBridge(CpfBatchRemoteCodec codec,MessageChannel stepRequests,MessageChannel chunkRequests,
            PollableChannel replies,CpfBatchRemoteMessageLedger ledger,String ownerId){this.codec=codec;this.stepRequests=stepRequests;this.chunkRequests=chunkRequests;this.replies=replies;this.ledger=ledger;this.ownerId=ownerId;}
    public boolean request(String json){return accept("REQUEST",json,message->{boolean accepted=message.getPayload() instanceof ChunkRequest<?>?chunkRequests.send(message):stepRequests.send(message);if(!accepted)throw new IllegalStateException("BATCH_REMOTE_WORKER_CHANNEL_REJECTED");});}
    public boolean reply(String json){return accept("REPLY",json,message->{if(!replies.send(message))throw new IllegalStateException("BATCH_REMOTE_MANAGER_REPLY_REJECTED");});}
    private boolean accept(String direction,String json,java.util.function.Consumer<Message<?>> consumer){
        CpfBatchRemoteEnvelope envelope=codec.readEnvelope(json);
        CpfBatchRemoteMessageLedger.Claim claim=ledger.claim(direction,envelope.messageId(),envelope.payloadSha256(),envelope.expiresAt(),ownerId);
        if(claim==CpfBatchRemoteMessageLedger.Claim.DUPLICATE_COMPLETE)return false;
        if(claim==CpfBatchRemoteMessageLedger.Claim.IN_PROGRESS)throw new IllegalStateException("BATCH_REMOTE_MESSAGE_IN_PROGRESS");
        try{consumer.accept(codec.decode(envelope));ledger.complete(direction,envelope.messageId(),ownerId);return true;}
        catch(RuntimeException failure){try{ledger.fail(direction,envelope.messageId(),ownerId,failure.getClass().getSimpleName());}catch(RuntimeException fence){failure.addSuppressed(fence);}throw failure;}
    }
}
