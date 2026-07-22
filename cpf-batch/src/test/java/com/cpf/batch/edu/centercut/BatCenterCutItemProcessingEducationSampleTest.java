package cpf.bat.edu.centercut;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatCenterCutItemProcessingEducationSampleTest {

    @Test
    void itemProcessingReturnsFailureStatusForInvalidItem() {
        assertThat(new BatCenterCutItemProcessingEducationSample().process("ITEM-1", false).status())
                .isEqualTo("FAILED");
    }
}
