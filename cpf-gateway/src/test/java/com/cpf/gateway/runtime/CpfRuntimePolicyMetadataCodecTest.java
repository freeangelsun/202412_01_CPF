package com.cpf.gateway.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimePolicyMetadataCodecTest {
    private final CpfRuntimePolicyMetadataCodec codec =
            new CpfRuntimePolicyMetadataCodec(new ObjectMapper());

    @Test
    void roundTripsVersionedJsonWithoutDelimiterLoss() {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("line", "a=b\nc");
        source.put("percent", "100%");

        String encoded = codec.encode(source);

        assertTrue(encoded.startsWith(CpfRuntimePolicyMetadataCodec.V1_PREFIX));
        assertEquals(source, codec.decode(encoded));
    }

    @Test
    void readsStrictLegacyRowsForUpgradeCompatibility() {
        assertEquals(Map.of("key", "a=b\nc"), codec.decode("key=a%3Db%0Ac"));
    }

    @Test
    void rejectsMalformedLegacyMetadataInsteadOfReturningPartialMap() {
        assertThrows(IllegalStateException.class, () -> codec.decode("broken-line"));
        assertThrows(IllegalStateException.class, () -> codec.decode("a=1\na=2"));
    }

    @Test
    void rejectsNullValues() {
        LinkedHashMap<String, String> source = new LinkedHashMap<>();
        source.put("key", null);
        assertThrows(IllegalArgumentException.class, () -> codec.encode(source));
    }
}
