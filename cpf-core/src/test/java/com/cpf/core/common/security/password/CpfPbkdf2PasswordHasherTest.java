package com.cpf.core.common.security.password;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class CpfPbkdf2PasswordHasherTest {

    private static final String PRIMARY_PASSWORD_FIXTURE = "qa37-fixture-Strong!Password#2026";
    private static final String LEGACY_PASSWORD_FIXTURE = "qa37-fixture-Legacy!Password#1";

    private final CpfPbkdf2PasswordHasher hasher = new CpfPbkdf2PasswordHasher(210_000, 256, new char[0]);

    @Test
    void 개별Salt와버전정보를포함해비밀번호를검증한다() {
        char[] password = PRIMARY_PASSWORD_FIXTURE.toCharArray();

        String first = hasher.hash(password);
        String second = hasher.hash(password);

        assertThat(first).isNotEqualTo(second);
        assertThat(hasher.verify(password, first).matched()).isTrue();
        assertThat(hasher.verify("wrong".toCharArray(), first).matched()).isFalse();
        assertThat(hasher.needsRehash(first)).isFalse();
    }

    @Test
    void 기존저장형식은검증후재해시대상으로판정한다() {
        CpfPbkdf2PasswordHasher legacyWriter = new CpfPbkdf2PasswordHasher(210_000, 256, new char[0]);
        String current = legacyWriter.hash(LEGACY_PASSWORD_FIXTURE.toCharArray());
        String[] parts = current.split("\\$");
        String legacy = "PBKDF2$210000$"
                + Base64.getEncoder().encodeToString(Base64.getUrlDecoder().decode(parts[6])) + "$"
                + Base64.getEncoder().encodeToString(Base64.getUrlDecoder().decode(parts[7]));

        CpfPasswordVerification result = hasher.verify(LEGACY_PASSWORD_FIXTURE.toCharArray(), legacy);

        assertThat(result.matched()).isTrue();
        assertThat(result.rehashRequired()).isTrue();
    }
}
