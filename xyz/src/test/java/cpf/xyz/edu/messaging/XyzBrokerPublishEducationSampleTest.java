package cpf.xyz.edu.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzBrokerPublishEducationSampleTest {

    @Test
    void publishPlanUsesPfwBrokerEnvelope() {
        assertThat(new XyzBrokerPublishEducationSample().publishPlan("T-1", "ID-1").message().topic())
                .isEqualTo("cpf.xyz.changed");
    }
}
