package com.cpf.integration.fixedlength;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

class CpfFixedLengthAutoConfigurationTest {
    @Test
    void createsRegistryForCanonicalFixedLengthCodecType() {
        ConcurrentHashMap<String, CpfFixedLengthCodec> registry =
                new CpfFixedLengthAutoConfiguration().cpfFixedLengthCodecRegistry();
        assertThat(registry).isEmpty();
    }
}
