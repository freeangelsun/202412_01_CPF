package cpf.cmn.edu.fixedlength;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CmnFixedLengthRoundTripEducationSampleTest {

    @Test
    void roundTripReturnsOriginalStatus() {
        assertThat(new CmnFixedLengthRoundTripEducationSample().roundTrip("APP", "123", "A").fields())
                .containsEntry("status", "A");
    }
}
