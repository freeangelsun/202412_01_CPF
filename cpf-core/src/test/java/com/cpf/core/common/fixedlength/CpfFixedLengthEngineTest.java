package com.cpf.core.common.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthAlignment;
import com.cpf.core.api.fixedlength.CpfFixedLengthException;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldType;
import com.cpf.core.api.fixedlength.CpfFixedLengthGroupSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayout;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayoutRegistry;
import com.cpf.core.api.fixedlength.CpfFixedLengthParseResult;
import com.cpf.core.api.fixedlength.CpfFixedLengthParser;
import com.cpf.core.api.fixedlength.CpfFixedLengthWriteResult;
import com.cpf.core.api.fixedlength.CpfFixedLengthWriter;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfFixedLengthEngineTest {
    private final CpfFixedLengthWriter writer = new DefaultCpfFixedLengthWriter();
    private final CpfFixedLengthParser parser = new DefaultCpfFixedLengthParser();

    @Test
    void writesAndParsesUtf8ByByteLengthWithMaskingAndTypedValues() {
        CpfFixedLengthLayout layout = new CpfFixedLengthLayout(
                StandardCharsets.UTF_8,
                20,
                List.of(
                        field("accountNo", 1, 10, CpfFixedLengthFieldType.STRING, true),
                        field("koreanName", 11, 6, CpfFixedLengthFieldType.STRING, false),
                        field("amount", 17, 4, CpfFixedLengthFieldType.NUMBER, false)));

        CpfFixedLengthWriteResult written = writer.write(
                Map.of("accountNo", "1234567890", "koreanName", "홍길", "amount", "123"),
                layout);
        CpfFixedLengthParseResult parsed = parser.parse(written.message(), layout);

        assertThat(written.byteLength()).isEqualTo(20);
        assertThat(written.bytes()).hasSize(20);
        assertThat(written.maskedFields()).containsEntry("accountNo", "1********0");
        assertThat(parsed.valid()).isTrue();
        assertThat(parsed.fields())
                .containsEntry("accountNo", "1234567890")
                .containsEntry("koreanName", "홍길")
                .containsEntry("amount", "123");
        assertThat(parsed.typedFields()).containsEntry("amount", new BigInteger("123"));
        assertThat(parsed.maskedFields()).containsEntry("accountNo", "1********0");
    }

    @Test
    void writesAndParsesEucKrWithoutConfusingCharactersAndBytes() {
        Charset eucKr = Charset.forName("EUC-KR");
        CpfFixedLengthLayout layout = CpfFixedLengthLayout.eucKr(
                12,
                List.of(
                        field("name", 1, 6, CpfFixedLengthFieldType.STRING, false),
                        field("code", 7, 6, CpfFixedLengthFieldType.STRING, false)));

        CpfFixedLengthWriteResult written = writer.write(
                Map.of("name", "홍길동", "code", "A1"),
                layout);
        CpfFixedLengthParseResult parsed = parser.parse(written.bytes(), layout);

        assertThat(written.message().getBytes(eucKr)).hasSize(12);
        assertThat(parsed.valid()).isTrue();
        assertThat(parsed.fields()).containsEntry("name", "홍길동").containsEntry("code", "A1");
    }

    @Test
    void rejectsAMultibyteCharacterSplitAcrossFieldBoundaries() {
        Charset eucKr = Charset.forName("EUC-KR");
        CpfFixedLengthLayout layout = CpfFixedLengthLayout.eucKr(
                2,
                List.of(
                        field("firstByte", 1, 1, CpfFixedLengthFieldType.STRING, false),
                        field("secondByte", 2, 1, CpfFixedLengthFieldType.STRING, false)));

        CpfFixedLengthParseResult parsed = parser.parse("가".getBytes(eucKr), layout);

        assertThat(parsed.valid()).isFalse();
        assertThat(parsed.errors())
                .filteredOn(error -> error.errorCode().equals("CPF_FIXED_FIELD_ENCODING_INVALID"))
                .hasSize(2)
                .allSatisfy(error -> {
                    assertThat(error.byteOffset()).isGreaterThanOrEqualTo(0);
                    assertThat(error.originalPosition()).isEqualTo(error.byteOffset() + 1);
                });
    }

    @Test
    void writesAndParsesRepeatingGroupsFromTheCountField() {
        CpfFixedLengthLayout layout = CpfFixedLengthLayout.utf8(
                24,
                List.of(
                        field("messageType", 1, 2, CpfFixedLengthFieldType.STRING, false),
                        field("itemCount", 3, 2, CpfFixedLengthFieldType.NUMBER, false)),
                List.of(new CpfFixedLengthGroupSpec(
                        "items",
                        "itemCount",
                        5,
                        2,
                        List.of(
                                field("itemCode", 1, 3, CpfFixedLengthFieldType.STRING, false),
                                field("amount", 4, 4, CpfFixedLengthFieldType.NUMBER, true)))));

        CpfFixedLengthWriteResult written = writer.write(
                Map.of(
                        "messageType", "RQ",
                        "items", List.of(
                                Map.of("itemCode", "A1", "amount", "12"),
                                Map.of("itemCode", "B2", "amount", "345"))),
                layout);
        CpfFixedLengthParseResult parsed = parser.parse(written.bytes(), layout);

        assertThat(written.byteLength()).isEqualTo(24);
        assertThat(written.maskedFields()).containsEntry("itemCount", "2");
        assertThat(parsed.valid()).isTrue();
        assertThat(parsed.fields()).containsEntry("itemCount", "2");
        assertThat(parsed.groups().get("items").get(0))
                .containsEntry("itemCode", "A1")
                .containsEntry("amount", "12");
        assertThat(parsed.groups().get("items").get(1))
                .containsEntry("itemCode", "B2")
                .containsEntry("amount", "345");
        assertThat(parsed.maskedGroups().get("items").get(0)).containsEntry("amount", "**");
    }

    @Test
    void neverLeaksSensitiveRejectedValuesInStructuredErrorsOrExceptionMessages() {
        CpfFixedLengthLayout layout = CpfFixedLengthLayout.utf8(
                4,
                List.of(field("secretNumber", 1, 4, CpfFixedLengthFieldType.NUMBER, true)));

        CpfFixedLengthParseResult parsed = parser.parse("12A4", layout);

        assertThat(parsed.valid()).isFalse();
        assertThat(parsed.errors()).singleElement().satisfies(error -> {
            assertThat(error.errorCode()).isEqualTo("CPF_FIXED_FIELD_TYPE_INVALID");
            assertThat(error.rejectedValue()).isEqualTo("1**4");
            assertThat(error.message()).doesNotContain("12A4");
        });

        CpfFixedLengthLayout eucKrLayout = new CpfFixedLengthLayout(
                Charset.forName("EUC-KR"),
                4,
                List.of(field("secret", 1, 4, CpfFixedLengthFieldType.STRING, true)));
        assertThatThrownBy(() -> writer.write(Map.of("secret", "😀"), eucKrLayout))
                .isInstanceOf(CpfFixedLengthException.class)
                .hasMessageNotContaining("😀")
                .satisfies(exception -> {
                    CpfFixedLengthException fixedLengthException = (CpfFixedLengthException) exception;
                    assertThat(fixedLengthException.errors()).singleElement()
                            .satisfies(error -> assertThat(error.rejectedValue()).isEqualTo("*"));
                });
    }

    @Test
    void validatesTypesAndUsesVersionedThreadSafeRegistry() throws Exception {
        CpfFixedLengthLayout layout = new CpfFixedLengthLayout(
                "EDU_FIXED_001",
                "2026-07",
                StandardCharsets.UTF_8,
                16,
                List.of(
                        field("baseDate", 1, 8, CpfFixedLengthFieldType.DATE, false),
                        field("amount", 9, 8, CpfFixedLengthFieldType.NUMBER, false)),
                List.of());
        CpfFixedLengthLayoutRegistry registry = new CpfFixedLengthLayoutRegistry();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Void>> registrations = new ArrayList<>();
            for (int index = 0; index < 40; index++) {
                registrations.add(() -> {
                    registry.register(layout);
                    assertThat(registry.require("EDU_FIXED_001", "2026-07")).isSameAs(layout);
                    return null;
                });
            }
            for (Future<Void> future : executor.invokeAll(registrations)) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        CpfFixedLengthParseResult parsed = parser.parse(
                "2026023000000100",
                registry.require("EDU_FIXED_001", "2026-07"));
        assertThat(registry.size()).isEqualTo(1);
        assertThat(parsed.errors())
                .anyMatch(error -> error.errorCode().equals("CPF_FIXED_FIELD_TYPE_INVALID"));
    }

    @Test
    void statelessParserAndWriterAreSafeForConcurrentReuse() throws Exception {
        CpfFixedLengthLayout layout = CpfFixedLengthLayout.utf8(
                12,
                List.of(
                        field("bankCode", 1, 3, CpfFixedLengthFieldType.STRING, false),
                        field("userNo", 4, 9, CpfFixedLengthFieldType.STRING, true)));
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> calls = new ArrayList<>();
            for (int index = 0; index < 100; index++) {
                int current = index;
                calls.add(() -> {
                    String userNo = String.format("%09d", current);
                    CpfFixedLengthWriteResult written = writer.write(
                            Map.of("bankCode", "001", "userNo", userNo),
                            layout);
                    CpfFixedLengthParseResult parsed = parser.parse(written.bytes(), layout);
                    if (!parsed.valid()) {
                        throw new AssertionError(parsed.errors());
                    }
                    return parsed.fields().get("userNo");
                });
            }
            List<Future<String>> futures = executor.invokeAll(calls);
            for (int index = 0; index < futures.size(); index++) {
                assertThat(futures.get(index).get()).isEqualTo(String.format("%09d", index));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private CpfFixedLengthFieldSpec field(
            String name,
            int start,
            int length,
            CpfFixedLengthFieldType type,
            boolean sensitive) {
        return new CpfFixedLengthFieldSpec(
                name,
                start,
                length,
                type,
                true,
                '\0',
                CpfFixedLengthAlignment.AUTO,
                sensitive);
    }
}
