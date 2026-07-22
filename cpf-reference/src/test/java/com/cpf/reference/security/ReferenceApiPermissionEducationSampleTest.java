package com.cpf.reference.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceApiPermissionEducationSampleTest {

    @Test
    void actionRequiresPermission() {
        assertThat(new ReferenceApiPermissionEducationSample().allowed(Set.of("READ", "UPDATE"), "UPDATE")).isTrue();
        assertThat(new ReferenceApiPermissionEducationSample().allowed(Set.of("READ"), "DELETE")).isFalse();
    }
}
