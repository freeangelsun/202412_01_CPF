package com.cpf.core.common.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthField;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldType;
import com.cpf.core.api.fixedlength.CpfFixedLengthParseResult;
import com.cpf.core.api.fixedlength.CpfFixedLengthWriteResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CpfFixedLengthDtoMapperTest {
    private final DefaultCpfFixedLengthDtoMapper mapper = new DefaultCpfFixedLengthDtoMapper();

    @Test
    void writesAndParsesAnnotatedRecordThroughTheCoreEngine() {
        EducationTelegram source = new EducationTelegram(
                "M000000001",
                42L,
                new BigDecimal("123.45"),
                true,
                LocalDate.of(2026, 7, 22));

        CpfFixedLengthWriteResult written = mapper.writeFromDto(source);
        CpfFixedLengthParseResult parsed = mapper.parseToMap(
                written.message(),
                EducationTelegram.class);
        EducationTelegram restored = mapper.parseToDto(
                written.message(),
                EducationTelegram.class);

        assertThat(written.message()).isEqualTo("M0000000010004200012345Y20260722");
        assertThat(parsed.valid()).isTrue();
        assertThat(parsed.typedFields()).containsEntry("balance", new BigDecimal("123.45"));
        assertThat(restored).isEqualTo(source);
    }

    @Test
    void annotatedDtoCanSelectEucKrAndUsesByteLengths() {
        Charset eucKr = Charset.forName("EUC-KR");
        EucKrTelegram source = new EucKrTelegram("홍길동", "A1");

        CpfFixedLengthWriteResult written = mapper.writeFromDto(source, eucKr);
        EucKrTelegram restored = mapper.parseToDto(
                written.message(),
                EucKrTelegram.class,
                eucKr);

        assertThat(written.byteLength()).isEqualTo(10);
        assertThat(restored).isEqualTo(source);
    }

    private record EducationTelegram(
            @CpfFixedLengthField(order = 1, length = 10, required = true)
            String memberNo,
            @CpfFixedLengthField(order = 2, length = 5, type = CpfFixedLengthFieldType.NUMBER, required = true)
            Long amount,
            @CpfFixedLengthField(
                    order = 3,
                    length = 8,
                    type = CpfFixedLengthFieldType.AMOUNT,
                    scale = 2,
                    required = true)
            BigDecimal balance,
            @CpfFixedLengthField(order = 4, length = 1, type = CpfFixedLengthFieldType.BOOLEAN, required = true)
            Boolean active,
            @CpfFixedLengthField(order = 5, length = 8, type = CpfFixedLengthFieldType.DATE, required = true)
            LocalDate baseDate) {
    }

    private record EucKrTelegram(
            @CpfFixedLengthField(order = 1, length = 6, required = true)
            String name,
            @CpfFixedLengthField(order = 2, length = 4, required = true)
            String code) {
    }
}
