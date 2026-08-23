package com.cpf.platform.operations.runtimecontrol.internal;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeInstanceRegistration;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfRuntimeInstanceIdentityContractTest {
    private static final Instant STARTED = Instant.parse("2026-08-20T07:00:00Z");

    @Test void sameProcessCanReregisterActiveLease() {
        assertThat(CpfRuntimeControlPlaneRepository.sameProcessIdentity(row(8123L, STARTED), registration(8123L, STARTED)))
                .isTrue();
    }

    @Test void sameHostDifferentProcessCannotReuseActiveInstanceId() {
        assertThat(CpfRuntimeControlPlaneRepository.sameProcessIdentity(row(8123L, STARTED), registration(8124L, STARTED.plusSeconds(10))))
                .isFalse();
    }

    @Test void differentSystemCannotReuseActiveInstanceIdEvenWithSamePid() {
        Map<String,Object> row = row(8123L, STARTED);
        CpfRuntimeInstanceRegistration incoming = new CpfRuntimeInstanceRegistration(
                "HOST-A", "svc", "EXS", "local", null, null, "http://HOST-A:8080",
                "1", "commit", "APPLICATION", "AUTO_CONFIGURATION", "1", "hash",
                Map.of(), Map.of(), null, null, "HOST-A", "EXS", "app", "APPLICATION",
                8123L, "25", "1", "1", STARTED, STARTED, 60);
        assertThat(CpfRuntimeControlPlaneRepository.sameProcessIdentity(row, incoming)).isFalse();
    }

    private static Map<String,Object> row(long pid, Instant started) {
        return Map.of(
                "system_code", "MBR",
                "runtime_hostname", "HOST-A",
                "application_name", "app",
                "process_id", String.valueOf(pid),
                "started_at", Timestamp.from(started));
    }

    private static CpfRuntimeInstanceRegistration registration(long pid, Instant started) {
        return new CpfRuntimeInstanceRegistration(
                "HOST-A", "svc", "MBR", "local", null, null, "http://HOST-A:8080",
                "1", "commit", "APPLICATION", "AUTO_CONFIGURATION", "1", "hash",
                Map.of(), Map.of(), null, null, "HOST-A", "MBR", "app", "APPLICATION",
                pid, "25", "1", "1", started, started, 60);
    }
}
