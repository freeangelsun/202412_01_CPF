package com.cpf.common.sec.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CmnCryptoServiceTest {
    private final CmnCryptoService cryptoService = new CmnCryptoService();

    @Test
    void base64UrlRoundTripAndDigestHelpersWork() {
        String text = "CPF CoreFlow Platform Framework";

        String encoded = cryptoService.base64UrlEncode(text);

        assertThat(encoded).doesNotContain("=");
        assertThat(cryptoService.base64UrlDecodeToString(encoded)).isEqualTo(text);
        assertThat(cryptoService.sha256Hex(text)).hasSize(64);
        assertThat(cryptoService.sha256Base64Url(text)).isNotBlank();
        assertThat(cryptoService.hmacSha256Base64Url(text, "secret")).isNotBlank();
        assertThat(cryptoService.hmacSha256Hex(text, "secret")).hasSize(64);
        assertThat(cryptoService.secureRandomHex(16)).hasSize(32);
    }
}

