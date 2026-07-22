package com.cpf.reference.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceOperationTraceEducationSampleTest {

    @Test
    void traceKeysContainAdmLink() {
        assertThat(new ReferenceOperationTraceEducationSample().traceKeys("T-1", "/api/v1/ref"))
                .containsEntry("admLink", "/adm/opr/logs?transactionGlobalId=T-1");
    }
}
