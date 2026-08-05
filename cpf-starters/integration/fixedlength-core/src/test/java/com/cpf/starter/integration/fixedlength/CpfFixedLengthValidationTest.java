package com.cpf.starter.integration.fixedlength;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteOrder;
import org.junit.jupiter.api.Test;

class CpfFixedLengthValidationTest {

    private final CpfBinaryFieldCodec codec = new CpfBinaryFieldCodec();

    @Test
    void rejectsNullAlignmentInsteadOfSilentlyTreatingItAsRightAligned() {
        assertThrows(NullPointerException.class,
                () -> new CpfFixedLengthField("amount", 0, 8, null, '0', true));
    }

    @Test
    void rejectsNullByteOrderForBothUnsignedDirections() {
        assertThrows(NullPointerException.class, () -> codec.unsigned(1L, 2, null));
        assertThrows(NullPointerException.class, () -> codec.unsigned(new byte[] {0, 1}, null));
    }

    @Test
    void rejectsInvalidPackedBcdNibbleBeforeReturningCorruptedDigits() {
        assertThrows(IllegalArgumentException.class,
                () -> codec.packedBcd(new byte[] {(byte) 0x1A}, 2));
    }

    @Test
    void preservesValidEndianAndBcdContracts() {
        assertEquals("0102", codec.hex(codec.unsigned(0x0102, 2, ByteOrder.BIG_ENDIAN)));
        assertEquals("0201", codec.hex(codec.unsigned(0x0102, 2, ByteOrder.LITTLE_ENDIAN)));
        assertEquals("123", codec.packedBcd(codec.packedBcd("123"), 3));
    }
}
