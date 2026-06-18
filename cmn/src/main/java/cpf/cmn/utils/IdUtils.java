package cpf.cmn.utils;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 업무에서 자주 쓰는 임시 식별자를 생성하는 유틸리티입니다.
 *
 * <p>글로벌 거래 ID는 PFW의 {@code TransactionIdGenerator}를 사용해야 합니다.
 * 이 클래스는 업무 임시번호, 화면 요청번호, 샘플 데이터 식별자처럼 거래 ID가 아닌 보조 식별자에만 사용합니다.</p>
 */
public final class IdUtils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private IdUtils() {
    }

    /**
     * 하이픈 없는 UUID 문자열을 생성합니다.
     *
     * @return 32자리 UUID 문자열
     */
    public static String uuid32() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 지정한 접두어와 날짜, 난수를 조합해 업무용 임시 ID를 생성합니다.
     *
     * @param prefix 업무 접두어
     * @return 예: TMP20260612095615123456
     */
    public static String temporaryId(String prefix) {
        String safePrefix = TextUtils.defaultIfBlank(prefix, "TMP").trim().toUpperCase();
        int random = SECURE_RANDOM.nextInt(1_000_000);
        return safePrefix + DateTimeUtils.nowDateTimeMillis() + String.format("%06d", random);
    }
}

