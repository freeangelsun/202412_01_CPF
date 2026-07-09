package cpf.xyz.edu.servicecall;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzServiceCallEngineEducationSampleTest {

    @Test
    void serviceCallSampleUsesPfwPlan() {
        assertThat(new XyzServiceCallEngineEducationSample().buildAccountCallPlan().serviceId())
                .isEqualTo("ACC");
    }
}
