package com.cpf.gateway.scg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cpf.gateway.api.CpfGatewayLedgerPort;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Ledger 저장 장애가 원 거래를 오염시키지 않고 durable spool로 복구되는지 검증합니다. */
class CpfGatewayRecoverySpoolTest {
    @TempDir Path temp;

    @Test
    void persistsFailedCompletionAndReplaysItInOrder() throws Exception {
        CpfGatewayLedgerPort ledger = mock(CpfGatewayLedgerPort.class);
        CpfGatewayLedgerPort.TransactionCompletion completion = new CpfGatewayLedgerPort.TransactionCompletion(
                "gtx-1", "inst-1", "COMPLETED", "200", "BIZ-0000", null,
                false, 120, 16, OffsetDateTime.now());
        doThrow(new IllegalStateException("authorization=secret"))
                .doNothing()
                .when(ledger).complete(completion);
        CpfGatewayLedgerRecoverySpool spool = new CpfGatewayLedgerRecoverySpool(
                ledger, new ObjectMapper().findAndRegisterModules(), properties());

        spool.complete(completion);
        Path directory = temp.resolve("ledger-recovery");
        try (var files = Files.list(directory)) {
            assertThat(files.filter(Files::isRegularFile).count()).isEqualTo(1);
        }

        spool.replay();

        verify(ledger, org.mockito.Mockito.times(2)).complete(completion);
        try (var files = Files.list(directory)) {
            assertThat(files.filter(Files::isRegularFile).count()).isZero();
        }
    }

    private CpfGatewaySafetyProperties properties() {
        CpfGatewaySafetyProperties properties = new CpfGatewaySafetyProperties();
        properties.setLogSpoolDirectory(temp.toString());
        properties.setLogSpoolBytesCap(1024 * 1024);
        return properties;
    }
}
