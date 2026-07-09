package cpf.xyz.edu.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class XyzApiPermissionEducationSampleTest {

    @Test
    void actionRequiresPermission() {
        assertThat(new XyzApiPermissionEducationSample().allowed(Set.of("READ", "UPDATE"), "UPDATE")).isTrue();
        assertThat(new XyzApiPermissionEducationSample().allowed(Set.of("READ"), "DELETE")).isFalse();
    }
}
