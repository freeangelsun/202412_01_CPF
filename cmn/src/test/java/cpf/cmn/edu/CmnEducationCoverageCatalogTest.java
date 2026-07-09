package cpf.cmn.edu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CmnEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainCommonEducationAreas() {
        assertThat(CmnEducationCoverageCatalog.requiredSampleIds())
                .hasSize(19)
                .contains(
                        "CMN-EDU-FIXED-001",
                        "CMN-EDU-MSG-001",
                        "CMN-EDU-CODE-001",
                        "CMN-EDU-MASK-001");
    }
}
