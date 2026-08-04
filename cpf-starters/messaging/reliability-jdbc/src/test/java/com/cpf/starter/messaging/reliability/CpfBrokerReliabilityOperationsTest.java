package com.cpf.starter.messaging.reliability;

import com.cpf.core.common.broker.CpfBrokerReplayPort;
import com.cpf.core.common.broker.CpfBrokerResult;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfBrokerReliabilityOperationsTest {

    @Test
    void replayUsesTransactionAndPassesNormalizedMessageId() throws Exception {
        RecordingReplayPort port = new RecordingReplayPort();
        CpfBrokerReliabilityOperations operations = new CpfBrokerReliabilityOperations(port);

        CpfBrokerResult result = operations.replay(" msg-1 ", " operator-1 ", " replay incident ");

        assertThat(result.status()).isEqualTo("ACCEPTED");
        assertThat(port.messageId).isEqualTo("msg-1");
        Method method = CpfBrokerReliabilityOperations.class.getMethod(
                "replay", String.class, String.class, String.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void invalidAuditOrMessageIsRejectedBeforePortSideEffect() {
        RecordingReplayPort port = new RecordingReplayPort();
        CpfBrokerReliabilityOperations operations = new CpfBrokerReliabilityOperations(port);

        assertThatThrownBy(() -> operations.replay(" ", "operator", "reason"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> operations.replay("msg", " ", "reason"))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> operations.replay("msg", "operator", " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(port.calls).isZero();
    }

    @Test
    void replayRangeRejectsInvalidBoundaryAndDoesNotSilentlyClampLimit() {
        RecordingReplayPort port = new RecordingReplayPort();
        CpfBrokerReliabilityOperations operations = new CpfBrokerReliabilityOperations(port);
        Instant now = Instant.parse("2026-08-04T00:00:00Z");

        assertThatThrownBy(() -> operations.replayRange("topic", now, now.minusSeconds(1), 10, "op", "reason"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> operations.replayRange("topic", null, null, 0, "op", "reason"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> operations.replayRange("topic", null, null, 5001, "op", "reason"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(port.calls).isZero();
    }

    @Test
    void replayRangeNormalizesBlankTopicAndPassesExactLimit() {
        RecordingReplayPort port = new RecordingReplayPort();
        CpfBrokerReliabilityOperations operations = new CpfBrokerReliabilityOperations(port);

        operations.replayRange(" ", null, null, 5000, "op", "reason");

        assertThat(port.topic).isNull();
        assertThat(port.limit).isEqualTo(5000);
        assertThat(port.calls).isEqualTo(1);
    }

    private static final class RecordingReplayPort implements CpfBrokerReplayPort {
        private int calls;
        private String messageId;
        private String topic;
        private int limit;

        @Override
        public CpfBrokerResult replay(String messageId) {
            calls++;
            this.messageId = messageId;
            return CpfBrokerResult.accepted(messageId, "TEST", messageId);
        }

        @Override
        public List<CpfBrokerResult> replayRange(String topic, Instant from, Instant to, int limit) {
            calls++;
            this.topic = topic;
            this.limit = limit;
            return List.of();
        }
    }
}
