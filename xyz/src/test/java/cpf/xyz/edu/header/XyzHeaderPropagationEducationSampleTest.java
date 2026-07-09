package cpf.xyz.edu.header;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzHeaderPropagationEducationSampleTest {

    @Test
    void propagationContainsModuleInstanceAndClientVersion() {
        assertThat(new XyzHeaderPropagationEducationSample().propagate("T-1", "XYZ", "xyz-local-01"))
                .containsEntry("x-cpf-module-id", "XYZ")
                .containsEntry("x-cpf-instance-id", "xyz-local-01")
                .containsEntry("x-cpf-client-version", "edu-v1");
    }
}
