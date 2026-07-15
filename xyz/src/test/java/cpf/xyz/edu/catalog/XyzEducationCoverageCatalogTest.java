package cpf.xyz.edu.catalog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainOnlineEducationAreas() {
        assertThat(XyzEducationCoverageCatalog.requiredSampleIds())
                .hasSize(71)
                .contains(
                        "XYZ-EDU-CRUD-001",
                        "XYZ-EDU-LIST-007",
                        "XYZ-EDU-CALL-003",
                        "XYZ-EDU-HEADER-004",
                        "XYZ-EDU-VALID-004",
                        "XYZ-EDU-OPER-002",
                        "XYZ-EDU-AI-006",
                        "XYZ-EDU-ATTACH-002");
    }
}
