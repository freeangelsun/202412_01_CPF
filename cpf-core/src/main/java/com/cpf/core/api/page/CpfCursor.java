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

    public static CpfCursor encode(String raw) {
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(Objects.requireNonNull(raw, "raw").getBytes(StandardCharsets.UTF_8));
        return new CpfCursor(encoded);
    }

    public String decode() {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
