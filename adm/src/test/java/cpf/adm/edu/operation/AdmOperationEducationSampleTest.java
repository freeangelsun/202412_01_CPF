package cpf.adm.edu.operation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdmOperationEducationSampleTest {

    @Test
    void transactionLogQueryIncludesTabsAndMaskingPolicy() {
        assertThat(new AdmOperationEducationSample().transactionLogQuery("T-1"))
                .containsEntry("transactionGlobalId", "T-1")
                .containsEntry("masking", "enabled");
    }
}
