package com.cpf.reference.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceBrokerPublishEducationSampleTest {

    @Test
    void publishPlanUsesCpfBrokerEnvelope() {
        var envelope = new ReferenceBrokerPublishEducationSample().publishPlan("T-1", "ID-1");

        assertThat(envelope.message().topic()).isEqualTo("com.cpf.reference.changed");
        assertThat(envelope.producerModule()).isEqualTo("REF");
        assertThat(envelope.consumerModule()).isEqualTo("REF");
    }
}
