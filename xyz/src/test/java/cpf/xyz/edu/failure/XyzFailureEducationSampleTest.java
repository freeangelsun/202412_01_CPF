package cpf.xyz.edu.failure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzFailureEducationSampleTest {

    @Test
    void failureResponseDoesNotExposeInternalCause() {
        XyzFailureEducationSample.FailureResponse response = new XyzFailureEducationSample()
                .businessError("XYZ-400", "sql detail");

        assertThat(response.userMessage()).isEqualTo("요청을 처리할 수 없습니다.");
        assertThat(response.internalCauseForLogOnly()).isEqualTo("MASKED");
    }
}
