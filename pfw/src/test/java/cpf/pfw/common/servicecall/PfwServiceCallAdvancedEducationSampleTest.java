package cpf.pfw.common.servicecall;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PfwServiceCallAdvancedEducationSampleTest {

    private final PfwServiceCallAdvancedEducationSample sample = new PfwServiceCallAdvancedEducationSample();

    @Test
    void directInstanceModeKeepsSelectedInstanceIdInResultAndHistory() {
        PfwServiceCallAdvancedEducationSample.CallScenario scenario =
                sample.directInstanceCall("20260709030000000ACClocal010000001", "mbr-local-01");

        assertThat(scenario.request().instanceId()).isEqualTo("mbr-local-01");
        assertThat(scenario.result().target().instanceId()).isEqualTo("mbr-local-01");
        assertThat(scenario.history().selectedInstanceId()).isEqualTo("mbr-local-01");
        assertThat(scenario.history().routingMode()).isEqualTo("DIRECT");
    }

    @Test
    void lbEndpointModeSelectsHealthyInstanceAndWritesCallHistory() {
        PfwServiceCallAdvancedEducationSample.CallScenario scenario =
                sample.lbEndpointCall("20260709030000000ACClocal010000002");

        assertThat(scenario.result().target().instanceId()).isEqualTo("mbr-local-02");
        assertThat(scenario.history().status()).isEqualTo("SUCCESS");
        assertThat(scenario.history().attemptCount()).isEqualTo(1);
        assertThat(scenario.request().headers())
                .containsEntry("x-cpf-transaction-global-id", "20260709030000000ACClocal010000002");
    }

    @Test
    void circuitFailureKeepsRetryAndCircuitResult() {
        PfwServiceCallAdvancedEducationSample.CallScenario scenario =
                sample.circuitOpenFailure("20260709030000000ACClocal010000003");

        assertThat(scenario.request().retryCount()).isEqualTo(2);
        assertThat(scenario.result().attemptCount()).isEqualTo(3);
        assertThat(scenario.result().failureCode()).isEqualTo("CIRCUIT_OPEN");
        assertThat(scenario.history().failureCode()).isEqualTo("CIRCUIT_OPEN");
    }
}
