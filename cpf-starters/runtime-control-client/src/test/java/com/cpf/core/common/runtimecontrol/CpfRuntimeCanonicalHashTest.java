package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CpfRuntimeCanonicalHashTest {
    @Test
    void mapKeyOrderDoesNotChangeHash() {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("b", 2);
        a.put("a", 1);
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("a", 1);
        b.put("b", 2);
        assertThat(CpfRuntimeCanonicalHash.sha256(a)).isEqualTo(CpfRuntimeCanonicalHash.sha256(b));
    }

    @Test
    void payloadChangeChangesHash() {
        assertThat(CpfRuntimeCanonicalHash.sha256(Map.of("a", 1)))
                .isNotEqualTo(CpfRuntimeCanonicalHash.sha256(Map.of("a", 2)));
    }

    @Test
    void typedPayloadKeepsCanonicalHashAcrossFieldOrder() {
        CpfRuntimePayload left = CpfRuntimePayload.parse("{\"b\":2,\"a\":1}");
        CpfRuntimePayload right = CpfRuntimePayload.parse("{\"a\":1,\"b\":2}");
        assertThat(CpfRuntimeCanonicalHash.sha256(left)).isEqualTo(CpfRuntimeCanonicalHash.sha256(right));
    }
}
