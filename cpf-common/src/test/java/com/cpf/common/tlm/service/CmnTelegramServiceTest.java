package com.cpf.common.tlm.service;

import com.cpf.common.mqe.service.CmnMessageCodec;
import com.cpf.common.tlm.core.CmnTelegramField;
import com.cpf.common.tlm.core.CmnTelegramFieldSpec;
import com.cpf.common.tlm.core.CmnTelegramFieldType;
import com.cpf.common.tlm.core.CmnTelegramParseResult;
import com.cpf.core.common.exception.CpfValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 고정길이 전문의 양방향 변환과 스키마 경계 검증을 확인합니다. */
class CmnTelegramServiceTest {
    private CmnTelegramService telegramService;

    @BeforeEach
    void setUp() {
        telegramService = new CmnTelegramService(new CmnMessageCodec(new ObjectMapper().findAndRegisterModules()));
    }

    /** DTO 값을 고정길이 전문으로 만들고 동일한 DTO로 복원하는지 확인합니다. */
    @Test
    void writesAndParsesAnnotatedRecord() {
        EducationTelegram source = new EducationTelegram("M000000001", 42L, LocalDate.of(2026, 7, 22));

        String telegram = telegramService.writeFromDto(source);
        EducationTelegram restored = telegramService.parseToDto(telegram, EducationTelegram.class);

        assertThat(telegram).isEqualTo("M0000000010004220260722");
        assertThat(restored).isEqualTo(source);
    }

    /** 짧거나 긴 전문을 안전하게 처리하고 길이 차이를 경고로 제공하는지 확인합니다. */
    @Test
    void reportsShortAndLongTelegramBoundaries() {
        List<CmnTelegramFieldSpec> schema = List.of(
                CmnTelegramFieldSpec.of("memberNo", 1, 4, CmnTelegramFieldType.STRING));

        CmnTelegramParseResult shortResult = telegramService.parseToMap("AB", schema);
        CmnTelegramParseResult longResult = telegramService.parseToMap("ABCDE", schema);

        assertThat(shortResult.rawFields()).containsEntry("memberNo", "AB  ");
        assertThat(shortResult.warnings()).singleElement().asString().contains("짧아");
        assertThat(longResult.rawFields()).containsEntry("__remaining", "E");
        assertThat(longResult.warnings()).singleElement().asString().contains("길어");
    }

    /** 필드 순서 또는 이름이 중복된 스키마를 모호하게 해석하지 않는지 확인합니다. */
    @Test
    void rejectsDuplicateSchemaIdentity() {
        CmnTelegramFieldSpec first = CmnTelegramFieldSpec.of("memberNo", 1, 4, CmnTelegramFieldType.STRING);
        CmnTelegramFieldSpec duplicateOrder = CmnTelegramFieldSpec.of("name", 1, 4, CmnTelegramFieldType.STRING);
        CmnTelegramFieldSpec duplicateName = CmnTelegramFieldSpec.of("memberNo", 2, 4, CmnTelegramFieldType.STRING);

        assertThatThrownBy(() -> telegramService.parseToMap("ABCDEFGH", List.of(first, duplicateOrder)))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("순서가 중복");
        assertThatThrownBy(() -> telegramService.parseToMap("ABCDEFGH", List.of(first, duplicateName)))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("필드명이 중복");
    }

    /** 숫자 필드에 숫자가 아닌 값이 들어오면 필드 식별자를 포함해 실패하는지 확인합니다. */
    @Test
    void reportsInvalidTypedField() {
        List<CmnTelegramFieldSpec> schema = List.of(
                CmnTelegramFieldSpec.of("amount", 1, 4, CmnTelegramFieldType.NUMBER));

        assertThatThrownBy(() -> telegramService.parseToMap("ABCD", schema))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("field=amount")
                .hasMessageContaining("type=NUMBER");
    }

    /** EDU에서 그대로 참고할 수 있는 최소 고정길이 전문 DTO입니다. */
    private record EducationTelegram(
            @CmnTelegramField(order = 1, length = 10) String memberNo,
            @CmnTelegramField(order = 2, length = 5, type = CmnTelegramFieldType.NUMBER) Long amount,
            @CmnTelegramField(order = 3, length = 8, type = CmnTelegramFieldType.DATE) LocalDate baseDate) {
    }
}
