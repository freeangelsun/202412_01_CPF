package cpf.bat.edu.catalog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainBatchEducationAreas() {
        assertThat(BatEducationCoverageCatalog.requiredSampleIds())
                .hasSize(36)
                .contains(
                        "BAT-EDU-JOB-001",
                        "BAT-EDU-TRX-004",
                        "BAT-EDU-CALL-006",
                        "BAT-EDU-CENTER-007",
                        "BAT-EDU-LOG-005",
                        "BAT-EDU-RECON-002");
    }
}
