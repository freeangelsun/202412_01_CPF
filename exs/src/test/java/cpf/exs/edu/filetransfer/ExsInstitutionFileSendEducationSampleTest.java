package cpf.exs.edu.filetransfer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExsInstitutionFileSendEducationSampleTest {

    @Test
    void fileSendPlanUsesInstitutionEndpointCode() {
        assertThat(new ExsInstitutionFileSendEducationSample().sendPlan("T-1", "BANKA").endpointCode())
                .isEqualTo("EXS_BANKA");
    }
}
