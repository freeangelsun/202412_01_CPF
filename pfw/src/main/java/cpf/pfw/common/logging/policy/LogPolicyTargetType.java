package cpf.pfw.common.logging.policy;

import java.util.Arrays;
import java.util.Locale;

/**
 * PFW 로그 정책이 적용되는 대상 유형입니다.
 *
 * <p>운영 표준 명칭은 {@code ONLINE_TRANSACTION}, {@code BATCH_JOB},
 * {@code BATCH_STEP}입니다. 기존 seed에 남아 있던 {@code TRANSACTION}은
 * 온라인 거래 정책의 호환 별칭으로만 허용합니다.</p>
 */
public enum LogPolicyTargetType {
    ONLINE_TRANSACTION("ONLINE_TRANSACTION", "TRANSACTION"),
    BATCH_JOB("BATCH_JOB"),
    BATCH_STEP("BATCH_STEP"),
    MODULE("MODULE");

    private final String code;
    private final String[] aliases;

    LogPolicyTargetType(String code, String... aliases) {
        this.code = code;
        this.aliases = aliases;
    }

    public String code() {
        return code;
    }

    public String[] databaseCodes() {
        String[] values = new String[aliases.length + 1];
        values[0] = code;
        System.arraycopy(aliases, 0, values, 1, aliases.length);
        return values;
    }

    public static LogPolicyTargetType fromCode(String value) {
        if (value == null || value.isBlank()) {
            return ONLINE_TRANSACTION;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> type.code.equals(normalized)
                        || Arrays.asList(type.aliases).contains(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 로그 정책 대상 유형입니다: " + value));
    }
}
