package cpf.pfw.common.runtime;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PfwRuntimeWorkerEducationSampleTest {

    @Test
    void workerControlRequiresAuditReasonAndChangesState() {
        PfwRuntimeWorkerEducationSample.InMemoryRuntimeRegistry registry =
                new PfwRuntimeWorkerEducationSample().runtimeRegistry();

        assertThatThrownBy(() -> registry.control(new CpfWorkerControlRequest(
                "BAT-WORKER-01",
                "PAUSE",
                "adm01",
                "",
                Map.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("감사 사유");

        CpfWorkerControlResult result = registry.control(new CpfWorkerControlRequest(
                "BAT-WORKER-01",
                "PAUSE",
                "adm01",
                "장애 조치를 위해 일시 중지합니다.",
                Map.of()));

        assertThat(result.status()).isEqualTo("PAUSED");
        assertThat(registry.adminStatus("BAT-WORKER-01").state()).isEqualTo("PAUSED");
    }

    @Test
    void heartbeatAndGhostDetectionExposeAdminStatus() {
        PfwRuntimeWorkerEducationSample.InMemoryRuntimeRegistry registry =
                new PfwRuntimeWorkerEducationSample().runtimeRegistry();

        CpfHeartbeatResult heartbeat = registry.heartbeat(new CpfHeartbeatRequest(
                "BAT-WORKER-01",
                "BATCH_WORKER",
                Instant.parse("2026-07-09T03:05:00Z"),
                Map.of("host", "local")));

        assertThat(heartbeat.status()).isEqualTo("UPDATED");
        assertThat(registry.adminStatus("BAT-WORKER-01").lastHeartbeatAt())
                .isEqualTo(Instant.parse("2026-07-09T03:05:00Z"));
        assertThat(registry.ghostCandidates(Instant.parse("2026-07-09T03:09:59Z"), Duration.ofMinutes(5)))
                .extracting(CpfRuntimeGhostCandidate::componentId)
                .contains("BAT-WORKER-02")
                .doesNotContain("BAT-WORKER-01");
    }
}
