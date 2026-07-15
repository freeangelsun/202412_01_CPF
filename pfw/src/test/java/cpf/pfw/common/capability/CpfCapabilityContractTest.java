package cpf.pfw.common.capability;

import cpf.pfw.common.admin.CpfBrokerStatusQuery;
import cpf.pfw.common.admin.CpfCredentialStatusQuery;
import cpf.pfw.common.admin.CpfFileTransferStatusQuery;
import cpf.pfw.common.admin.CpfRuntimeHealthStatusQuery;
import cpf.pfw.common.broker.CpfBrokerEnvelope;
import cpf.pfw.common.broker.CpfBrokerHistoryQuery;
import cpf.pfw.common.broker.CpfBrokerHistoryRecord;
import cpf.pfw.common.broker.CpfBrokerDlqReplayRequest;
import cpf.pfw.common.broker.CpfBrokerDlqReplayResult;
import cpf.pfw.common.broker.CpfBrokerMessage;
import cpf.pfw.common.broker.CpfBrokerResult;
import cpf.pfw.common.filetransfer.CpfFileChecksumPolicy;
import cpf.pfw.common.filetransfer.CpfFileTransferHistoryQuery;
import cpf.pfw.common.filetransfer.CpfFileTransferEndpoint;
import cpf.pfw.common.filetransfer.CpfFileTransferPolicy;
import cpf.pfw.common.filetransfer.CpfFileTransferProtocol;
import cpf.pfw.common.filetransfer.CpfFileTransferRequest;
import cpf.pfw.common.filetransfer.CpfFileTransferResult;
import cpf.pfw.common.filetransfer.CpfFileTransferRetryPolicy;
import cpf.pfw.common.runtime.CpfGhostDetectionResult;
import cpf.pfw.common.runtime.CpfHeartbeatRequest;
import cpf.pfw.common.runtime.CpfLockHandle;
import cpf.pfw.common.runtime.CpfLockAcquireRequest;
import cpf.pfw.common.runtime.CpfLockAcquireResult;
import cpf.pfw.common.runtime.CpfRuntimeHealthStatus;
import cpf.pfw.common.runtime.CpfWorkerControlRequest;
import cpf.pfw.common.security.CpfCredentialRef;
import cpf.pfw.common.security.CpfCredentialValidationResult;
import cpf.pfw.common.security.CpfTokenRequest;
import cpf.pfw.common.security.CpfTokenResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CpfCapabilityContractTest {

    @Test
    void brokerEnvelopeCarriesTransactionTraceAndDefensivePayloadCopy() {
        byte[] payload = "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        CpfBrokerMessage message = new CpfBrokerMessage(
                "MSG-1",
                "cpf.test.topic",
                "KEY-1",
                payload,
                "application/json",
                Map.of("X-Cpf-Trace-Id", "TRACE-1"));

        payload[0] = 'X';
        CpfBrokerEnvelope envelope = new CpfBrokerEnvelope(
                "20260708120000000MBRlocal010000001",
                "SEG-1",
                "MBR",
                "BZA",
                "IDEMP-1",
                Instant.parse("2026-07-08T03:00:00Z"),
                message,
                Map.of("retryPolicy", "standard"));

        assertThat(new String(envelope.message().payload(), java.nio.charset.StandardCharsets.UTF_8))
                .isEqualTo("hello");
        assertThat(envelope.transactionGlobalId()).hasSize(34);
        assertThat(envelope.attributes()).containsEntry("retryPolicy", "standard");
        assertThat(CpfBrokerResult.accepted("MSG-1", "KAFKA", "0").status()).isEqualTo("ACCEPTED");

        CpfBrokerHistoryRecord historyRecord = new CpfBrokerHistoryRecord(
                "HIS-1",
                "PUBLISH",
                "KAFKA",
                message.topic(),
                message.messageId(),
                envelope.transactionGlobalId(),
                envelope.idempotencyKey(),
                "SUCCESS",
                null,
                Map.of("partition", "0"));
        CpfBrokerHistoryQuery query = new CpfBrokerHistoryQuery("KAFKA", message.topic(), null, envelope.transactionGlobalId(), "SUCCESS", null, null, 0);
        CpfBrokerDlqReplayRequest replayRequest = new CpfBrokerDlqReplayRequest(message.topic(), message.messageId(), "admin", "재처리 사유", null);
        CpfBrokerDlqReplayResult replayResult = new CpfBrokerDlqReplayResult(message.messageId(), "SUCCESS", envelope.transactionGlobalId(), null, null);

        assertThat(historyRecord.attributes()).containsEntry("partition", "0");
        assertThat(query.limit()).isEqualTo(100);
        assertThat(replayRequest.requestedAt()).isNotNull();
        assertThat(replayResult.status()).isEqualTo("SUCCESS");
    }

    @Test
    void fileTransferContractUsesCredentialReferenceOnly() {
        CpfCredentialRef credentialRef = new CpfCredentialRef("partner", "SFTP_PARTNER_01", "v1", "partner-prod-key");
        CpfFileTransferEndpoint endpoint = new CpfFileTransferEndpoint(
                "BZA_SFTP_PARTNER",
                "SFTP",
                "sftp.partner.example",
                22,
                "/receive",
                credentialRef,
                Duration.ofSeconds(10),
                Map.of("renamePolicy", "temp-then-rename"));
        CpfFileTransferRequest request = new CpfFileTransferRequest(
                "20260708120000000BZAlocal010000001",
                "SEG-1",
                endpoint.endpointCode(),
                "UPLOAD",
                "D:/cpf/out/a.dat",
                "/receive/a.dat",
                "sha256:demo",
                100L,
                Map.of("duplicatePolicy", "reject"));

        CpfFileTransferResult result = CpfFileTransferResult.success(request, "sha256:demo", 100L);
        CpfFileTransferRetryPolicy retryPolicy = new CpfFileTransferRetryPolicy(0, null, true);
        CpfFileTransferPolicy transferPolicy = new CpfFileTransferPolicy(true, null, true, "/archive", null, retryPolicy);
        CpfFileChecksumPolicy checksumPolicy = new CpfFileChecksumPolicy(null, true, "sha256:demo");
        CpfFileTransferHistoryQuery historyQuery = new CpfFileTransferHistoryQuery(endpoint.endpointCode(), CpfFileTransferProtocol.SFTP.name(), "SUCCESS", request.transactionGlobalId(), null, null, 0);

        assertThat(endpoint.credentialRef().masked()).doesNotContain("partner-prod-key");
        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.remotePath()).isEqualTo("/receive/a.dat");
        assertThat(transferPolicy.tempSuffix()).isEqualTo(".tmp");
        assertThat(transferPolicy.duplicatePolicy()).isEqualTo("REJECT");
        assertThat(checksumPolicy.algorithm()).isEqualTo("SHA-256");
        assertThat(historyQuery.limit()).isEqualTo(100);
    }

    @Test
    void securityContractMasksSecretsAndUsesReferences() {
        CpfCredentialRef credentialRef = new CpfCredentialRef("oauth", "CLIENT_SECRET_01", "v3", "partner-client-secret");
        CpfTokenRequest tokenRequest = new CpfTokenRequest(credentialRef, null, "partner-api", "read write", Map.of("tenant", "T1"));
        CpfTokenResult tokenResult = new CpfTokenResult("TOKEN-REF-1", null, Instant.now().plusSeconds(300), "raw-token-value");
        CpfCredentialValidationResult validationResult = new CpfCredentialValidationResult(true, credentialRef.credentialId(), "ACTIVE", null, null);

        assertThat(tokenRequest.grantType()).isEqualTo("client_credentials");
        assertThat(tokenResult.maskedToken()).startsWith("***");
        assertThat(tokenResult.maskedToken()).doesNotContain("raw-token-value");
        assertThat(validationResult.valid()).isTrue();
        assertThat(credentialRef.masked()).doesNotContain("partner-client-secret");
    }

    @Test
    void runtimeAndAdminDtosStayInsidePfwCommonPackages() {
        CpfLockHandle lockHandle = new CpfLockHandle("job:daily-close", "BAT-1", Instant.now(), Instant.now().plusSeconds(30));
        CpfLockAcquireRequest acquireRequest = new CpfLockAcquireRequest("job:daily-close", "BAT-1", null);
        CpfLockAcquireResult acquireResult = new CpfLockAcquireResult(true, lockHandle, null);
        CpfHeartbeatRequest heartbeatRequest = new CpfHeartbeatRequest("BAT-1", "BATCH_WORKER", null, Map.of("slot", "1"));
        CpfRuntimeHealthStatus status = new CpfRuntimeHealthStatus(
                "BAT-1",
                "BATCH_WORKER",
                "UP",
                Instant.now(),
                Map.of("heartbeatLagMs", "10"));
        CpfGhostDetectionResult ghostDetectionResult = new CpfGhostDetectionResult(null, List.of());
        CpfWorkerControlRequest workerControlRequest = new CpfWorkerControlRequest("BAT-1", null, "admin", "점검", null);

        assertThat(lockHandle.lockKey()).isEqualTo("job:daily-close");
        assertThat(acquireRequest.ttl()).isEqualTo(Duration.ofSeconds(30));
        assertThat(acquireResult.acquired()).isTrue();
        assertThat(heartbeatRequest.attributes()).containsEntry("slot", "1");
        assertThat(status.attributes()).containsEntry("heartbeatLagMs", "10");
        assertThat(ghostDetectionResult.candidates()).isEmpty();
        assertThat(workerControlRequest.action()).isEqualTo("STATUS");
        assertThat(CpfBrokerStatusQuery.class.getPackageName()).startsWith("cpf.pfw.common.admin");
        assertThat(CpfFileTransferStatusQuery.class.getPackageName()).startsWith("cpf.pfw.common.admin");
        assertThat(CpfCredentialStatusQuery.class.getPackageName()).startsWith("cpf.pfw.common.admin");
        assertThat(CpfRuntimeHealthStatusQuery.class.getPackageName()).startsWith("cpf.pfw.common.admin");
    }
}
