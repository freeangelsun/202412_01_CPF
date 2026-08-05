package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;

class CpfBatchKafkaInboundBridgeAttemptOwnerTest {
    @Test
    void usesOneBoundedUniqueOwnerPerDeliveryAttempt() {
        CpfBatchRemoteCodec codec = mock(CpfBatchRemoteCodec.class);
        CpfSynchronousWorkerChannel stepRequests = mock(CpfSynchronousWorkerChannel.class);
        CpfSynchronousWorkerChannel chunkRequests = mock(CpfSynchronousWorkerChannel.class);
        PollableChannel replies = mock(PollableChannel.class);
        Message<?> decoded = mock(Message.class);
        CpfBatchRemoteEnvelope first = mock(CpfBatchRemoteEnvelope.class);
        CpfBatchRemoteEnvelope second = mock(CpfBatchRemoteEnvelope.class);
        when(first.messageId()).thenReturn("M-1");
        when(second.messageId()).thenReturn("M-2");
        when(first.payloadSha256()).thenReturn("a".repeat(64));
        when(second.payloadSha256()).thenReturn("b".repeat(64));
        when(first.expiresAt()).thenReturn(Instant.now().plusSeconds(60));
        when(second.expiresAt()).thenReturn(Instant.now().plusSeconds(60));
        when(codec.readEnvelope("first")).thenReturn(first);
        when(codec.readEnvelope("second")).thenReturn(second);
        when(codec.decode(any())).thenReturn(decoded);
        when(stepRequests.send(decoded)).thenReturn(true);
        CapturingLedger ledger = new CapturingLedger();
        CpfBatchKafkaInboundBridge bridge = new CpfBatchKafkaInboundBridge(
                codec, stepRequests, chunkRequests, replies, ledger, "X".repeat(128));

        assertTrue(bridge.request("first"));
        assertTrue(bridge.request("second"));

        assertEquals(2, ledger.claimOwners.size());
        assertNotEquals(ledger.claimOwners.get(0), ledger.claimOwners.get(1));
        for (int i = 0; i < ledger.claimOwners.size(); i++) {
            String owner = ledger.claimOwners.get(i);
            assertEquals(owner, ledger.completeOwners.get(i));
            assertTrue(owner.length() <= 150);
            assertTrue(owner.startsWith("X".repeat(100) + ":"));
        }
        verify(stepRequests, org.mockito.Mockito.times(2)).send(decoded);
    }


    @Test
    void unknownSideEffectIsDurablyBlockedAndReturnsForKafkaAck() {
        CpfBatchRemoteCodec codec = mock(CpfBatchRemoteCodec.class);
        CpfSynchronousWorkerChannel stepRequests = mock(CpfSynchronousWorkerChannel.class);
        CpfSynchronousWorkerChannel chunkRequests = mock(CpfSynchronousWorkerChannel.class);
        PollableChannel replies = mock(PollableChannel.class);
        Message<?> decoded = mock(Message.class);
        CpfBatchRemoteEnvelope envelope = mock(CpfBatchRemoteEnvelope.class);
        when(envelope.messageId()).thenReturn("M-UNKNOWN");
        when(envelope.payloadSha256()).thenReturn("c".repeat(64));
        when(envelope.expiresAt()).thenReturn(Instant.now().plusSeconds(60));
        when(codec.readEnvelope("unknown")).thenReturn(envelope);
        when(codec.decode(envelope)).thenReturn(decoded);
        when(stepRequests.send(decoded)).thenThrow(
                new CpfBatchUnknownResultException("REMOTE_RESULT_UNKNOWN", "response lost"));
        CapturingLedger ledger = new CapturingLedger();
        CpfBatchKafkaInboundBridge bridge = new CpfBatchKafkaInboundBridge(
                codec, stepRequests, chunkRequests, replies, ledger, "worker-1");

        assertFalse(bridge.request("unknown"));

        assertEquals(List.of("M-UNKNOWN"), ledger.unknownMessages);
        assertEquals(List.of(), ledger.completeOwners);
        assertEquals(List.of(), ledger.failedMessages);
    }

    private static final class CapturingLedger implements CpfBatchRemoteMessageLedger {
        private final List<String> claimOwners = new ArrayList<>();
        private final List<String> completeOwners = new ArrayList<>();
        private final List<String> unknownMessages = new ArrayList<>();
        private final List<String> failedMessages = new ArrayList<>();

        @Override
        public Claim claim(String direction, String messageId, String payloadSha256,
                           Instant expiresAt, String ownerId) {
            claimOwners.add(ownerId);
            return Claim.CLAIMED;
        }

        @Override
        public void complete(String direction, String messageId, String ownerId) {
            completeOwners.add(ownerId);
        }

        @Override
        public void fail(String direction, String messageId, String ownerId, String errorCode) {
            failedMessages.add(messageId);
        }

        @Override
        public void unknown(String direction, String messageId, String ownerId, String errorCode) {
            unknownMessages.add(messageId);
        }
    }
}
