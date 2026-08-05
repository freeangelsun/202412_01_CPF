package com.cpf.admin.opr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.cpf.core.api.batch.CpfBatchRiskCommand;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class AdmBatchApprovalServiceRequesterIdentityTest {
    @Mock JdbcTemplate jdbc;

    @Test
    void reserveValidatesFingerprintWithLedgerRequesterAndKeepsExecutorSeparate() {
        CpfBatchRiskCommand executorCommand = new CpfBatchRiskCommand(
                "requestRetry", "bat_execution", "42", "BATCH_RETRY",
                "approver-02", "incident recovery", "101", "idem-101", 7L, "");
        CpfBatchRiskCommand approvedSnapshot =
                AdmBatchApprovalService.withRequestUser(executorCommand, "requester-01");
        Map<String, Object> approval = new LinkedHashMap<>();
        approval.put("approval_request_id", 101L);
        approval.put("action_type", "BATCH_RETRY");
        approval.put("owner_module", "BAT");
        approval.put("owner_command", "requestRetry");
        approval.put("target_type", "bat_execution");
        approval.put("target_id", "42");
        approval.put("requested_by", "requester-01");
        approval.put("command_payload_hash", approvedSnapshot.fingerprint());
        approval.put("approval_status", "APPROVED");
        approval.put("expire_at", Timestamp.from(Instant.now().plusSeconds(600)));
        approval.put("version_no", 3L);

        when(jdbc.queryForMap(anyString(), eq(101L)))
                .thenReturn(approval)
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq(101L), eq("approver-02"), eq("requester-01")))
                .thenReturn(1L);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);

        AdmBatchApprovalService.Reservation reservation =
                new AdmBatchApprovalService(jdbc).reserve(executorCommand);

        assertThat(reservation.requestedBy()).isEqualTo("requester-01");
        assertThat(reservation.commandRequestId()).isEqualTo("idem-101");
        assertThat(reservation.replay()).isFalse();
    }


    @Test
    void reserveRejectsExecutorWhoDidNotApprove() {
        CpfBatchRiskCommand command = command("approver-02", "101", "idem-101");
        when(jdbc.queryForMap(anyString(), eq(101L)))
                .thenReturn(approval(command, "requester-01", Instant.now().plusSeconds(600)))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq(101L), eq("approver-02"), eq("requester-01")))
                .thenReturn(0L);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new AdmBatchApprovalService(jdbc).reserve(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("executing operator");
    }

    @Test
    void reserveRejectsMissingExpiry() {
        CpfBatchRiskCommand command = command("approver-02", "101", "idem-101");
        Map<String,Object> row = approval(command, "requester-01", null);
        when(jdbc.queryForMap(anyString(), eq(101L)))
                .thenReturn(row)
                .thenThrow(new EmptyResultDataAccessException(1));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new AdmBatchApprovalService(jdbc).reserve(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expiry is required");
    }

    private static CpfBatchRiskCommand command(String executor, String approvalId, String idempotencyKey) {
        return new CpfBatchRiskCommand(
                "requestRetry", "bat_execution", "42", "BATCH_RETRY", executor,
                "incident recovery", approvalId, idempotencyKey, 7L, "");
    }

    private static Map<String,Object> approval(CpfBatchRiskCommand executorCommand, String requester, Instant expiry) {
        CpfBatchRiskCommand approved = AdmBatchApprovalService.withRequestUser(executorCommand, requester);
        Map<String,Object> row = new LinkedHashMap<>();
        row.put("approval_request_id", 101L); row.put("action_type", "BATCH_RETRY");
        row.put("owner_module", "BAT"); row.put("owner_command", "requestRetry");
        row.put("target_type", "bat_execution"); row.put("target_id", "42");
        row.put("requested_by", requester); row.put("command_payload_hash", approved.fingerprint());
        row.put("approval_status", "APPROVED"); row.put("expire_at", expiry == null ? null : Timestamp.from(expiry));
        row.put("version_no", 3L); return row;
    }
}
