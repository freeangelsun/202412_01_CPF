package com.cpf.data.persistence.api.page;

/**
 * 외부 API의 Keyset/Cursor 값을 인코딩하고 위변조를 검증하는 CPF 공개 계약입니다.
 *
 * <p>외부 Paging API는 단순 Base64 문자열을 직접 만들지 말고 이 계약을 사용합니다.</p>
 */
public interface CpfCursorCodec {
    /** 원본 Cursor payload를 외부 전달용 토큰으로 변환합니다.
     * @param payload null이 아닌 원본 cursor payload
     * @return 위변조 검증 가능한 외부 전달 토큰
     * @throws IllegalArgumentException 구현 정책상 payload를 허용할 수 없는 경우
     */
    String encode(String payload);

    /** 서명을 검증한 뒤 원본 payload를 반환합니다. 위변조 토큰은 예외로 거부합니다.
     * @param token 외부에서 전달된 cursor token
     * @return 검증된 원본 payload
     * @throws IllegalArgumentException token 형식/서명/payload가 올바르지 않은 경우
     */
    String decode(String token);
}
