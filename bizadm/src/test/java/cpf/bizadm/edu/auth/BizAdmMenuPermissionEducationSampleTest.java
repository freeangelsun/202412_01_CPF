package cpf.bizadm.edu.auth;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BizAdmMenuPermissionEducationSampleTest {

    @Test
    void menuPermissionRequiresAssignedMenu() {
        assertThat(new BizAdmMenuPermissionEducationSample().canAccess(Set.of("ADM_LOG"), "ADM_LOG")).isTrue();
    }
}
