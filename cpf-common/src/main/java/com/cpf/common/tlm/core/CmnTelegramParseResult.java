package cpf.cmn.tlm.core;

import java.util.List;
import java.util.Map;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
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

