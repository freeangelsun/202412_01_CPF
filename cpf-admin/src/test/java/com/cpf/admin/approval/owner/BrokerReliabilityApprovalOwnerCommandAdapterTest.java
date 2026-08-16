package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.opr.reliability.AdmBrokerDlqReplayApprovalSnapshot;
import com.cpf.platform.operations.api.reliability.CpfReliabilityOperationsPort;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BrokerReliabilityApprovalOwnerCommandAdapterTest {

    @Test
    void supportsOnlyExactBrokerOwnerTupleWithoutPunctuationFolding() {
        CpfReliabilityOperationsPort operations = mock(CpfReliabilityOperationsPort.class);
        AdmApprovalRepository approvals = mock(AdmApprovalRepository.class);
        BrokerReliabilityApprovalOwnerCommandAdapter adapter =
                new BrokerReliabilityApprovalOwnerCommandAdapter(operations, approvals);

        assertThat(adapter.supports("cpf-starters-messaging-reliability-jdbc", "BROKER_DLQ_REPLAY", "BROKER_DLQ_REPLAY", "CPF_BROKER_DLQ")).isTrue();
        assertThat(adapter.supports("CPF-STARTERS-MESSAGING-RELIABILITY-JDBC", "BROKER_DLQ_REPLAY", "BROKER_DLQ_REPLAY", "CPF_BROKER_DLQ")).isFalse();
        assertThat(adapter.supports("cpf-starters-messaging-reliability-jdbc", "broker_dlq_replay", "BROKER_DLQ_REPLAY", "CPF_BROKER_DLQ")).isFalse();
        assertThat(adapter.supports("cpf-starters-messaging-reliability-jdbc", "BROKER_DLQ_REPLAY", "broker_dlq_replay", "CPF_BROKER_DLQ")).isFalse();
        assertThat(adapter.supports("cpf-starters-messaging-reliability-jdbc", "BROKER_DLQ_REPLAY", "BROKER_DLQ_REPLAY", "cpf_broker_dlq")).isFalse();
        assertThat(adapter.supports("cpf_starters_messaging_reliability_jdbc", "BROKER_DLQ_REPLAY", "BROKER_DLQ_REPLAY", "CPF_BROKER_DLQ")).isFalse();
        assertThat(adapter.supports("cpf-starters-messaging-reliability-jdbc-extra", "BROKER_DLQ_REPLAY", "BROKER_DLQ_REPLAY", "CPF_BROKER_DLQ")).isFalse();
        assertThat(adapter.supports("cpf-starters-messaging-reliability-jdbc", "BROKER_DLQ_REPLAY_EXTRA", "BROKER_DLQ_REPLAY", "CPF_BROKER_DLQ")).isFalse();
    }

    @Test
    void executesOnlyMatchingUnexpiredIndependentApprovalSnapshot() {
        CpfReliabilityOperationsPort operations = mock(CpfReliabilityOperationsPort.class);
        AdmApprovalRepository approvals = mock(AdmApprovalRepository.class);
        Map<String, Object> dlq = dlq(0, Instant.parse("2026-08-05T01:00:00Z"));
        var snapshot = AdmBrokerDlqReplayApprovalSnapshot.from(dlq);
        AdmApprovedOperationCommand command = command(snapshot.hash(), "REQ-1", "APR-1");
        when(approvals.findRequest(11L)).thenReturn(Optional.of(approval(command, Instant.now().plusSeconds(600))));
        when(approvals.findParticipants(11L)).thenReturn(List.of(
                Map.of("operatorId", "APR-1", "decisionStatus", "APPROVED")));
        when(operations.findDlq(null, null, null, 1_000)).thenReturn(List.of(dlq));
        when(operations.requestDlqReplay("MSG-1", "APR-1", "incident replay"))
                .thenReturn(new CpfReliabilityOperationsPort.ChangeResult(dlq, dlq, "approved"));
        BrokerReliabilityApprovalOwnerCommandAdapter adapter =
                new BrokerReliabilityApprovalOwnerCommandAdapter(operations, approvals);

        var result = adapter.execute(command);

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        verify(operations).requestDlqReplay("MSG-1", "APR-1", "incident replay");
    }

    @Test
    void expiredApprovalFailsBeforeOwnerMutation() {
        CpfReliabilityOperationsPort operations = mock(CpfReliabilityOperationsPort.class);
        AdmApprovalRepository approvals = mock(AdmApprovalRepository.class);
        Map<String, Object> dlq = dlq(0, Instant.parse("2026-08-05T01:00:00Z"));
        var snapshot = AdmBrokerDlqReplayApprovalSnapshot.from(dlq);
        AdmApprovedOperationCommand command = command(snapshot.hash(), "REQ-1", "APR-1");
        when(approvals.findRequest(11L)).thenReturn(Optional.of(approval(command, Instant.now().minusSeconds(1))));
        BrokerReliabilityApprovalOwnerCommandAdapter adapter =
                new BrokerReliabilityApprovalOwnerCommandAdapter(operations, approvals);

        var result = adapter.execute(command);

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        assertThat(result.resultCode()).isEqualTo("BROKER_DLQ_APPROVAL_EXPIRED");
        verify(operations, never()).requestDlqReplay(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void changedDlqSnapshotFailsClosed() {
        CpfReliabilityOperationsPort operations = mock(CpfReliabilityOperationsPort.class);
        AdmApprovalRepository approvals = mock(AdmApprovalRepository.class);
        Map<String, Object> approved = dlq(0, Instant.parse("2026-08-05T01:00:00Z"));
        var snapshot = AdmBrokerDlqReplayApprovalSnapshot.from(approved);
        AdmApprovedOperationCommand command = command(snapshot.hash(), "REQ-1", "APR-1");
        when(approvals.findRequest(11L)).thenReturn(Optional.of(approval(command, Instant.now().plusSeconds(600))));
        when(approvals.findParticipants(11L)).thenReturn(List.of(
                Map.of("operatorId", "APR-1", "decisionStatus", "APPROVED")));
        when(operations.findDlq(null, null, null, 1_000))
                .thenReturn(List.of(dlq(1, Instant.parse("2026-08-05T01:01:00Z"))));
        BrokerReliabilityApprovalOwnerCommandAdapter adapter =
                new BrokerReliabilityApprovalOwnerCommandAdapter(operations, approvals);

        var result = adapter.execute(command);

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        assertThat(result.resultCode()).isEqualTo("BROKER_DLQ_APPROVAL_HASH_MISMATCH");
        verify(operations, never()).requestDlqReplay(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void selfApprovalAndMissingIndependentDecisionAreRejected() {
        CpfReliabilityOperationsPort operations = mock(CpfReliabilityOperationsPort.class);
        AdmApprovalRepository approvals = mock(AdmApprovalRepository.class);
        Map<String, Object> dlq = dlq(0, Instant.parse("2026-08-05T01:00:00Z"));
        var snapshot = AdmBrokerDlqReplayApprovalSnapshot.from(dlq);
        BrokerReliabilityApprovalOwnerCommandAdapter adapter =
                new BrokerReliabilityApprovalOwnerCommandAdapter(operations, approvals);

        var self = adapter.execute(command(snapshot.hash(), "OP-1", "OP-1"));
        assertThat(self.resultCode()).isEqualTo("BROKER_DLQ_SELF_APPROVAL");

        AdmApprovedOperationCommand independentMissing = command(snapshot.hash(), "REQ-1", "EXEC-1");
        when(approvals.findRequest(11L)).thenReturn(Optional.of(
                approval(independentMissing, Instant.now().plusSeconds(600))));
        when(approvals.findParticipants(11L)).thenReturn(List.of(
                Map.of("operatorId", "REQ-1", "decisionStatus", "APPROVED")));
        var missing = adapter.execute(independentMissing);
        assertThat(missing.resultCode()).isEqualTo("BROKER_DLQ_EXECUTOR_APPROVAL_REQUIRED");
        verify(operations, never()).requestDlqReplay(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void approvedDecisionCannotBeBorrowedByDifferentExecutor() {
        CpfReliabilityOperationsPort operations = mock(CpfReliabilityOperationsPort.class);
        AdmApprovalRepository approvals = mock(AdmApprovalRepository.class);
        Map<String, Object> dlq = dlq(0, Instant.parse("2026-08-05T01:00:00Z"));
        var snapshot = AdmBrokerDlqReplayApprovalSnapshot.from(dlq);
        AdmApprovedOperationCommand command = command(snapshot.hash(), "REQ-1", "EXEC-2");
        when(approvals.findRequest(11L)).thenReturn(Optional.of(approval(command, Instant.now().plusSeconds(600))));
        when(approvals.findParticipants(11L)).thenReturn(List.of(
                Map.of("operatorId", "APR-1", "decisionStatus", "APPROVED")));
        BrokerReliabilityApprovalOwnerCommandAdapter adapter =
                new BrokerReliabilityApprovalOwnerCommandAdapter(operations, approvals);

        var result = adapter.execute(command);

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        assertThat(result.resultCode()).isEqualTo("BROKER_DLQ_EXECUTOR_APPROVAL_REQUIRED");
        verify(operations, never()).requestDlqReplay(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void malformedApprovalExpiryFailsDeterministically() {
        CpfReliabilityOperationsPort operations = mock(CpfReliabilityOperationsPort.class);
        AdmApprovalRepository approvals = mock(AdmApprovalRepository.class);
        Map<String, Object> dlq = dlq(0, Instant.parse("2026-08-05T01:00:00Z"));
        var snapshot = AdmBrokerDlqReplayApprovalSnapshot.from(dlq);
        AdmApprovedOperationCommand command = command(snapshot.hash(), "REQ-1", "APR-1");
        Map<String, Object> request = new java.util.LinkedHashMap<>(approval(command, Instant.now().plusSeconds(600)));
        request.put("expireAt", "not-a-timestamp");
        when(approvals.findRequest(11L)).thenReturn(Optional.of(request));
        BrokerReliabilityApprovalOwnerCommandAdapter adapter =
                new BrokerReliabilityApprovalOwnerCommandAdapter(operations, approvals);

        var result = adapter.execute(command);

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        assertThat(result.resultCode()).isEqualTo("BROKER_DLQ_APPROVAL_EXPIRY_INVALID");
    }

    private static AdmApprovedOperationCommand command(String hash, String requestedBy, String approvedBy) {
        return new AdmApprovedOperationCommand(
                11L, "CMD-11", "BROKER_DLQ_REPLAY",
                "cpf-starters-messaging-reliability-jdbc", "BROKER_DLQ_REPLAY",
                "CPF_BROKER_DLQ", "MSG-1", hash, "{}",
                requestedBy, approvedBy, "incident replay", "TX-APP-1");
    }

    private static Map<String, Object> approval(AdmApprovedOperationCommand command, Instant expiresAt) {
        return Map.ofEntries(
                Map.entry("approvalRequestId", command.approvalRequestId()),
                Map.entry("approvalStatus", "EXECUTING"),
                Map.entry("actionType", command.actionType()),
                Map.entry("ownerModule", command.ownerModule()),
                Map.entry("ownerCommand", command.ownerCommand()),
                Map.entry("targetType", command.targetType()),
                Map.entry("targetId", command.targetId()),
                Map.entry("requestedBy", command.requestedBy()),
                Map.entry("requestReason", command.reason()),
                Map.entry("transactionId", command.transactionId()),
                Map.entry("payloadHash", command.payloadHash()),
                Map.entry("expireAt", Timestamp.from(expiresAt)));
    }

    private static Map<String, Object> dlq(int replayCount, Instant updatedAt) {
        return Map.ofEntries(
                Map.entry("dlq_id", 1L),
                Map.entry("message_id", "MSG-1"),
                Map.entry("topic", "cpf.topic"),
                Map.entry("transaction_id", "TX-1"),
                Map.entry("segment_id", "SEG-1"),
                Map.entry("replay_status", "WAITING"),
                Map.entry("replay_count", replayCount),
                Map.entry("updated_at", Timestamp.from(updatedAt)));
    }
}
