package com.cpf.batch.execution;

import java.util.UUID;
import org.springframework.batch.integration.chunk.ChunkRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;

/** Kafka Envelope를 Durable claim 후 Spring Batch Remote Channel로 전달합니다. */
public final class CpfBatchKafkaInboundBridge implements CpfBatchInboundHandler {
    private final CpfBatchRemoteCodec codec;
    private final CpfSynchronousWorkerChannel stepRequests;
    private final CpfSynchronousWorkerChannel chunkRequests;
    private final PollableChannel replies;
    private final CpfBatchRemoteMessageLedger ledger;
    private final String ownerId;

    public CpfBatchKafkaInboundBridge(
            CpfBatchRemoteCodec codec,
            CpfSynchronousWorkerChannel stepRequests,
            CpfSynchronousWorkerChannel chunkRequests,
            PollableChannel replies,
            CpfBatchRemoteMessageLedger ledger,
            String ownerId) {
        this.codec = codec;
        this.stepRequests = stepRequests;
        this.chunkRequests = chunkRequests;
        this.replies = replies;
        this.ledger = ledger;
        this.ownerId = ownerId;
    }

    public boolean request(String json) {
        return accept("REQUEST", json, message -> {
            boolean accepted = message.getPayload() instanceof ChunkRequest<?>
                    ? chunkRequests.send(message)
                    : stepRequests.send(message);
            if (!accepted) throw new IllegalStateException("BATCH_REMOTE_WORKER_CHANNEL_REJECTED");
        });
    }

    public boolean reply(String json) {
        return accept("REPLY", json, message -> {
            if (!replies.send(message)) {
                throw new IllegalStateException("BATCH_REMOTE_MANAGER_REPLY_REJECTED");
            }
        });
    }

    private boolean accept(
            String direction,
            String json,
            java.util.function.Consumer<Message<?>> consumer) {
        CpfBatchRemoteEnvelope envelope = codec.readEnvelope(json);
        String attemptOwnerId = attemptOwnerId();
        CpfBatchRemoteMessageLedger.Claim claim = ledger.claim(
                direction,
                envelope.messageId(),
                envelope.payloadSha256(),
                envelope.expiresAt(),
                attemptOwnerId);
        if (claim == CpfBatchRemoteMessageLedger.Claim.DUPLICATE_COMPLETE
                || claim == CpfBatchRemoteMessageLedger.Claim.UNKNOWN_RECONCILE_REQUIRED) {
            // Kafka listener가 offset을 ACK한다. UNKNOWN은 운영 Reconcile 전까지 재실행하지 않는다.
            return false;
        }
        if (claim == CpfBatchRemoteMessageLedger.Claim.IN_PROGRESS) {
            throw new IllegalStateException("BATCH_REMOTE_MESSAGE_IN_PROGRESS");
        }
        try {
            consumer.accept(codec.decode(envelope));
            ledger.complete(direction, envelope.messageId(), attemptOwnerId);
            return true;
        } catch (CpfBatchUnknownResultException unknown) {
            /*
             * 외부 side effect 또는 ACK/response 손실 가능성이 있으므로 FAILED로 낮추지 않는다.
             * UNKNOWN 저장 후 정상 반환해 Kafka redelivery를 ACK하고 명시적 대사를 기다린다.
             */
            ledger.unknown(
                    direction,
                    envelope.messageId(),
                    attemptOwnerId,
                    unknown.getClass().getSimpleName());
            return false;
        } catch (RuntimeException failure) {
            try {
                ledger.fail(
                        direction,
                        envelope.messageId(),
                        attemptOwnerId,
                        failure.getClass().getSimpleName());
            } catch (RuntimeException fence) {
                failure.addSuppressed(fence);
            }
            throw failure;
        }
    }

    private String attemptOwnerId() {
        String stablePrefix = ownerId.length() <= 100 ? ownerId : ownerId.substring(0, 100);
        return stablePrefix + ":" + UUID.randomUUID();
    }
}
