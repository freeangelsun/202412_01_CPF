package com.cpf.reference.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceBrokerPublishEducationSampleTest {

    @Test
    void publishPlanUsesCpfBrokerEnvelope() {
        assertThat(new ReferenceBrokerPublishEducationSample().publishPlan("T-1", "ID-1").message().topic())
                .isEqualTo("com.cpf.reference.changed");
    }
}
