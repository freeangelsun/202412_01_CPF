package com.cpf.education.transaction.recovery.failure;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationFailureEducationSampleTest {

    @Test
    void failureResponseDoesNotExposeInternalCause() {
        EducationFailureEducationSample.FailureResponse response = new EducationFailureEducationSample()
                .businessError("EDU-400", "sql detail");

        assertThat(response.userMessage()).isEqualTo("요청을 처리할 수 없습니다.");
        assertThat(response.internalCauseForLogOnly()).isEqualTo("MASKED");
    }
}
