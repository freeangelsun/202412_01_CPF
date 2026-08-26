package com.cpf.admin.opr.service;

import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.platform.operations.api.reliability.CpfReliabilityOperationsPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class AdmReliabilityServiceTest {

    @Test
    void delegatesReliabilityQueriesToCpfPublicPort() {
        CpfReliabilityOperationsPort port = mock(CpfReliabilityOperationsPort.class);
        when(port.findOutbox("FAILED", "TX-1", "cpf.topic", 1000))
                .thenReturn(List.of(Map.of("messageId", "M1")));
        AdmReliabilityService service = new AdmReliabilityService(port);

        List<Map<String, Object>> result = service.findOutbox("FAILED", "TX-1", "cpf.topic", 1000);

        assertThat(result).singleElement()
                .satisfies(row -> assertThat(row).containsEntry("messageId", "M1"));
        verify(port).findOutbox("FAILED", "TX-1", "cpf.topic", 1000);
    }

    @Test
    void dlqReplayCreatesExpiringApprovalSnapshotWithoutDirectMutation() {
        CpfReliabilityOperationsPort port = mock(CpfReliabilityOperationsPort.class);
        AdmApprovalService approvals = mock(AdmApprovalService.class);
        Map<String, Object> dlq = Map.ofEntries(
                Map.entry("dlq_id", 7L),
                Map.entry("message_id", "M1"),
                Map.entry("topic", "cpf.topic"),
                Map.entry("transaction_id", "TX-1"),
                Map.entry("segment_id", "SEG-1"),
                Map.entry("failure_reason", "token=must-not-copy"),
                Map.entry("replay_status", "WAITING"),
                Map.entry("replay_count", 2),
                Map.entry("updated_at", Timestamp.from(Instant.parse("2026-08-05T01:00:00Z"))));
        when(port.findDlq(null, null, null, 1_000)).thenReturn(List.of(dlq));
        when(approvals.requestApproval(any(), eq("OP-1")))
                .thenReturn(Map.of("approvalRequestId", 99L, "approvalStatus", "PENDING"));
        AdmReliabilityService service = new AdmReliabilityService(port);
        service.setApprovalService(approvals);

        Map<String, Object> result = service.requestDlqReplayApproval("M1", "OP-1", "incident replay");

        assertThat(result).containsEntry("approvalRequestId", 99L);
        ArgumentCaptor<AdmApprovalService.CreateRequest> captor =
                ArgumentCaptor.forClass(AdmApprovalService.CreateRequest.class);
        verify(approvals).requestApproval(captor.capture(), eq("OP-1"));
        AdmApprovalService.CreateRequest request = captor.getValue();
        assertThat(request.actionType()).isEqualTo("BROKER_DLQ_REPLAY");
        assertThat(request.ownerModule()).isEqualTo("cpf-starters-messaging-reliability-jdbc");
        assertThat(request.ownerCommand()).isEqualTo("BROKER_DLQ_REPLAY");
        assertThat(request.targetType()).isEqualTo("CPF_BROKER_DLQ");
        assertThat(request.payloadSnapshot()).doesNotContain("token", "must-not-copy", "failure_reason");
        assertThat(request.expireAt()).isAfter(Instant.now());
    }

    @Test
    void dlqReplayFailsClosedWithoutApprovalEngineAndDirectApi() {
        CpfReliabilityOperationsPort port = mock(CpfReliabilityOperationsPort.class);
        AdmReliabilityService service = new AdmReliabilityService(port);

        assertThatThrownBy(() -> service.requestDlqReplayApproval("M1", "OP-1", "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Approval Service");
        assertThatThrownBy(() -> service.requestDlqReplay("M1", "OP-1", "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("승인 요청");
    }

    @Test
    void mapsCpfChangeResultWithoutLosingAuditReason() {
        CpfReliabilityOperationsPort port = mock(CpfReliabilityOperationsPort.class);
        when(port.findUnknownResult("U1"))
                .thenReturn(Optional.of(Map.of("unknown_type", "SERVICE_CALL")));
        when(port.resolveUnknown("U1", "CONFIRMED_SUCCESS", 7L, "OP1", "외부 결과 확인"))
                .thenReturn(new CpfReliabilityOperationsPort.ChangeResult(
                        Map.of("unknownStatus", "CHECK_PENDING"),
                        Map.of("unknownStatus", "CONFIRMED_SUCCESS"),
                        "외부 결과 확인"));
        AdmReliabilityService service = new AdmReliabilityService(port);

        AdmReliabilityService.ChangeResult result = service.resolveUnknown(
                "U1", "CONFIRMED_SUCCESS", 7L, "OP1", "외부 결과 확인");

        assertThat(result.before()).containsEntry("unknownStatus", "CHECK_PENDING");
        assertThat(result.after()).containsEntry("unknownStatus", "CONFIRMED_SUCCESS");
        assertThat(result.reason()).isEqualTo("외부 결과 확인");
    }
}
