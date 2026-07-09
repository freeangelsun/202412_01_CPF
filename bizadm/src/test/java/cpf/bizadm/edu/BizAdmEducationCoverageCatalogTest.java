package cpf.bizadm.edu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BizAdmEducationCoverageCatalogTest {

    @Test
    void requiredSampleIdsContainBusinessAdminEducationAreas() {
        assertThat(BizAdmEducationCoverageCatalog.requiredSampleIds())
                .hasSize(6)
                .contains(
                        "BIZADM-EDU-AUTH-001",
                        "BIZADM-EDU-DOWNLOAD-001",
                        "BIZADM-EDU-MASK-001");
    }
}
