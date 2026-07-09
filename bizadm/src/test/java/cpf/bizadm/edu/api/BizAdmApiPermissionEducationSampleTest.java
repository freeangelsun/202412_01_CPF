package cpf.bizadm.edu.api;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BizAdmApiPermissionEducationSampleTest {

    @Test
    void apiPermissionUsesMethodAndPath() {
        assertThat(new BizAdmApiPermissionEducationSample().allowed(Set.of("POST /api/v1/adm/cache/refresh"), "POST", "/api/v1/adm/cache/refresh")).isTrue();
    }
}
