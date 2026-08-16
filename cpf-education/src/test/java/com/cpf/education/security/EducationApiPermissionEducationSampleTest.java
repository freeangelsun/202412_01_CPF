package com.cpf.education.security;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EducationApiPermissionEducationSampleTest {

    @Test
    void actionRequiresPermission() {
        assertThat(new EducationApiPermissionEducationSample().allowed(Set.of("READ", "UPDATE"), "UPDATE")).isTrue();
        assertThat(new EducationApiPermissionEducationSample().allowed(Set.of("READ"), "DELETE")).isFalse();
    }
}
