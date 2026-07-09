package cpf.adm.edu.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdmRuntimeEducationSampleTest {

    @Test
    void runtimeStatusUsesPfwRuntimeContract() {
        assertThat(new AdmRuntimeEducationSample().status("ADM", "adm-local-01").status()).isEqualTo("UP");
    }
}
