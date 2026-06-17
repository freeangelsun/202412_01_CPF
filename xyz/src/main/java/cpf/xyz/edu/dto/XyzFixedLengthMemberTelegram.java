package cpf.xyz.edu.dto;

import cpf.cmn.tlm.core.CmnTelegramField;
import cpf.cmn.tlm.core.CmnTelegramFieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * XYZ 援먯쑁??怨좎젙湲몄씠 ?뚯썝 ?꾨Ц DTO?낅땲??
 *
 * <p>媛?record component??{@link CmnTelegramField}媛 ?꾨Ц ?쒖꽌, 湲몄씠, ?먮즺?뺤쓣 ?뺤쓽?⑸땲??
 * ?좉퇋 媛쒕컻?먮뒗 ??DTO瑜?李멸퀬???낅Т蹂??꾨Ц DTO瑜?留뚮뱾怨? {@code CmnTelegramService}濡? * ?꾨Ц ?뚯떛怨??앹꽦??泥섎━?⑸땲??</p>
 *
 * @param memberNo   ?뚯썝踰덊샇 10?먮━
 * @param memberName ?뚯썝?20?먮━
 * @param balance    ?붿븸 12?먮━, ?뚯닔 2?먮━
 * @param active     ?뺤긽 ?щ? 1?먮━
 * @param baseDate   湲곗???8?먮━ yyyyMMdd
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

