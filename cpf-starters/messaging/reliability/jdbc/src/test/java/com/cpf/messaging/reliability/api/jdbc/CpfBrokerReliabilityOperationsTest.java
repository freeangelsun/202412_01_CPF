package com.cpf.messaging.reliability.api.jdbc;

import com.cpf.messaging.spi.broker.CpfBrokerReplayPort;
import com.cpf.messaging.spi.broker.CpfBrokerResult;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CpfBrokerReliabilityOperationsTest {

    @Test
    void approvedReplayUsesAtomicDatabaseStateTransitionWithoutLowLevelPort() throws Exception {
        RecordingReplayPort port = new RecordingReplayPort();
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Map<String, Object> before = dlq("WAITING", 0);
        Map<String, Object> after = dlq("REQUESTED", 1);
        when(jdbc.query(any(PreparedStatementCreator.class), any(ColumnMapRowMapper.class)))
                .thenReturn(List.of(before), List.of(after));
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        CpfBrokerReliabilityOperations operations = new CpfBrokerReliabilityOperations(port, jdbc);

        var result = operations.requestDlqReplay(" msg-1 ", " operator-1 ", " replay incident ");

        assertThat(result.before()).isEqualTo(before);
        assertThat(result.after()).isEqualTo(after);
        assertThat(result.reason()).isEqualTo("replay incident");
        assertThat(port.calls).isZero();
        verify(jdbc, times(2)).update(anyString(), any(Object[].class));
        Method method = CpfBrokerReliabilityOperations.class.getMethod(
                "requestDlqReplay", String.class, String.class, String.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void invalidApprovedReplayInputIsRejectedBeforeDatabaseMutation() {
        RecordingReplayPort port = new RecordingReplayPort();
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfBrokerReliabilityOperations operations = new CpfBrokerReliabilityOperations(port, jdbc);

        assertThatThrownBy(() -> operations.requestDlqReplay(" ", "operator", "reason"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> operations.requestDlqReplay("msg", " ", "reason"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> operations.requestDlqReplay("msg", "operator", " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(port.calls).isZero();
        verifyNoInteractions(jdbc);
    }

    private static Map<String, Object> dlq(String replayStatus, int replayCount) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("message_id", "msg-1");
        row.put("replay_status", replayStatus);
        row.put("replay_count", replayCount);
        return row;
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
