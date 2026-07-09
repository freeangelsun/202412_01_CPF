package cpf.pfw.common.masking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PfwMaskingPolicyEducationSampleTest {

    @Test
    void maskingPolicyHidesPersonalAndSecretValues() {
        PfwMaskingPolicyEducationSample sample = new PfwMaskingPolicyEducationSample();

        assertThat(sample.maskEmail("user@example.com")).isEqualTo("u***@example.com");
        assertThat(sample.maskSecret("raw-secret")).isEqualTo("****");
    }
}
