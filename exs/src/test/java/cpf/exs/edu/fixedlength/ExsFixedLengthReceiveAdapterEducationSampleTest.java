package cpf.exs.edu.fixedlength;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExsFixedLengthReceiveAdapterEducationSampleTest {

    @Test
    void receiveAdapterParsesFixedLengthMessage() {
        assertThat(new ExsFixedLengthReceiveAdapterEducationSample().parseReceiveTelegram("WEB0000000001A"))
                .containsEntry("status", "A");
    }
}
