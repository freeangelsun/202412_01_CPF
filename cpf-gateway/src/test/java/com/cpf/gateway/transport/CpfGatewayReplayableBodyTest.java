package com.cpf.gateway.transport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfGatewayReplayableBodyTest {
    @TempDir Path tempDir;

    @Test
    void smallBodyCanBeOpenedForEveryRetry() throws Exception {
        byte[] source = "retryable-body".getBytes(StandardCharsets.UTF_8);
        CpfGatewayTransferPolicy policy = new CpfGatewayTransferPolicy(1024, 128, 4096, 100, 1000, tempDir);
        try (CpfGatewayReplayableBody body = CpfGatewayReplayableBody.capture(
                new ByteArrayInputStream(source), source.length, policy)) {
            assertThat(body.fileBacked()).isFalse();
            assertThat(body.openStream().readAllBytes()).isEqualTo(source);
            assertThat(body.openStream().readAllBytes()).isEqualTo(source);
        }
    }

    @Test
    void largeBodySpoolsToFileAndEnforcesMaximum() throws Exception {
        byte[] source = new byte[512];
        CpfGatewayTransferPolicy policy = new CpfGatewayTransferPolicy(1024, 64, 4096, 100, 1000, tempDir);
        CpfGatewayReplayableBody body = CpfGatewayReplayableBody.capture(
                new ByteArrayInputStream(source), -1, policy);
        assertThat(body.fileBacked()).isTrue();
        assertThat(body.openStream().readAllBytes()).hasSize(512);
        body.close();
        assertThatThrownBy(body::openStream).isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> CpfGatewayReplayableBody.capture(
                new ByteArrayInputStream(new byte[2048]), -1, policy))
                .isInstanceOf(CpfGatewayPayloadTooLargeException.class);
    }
}
