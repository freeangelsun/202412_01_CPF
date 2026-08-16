package com.cpf.education.verification.catalog;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainOnlineEducationAreas() {
        assertThat(EducationEducationCoverageCatalog.requiredSampleIds())
                .hasSize(77)
                .contains(
                        "EDU Education-CRUD-001",
                        "EDU Education-LIST-007",
                        "EDU Education-CALL-003",
                        "EDU Education-HEADER-004",
                        "EDU Education-VALID-004",
                        "EDU Education-OPER-002",
                        "EDU Education-ATTACH-002",
                        "EDU Education-BATCH-RESTART-001",
                        "EDU Education-BATCH-UNKNOWN-001",
                        "EDU Education-CENTER-RESULT-001");
    }
}
