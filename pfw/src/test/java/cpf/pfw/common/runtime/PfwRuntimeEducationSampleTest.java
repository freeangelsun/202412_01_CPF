package cpf.pfw.common.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PfwRuntimeEducationSampleTest {

    @Test
    void heartbeatContainsModuleAndInstanceIdentity() {
        PfwRuntimeEducationSample.RuntimeStatus status = new PfwRuntimeEducationSample()
                .heartbeat("ADM", "adm-local-01");

        assertThat(status.status()).isEqualTo("UP");
        assertThat(status.capabilities()).containsEntry("ghostDetection", "enabled");
    }
}
