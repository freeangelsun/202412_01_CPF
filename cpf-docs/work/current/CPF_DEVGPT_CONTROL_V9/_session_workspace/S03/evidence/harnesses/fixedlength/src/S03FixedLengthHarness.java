import com.cpf.starter.integration.fixedlength.*;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class S03FixedLengthHarness {
    private static int cases;

    public static void main(String[] args) {
        CpfBinaryFieldCodec codec = new CpfBinaryFieldCodec();
        expect(NullPointerException.class,
                () -> new CpfFixedLengthField("amount", 0, 8, null, '0', true));
        expect(NullPointerException.class, () -> codec.unsigned(1L, 2, null));
        expect(NullPointerException.class, () -> codec.unsigned(new byte[] {0, 1}, null));
        expect(IllegalArgumentException.class, () -> codec.packedBcd(new byte[] {(byte) 0x1A}, 2));
        check("0102".equals(codec.hex(codec.unsigned(0x0102, 2, ByteOrder.BIG_ENDIAN))), "big endian");
        check("0201".equals(codec.hex(codec.unsigned(0x0102, 2, ByteOrder.LITTLE_ENDIAN))), "little endian");
        check("123".equals(codec.packedBcd(codec.packedBcd("123"), 3)), "bcd roundtrip");

        CpfFixedLengthLayout layout = new CpfFixedLengthLayout(
                "payment", 8, StandardCharsets.US_ASCII,
                List.of(new CpfFixedLengthField("amount", 0, 8,
                        CpfFixedLengthField.Alignment.RIGHT, '0', true)));
        CpfFixedLengthCodec fixed = new CpfFixedLengthCodec(layout);
        byte[] encoded = fixed.encode(Map.of("amount", "123"));
        check("00000123".equals(new String(encoded, StandardCharsets.US_ASCII)), "fixed encode");
        check("123".equals(fixed.decode(encoded).get("amount")), "fixed decode");
        System.out.println("S03_FIXED_LENGTH_VALIDATION_HARNESS PASS cases=" + cases);
    }

    private static void check(boolean condition, String label) {
        cases++;
        if (!condition) throw new AssertionError(label);
    }

    private static void expect(Class<? extends Throwable> type, Runnable action) {
        cases++;
        try {
            action.run();
            throw new AssertionError("expected " + type.getSimpleName());
        } catch (Throwable actual) {
            if (!type.isInstance(actual)) throw new AssertionError("unexpected " + actual, actual);
        }
    }
}
