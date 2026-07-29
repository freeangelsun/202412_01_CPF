package com.cpf.admin.opr.filejob;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

class AdmFileJobRepositoryDataSourceOwnershipTest {

    @Test
    void bindsRepositoryToAdmOwnedJdbcTemplate() {
        Constructor<?> constructor = AdmFileJobRepository.class.getDeclaredConstructors()[0];

        Qualifier qualifier = constructor.getParameters()[0].getAnnotation(Qualifier.class);

        assertThat(qualifier).isNotNull();
        assertThat(qualifier.value()).isEqualTo("admJdbcTemplate");
    }
}
