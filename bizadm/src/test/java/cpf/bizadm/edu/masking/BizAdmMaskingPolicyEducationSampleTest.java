package cpf.bizadm.edu.masking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BizAdmMaskingPolicyEducationSampleTest {

    @Test
    void emailIsMaskedByPfwPolicy() {
        assertThat(new BizAdmMaskingPolicyEducationSample().email("user@example.com"))
                .isEqualTo("u***@example.com");
    }
}
