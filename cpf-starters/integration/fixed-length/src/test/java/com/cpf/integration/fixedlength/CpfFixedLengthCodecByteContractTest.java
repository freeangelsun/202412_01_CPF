package com.cpf.integration.fixedlength;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfFixedLengthCodecByteContractTest {
    @Test
    void encodesMultibyteValueUsingByteOffsetsAndExactRecordLength() {
        CpfFixedLengthCodec codec = codec();
        byte[] encoded = codec.encode(Map.of("name", "가나", "amount", "12"));
        assertEquals(10, encoded.length);
        assertEquals(Map.of("name", "가나", "amount", "12"), codec.decode(encoded));
    }

    @Test
    void rejectsByteOverflowUnknownFieldAndTruncatedMultibyteInput() {
        CpfFixedLengthCodec codec = codec();
        assertThrows(IllegalArgumentException.class,
                () -> codec.encode(Map.of("name", "가나다", "amount", "12")));
        assertThrows(IllegalArgumentException.class,
                () -> codec.encode(Map.of("name", "가", "amount", "12", "secret", "x")));
        byte[] malformed = codec.encode(Map.of("name", "가", "amount", "12"));
        malformed[1] = (byte) 0x20;
        assertThrows(IllegalArgumentException.class, () -> codec.decode(malformed));
    }

    @Test
    void rejectsDuplicateFieldNamesAndMultibytePad() {
        CpfFixedLengthField first = new CpfFixedLengthField("x", 0, 2, CpfFixedLengthField.Alignment.LEFT, ' ', false);
        CpfFixedLengthField duplicate = new CpfFixedLengthField("x", 2, 2, CpfFixedLengthField.Alignment.LEFT, ' ', false);
        assertThrows(IllegalArgumentException.class, () -> new CpfFixedLengthLayout(
                "dup", 4, StandardCharsets.UTF_8, List.of(first, duplicate)));
        CpfFixedLengthLayout layout = new CpfFixedLengthLayout(
                "pad", 4, StandardCharsets.UTF_8,
                List.of(new CpfFixedLengthField("x", 0, 4, CpfFixedLengthField.Alignment.LEFT, '가', false)));
        assertThrows(IllegalArgumentException.class, () -> new CpfFixedLengthCodec(layout));
    }

    @Test
    void preservesDefensiveExactBytes() {
        CpfFixedLengthCodec codec = codec();
        assertArrayEquals("가나0012".getBytes(StandardCharsets.UTF_8), codec.encode(Map.of("name", "가나", "amount", "12")));
    }

    private static CpfFixedLengthCodec codec() {
        return new CpfFixedLengthCodec(new CpfFixedLengthLayout(
                "sample", 10, StandardCharsets.UTF_8,
                List.of(
                        new CpfFixedLengthField("name", 0, 6, CpfFixedLengthField.Alignment.LEFT, ' ', true),
                        new CpfFixedLengthField("amount", 6, 4, CpfFixedLengthField.Alignment.RIGHT, '0', true))));
    }
}
