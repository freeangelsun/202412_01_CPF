package com.cpf.starter.security.session.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CpfCredentialCipherTest {
    @Test void encryptsWithHandleBoundAadAndRejectsTampering() {
        byte[] key = new byte[32]; java.util.Arrays.fill(key, (byte) 7);
        CpfCredentialCipher cipher = new CpfCredentialCipher(key);
        var encrypted = cipher.encrypt("handle-1", "secret-token");
        assertThat(cipher.decrypt("handle-1", encrypted.iv(), encrypted.ciphertext())).isEqualTo("secret-token");
        assertThatThrownBy(() -> cipher.decrypt("handle-2", encrypted.iv(), encrypted.ciphertext()))
                .isInstanceOf(SecurityException.class).hasMessageContaining("INTEGRITY");
        byte[] damaged = encrypted.ciphertext(); damaged[0] ^= 1;
        assertThatThrownBy(() -> cipher.decrypt("handle-1", encrypted.iv(), damaged))
                .isInstanceOf(SecurityException.class);
    }
}
