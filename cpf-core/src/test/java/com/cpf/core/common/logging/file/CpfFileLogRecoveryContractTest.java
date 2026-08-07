package com.cpf.core.common.logging.file;

import com.cpf.core.api.logging.CpfFileLogRuntimeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class CpfFileLogRecoveryContractTest {
    @Test
    void durableSpoolReplaysAndDeduplicatesWithoutRawSecret() throws Exception {
        Path base=Files.createTempDirectory("cpf-logfail-test-");
        MockEnvironment env=new MockEnvironment().withProperty("cpf.logging.file.recovery-spool-root",base.resolve("spool").toString());
        CpfFileLogRecoverySpool spool=new CpfFileLogRecoverySpool(env, Clock.fixed(Instant.parse("2026-08-08T00:00:00Z"), ZoneOffset.UTC));
        Path target=base.resolve("recovered.log");
        assertThat(spool.enqueue(target,"{\"password\":\"secret-value\",\"event\":\"FAIL\"}")).isTrue();
        spool.replayAvailable();
        String recovered=Files.readString(target);
        assertThat(recovered).doesNotContain("secret-value").contains("\"cpfRecoveryChecksum\"");
        assertThat(recovered.lines()).allMatch(line -> line.isBlank() || (line.startsWith("{") && line.endsWith("}")));
        CpfFileLogRecoverySpool.Diagnostics d=spool.diagnostics();
        assertThat(d.pending()).isZero();
        assertThat(d.replayed()).isEqualTo(1L);
        assertThat(d.terminalLoss()).isZero();
    }
}
