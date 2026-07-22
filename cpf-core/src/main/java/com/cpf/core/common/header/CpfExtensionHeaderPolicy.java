package cpf.pfw.common.header;

import java.util.Locale;
import java.util.Set;

/**
 * CPF 확장 헤더의 naming rule과 보안 차단 기준을 모아 둔 정책입니다.
 *
 * <p>확장 헤더는 운영자가 ADM에서 등록하는 관리 항목이 아니라 개발팀이 표준에 맞춰
 * 코드와 테스트로 추가하는 개발 표준입니다. {@code X-Cpf-Ext-*} 규칙에 맞으면 CPF 확장
 * 헤더로 인식하되, 인증값이나 secret을 우회 저장하거나 전파하는 이름은 차단합니다.</p>
 */
public final class CpfExtensionHeaderPolicy {
    public static final String PREFIX = "X-Cpf-Ext-";

    private static final Set<String> BLOCKED_NAME_TOKENS = Set.of(
            "authorization",
            "auth",
            "token",
            "api-key",
            "apikey",
            "api_key",
            "secret",
            "password",
            "passwd",
            "credential",
            "signature",
            "nonce"
    );

    private CpfExtensionHeaderPolicy() {
    }

    public static boolean isExtensionHeader(String headerName) {
        return hasText(headerName) && lower(headerName).startsWith(lower(PREFIX)) && headerName.length() > PREFIX.length();
    }

    public static boolean isAllowedExtensionHeader(String headerName) {
        return isExtensionHeader(headerName) && !isBlockedSecurityAlias(headerName);
    }

    public static boolean isBlockedSecurityAlias(String headerName) {
        if (!isExtensionHeader(headerName)) {
            return false;
        }
        String suffix = lower(headerName.substring(PREFIX.length()));
        for (String blocked : BLOCKED_NAME_TOKENS) {
            if (suffix.contains(blocked)) {
                return true;
            }
        }
        return false;
    }

    public static void requireAllowedExtensionHeader(String headerName) {
        if (!isExtensionHeader(headerName)) {
            throw new IllegalArgumentException("CPF 확장 헤더 naming rule은 X-Cpf-Ext-* 입니다. headerName=" + headerName);
        }
        if (isBlockedSecurityAlias(headerName)) {
            throw new IllegalArgumentException("인증값, token, API key, secret류는 CPF 확장 헤더로 우회 저장하거나 전파할 수 없습니다. headerName=" + headerName);
        }
    }

    private static String lower(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
