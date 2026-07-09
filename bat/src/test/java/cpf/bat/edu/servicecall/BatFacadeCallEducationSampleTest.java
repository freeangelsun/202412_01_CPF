package cpf.bat.edu.servicecall;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatFacadeCallEducationSampleTest {

    @Test
    void facadeCallPlanForbidsControllerDirectCall() {
        assertThat(new BatFacadeCallEducationSample().plan("MemberFacade", "find").rule())
                .isEqualTo("controller-direct-call-forbidden");
    }
}
