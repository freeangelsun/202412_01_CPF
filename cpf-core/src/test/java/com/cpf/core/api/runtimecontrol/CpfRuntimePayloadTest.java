package com.cpf.core.api.runtimecontrol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfRuntimePayloadTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void canonicalizesObjectFieldsRecursively() {
        CpfRuntimePayload payload = CpfRuntimePayload.parse("{\"z\":1,\"a\":{\"y\":2,\"x\":1}}");
        assertThat(payload.canonicalJson()).isEqualTo("{\"a\":{\"x\":1,\"y\":2},\"z\":1}");
    }

    @Test
    void rejectsNonObjectPayload() {
        assertThatThrownBy(() -> CpfRuntimePayload.parse("[1,2]"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void serializesAsJsonObjectAndRoundTrips() throws Exception {
        CpfRuntimePayload original = CpfRuntimePayload.parse("{\"enabled\":true}");
        String json = mapper.writeValueAsString(original);
        assertThat(json).isEqualTo("{\"enabled\":true}");
        assertThat(mapper.readValue(json, CpfRuntimePayload.class)).isEqualTo(original);
    }
}
