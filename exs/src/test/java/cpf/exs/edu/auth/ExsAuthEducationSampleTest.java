package cpf.exs.edu.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExsAuthEducationSampleTest {

    @Test
    void authUsesCredentialReference() {
        assertThat(new ExsAuthEducationSample().credential("BANKA").keyAlias())
                .isEqualTo("exs/BANKA/client");
    }
}
