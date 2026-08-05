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
    void directReplayFailsClosedBeforePortSideEffect() throws Exception {
        RecordingReplayPort port = new RecordingReplayPort();
        CpfBrokerReliabilityOperations operations = new CpfBrokerReliabilityOperations(port);

        assertThatThrownBy(() -> operations.replay(" msg-1 ", " operator-1 ", " replay incident "))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("approved owner command");
        assertThat(port.calls).isZero();
        Method method = CpfBrokerReliabilityOperations.class.getMethod(
                "replay", String.class, String.class, String.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void invalidAuditOrMessageIsRejectedBeforeApprovalError() {
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
    void replayRangeValidatesBoundaryThenFailsClosed() {
        RecordingReplayPort port = new RecordingReplayPort();
        CpfBrokerReliabilityOperations operations = new CpfBrokerReliabilityOperations(port);
        Instant now = Instant.parse("2026-08-04T00:00:00Z");

        assertThatThrownBy(() -> operations.replayRange("topic", now, now.minusSeconds(1), 10, "op", "reason"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> operations.replayRange("topic", null, null, 0, "op", "reason"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> operations.replayRange("topic", null, null, 5001, "op", "reason"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> operations.replayRange("topic", null, null, 100, "op", "reason"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("per-target approval");
        assertThat(port.calls).isZero();
    }

    private static final class RecordingReplayPort implements CpfBrokerReplayPort {
        private int calls;

        @Override
        public CpfBrokerResult replay(String messageId) {
            calls++;
            return CpfBrokerResult.accepted(messageId, "TEST", messageId);
        }

        @Override
        public List<CpfBrokerResult> replayRange(String topic, Instant from, Instant to, int limit) {
            calls++;
            return List.of();
        }
    }
}
