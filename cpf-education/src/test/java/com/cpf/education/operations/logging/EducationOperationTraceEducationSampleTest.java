package com.cpf.education.operations.logging;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationOperationTraceEducationSampleTest {

    @Test
    void traceKeysContainAdmLink() {
        assertThat(new EducationOperationTraceEducationSample().traceKeys("T-1", "/api/v1/ref"))
                .containsEntry("admLink", "/adm/opr/logs?transactionId=T-1");
    }
}
