package com.cpf.core.api.page;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * 외부에 DB PK/내부 문자열을 그대로 노출하지 않는 불투명 Cursor 값입니다.
 *
 * <p>이 값 객체의 encode/decode는 내부 전송 인코딩만 담당합니다. 외부 API Cursor는
 * {@link CpfCursorCodec}, 기본적으로 {@link CpfHmacCursorCodec}을 사용해 위변조를 검증해야 합니다.</p>
 */
public record CpfCursor(String value) {
    public CpfCursor {
        value = Objects.requireNonNull(value, "value");
        if (value.isBlank()) throw new IllegalArgumentException("cursor는 비어 있을 수 없습니다.");
    }

    /** 내부 원문을 URL-safe Base64 값으로 인코딩합니다. 외부 API에는 서명 Codec을 사용해야 합니다.
     * @param raw null이 아닌 내부 cursor 원문
     * @return 불투명 인코딩 값
     * @throws NullPointerException raw가 null인 경우
     */
    public static CpfCursor encode(String raw) {
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(Objects.requireNonNull(raw, "raw").getBytes(StandardCharsets.UTF_8));
        return new CpfCursor(encoded);
    }

    /** 내부 Base64 cursor를 원문으로 디코딩합니다.
     * @return UTF-8 원문
     * @throws IllegalArgumentException 저장 값이 올바른 Base64가 아닌 경우
     */
    public String decode() {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
