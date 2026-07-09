package cpf.exs.edu.fixedlength;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExsFixedLengthSendAdapterEducationSampleTest {

    @Test
    void sendAdapterBuildsFixedLengthMessage() {
        assertThat(new ExsFixedLengthSendAdapterEducationSample().buildSendTelegram("WEB", "1", "A"))
                .hasSize(14);
    }
}
