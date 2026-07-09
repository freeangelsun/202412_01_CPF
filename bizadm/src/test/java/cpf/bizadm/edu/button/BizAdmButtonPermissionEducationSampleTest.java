package cpf.bizadm.edu.button;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BizAdmButtonPermissionEducationSampleTest {

    @Test
    void forbiddenButtonIsHiddenOrDisabled() {
        assertThat(new BizAdmButtonPermissionEducationSample().state(Set.of("READ"), "DELETE"))
                .isEqualTo("HIDDEN_OR_DISABLED");
    }
}
