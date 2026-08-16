package com.cpf.data.persistence.api.page;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;

/**
 * HMAC-SHA256 기반 CPF Signed Cursor 기본 구현입니다.
 *
 * <p>운영 Secret은 Source/설정 파일에 평문으로 두지 않고 Secret Manager/환경 주입으로 전달해야 합니다.
 * Key rotation이 필요한 환경은 version별 Codec을 감싸는 고객 Adapter를 제공할 수 있습니다.</p>
 */
public final class CpfHmacCursorCodec implements CpfCursorCodec {
    private static final String VERSION = "v1";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final byte[] secret;

    /** HMAC-SHA256 cursor codec을 생성합니다.
     * @param secret 최소 32 byte의 외부 주입 Secret. 내부에서 복사하여 보관합니다.
     * @throws NullPointerException secret이 null인 경우
     * @throws IllegalArgumentException secret이 32 byte 미만인 경우
     */
    public CpfHmacCursorCodec(byte[] secret) {
        Objects.requireNonNull(secret, "secret");
        if (secret.length < 32) {
            throw new IllegalArgumentException("Signed Cursor HMAC secret은 최소 32 byte여야 합니다.");
        }
        this.secret = secret.clone();
    }

    /** payload를 version/body/HMAC 형태의 URL-safe token으로 변환합니다.
     * @param payload null이 아닌 cursor 원문
     * @return 서명된 외부 cursor token
     * @throws NullPointerException payload가 null인 경우
     * @throws IllegalStateException HMAC 계산을 수행할 수 없는 경우
     */
    @Override
    public String encode(String payload) {
        Objects.requireNonNull(payload, "payload");
        String body = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signingInput = VERSION + "." + body;
        return signingInput + "." + base64(sign(signingInput));
    }

    /** token의 version과 HMAC을 상수시간 비교로 검증한 뒤 payload를 반환합니다.
     * @param token 외부에서 받은 cursor token
     * @return 검증된 UTF-8 payload
     * @throws IllegalArgumentException null/blank, 형식 오류, 서명 불일치, Base64 오류인 경우
     * @throws IllegalStateException HMAC 계산을 수행할 수 없는 경우
     */
    @Override
    public String decode(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("cursor token은 필수입니다.");
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || !VERSION.equals(parts[0]) || parts[1].isBlank() || parts[2].isBlank()) {
            throw new IllegalArgumentException("지원하지 않는 cursor token 형식입니다.");
        }
        String signingInput = parts[0] + "." + parts[1];
        byte[] expected = sign(signingInput);
        byte[] supplied;
        try {
            supplied = Base64.getUrlDecoder().decode(parts[2]);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("cursor signature 인코딩이 올바르지 않습니다.", ex);
        }
        if (!MessageDigest.isEqual(expected, supplied)) {
            throw new IllegalArgumentException("cursor signature 검증에 실패했습니다.");
        }
        try {
            return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("cursor payload 인코딩이 올바르지 않습니다.", ex);
        }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (Exception ex) {
            throw new IllegalStateException("Signed Cursor HMAC 계산에 실패했습니다.", ex);
        }
    }

    private String base64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
