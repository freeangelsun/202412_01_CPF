package com.cpf.core.common.edu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainFrameworkCapabilityAreas() {
        assertThat(CpfEducationCoverageCatalog.requiredSampleIds())
                .hasSize(38)
                .contains(
                        "CPF-EDU-CALL-001",
                        "CPF-EDU-BROKER-008",
                        "CPF-EDU-FILE-006",
                        "CPF-EDU-SEC-005",
                        "CPF-EDU-RUNTIME-006");
    }
}
