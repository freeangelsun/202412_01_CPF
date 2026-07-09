package cpf.pfw.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PfwSecurityEducationSampleTest {

    @Test
    void credentialReferenceDoesNotExposeRawSecret() {
        PfwSecurityEducationSample.CredentialReference reference = new PfwSecurityEducationSample()
                .credentialReference("vault", "cpf/db/app");

        assertThat(reference.provider()).isEqualTo("vault");
        assertThat(reference.policy()).containsEntry("exposeRawSecret", "false");
    }
}
