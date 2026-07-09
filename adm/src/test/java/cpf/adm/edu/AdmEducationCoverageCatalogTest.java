package cpf.adm.edu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdmEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainOperationEducationAreas() {
        assertThat(AdmEducationCoverageCatalog.requiredSampleIds())
                .hasSize(10)
                .contains(
                        "ADM-EDU-OPR-001",
                        "ADM-EDU-OPR-006",
                        "ADM-EDU-OPR-010");
    }
}
