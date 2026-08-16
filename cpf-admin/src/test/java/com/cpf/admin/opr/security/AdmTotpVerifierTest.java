package com.cpf.admin.opr.security;

import com.cpf.core.api.error.CpfValidationException;
import com.cpf.security.api.secret.CpfSecretValue;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdmTotpVerifierTest {
    private final AdmTotpVerifier verifier = new AdmTotpVerifier();

    @Test
    void verifiesRfc6238Sha1VectorAsSixDigits() {
        try (CpfSecretValue value = new CpfSecretValue("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ".toCharArray())) {
            assertThat(verifier.verify(value, "287082", Instant.ofEpochSecond(59))).isTrue();
        }
    }

    @Test
    void acceptsOnlyBoundedClockSkewAndRejectsWrongCode() {
        try (CpfSecretValue value = new CpfSecretValue("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ".toCharArray())) {
            assertThat(verifier.verify(value, "287082", Instant.ofEpochSecond(59 + 30))).isTrue();
        }
        try (CpfSecretValue value = new CpfSecretValue("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ".toCharArray())) {
            assertThat(verifier.verify(value, "287082", Instant.ofEpochSecond(59 + 60))).isFalse();
        }
    }

    @Test
    void rejectsMalformedOtpAndBase32Secret() {
        try (CpfSecretValue value = new CpfSecretValue("INVALID0".toCharArray())) {
            assertThatThrownBy(() -> verifier.verify(value, "12345A", Instant.EPOCH))
                    .isInstanceOf(CpfValidationException.class);
        }
    }
}
