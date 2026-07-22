package com.cpf.reference.telegram.dto;

import com.cpf.common.tlm.core.CmnTelegramField;
import com.cpf.common.tlm.core.CmnTelegramFieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 문자열·금액·불리언·일자를 포함한 고정길이 회원 전문 교육 DTO입니다. */
public record ReferenceFixedLengthMemberTelegram(
        @CmnTelegramField(order = 1, length = 10, type = CmnTelegramFieldType.STRING)
        String memberNo,

        @CmnTelegramField(order = 2, length = 20, type = CmnTelegramFieldType.STRING)
        String memberName,

        @CmnTelegramField(order = 3, length = 12, type = CmnTelegramFieldType.DECIMAL, scale = 2)
        BigDecimal balance,

        @CmnTelegramField(order = 4, length = 1, type = CmnTelegramFieldType.BOOLEAN)
        Boolean active,

        @CmnTelegramField(order = 5, length = 8, type = CmnTelegramFieldType.DATE)
        LocalDate baseDate) {
}

