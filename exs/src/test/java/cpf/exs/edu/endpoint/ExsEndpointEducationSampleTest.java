package cpf.exs.edu.endpoint;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExsEndpointEducationSampleTest {

    @Test
    void endpointMarksExternalRuntimeRequired() {
        assertThat(new ExsEndpointEducationSample().endpoint("BANKA"))
                .containsEntry("endpointCode", "EXS_BANKA")
                .containsEntry("runtime", "external-runtime-required");
    }
}
