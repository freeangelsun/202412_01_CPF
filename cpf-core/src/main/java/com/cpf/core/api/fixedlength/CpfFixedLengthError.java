package com.cpf.core.api.fixedlength;

/**
 * 고정길이 전문 처리 중 발생한 안전한 구조화 오류입니다.
 *
 * @param fieldName layout 필드 경로. 반복부는 {@code group[index].field} 형식입니다.
 * @param errorCode 안정적인 CPF 오류 코드
 * @param message 원문 민감값을 포함하지 않는 진단 메시지
 * @param byteOffset 0부터 시작하는 원문 byte offset. 알 수 없으면 -1입니다.
 * @param originalPosition 1부터 시작하는 원문 byte 위치. 알 수 없으면 -1입니다.
 * @param rejectedValue 민감 필드이면 마스킹된 거절 값
 */
public record CpfFixedLengthError(
        String fieldName,
        String errorCode,
        String message,
        int byteOffset,
        int originalPosition,
        String rejectedValue) {

    public CpfFixedLengthError(String fieldName, String errorCode, String message) {
        this(fieldName, errorCode, message, -1, -1, "");
    }

    public CpfFixedLengthError {
        fieldName = fieldName == null || fieldName.isBlank() ? "_message" : fieldName;
        errorCode = errorCode == null || errorCode.isBlank() ? "CPF_FIXED_LENGTH_ERROR" : errorCode;
        message = message == null ? "" : message;
        rejectedValue = rejectedValue == null ? "" : rejectedValue;
        if (byteOffset < -1) {
            throw new IllegalArgumentException("byteOffset은 -1 이상이어야 합니다.");
        }
        if (originalPosition < -1) {
            throw new IllegalArgumentException("originalPosition은 -1 이상이어야 합니다.");
        }
    }
}
