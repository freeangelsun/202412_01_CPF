package com.cpf.education.integration.telegram.dto;
import com.cpf.integration.fixedlength.api.CpfFixedLengthField;
import com.cpf.integration.fixedlength.api.CpfFixedLengthFieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 문자열·금액·불리언·일자를 포함한 중립 고정길이 전문 교육 DTO입니다. */
public record EducationFixedLengthEducationTelegram(
        @CpfFixedLengthField(
                order = 1,
                length = 10,
                type = CpfFixedLengthFieldType.STRING,
                required = true,
                sensitive = true)
        String educationKey,

        @CpfFixedLengthField(order = 2, length = 20, type = CpfFixedLengthFieldType.STRING, required = true)
        String displayName,

        @CpfFixedLengthField(
                order = 3,
                length = 12,
                type = CpfFixedLengthFieldType.AMOUNT,
                scale = 2,
                required = true)
        BigDecimal amount,

        @CpfFixedLengthField(order = 4, length = 1, type = CpfFixedLengthFieldType.BOOLEAN, required = true)
        Boolean active,

        @CpfFixedLengthField(order = 5, length = 8, type = CpfFixedLengthFieldType.DATE, required = true)
        LocalDate baseDate) {
}
