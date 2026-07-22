package cpf.bat.worker;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatWorkerPropertiesTest {
    @Test
    void appliesSafeDefaults() {
        BatWorkerProperties properties = new BatWorkerProperties(
                false, null, null, null, 0, 0, 0, 0, 0, true);

        assertThat(properties.version()).isEqualTo("unknown");
        assertThat(properties.capabilities()).containsExactly("*");
        assertThat(properties.maxConcurrency()).isEqualTo(1);
        assertThat(properties.queueCapacity()).isEqualTo(1);
        assertThat(properties.leaseSeconds()).isEqualTo(30);
    }

    @Test
    void rejectsLeaseShorterThanHeartbeat() {
        assertThatThrownBy(() -> new BatWorkerProperties(
                true, "worker-1", "1.0.0", Set.of("JOB"),
                1, 1, 100, 2_000, 1, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("leaseSeconds");
    }
}
