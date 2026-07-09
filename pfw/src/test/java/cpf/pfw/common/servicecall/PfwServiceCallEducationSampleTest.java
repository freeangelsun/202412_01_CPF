package cpf.pfw.common.servicecall;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PfwServiceCallEducationSampleTest {

    @Test
    void callPlanContainsTimeoutRetryFailoverAndCircuitPolicy() {
        PfwServiceCallEducationSample.CallPlan plan = new PfwServiceCallEducationSample()
                .buildPlan("MBR", "member-summary");

        assertThat(plan.timeout().toSeconds()).isEqualTo(3);
        assertThat(plan.retryCount()).isEqualTo(2);
        assertThat(plan.failoverEnabled()).isTrue();
        assertThat(plan.circuitBreakerEnabled()).isTrue();
        assertThat(plan.candidateInstances()).containsExactly("instance-a", "instance-b");
    }
}
