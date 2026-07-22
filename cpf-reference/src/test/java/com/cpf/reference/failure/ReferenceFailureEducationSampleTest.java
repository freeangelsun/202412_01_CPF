package com.cpf.reference.failure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceFailureEducationSampleTest {

    @Test
    void failureResponseDoesNotExposeInternalCause() {
        ReferenceFailureEducationSample.FailureResponse response = new ReferenceFailureEducationSample()
                .businessError("REF-400", "sql detail");

        assertThat(response.userMessage()).isEqualTo("요청을 처리할 수 없습니다.");
        assertThat(response.internalCauseForLogOnly()).isEqualTo("MASKED");
    }
}
