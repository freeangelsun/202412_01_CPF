package com.cpf.starter.fixedlength;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.starter.integration.fixedlength.CpfFixedLengthCodec;
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
