package cpf.pfw.common.header;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * CPF 표준 헤더 사전입니다.
 *
 * <p>이 사전은 문서, 검증, 로그 마스킹, ADM 로그 상세 표시, WebClient/RestClient 자동 전파가 같은
 * 기준을 보도록 유지하는 단일 기준점입니다.</p>
 */
public final class CpfHeaderSpecs {
    private static final List<CpfHeaderSpec> ALL = List.of(
            spec(CpfHeaderNames.TRANSACTION_ID, CpfHeaderCategory.REQUIRED, "CPF 거래 글로벌 ID", "클라이언트 또는 PFW", "수신 인터셉터", true, true, "TRANSACTION_ID", 64, true, false, "요약"),
            spec(CpfHeaderNames.STANDARD_EXECUTION_ID, CpfHeaderCategory.RECOMMENDED, "CPF 온라인·배치 표준 실행 ID", "호출 서비스 또는 PFW", "수신 인터셉터", true, true, "BUSINESS_TRANSACTION_ID", 20, true, false, "요약"),
            spec(CpfHeaderNames.PROTOCOL_VERSION, CpfHeaderCategory.RECOMMENDED, "CPF 서비스 호출 규격 버전", "호출 서비스 또는 PFW", "수신 인터셉터", true, true, null, 20, true, false, "호출"),
            spec(CpfHeaderNames.PARENT_TRANSACTION_ID, CpfHeaderCategory.RECOMMENDED, "직전 상위 거래 ID", "상위 서비스", "PFW 헤더 추출기", true, false, "PARENT_TRANSACTION_ID", 64, true, false, "추적"),
            spec(CpfHeaderNames.ORIGINAL_TRANSACTION_ID, CpfHeaderCategory.RECOMMENDED, "최초 유입 거래 ID", "최초 유입 서비스", "PFW 헤더 추출기", true, false, "ORIGINAL_TRANSACTION_ID", 64, true, false, "추적"),
            spec(CpfHeaderNames.ROOT_TRANSACTION_ID, CpfHeaderCategory.INTERNAL_ONLY, "복합 거래 최초 거래 ID", "PFW", "PFW 구간 추적", true, false, null, 100, true, false, "추적"),
            spec(CpfHeaderNames.TRANSACTION_SEGMENT_ID, CpfHeaderCategory.INTERNAL_ONLY, "현재 거래 구간 ID", "PFW", "PFW 구간 추적", true, false, null, 120, true, false, "추적"),
            spec(CpfHeaderNames.PARENT_TRANSACTION_SEGMENT_ID, CpfHeaderCategory.INTERNAL_ONLY, "상위 거래 구간 ID", "PFW", "PFW 구간 추적", true, false, null, 120, true, false, "추적"),
            spec(CpfHeaderNames.TRANSACTION_CALL_DEPTH, CpfHeaderCategory.INTERNAL_ONLY, "거래 구간 호출 깊이", "PFW", "PFW 구간 추적", true, false, null, 10, true, false, "추적"),
            spec(CpfHeaderNames.REQUEST_ID, CpfHeaderCategory.RECOMMENDED, "호출자 기준 요청 ID", "클라이언트", "PFW 헤더 추출기", true, false, "REQUEST_ID", 80, true, false, "추적"),
            spec(CpfHeaderNames.EXTERNAL_REQUEST_ID, CpfHeaderCategory.OPTIONAL, "외부 기관 요청 ID", "외부 기관", "PFW 헤더 추출기", true, false, "EXTERNAL_REQUEST_ID", 100, true, false, "추적"),
            spec(CpfHeaderNames.CORRELATION_ID, CpfHeaderCategory.RECOMMENDED, "업무 흐름 상관 ID", "클라이언트 또는 Gateway", "PFW 헤더 추출기", true, true, "CORRELATION_ID", 100, true, false, "추적"),
            spec(CpfHeaderNames.IDEMPOTENCY_KEY, CpfHeaderCategory.RECOMMENDED, "멱등 처리 키", "클라이언트", "업무 서비스", true, false, "IDEMPOTENCY_KEY", 120, true, true, "추적"),
            spec(CpfHeaderNames.IDEMPOTENCY_KEY_ALIAS, CpfHeaderCategory.OPTIONAL, "표준 멱등 키 별칭", "클라이언트", "PFW 헤더 추출기", true, false, "IDEMPOTENCY_KEY", 120, true, true, "추적"),

            spec(CpfHeaderNames.TRACE_ID, CpfHeaderCategory.RECOMMENDED, "분산 추적 trace ID", "클라이언트 또는 PFW", "PFW 필터", true, true, "TRACE_ID", 80, true, false, "추적"),
            spec(CpfHeaderNames.SPAN_ID, CpfHeaderCategory.INTERNAL_ONLY, "현재 처리 span ID", "PFW", "PFW 필터", false, true, "SPAN_ID", 80, true, false, "추적"),
            spec(CpfHeaderNames.PARENT_SPAN_ID, CpfHeaderCategory.RECOMMENDED, "상위 span ID", "상위 서비스", "PFW 필터", true, false, "PARENT_SPAN_ID", 80, true, false, "추적"),
            spec(CpfHeaderNames.TRACEPARENT, CpfHeaderCategory.RECOMMENDED, "W3C traceparent", "Gateway 또는 APM", "PFW 헤더 추출기", true, false, "TRACEPARENT", 128, true, false, "추적"),
            spec(CpfHeaderNames.TRACESTATE, CpfHeaderCategory.OPTIONAL, "W3C tracestate", "Gateway 또는 APM", "PFW 헤더 추출기", true, false, "TRACESTATE", 512, true, true, "추적"),

            spec(CpfHeaderNames.API_VERSION, CpfHeaderCategory.RECOMMENDED, "호출 API 버전", "클라이언트", "PFW 헤더 추출기", true, false, "API_VERSION", 20, true, false, "호출"),
            spec(CpfHeaderNames.REQUEST_TYPE, CpfHeaderCategory.REQUIRED, "요청 유형", "클라이언트", "수신 인터셉터", true, false, "REQUEST_TYPE", 30, true, false, "채널"),
            spec(CpfHeaderNames.ORIGINAL_CHANNEL_CODE, CpfHeaderCategory.REQUIRED, "최초 유입 채널", "클라이언트 또는 Gateway", "수신 인터셉터", true, false, "ORIGINAL_CHANNEL_CODE", 30, true, false, "채널"),
            spec(CpfHeaderNames.CHANNEL_CODE, CpfHeaderCategory.REQUIRED, "현재 처리 채널", "클라이언트 또는 Gateway", "수신 인터셉터", true, false, "CHANNEL_CODE", 30, true, false, "채널"),
            spec(CpfHeaderNames.CHANNEL_DETAIL_CODE, CpfHeaderCategory.OPTIONAL, "세부 채널", "클라이언트 또는 Gateway", "PFW 헤더 추출기", true, false, "CHANNEL_DETAIL_CODE", 50, true, false, "채널"),

            spec(CpfHeaderNames.USER_ID, CpfHeaderCategory.RECOMMENDED, "사용자 계정 ID", "인증 시스템", "인증/감사 계층", true, false, "EXEC_USER", 100, true, true, "주체"),
            spec(CpfHeaderNames.OPERATOR_ID, CpfHeaderCategory.RECOMMENDED, "ADM 운영자 ID", "ADM 인증", "인증/감사 계층", true, false, "OPERATOR_ID", 100, true, true, "주체"),
            spec(CpfHeaderNames.CUSTOMER_NO, CpfHeaderCategory.OPTIONAL, "고객 번호", "업무 서비스", "업무 서비스", true, false, "CUSTOMER_NO", 50, true, true, "주체"),
            spec(CpfHeaderNames.MEMBER_NO, CpfHeaderCategory.OPTIONAL, "회원 번호", "MBR 서비스", "업무 서비스", true, false, "MEMBER_NO", 50, true, true, "주체"),
            spec(CpfHeaderNames.TENANT_ID, CpfHeaderCategory.OPTIONAL, "테넌트 ID", "Gateway 또는 업무 서비스", "업무 서비스", true, false, "TENANT_ID", 50, true, false, "주체"),
            spec(CpfHeaderNames.ORGANIZATION_CODE, CpfHeaderCategory.OPTIONAL, "조직 코드", "업무 서비스", "업무 서비스", true, false, "ORGANIZATION_CODE", 50, true, false, "주체"),
            spec(CpfHeaderNames.BRANCH_CODE, CpfHeaderCategory.OPTIONAL, "지점 코드", "업무 서비스", "업무 서비스", true, false, "BRANCH_CODE", 50, true, false, "주체"),

            spec(CpfHeaderNames.CLIENT_IP, CpfHeaderCategory.RECOMMENDED, "신뢰 정책으로 해석한 클라이언트 IP", "PFW", "PFW trusted proxy 정책", true, false, "CLIENT_IP", 64, true, true, "접속"),
            spec(CpfHeaderNames.FORWARDED_FOR, CpfHeaderCategory.FORBIDDEN_TO_LOG_RAW, "프록시 경유 IP 목록", "Proxy/LB/WAF", "PFW 필터", false, false, "FORWARDED_FOR", 512, false, true, "접속"),
            spec(CpfHeaderNames.FORWARDED, CpfHeaderCategory.FORBIDDEN_TO_LOG_RAW, "표준 Forwarded 헤더", "Proxy/LB/WAF", "PFW 필터", false, false, "FORWARDED", 512, false, true, "접속"),
            spec(CpfHeaderNames.REAL_IP, CpfHeaderCategory.RECOMMENDED, "실제 클라이언트 IP 후보", "Proxy/LB/WAF", "PFW 필터", false, false, "REAL_IP", 64, true, true, "접속"),
            spec(CpfHeaderNames.CLIENT_COUNTRY_CODE, CpfHeaderCategory.OPTIONAL, "클라이언트 국가 ISO 3166-1 alpha-2 코드", "Gateway 또는 WAF", "PFW 헤더 추출기", true, false, "CLIENT_COUNTRY_CODE", 2, true, false, "접속"),
            spec(CpfHeaderNames.CLIENT_REGION_CODE, CpfHeaderCategory.OPTIONAL, "클라이언트 지역 코드", "Gateway 또는 WAF", "PFW 헤더 추출기", true, false, "CLIENT_REGION_CODE", 30, true, false, "접속"),
            spec(CpfHeaderNames.TIMEZONE, CpfHeaderCategory.RECOMMENDED, "서버 처리 기준 시간대", "클라이언트 또는 Gateway", "PFW 헤더 추출기", true, false, "TIMEZONE", 60, true, false, "접속"),
            spec(CpfHeaderNames.CLIENT_TIMEZONE, CpfHeaderCategory.OPTIONAL, "클라이언트 표시 기준 시간대", "클라이언트", "PFW 헤더 추출기", true, false, "CLIENT_TIMEZONE", 60, true, false, "접속"),

            spec(CpfHeaderNames.CLIENT_APP_ID, CpfHeaderCategory.RECOMMENDED, "클라이언트 앱 ID", "클라이언트", "PFW 헤더 추출기", true, false, "CLIENT_APP_ID", 80, true, false, "호출"),
            spec(CpfHeaderNames.CLIENT_VERSION, CpfHeaderCategory.RECOMMENDED, "클라이언트 버전", "클라이언트", "PFW 헤더 추출기", true, false, "CLIENT_VERSION", 40, true, false, "호출"),
            spec(CpfHeaderNames.CALLER_SERVICE, CpfHeaderCategory.RECOMMENDED, "호출 서비스 ID", "상위 서비스", "PFW 헤더 추출기", true, false, "CALLER_SERVICE", 80, true, false, "호출"),
            spec(CpfHeaderNames.CALLER_INSTANCE_ID, CpfHeaderCategory.OPTIONAL, "호출 인스턴스 ID", "상위 서비스", "PFW 헤더 추출기", true, false, "CALLER_INSTANCE_ID", 120, true, false, "호출"),
            spec(CpfHeaderNames.USER_AGENT, CpfHeaderCategory.RECOMMENDED, "User-Agent", "클라이언트", "PFW 헤더 추출기", false, false, "USER_AGENT", 500, true, true, "호출"),
            spec(CpfHeaderNames.LOCALE, CpfHeaderCategory.RECOMMENDED, "언어/지역 코드", "클라이언트", "PFW 헤더 추출기", true, false, "LOCALE", 20, true, false, "호출"),
            spec(CpfHeaderNames.SCREEN_ID, CpfHeaderCategory.OPTIONAL, "화면 ID", "클라이언트", "PFW 헤더 추출기", true, false, "SCREEN_ID", 80, true, false, "호출"),
            spec(CpfHeaderNames.DEVICE_ID, CpfHeaderCategory.OPTIONAL, "기기 ID", "클라이언트", "PFW 헤더 추출기", true, false, "DEVICE_ID", 120, true, true, "호출"),
            spec(CpfHeaderNames.CLIENT_REQUEST_TIME, CpfHeaderCategory.OPTIONAL, "클라이언트 요청 시각", "클라이언트", "PFW 헤더 추출기", true, false, "CLIENT_REQUEST_TIME", 40, true, false, "호출"),

            spec(CpfHeaderNames.AUTHORIZATION, CpfHeaderCategory.FORBIDDEN_TO_LOG_RAW, "인증 토큰", "클라이언트", "인증 필터", false, false, null, 0, false, true, "보안"),
            spec(CpfHeaderNames.API_KEY, CpfHeaderCategory.FORBIDDEN_TO_LOG_RAW, "API Key", "클라이언트", "인증 필터", false, false, null, 0, false, true, "보안"),
            spec(CpfHeaderNames.REQUEST_SIGNATURE, CpfHeaderCategory.FORBIDDEN_TO_LOG_RAW, "요청 서명", "클라이언트", "인증 필터", false, false, "REQUEST_SIGNATURE", 256, false, true, "보안"),
            spec(CpfHeaderNames.REQUEST_TIMESTAMP, CpfHeaderCategory.RECOMMENDED, "서명 기준 요청 시각", "클라이언트", "인증 필터", true, false, "REQUEST_TIMESTAMP", 40, true, false, "보안"),
            spec(CpfHeaderNames.NONCE, CpfHeaderCategory.FORBIDDEN_TO_LOG_RAW, "재전송 방지 난수", "클라이언트", "인증 필터", false, false, "NONCE", 128, false, true, "보안"),

            spec(CpfHeaderNames.RESERVED_FIELD_1, CpfHeaderCategory.OPTIONAL, "프로젝트 확장 예약 필드 1", "업무 서비스", "업무 서비스", true, false, "RESERVED_FIELD_1", 200, true, true, "확장"),
            spec(CpfHeaderNames.RESERVED_FIELD_2, CpfHeaderCategory.OPTIONAL, "프로젝트 확장 예약 필드 2", "업무 서비스", "업무 서비스", true, false, "RESERVED_FIELD_2", 200, true, true, "확장"),
            spec(CpfHeaderNames.RESERVED_FIELD_3, CpfHeaderCategory.OPTIONAL, "프로젝트 확장 예약 필드 3", "업무 서비스", "업무 서비스", true, false, "RESERVED_FIELD_3", 200, true, true, "확장"),
            spec(CpfHeaderNames.RESERVED_FIELD_4, CpfHeaderCategory.OPTIONAL, "프로젝트 확장 예약 필드 4", "업무 서비스", "업무 서비스", true, false, "RESERVED_FIELD_4", 200, true, true, "확장"),
            spec(CpfHeaderNames.RESERVED_FIELD_5, CpfHeaderCategory.OPTIONAL, "프로젝트 확장 예약 필드 5", "업무 서비스", "업무 서비스", true, false, "RESERVED_FIELD_5", 200, true, true, "확장"),

            spec(CpfHeaderNames.EXTENSION_1, CpfHeaderCategory.OPTIONAL, "CPF 확장 예약 헤더 1", "업무 서비스", "PFW 확장 헤더 정책", true, false, null, 200, true, false, "확장"),
            spec(CpfHeaderNames.EXTENSION_2, CpfHeaderCategory.OPTIONAL, "CPF 확장 예약 헤더 2", "업무 서비스", "PFW 확장 헤더 정책", true, false, null, 200, true, false, "확장"),
            spec(CpfHeaderNames.EXTENSION_3, CpfHeaderCategory.OPTIONAL, "CPF 확장 예약 헤더 3", "업무 서비스", "PFW 확장 헤더 정책", true, false, null, 200, true, false, "확장"),
            spec(CpfHeaderNames.EXTENSION_4, CpfHeaderCategory.OPTIONAL, "CPF 확장 예약 헤더 4", "업무 서비스", "PFW 확장 헤더 정책", true, false, null, 200, true, false, "확장"),
            spec(CpfHeaderNames.EXTENSION_5, CpfHeaderCategory.OPTIONAL, "CPF 확장 예약 헤더 5", "업무 서비스", "PFW 확장 헤더 정책", true, false, null, 200, true, false, "확장")
    );

    private static final Map<String, CpfHeaderSpec> BY_LOWER_NAME = toMap(ALL);
    private static final Set<String> REQUIRED_NAMES = Set.of(
            lower(CpfHeaderNames.TRANSACTION_ID),
            lower(CpfHeaderNames.REQUEST_TYPE),
            lower(CpfHeaderNames.ORIGINAL_CHANNEL_CODE),
            lower(CpfHeaderNames.CHANNEL_CODE)
    );

    private CpfHeaderSpecs() {
    }

    public static List<CpfHeaderSpec> all() {
        return ALL;
    }

    public static Optional<CpfHeaderSpec> find(String name) {
        if (name == null) {
            return Optional.empty();
        }
        CpfHeaderSpec registered = BY_LOWER_NAME.get(lower(name));
        if (registered != null) {
            return Optional.of(registered);
        }
        if (CpfExtensionHeaderPolicy.isAllowedExtensionHeader(name)) {
            return Optional.of(extensionSpec(name));
        }
        return Optional.empty();
    }

    public static boolean isRequired(String name) {
        return name != null && REQUIRED_NAMES.contains(lower(name));
    }

    public static boolean shouldPropagate(String name) {
        if (CpfExtensionHeaderPolicy.isAllowedExtensionHeader(name)) {
            return true;
        }
        return find(name).map(CpfHeaderSpec::propagation).orElse(false);
    }

    public static boolean shouldMask(String name) {
        if (CpfExtensionHeaderPolicy.isAllowedExtensionHeader(name)) {
            return false;
        }
        return find(name).map(spec -> spec.masked() || !spec.loggable()).orElse(false);
    }

    public static boolean canLogRaw(String name) {
        if (CpfExtensionHeaderPolicy.isAllowedExtensionHeader(name)) {
            return true;
        }
        if (CpfExtensionHeaderPolicy.isBlockedSecurityAlias(name)) {
            return false;
        }
        return find(name).map(CpfHeaderSpec::loggable).orElse(true);
    }

    public static boolean isAllowedExtensionHeader(String name) {
        return CpfExtensionHeaderPolicy.isAllowedExtensionHeader(name);
    }

    private static CpfHeaderSpec spec(
            String name,
            CpfHeaderCategory category,
            String description,
            String producer,
            String validationPoint,
            boolean propagation,
            boolean responseHeader,
            String dbColumn,
            int recommendedDbLength,
            boolean loggable,
            boolean masked,
            String admSection) {
        return new CpfHeaderSpec(
                name,
                category,
                description,
                producer,
                validationPoint,
                propagation,
                responseHeader,
                dbColumn,
                recommendedDbLength,
                loggable,
                masked,
                admSection);
    }

    private static CpfHeaderSpec extensionSpec(String name) {
        return spec(
                name,
                CpfHeaderCategory.OPTIONAL,
                "CPF naming rule로 인식한 확장 헤더",
                "업무 서비스",
                "PFW 확장 헤더 정책",
                true,
                false,
                null,
                200,
                true,
                false,
                "확장");
    }

    private static Map<String, CpfHeaderSpec> toMap(List<CpfHeaderSpec> specs) {
        Map<String, CpfHeaderSpec> map = new LinkedHashMap<>();
        for (CpfHeaderSpec spec : specs) {
            map.put(lower(spec.name()), spec);
        }
        return Collections.unmodifiableMap(map);
    }

    private static String lower(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
