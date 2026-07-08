package cpf.pfw.common.capability;

import cpf.pfw.common.admin.CpfBrokerStatusQuery;
import cpf.pfw.common.admin.CpfCredentialStatusQuery;
import cpf.pfw.common.admin.CpfFileTransferStatusQuery;
import cpf.pfw.common.admin.CpfRuntimeHealthStatusQuery;
import cpf.pfw.common.broker.CpfBrokerEnvelope;
import cpf.pfw.common.broker.CpfBrokerMessage;
import cpf.pfw.common.broker.CpfBrokerResult;
import cpf.pfw.common.filetransfer.CpfFileTransferEndpoint;
import cpf.pfw.common.filetransfer.CpfFileTransferRequest;
import cpf.pfw.common.filetransfer.CpfFileTransferResult;
import cpf.pfw.common.runtime.CpfLockHandle;
import cpf.pfw.common.runtime.CpfRuntimeHealthStatus;
import cpf.pfw.common.security.CpfCredentialRef;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
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
                "EXS",
                "IDEMP-1",
                Instant.parse("2026-07-08T03:00:00Z"),
                message,
                Map.of("retryPolicy", "standard"));

        assertThat(new String(envelope.message().payload(), java.nio.charset.StandardCharsets.UTF_8))
                .isEqualTo("hello");
        assertThat(envelope.transactionGlobalId()).hasSize(34);
        assertThat(envelope.attributes()).containsEntry("retryPolicy", "standard");
        assertThat(CpfBrokerResult.accepted("MSG-1", "KAFKA", "0").status()).isEqualTo("ACCEPTED");
    }

    @Test
    void fileTransferContractUsesCredentialReferenceOnly() {
        CpfCredentialRef credentialRef = new CpfCredentialRef("partner", "SFTP_PARTNER_01", "v1", "partner-prod-key");
        CpfFileTransferEndpoint endpoint = new CpfFileTransferEndpoint(
                "EXS_SFTP_PARTNER",
                "SFTP",
                "sftp.partner.example",
                22,
                "/receive",
                credentialRef,
                Duration.ofSeconds(10),
                Map.of("renamePolicy", "temp-then-rename"));
        CpfFileTransferRequest request = new CpfFileTransferRequest(
                "20260708120000000EXSlocal010000001",
                "SEG-1",
                endpoint.endpointCode(),
                "UPLOAD",
                "D:/cpf/out/a.dat",
                "/receive/a.dat",
                "sha256:demo",
                100L,
                Map.of("duplicatePolicy", "reject"));

        CpfFileTransferResult result = CpfFileTransferResult.success(request, "sha256:demo", 100L);

        assertThat(endpoint.credentialRef().masked()).doesNotContain("partner-prod-key");
        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.remotePath()).isEqualTo("/receive/a.dat");
    }

    @Test
    void runtimeAndAdminDtosStayInsidePfwCommonPackages() {
        CpfLockHandle lockHandle = new CpfLockHandle("job:daily-close", "BAT-1", Instant.now(), Instant.now().plusSeconds(30));
        CpfRuntimeHealthStatus status = new CpfRuntimeHealthStatus(
                "BAT-1",
                "BATCH_WORKER",
                "UP",
                Instant.now(),
                Map.of("heartbeatLagMs", "10"));

        assertThat(lockHandle.lockKey()).isEqualTo("job:daily-close");
        assertThat(status.attributes()).containsEntry("heartbeatLagMs", "10");
        assertThat(CpfBrokerStatusQuery.class.getPackageName()).startsWith("cpf.pfw.common.admin");
        assertThat(CpfFileTransferStatusQuery.class.getPackageName()).startsWith("cpf.pfw.common.admin");
        assertThat(CpfCredentialStatusQuery.class.getPackageName()).startsWith("cpf.pfw.common.admin");
        assertThat(CpfRuntimeHealthStatusQuery.class.getPackageName()).startsWith("cpf.pfw.common.admin");
    }
}
