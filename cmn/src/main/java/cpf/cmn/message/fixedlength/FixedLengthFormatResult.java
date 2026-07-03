package cpf.cmn.message.fixedlength;

import java.util.Map;

/**
 * 고정길이 전문 format 결과입니다.
 *
 * @param message 생성된 전문
 * @param byteLength 전문 byte 길이
 * @param maskedFields 필드별 마스킹 값
 */
public record FixedLengthFormatResult(
        String message,
        int byteLength,
        Map<String, String> maskedFields) {
}
