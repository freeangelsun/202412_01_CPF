package com.cpf.reference.catalog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainOnlineEducationAreas() {
        assertThat(ReferenceEducationCoverageCatalog.requiredSampleIds())
                .hasSize(77)
                .contains(
                        "REF Reference-CRUD-001",
                        "REF Reference-LIST-007",
                        "REF Reference-CALL-003",
                        "REF Reference-HEADER-004",
                        "REF Reference-VALID-004",
                        "REF Reference-OPER-002",
                        "REF Reference-ATTACH-002",
                        "REF Reference-BATCH-RESTART-001",
                        "REF Reference-BATCH-UNKNOWN-001",
                        "REF Reference-CENTER-RESULT-001");
    }
}
