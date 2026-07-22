package com.cpf.reference.header;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceHeaderPropagationEducationSampleTest {

    @Test
    void propagationContainsModuleInstanceAndClientVersion() {
        assertThat(new ReferenceHeaderPropagationEducationSample().propagate("T-1", "REF", "ref-local-01"))
                .containsEntry("x-cpf-module-id", "REF")
                .containsEntry("x-cpf-instance-id", "ref-local-01")
                .containsEntry("x-cpf-client-version", "edu-v1");
    }
}
