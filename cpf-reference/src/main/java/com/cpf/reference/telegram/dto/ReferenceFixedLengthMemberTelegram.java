package cpf.xyz.telegram.dto;

import cpf.cmn.tlm.core.CmnTelegramField;
import cpf.cmn.tlm.core.CmnTelegramFieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
public record XyzFixedLengthMemberTelegram(
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

