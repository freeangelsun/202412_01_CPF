package cpf.exs.edu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExsEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainExternalSystemEducationAreas() {
        assertThat(ExsEducationCoverageCatalog.requiredSampleIds())
                .hasSize(20)
                .contains(
                        "EXS-EDU-FIXED-004",
                        "EXS-EDU-ENDPOINT-002",
                        "EXS-EDU-AUTH-003",
                        "EXS-EDU-FILE-002");
    }
}
