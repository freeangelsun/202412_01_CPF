package cpf.bat.edu.servicecall;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatServiceCallEngineEducationSampleTest {

    @Test
    void serviceCallEnginePlanUsesPfwPolicy() {
        assertThat(new BatServiceCallEngineEducationSample().buildMemberCallPlan().serviceId()).isEqualTo("MBR");
    }
}
