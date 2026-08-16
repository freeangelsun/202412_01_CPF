package com.cpf.education.web.header;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationHeaderPropagationEducationSampleTest {

    @Test
    void propagationContainsModuleInstanceAndClientVersion() {
        assertThat(new EducationHeaderPropagationEducationSample().propagate("T-1", "EDU", "edu-local-01"))
                .containsEntry("x-cpf-module-id", "EDU")
                .containsEntry("x-cpf-instance-id", "edu-local-01")
                .containsEntry("x-cpf-client-version", "edu-v1");
    }
}
