package com.cpf.core.common.runtimecontrol;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CpfRuntimeCanonicalHashTest {
    @Test
    void mapOrderDoesNotChangeFingerprintOrEvidenceHash() {
        Map<String,Object> left=new LinkedHashMap<>();
        left.put("b",2);left.put("a",1);
        Map<String,Object> right=Map.of("a",1,"b",2);
        assertEquals(CpfRuntimeCanonicalHash.sha256(left),CpfRuntimeCanonicalHash.sha256(right));
        assertEquals(CpfRuntimeCanonicalHash.sha256Hex(left),CpfRuntimeCanonicalHash.sha256Hex(right));
    }

    @Test
    void evidenceHashIsLowercaseSha256HexWithoutChangingLegacyFingerprintEncoding() {
        String fingerprint=CpfRuntimeCanonicalHash.sha256(Map.of("a",1));
        String evidence=CpfRuntimeCanonicalHash.sha256Hex(Map.of("a",1));
        assertEquals(43,fingerprint.length());
        assertEquals(64,evidence.length());
        assertEquals(evidence,evidence.toLowerCase(java.util.Locale.ROOT));
    }
}
