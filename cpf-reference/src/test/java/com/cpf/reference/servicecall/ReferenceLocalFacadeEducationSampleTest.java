package com.cpf.reference.servicecall;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceLocalFacadeEducationSampleTest {

    @Test
    void facadeCallUsesFacadeBoundary() {
        assertThat(new ReferenceLocalFacadeEducationSample().call("MemberFacade", "find").callType())
                .isEqualTo("LOCAL_FACADE");
    }
}
