package cpf.cmn.message.fixedlength;

/**
 * 고정길이 전문 처리 중 발생한 필드 단위 오류입니다.
 *
 * @param fieldName 오류가 발생한 필드명
 * @param errorCode 오류 코드
 * @param message 개발자와 운영자가 볼 수 있는 오류 설명
 */
public record FixedLengthMessageError(
        String fieldName,
        String errorCode,
        String message) {
}
