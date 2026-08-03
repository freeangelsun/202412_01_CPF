package com.cpf.starter.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CpfSessionReadinessVerifierTest {
    @Test
    void developmentEphemeralKeyIsStableWithinJvmButReturnedDefensively() {
        byte[] first = CpfSessionReadinessVerifier.decodeKey(null, false);
        byte[] second = CpfSessionReadinessVerifier.decodeKey(null, false);

        assertThat(first).containsExactly(second);
        assertThat(first).isNotSameAs(second);
        first[0] ^= 0x01;
        assertThat(CpfSessionReadinessVerifier.decodeKey(null, false))
                .containsExactly(second);
    }
}
