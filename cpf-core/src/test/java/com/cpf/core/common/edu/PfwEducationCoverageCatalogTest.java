package cpf.pfw.common.edu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PfwEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainFrameworkCapabilityAreas() {
        assertThat(PfwEducationCoverageCatalog.requiredSampleIds())
                .hasSize(38)
                .contains(
                        "PFW-EDU-CALL-001",
                        "PFW-EDU-BROKER-008",
                        "PFW-EDU-FILE-006",
                        "PFW-EDU-SEC-005",
                        "PFW-EDU-RUNTIME-006");
    }
}
