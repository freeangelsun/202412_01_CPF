package com.cpf.education.integration.servicecall;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationLocalFacadeEducationSampleTest {

    @Test
    void facadeCallUsesFacadeBoundary() {
        assertThat(new EducationLocalFacadeEducationSample().call("EducationFacade", "find").callType())
                .isEqualTo("LOCAL_FACADE");
    }
}
