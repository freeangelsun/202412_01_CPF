package cpf.pfw.common.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PfwSecurityCryptoEducationSampleTest {

    private final PfwSecurityCryptoEducationSample sample = new PfwSecurityCryptoEducationSample();

    @Test
    void signaturePortSignsAndRejectsTamperedPayload() {
        CpfCredentialRef ref = sample.credentialRef("cpf-signature-key");
        byte[] payload = "important-payload".getBytes(StandardCharsets.UTF_8);

        byte[] signature = sample.signaturePort().sign(ref, payload);

        assertThat(signature).isNotEmpty();
        assertThat(sample.signaturePort().verify(ref, payload, signature)).isTrue();
        assertThat(sample.signaturePort().verify(ref, "tampered".getBytes(StandardCharsets.UTF_8), signature)).isFalse();
    }

    @Test
    void encryptionPortRoundTripsWithoutPlainTextExposure() {
        CpfCredentialRef ref = sample.credentialRef("cpf-encryption-key");
        byte[] plain = "secret-business-payload".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = sample.encryptionPort().encrypt(ref, plain);
        byte[] decrypted = sample.encryptionPort().decrypt(ref, encrypted);

        assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo("secret-business-payload");
        assertThat(new String(encrypted, StandardCharsets.ISO_8859_1)).doesNotContain("secret-business-payload");
    }

    @Test
    void auditSafeCredentialStatusDoesNotContainRawSecret() {
        CpfCredentialRef ref = sample.credentialRef("cpf-vault-ref");

        PfwSecurityCryptoEducationSample.CredentialStatusSnapshot status = sample.credentialStatus(ref);

        assertThat(status.auditSafeMap()).containsEntry("status", "AVAILABLE");
        assertThat(status.auditSafeMap().values()).noneMatch(value -> value.contains("raw-secret"));
        assertThat(status.maskedDisplayName()).contains("****");
    }
}
