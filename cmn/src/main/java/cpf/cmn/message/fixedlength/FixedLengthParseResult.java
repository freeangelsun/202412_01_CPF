package cpf.cmn.message.fixedlength;

import java.util.List;
import java.util.Map;

/**
 * 고정길이 전문 parse 결과입니다.
 *
 * @param rawMessage 원문 전문
 * @param fields 필드별 원문 값
 * @param maskedFields 필드별 마스킹 값
 * @param errors 검증 오류 목록
 */
public record FixedLengthParseResult(
        String rawMessage,
        Map<String, String> fields,
        Map<String, String> maskedFields,
        List<String> errors) {

    public boolean valid() {
        return errors == null || errors.isEmpty();
    }
}
