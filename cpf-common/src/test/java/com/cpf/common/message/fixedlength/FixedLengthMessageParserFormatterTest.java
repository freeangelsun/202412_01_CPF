package com.cpf.common.message.fixedlength;

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

    @Test
    void registryAndTypeValidationExposeFieldErrors() {
        FixedLengthLayoutSpec layout = FixedLengthLayoutSpec.utf8(
                16,
                List.of(
                        new FixedLengthFieldSpec("baseDate", 1, 8, FixedLengthFieldType.DATE, true, '0', FixedLengthAlignment.RIGHT, false),
                        new FixedLengthFieldSpec("amount", 9, 8, FixedLengthFieldType.NUMBER, true, '0', FixedLengthAlignment.RIGHT, false)));
        FixedLengthLayoutRegistry registry = new FixedLengthLayoutRegistry();
        registry.register("EDU_FIXED_001", layout);

        FixedLengthMessageParser parser = new FixedLengthMessageParser();
        FixedLengthParseResult result = parser.parse("2026023000000100", registry.require("EDU_FIXED_001"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("FIXED_FIELD_TYPE_INVALID"));
        assertThat(result.fieldErrors()).anyMatch(error -> error.errorCode().equals("FIXED_FIELD_TYPE_INVALID"));
    }

    @Test
    void formatAndParseRepeatingGroupByCountField() {
        FixedLengthLayoutSpec layout = FixedLengthLayoutSpec.utf8(
                24,
                List.of(
                        new FixedLengthFieldSpec("messageType", 1, 2, FixedLengthFieldType.STRING, true, ' ', FixedLengthAlignment.LEFT, false),
                        new FixedLengthFieldSpec("itemCount", 3, 2, FixedLengthFieldType.NUMBER, true, '0', FixedLengthAlignment.RIGHT, false)),
                List.of(
                        new FixedLengthGroupSpec(
                                "items",
                                "itemCount",
                                5,
                                2,
                                List.of(
                                        new FixedLengthFieldSpec("itemCode", 1, 3, FixedLengthFieldType.STRING, true, ' ', FixedLengthAlignment.LEFT, false),
                                        new FixedLengthFieldSpec("amount", 4, 4, FixedLengthFieldType.NUMBER, true, '0', FixedLengthAlignment.RIGHT, true)))));

        FixedLengthMessageFormatter formatter = new FixedLengthMessageFormatter();
        FixedLengthMessageParser parser = new FixedLengthMessageParser();

        FixedLengthFormatResult formatted = formatter.format(
                Map.of(
                        "messageType", "RQ",
                        "items", List.of(
                                Map.of("itemCode", "A1", "amount", "12"),
                                Map.of("itemCode", "B2", "amount", "345"))),
                layout);
        FixedLengthParseResult parsed = parser.parse(formatted.message(), layout);

        assertThat(formatted.byteLength()).isEqualTo(24);
        assertThat(formatted.maskedFields()).containsEntry("itemCount", "2");
        assertThat(formatted.maskedGroups().get("items")).hasSize(2);
        assertThat(parsed.valid()).isTrue();
        assertThat(parsed.fields()).containsEntry("itemCount", "2");
        assertThat(parsed.groups().get("items").get(0)).containsEntry("itemCode", "A1").containsEntry("amount", "12");
        assertThat(parsed.groups().get("items").get(1)).containsEntry("itemCode", "B2").containsEntry("amount", "345");
        assertThat(parsed.maskedGroups().get("items").get(0).get("amount")).isEqualTo("**");
    }
}
