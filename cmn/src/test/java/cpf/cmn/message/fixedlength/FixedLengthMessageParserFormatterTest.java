package cpf.cmn.message.fixedlength;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FixedLengthMessageParserFormatterTest {

    @Test
    void formatAndParseUseByteLengthWithMasking() {
        FixedLengthLayoutSpec layout = new FixedLengthLayoutSpec(
                StandardCharsets.UTF_8,
                20,
                List.of(
                        new FixedLengthFieldSpec("accountNo", 1, 10, FixedLengthFieldType.STRING, true, ' ', FixedLengthAlignment.LEFT, true),
                        new FixedLengthFieldSpec("koreanName", 11, 6, FixedLengthFieldType.STRING, true, ' ', FixedLengthAlignment.LEFT, false),
                        new FixedLengthFieldSpec("amount", 17, 4, FixedLengthFieldType.NUMBER, true, '0', FixedLengthAlignment.RIGHT, false)));

        FixedLengthMessageFormatter formatter = new FixedLengthMessageFormatter();
        FixedLengthMessageParser parser = new FixedLengthMessageParser();

        FixedLengthFormatResult formatted = formatter.format(
                Map.of("accountNo", "1234567890", "koreanName", "홍길", "amount", "123"),
                layout);
        FixedLengthParseResult parsed = parser.parse(formatted.message(), layout);

        assertThat(formatted.byteLength()).isEqualTo(20);
        assertThat(formatted.message().getBytes(StandardCharsets.UTF_8)).hasSize(20);
        assertThat(formatted.maskedFields().get("accountNo")).isEqualTo("1********0");
        assertThat(parsed.valid()).isTrue();
        assertThat(parsed.fields())
                .containsEntry("accountNo", "1234567890")
                .containsEntry("koreanName", "홍길")
                .containsEntry("amount", "123");
        assertThat(parsed.maskedFields().get("accountNo")).isEqualTo("1********0");
    }
}
