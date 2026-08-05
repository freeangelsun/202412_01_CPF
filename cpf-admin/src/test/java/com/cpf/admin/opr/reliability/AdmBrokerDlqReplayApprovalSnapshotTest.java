package com.cpf.admin.opr.reliability;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdmBrokerDlqReplayApprovalSnapshotTest {

    @Test
    void producesDeterministicSensitiveDataFreeSnapshotAcrossColumnCaseAndOrder() {
        Instant updatedAt = Instant.parse("2026-08-05T01:02:03Z");
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("MESSAGE_ID", "MSG-1");
        first.put("DLQ_ID", 7L);
        first.put("TOPIC", "cpf.orders");
        first.put("TRANSACTION_ID", "TX-1");
        first.put("SEGMENT_ID", "SEG-1");
        first.put("REPLAY_STATUS", "WAITING");
        first.put("REPLAY_COUNT", 2);
        first.put("UPDATED_AT", Timestamp.from(updatedAt));
        first.put("FAILURE_REASON", "token=do-not-copy");
        first.put("PAYLOAD", "secret-body");

        Map<String, Object> second = Map.of(
                "updatedAt", Timestamp.from(updatedAt),
                "replayCount", 2,
                "replayStatus", "WAITING",
                "segmentId", "SEG-1",
                "transactionId", "TX-1",
                "topic", "cpf.orders",
                "dlqId", 7L,
                "messageId", "MSG-1");

        var left = AdmBrokerDlqReplayApprovalSnapshot.from(first);
        var right = AdmBrokerDlqReplayApprovalSnapshot.from(second);

        assertThat(left.json()).isEqualTo(right.json());
        assertThat(left.hash()).isEqualTo(right.hash());
        assertThat(left.json()).doesNotContain("token", "do-not-copy", "secret-body", "failureReason");
        assertThat(left.replayCount()).isEqualTo(2);
        assertThat(left.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void rejectsSnapshotWithoutOptimisticVersionTimestamp() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("message_id", "MSG-1");
        row.put("dlq_id", 1L);
        row.put("replay_status", "WAITING");
        row.put("replay_count", 0);

        assertThatThrownBy(() -> AdmBrokerDlqReplayApprovalSnapshot.from(row))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("updated_at");
    }
}
