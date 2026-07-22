package cpf.xyz.catalog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainOnlineEducationAreas() {
        assertThat(XyzEducationCoverageCatalog.requiredSampleIds())
                .hasSize(71)
                .contains(
                        "XYZ Reference-CRUD-001",
                        "XYZ Reference-LIST-007",
                        "XYZ Reference-CALL-003",
                        "XYZ Reference-HEADER-004",
                        "XYZ Reference-VALID-004",
                        "XYZ Reference-OPER-002",
                        "XYZ Reference-AI-006",
                        "XYZ Reference-ATTACH-002");
    }
}
