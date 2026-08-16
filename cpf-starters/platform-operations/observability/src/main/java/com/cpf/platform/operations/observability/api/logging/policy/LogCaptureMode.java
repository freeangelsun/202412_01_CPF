package com.cpf.platform.operations.observability.api.logging.policy;

import java.util.Locale;
import java.util.Set;

/**
 * 로그 수집 영역별 캡처 모드입니다.
 *
 * <p>하나의 Enum을 사용하되 {@link #validateFor(CaptureArea)}에서 영역별 허용 모드를
 * 엄격히 검사해 UI, DB, Resolver, Gateway가 같은 계약을 사용하게 합니다.</p>
 */
public enum LogCaptureMode {
    NONE,
    METADATA_ONLY,
    ALLOWLIST,
    ALLOWLIST_FIELDS,
    MASKED,
    HASHED,
    MASKED_BODY,
    ENCRYPTED_BODY,
    SUMMARY,
    FULL_MASKED;

    /** CaptureArea 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum CaptureArea { QUERY, HEADER, BODY, STACK }

    private static final Set<LogCaptureMode> QUERY_MODES = Set.of(NONE, ALLOWLIST, MASKED, HASHED);
    private static final Set<LogCaptureMode> HEADER_MODES = Set.of(NONE, ALLOWLIST, MASKED);
    private static final Set<LogCaptureMode> BODY_MODES = Set.of(
            NONE, METADATA_ONLY, ALLOWLIST_FIELDS, MASKED_BODY, ENCRYPTED_BODY);
    private static final Set<LogCaptureMode> STACK_MODES = Set.of(NONE, SUMMARY, FULL_MASKED);

    /** parse 작업을 CPF 표준 계약에 따라 수행한다. */
    public static LogCaptureMode parse(String value, LogCaptureMode fallback, CaptureArea area) {
        LogCaptureMode mode = fallback;
        if (value != null && !value.isBlank()) {
            try {
                mode = valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("지원하지 않는 로그 캡처 모드입니다: " + value, ex);
            }
        }
        if (mode == null) {
            mode = NONE;
        }
        return mode.validateFor(area);
    }

    /** validateFor 작업을 CPF 표준 계약에 따라 수행한다. */
    public LogCaptureMode validateFor(CaptureArea area) {
        Set<LogCaptureMode> allowed = switch (area) {
            case QUERY -> QUERY_MODES;
            case HEADER -> HEADER_MODES;
            case BODY -> BODY_MODES;
            case STACK -> STACK_MODES;
        };
        if (!allowed.contains(this)) {
            throw new IllegalArgumentException(area + " 영역에서 허용되지 않는 로그 캡처 모드입니다: " + this);
        }
        return this;
    }

    /** capturesPayload 작업을 CPF 표준 계약에 따라 수행한다. */
    public boolean capturesPayload() {
        return this != NONE && this != METADATA_ONLY;
    }
}
