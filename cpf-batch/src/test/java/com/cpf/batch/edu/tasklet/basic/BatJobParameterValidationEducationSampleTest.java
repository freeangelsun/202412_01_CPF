package cpf.bat.edu.tasklet.basic;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatJobParameterValidationEducationSampleTest {

    @Test
    void validateRequiresBusinessDateAndRequester() {
        BatJobParameterValidationEducationSample sample = new BatJobParameterValidationEducationSample();

        assertThat(sample.validate(Map.of("businessDate", "20260708", "requestedBy", "adm")))
                .containsEntry("requestedBy", "adm");
        assertThatThrownBy(() -> sample.validate(Map.of("businessDate", "20260708")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
