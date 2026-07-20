package cpf.xyz.servicecall;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzLocalFacadeEducationSampleTest {

    @Test
    void facadeCallUsesFacadeBoundary() {
        assertThat(new XyzLocalFacadeEducationSample().call("MemberFacade", "find").callType())
                .isEqualTo("LOCAL_FACADE");
    }
}
