package cpf.cmn.tlm.core;

import java.util.List;
import java.util.Map;

/**
 * 怨좎젙湲몄씠 ?꾨Ц ?뚯떛 寃곌낵?낅땲??
 *
 * @param originalText    ?먮Ц ?꾨Ц
 * @param expectedLength  ?ㅽ궎留?湲곗? 湲곕? 湲몄씠
 * @param actualLength    ?ㅼ젣 ?섏떊 湲몄씠
 * @param rawFields       ?꾨뱶蹂??먮Ц 臾몄옄?? * @param typedFields     ?꾨뱶蹂??먮즺??蹂??寃곌낵
 * @param json            JSON ?쒗쁽
 * @param warnings        湲몄씠 遺議? 珥덇낵 ??寃쎄퀬 紐⑸줉
 */
public record CmnTelegramParseResult(
        String originalText,
        int expectedLength,
        int actualLength,
        Map<String, String> rawFields,
        Map<String, Object> typedFields,
        String json,
        List<String> warnings) {
}

