package com.cpf.admin.approval.security;

import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AdmDataQualityCorrectionGatewayTest {
    @Test
    void verifierRunsBeforeProviderAndCanFailClosed() {
        CpfDataQualityCorrectionPort provider = mock(CpfDataQualityCorrectionPort.class);
        AtomicBoolean verified = new AtomicBoolean();
        AdmDataQualityCorrectionGateway gateway = new AdmDataQualityCorrectionGateway(provider, command -> {
            verified.set(true);
            throw new SecurityException("replay");
        });
        CpfDataQualityCorrectionPort.ApprovedCorrection command = new CpfDataQualityCorrectionPort.ApprovedCorrection(
                "Q-1", 1L, java.util.Map.of("status", "CORRECTED"), "operator", "reason",
                "APR-1", "a".repeat(64), "nonce-1234567890123456", "b".repeat(64),
                Instant.parse("2026-08-07T00:00:00Z"));

        assertThatThrownBy(() -> gateway.correctApproved(command)).isInstanceOf(SecurityException.class);
        assertThat(verified).isTrue();
        verify(provider, never()).correctApproved(command);
    }
}
