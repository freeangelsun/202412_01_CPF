package com.cpf.security.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/** Fail-closed masking for logs, audit records and operational output. */
/** CpfMaskingRuntime는 로그·화면·증적에 민감정보 원문이 노출되지 않도록 마스킹 정책과 상태를 관리합니다. */
public final class CpfMaskingRuntime {
    private static final int DEFAULT_MAX_LENGTH = 4000;
    private static final int MIN_MAX_LENGTH = 256;
    private static final int ABSOLUTE_MAX_LENGTH = 65536;
    private static final int MAX_MASKING_PASSES = 3;
    private static final Set<String> DEFAULT_SENSITIVE_KEYS = Set.of(
            "password", "passwd", "pwd", "token", "authorization", "auth", "secret",
            "cookie", "set-cookie", "credential", "signature", "privatekey", "private-key",
            "ssn", "rrn", "resident", "residentno", "accountno", "accountnumber",
            "cardno", "cardnumber", "pin", "otp", "apikey", "api_key", "clientsecret");
    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile(
            "(?is)(-----BEGIN(?: [A-Z0-9]+)? PRIVATE KEY-----).*?(-----END(?: [A-Z0-9]+)? PRIVATE KEY-----)");
    private static final Pattern JWT_PATTERN = Pattern.compile(
            "(?<![A-Za-z0-9_-])[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}(?![A-Za-z0-9_-])");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)(?<![A-Z0-9._%+-])[A-Z0-9._%+-]{1,64}@([A-Z0-9.-]+\\.[A-Z]{2,})(?![A-Z0-9._%+-])");
    private static final Pattern KOREAN_RRN_PATTERN = Pattern.compile("(?<!\\d)\\d{6}-?[1-4]\\d{6}(?!\\d)");
    private static final Pattern KOREAN_PHONE_PATTERN = Pattern.compile("(?<!\\d)(?:01[016789]|02|0[3-6][1-5])[- ]?\\d{3,4}[- ]?\\d{4}(?!\\d)");
    // 계좌/카드번호는 **독립 토큰**으로 나타난다(`accountNo=12345678901234567`, `"1234567890123456"`).
    // 앞뒤가 영숫자로 이어지면 그것은 식별자 토큰의 일부이지 계좌번호가 아니다.
    // 경계를 숫자(`(?<!\d)`)로만 잡았더니 CPF 가 발급한 추적 식별자가 훼손됐다:
    //   traceId  `da3f0a1b6562f69d...` -> `da3f***6562f69d...`
    // trace/span id 는 사용자 민감정보가 아니라 File/DB/ADM 통합 로그의 상관관계 키다(Harness §28.2).
    // 그래서 경계를 영숫자 토큰 경계로 좁힌다. 라벨이 붙은 값(`accountno=...`)은 key 기반 규칙이
    // 별도로 막고, 따옴표/구분자로 둘러싸인 순수 숫자열은 이 규칙이 그대로 막는다.
    private static final Pattern LONG_ACCOUNT_PATTERN =
            Pattern.compile("(?<![A-Za-z0-9])\\d{10,19}(?![A-Za-z0-9])");
    // 초기 상태는 fail-closed 로 모든 값 규칙을 켠다. 이후 무엇을 가릴지는 운영자가 ADM 에서 정한다.
    private static final AtomicReference<MaskingPolicy> POLICY =
            new AtomicReference<>(MaskingPolicy.create(DEFAULT_SENSITIVE_KEYS, DEFAULT_MAX_LENGTH,
                    true, CpfMaskingValueRule.defaults(), 3L, Instant.EPOCH,
                    sha256("CPF_MASKING_POLICY_INITIAL")));

    private CpfMaskingRuntime() {}

    /** mask는 민감정보 원문을 보존하지 않으면서 필요한 식별 가능 범위만 안전하게 반환합니다. */
    public static String mask(String value) {
        MaskingPolicy policy = POLICY.get();
        return mask(value, policy.maxLength(), policy);
    }

    /** mask는 민감정보 원문을 보존하지 않으면서 필요한 식별 가능 범위만 안전하게 반환합니다. */
    public static String mask(String value, int maxLength) {
        return mask(value, maxLength, POLICY.get());
    }

    /** Compatibility update. Operational callers should prefer CpfMaskingPolicyOperations. */
    /** replacePolicy는 마스킹 정책의 조회·변경을 버전/동시성 의미를 잃지 않도록 처리합니다. */
    public static MaskingPolicy replacePolicy(Set<String> sensitiveKeys, int maxLength, boolean maskBearerToken) {
        return replacePolicy(sensitiveKeys, maxLength, maskBearerToken, null);
    }

    /**
     * 값 규칙 선택까지 함께 교체합니다.
     *
     * @param valueRules 운영자가 선택한 값 규칙. {@code null} 이면 현재 선택을 유지합니다.
     */
    public static MaskingPolicy replacePolicy(Set<String> sensitiveKeys, int maxLength,
            boolean maskBearerToken, Set<CpfMaskingValueRule> valueRules) {
        while (true) {
            MaskingPolicy current = POLICY.get();
            MaskingPolicy next = MaskingPolicy.create(sensitiveKeys, maxLength, maskBearerToken,
                    valueRules == null ? current.valueRules() : valueRules,
                    current.version() + 1L, Instant.now(), sha256("CPF_MASKING_POLICY_COMPATIBILITY_UPDATE"));
            if (POLICY.compareAndSet(current, next)) return next;
        }
    }

    /** Optimistic atomic update used by the audited runtime policy manager. */
    /** compareAndSetPolicy는 마스킹 정책의 조회·변경을 버전/동시성 의미를 잃지 않도록 처리합니다. */
    public static PolicyUpdateResult compareAndSetPolicy(long expectedVersion, Set<String> sensitiveKeys,
            int maxLength, boolean maskBearerToken, Instant updatedAt, String changeHash) {
        return compareAndSetPolicy(expectedVersion, sensitiveKeys, maxLength, maskBearerToken,
                null, updatedAt, changeHash);
    }

    /**
     * 값 규칙 선택까지 함께 반영하는 낙관적 갱신입니다.
     *
     * @param valueRules 운영자가 선택한 값 규칙. {@code null} 이면 현재 선택을 유지합니다.
     */
    public static PolicyUpdateResult compareAndSetPolicy(long expectedVersion, Set<String> sensitiveKeys,
            int maxLength, boolean maskBearerToken, Set<CpfMaskingValueRule> valueRules,
            Instant updatedAt, String changeHash) {
        if (expectedVersion < 1L) throw new IllegalArgumentException("expectedVersion must be positive");
        MaskingPolicy current = POLICY.get();
        if (current.version() != expectedVersion) {
            return new PolicyUpdateResult(current, current, false);
        }
        MaskingPolicy next = MaskingPolicy.create(sensitiveKeys, maxLength, maskBearerToken,
                valueRules == null ? current.valueRules() : valueRules,
                expectedVersion + 1L, updatedAt, changeHash);
        if (POLICY.compareAndSet(current, next)) {
            return new PolicyUpdateResult(current, next, true);
        }
        return new PolicyUpdateResult(current, POLICY.get(), false);
    }

    /** currentPolicy는 마스킹 정책의 조회·변경을 버전/동시성 의미를 잃지 않도록 처리합니다. */
    public static MaskingPolicy currentPolicy() {
        return POLICY.get();
    }

    /** Current masking policy version for compatibility facades and operational diagnostics. */
    public static long policyVersion() {
        return POLICY.get().version();
    }

    private static String mask(String value, int maxLength, MaskingPolicy policy) {
        if (value == null || value.isBlank()) return value;
        String masked = value;
        for (int pass = 0; pass < MAX_MASKING_PASSES; pass++) {
            String before = masked;
            // 값 규칙은 코드가 아니라 **운영자가 ADM 에서 선택한 정책**이 정한다(Harness §28.1).
            // 무엇을 가릴지는 그 로그를 운영하는 사람이 결정해야 한다. 하드코딩으로 항상 적용했더니
            // CPF 가 발급한 거래ID/traceId 까지 훼손되어 통합 로그 상관관계가 끊긴 사고가 있었다.
            Set<CpfMaskingValueRule> rules = policy.valueRules();
            if (policy.maskBearerToken() && rules.contains(CpfMaskingValueRule.BEARER_TOKEN)) {
                masked = policy.authorizationPattern().matcher(masked).replaceAll("$1***");
            }
            if (rules.contains(CpfMaskingValueRule.PRIVATE_KEY)) {
                masked = PRIVATE_KEY_PATTERN.matcher(masked).replaceAll("$1***$2");
            }
            if (rules.contains(CpfMaskingValueRule.JWT)) {
                masked = JWT_PATTERN.matcher(masked).replaceAll("***JWT***");
            }
            if (rules.contains(CpfMaskingValueRule.EMAIL)) {
                masked = EMAIL_PATTERN.matcher(masked).replaceAll("***@$1");
            }
            if (rules.contains(CpfMaskingValueRule.KOREAN_RESIDENT_REGISTRATION_NUMBER)) {
                masked = KOREAN_RRN_PATTERN.matcher(masked).replaceAll("******-*******");
            }
            if (rules.contains(CpfMaskingValueRule.KOREAN_PHONE_NUMBER)) {
                masked = KOREAN_PHONE_PATTERN.matcher(masked).replaceAll("***-****-****");
            }
            if (rules.contains(CpfMaskingValueRule.LONG_NUMERIC_IDENTIFIER)) {
                masked = maskLongNumericIdentifiers(masked);
            }
            for (Pattern pattern : policy.jsonPatterns()) {
                masked = pattern.matcher(masked).replaceAll("$1***$2");
            }
            for (Pattern pattern : policy.escapedJsonPatterns()) {
                masked = pattern.matcher(masked).replaceAll("$1***$2");
            }
            for (Pattern pattern : policy.xmlPatterns()) {
                masked = pattern.matcher(masked).replaceAll("$1***$2");
            }
            for (Pattern pattern : policy.keyValuePatterns()) {
                masked = pattern.matcher(masked).replaceAll("$1***");
            }
            if (masked.equals(before)) break;
        }
        return truncate(masked, normalizeMaxLength(maxLength));
    }

    /** Field-aware fail-closed masking for identifiers stored in summary columns. */
    /** maskIdentifier는 민감정보 원문을 보존하지 않으면서 필요한 식별 가능 범위만 안전하게 반환합니다. */
    public static String maskIdentifier(String value) {
        if (value == null || value.isBlank()) return value;
        String normalized = value.trim();
        int keep = Math.min(4, normalized.length());
        return "***" + normalized.substring(normalized.length() - keep);
    }

    /**
     * 독립 토큰으로 나타난 10~19자리 숫자열만 계좌/카드번호로 보고 마스킹합니다.
     *
     * <p>경계를 숫자로만 잡았을 때 CPF 가 발급한 추적 식별자가 훼손됐다. 정본 거래ID
     * {@code 20260903001256762BATS1JCXLU0000001} 의 앞 17자리 timestamp 가 계좌번호로 오인되어
     * {@code ***6762BATS1JCXLU0000001} 이 되었고, traceId {@code da3f0a1b6562...} 도
     * {@code da3f***6562...} 로 잘렸다. 그 결과 File/DB/ADM 통합 로그를 잇는 **상관관계 키 자체가
     * 사라져** Batch→Domain 응답유실 검증의 lineage 대조가 실패했다.</p>
     *
     * <p>같은 값이 파일 로그의 <b>파일명</b>과 {@code CPF_TRANSACTION_LOG}, ADM 조회에는 원문으로
     * 남으므로 본문만 가리는 것은 보호 효과가 없고 계약만 깨뜨린다. 영숫자 토큰 경계
     * ({@link #LONG_ACCOUNT_PATTERN})로 좁히면 식별자는 보존되고, 라벨이 붙은 민감값
     * ({@code accountNo=...})은 key 기반 규칙이, 따옴표/구분자로 둘러싸인 순수 숫자열은 이 규칙이
     * 그대로 막는다.</p>
     *
     * <p>고정 테스트: {@code CpfMaskingRuntimeTransactionIdTest}.</p>
     */
    private static String maskLongNumericIdentifiers(String value) {
        var matcher = LONG_ACCOUNT_PATTERN.matcher(value);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String raw = matcher.group();
            String replacement = "***" + raw.substring(Math.max(0, raw.length() - 4));
            matcher.appendReplacement(out, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    /** truncate는 민감정보 원문을 보존하지 않으면서 필요한 식별 가능 범위만 안전하게 반환합니다. */
    public static String truncate(String value, int maxLength) {
        int normalized = normalizeMaxLength(maxLength);
        if (value == null || value.length() <= normalized) return value;
        return value.substring(0, normalized) + "...(truncated)";
    }

    private static int normalizeMaxLength(int value) {
        if (value < MIN_MAX_LENGTH) return MIN_MAX_LENGTH;
        return Math.min(value, ABSOLUTE_MAX_LENGTH);
    }

    /** PolicyUpdateResult는 CPF 공개 계약의 상태와 동작 의미를 명확히 표현합니다. */
    public record PolicyUpdateResult(MaskingPolicy previous, MaskingPolicy current, boolean applied) {
        public PolicyUpdateResult {
            if (previous == null || current == null) throw new IllegalArgumentException("policies are required");
        }
    }

    /** MaskingPolicy는 로그·화면·증적에 민감정보 원문이 노출되지 않도록 마스킹 정책과 상태를 관리합니다. */
    public record MaskingPolicy(
            long version,
            Set<String> sensitiveKeys,
            int maxLength,
            boolean maskBearerToken,
            /** 운영자가 ADM에서 선택한 값 규칙입니다(Harness §28.1). */
            Set<CpfMaskingValueRule> valueRules,
            String policyHash,
            Instant updatedAt,
            String changeHash,
            List<Pattern> jsonPatterns,
            List<Pattern> escapedJsonPatterns,
            List<Pattern> xmlPatterns,
            List<Pattern> keyValuePatterns,
            Pattern authorizationPattern) {

        public MaskingPolicy {
            if (version < 1L) throw new IllegalArgumentException("version must be positive");
            sensitiveKeys = Set.copyOf(sensitiveKeys);
            if (valueRules == null) throw new IllegalArgumentException("valueRules is required");
            valueRules = Set.copyOf(valueRules);
            updatedAt = java.util.Objects.requireNonNull(updatedAt, "updatedAt");
            if (policyHash == null || !policyHash.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("invalid policyHash");
            }
            if (changeHash == null || !changeHash.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("invalid changeHash");
            }
            jsonPatterns = List.copyOf(jsonPatterns);
            escapedJsonPatterns = List.copyOf(escapedJsonPatterns);
            xmlPatterns = List.copyOf(xmlPatterns);
            keyValuePatterns = List.copyOf(keyValuePatterns);
            authorizationPattern = java.util.Objects.requireNonNull(authorizationPattern, "authorizationPattern");
        }

        private static MaskingPolicy create(Set<String> sensitiveKeys, int maxLength, boolean maskBearerToken,
                Set<CpfMaskingValueRule> selectedRules, long version, Instant updatedAt, String changeHash) {
            // Bearer 마스킹 여부는 값 규칙 BEARER_TOKEN 과 같은 의미다. 두 표현이 어긋나면 운영자가
            // 화면에서 끈 규칙이 실제로는 계속 적용되는 사고가 난다. 여기서 한 번에 정합화한다.
            Set<CpfMaskingValueRule> valueRules =
                    selectedRules == null ? CpfMaskingValueRule.defaults() : Set.copyOf(selectedRules);
            if (!maskBearerToken && valueRules.contains(CpfMaskingValueRule.BEARER_TOKEN)) {
                LinkedHashSet<CpfMaskingValueRule> without = new LinkedHashSet<>(valueRules);
                without.remove(CpfMaskingValueRule.BEARER_TOKEN);
                valueRules = Set.copyOf(without);
            }
            boolean bearerEnabled = valueRules.contains(CpfMaskingValueRule.BEARER_TOKEN);
            LinkedHashSet<String> normalized = new LinkedHashSet<>(DEFAULT_SENSITIVE_KEYS);
            if (sensitiveKeys != null) {
                sensitiveKeys.stream()
                        .filter(key -> key != null && !key.isBlank())
                        .map(key -> key.trim().toLowerCase(Locale.ROOT))
                        .filter(key -> key.matches("[a-z0-9_.-]{2,64}"))
                        .forEach(normalized::add);
            }
            if (normalized.size() > 512) throw new IllegalArgumentException("too many sensitive keys");
            int normalizedMaxLength = normalizeMaxLength(maxLength);
            List<Pattern> json = new ArrayList<>();
            List<Pattern> escapedJson = new ArrayList<>();
            List<Pattern> xml = new ArrayList<>();
            List<Pattern> keyValue = new ArrayList<>();
            for (String rawKey : normalized) {
                String key = Pattern.quote(rawKey);
                json.add(Pattern.compile("(?i)(\\\"" + key
                        + "\\\"\\s*:\\s*\\\")(?:\\\\.|[^\\\"\\\\])*(\\\")"));
                escapedJson.add(Pattern.compile("(?i)(\\\\\\\"" + key
                        + "\\\\\\\"\\s*:\\s*\\\\\\\")(?:\\\\\\\\.|[^\\\\\\\"])*(\\\\\\\")"));
                xml.add(Pattern.compile("(?is)(<\\s*" + key + "(?:\\s+[^>]*)?>).*?(<\\s*/\\s*" + key + "\\s*>)"));
                keyValue.add(Pattern.compile("(?i)(\\b" + key + "\\b\\s*[=:]\\s*)"
                        + "(?:\\\"(?:\\\\.|[^\\\"\\\\])*\\\"|'(?:\\\\.|[^'\\\\])*'|[^,\\]\\}&;\\r\\n\\s]+)"));
            }
            String policyHash = policyHash(normalized, normalizedMaxLength, bearerEnabled, valueRules);
            return new MaskingPolicy(
                    version,
                    Set.copyOf(normalized),
                    normalizedMaxLength,
                    bearerEnabled,
                    valueRules,
                    policyHash,
                    java.util.Objects.requireNonNull(updatedAt, "updatedAt"),
                    requireHash(changeHash),
                    List.copyOf(json),
                    List.copyOf(escapedJson),
                    List.copyOf(xml),
                    List.copyOf(keyValue),
                    Pattern.compile("(?i)((?:\\bAuthorization\\b\\s*[:=]\\s*)?(?:\\bBearer\\b|\\bBasic\\b)\\s+)[^,;\\s\\]\\}\\\"]+"));
        }

        private static String policyHash(Set<String> keys, int maxLength, boolean bearer,
                Set<CpfMaskingValueRule> valueRules) {
            return sha256(String.join(",", new TreeSet<>(keys)) + '\n' + maxLength + '\n' + bearer
                    + '\n' + CpfMaskingValueRule.toCsv(valueRules));
        }

        private static String requireHash(String value) {
            if (value == null || !value.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("invalid changeHash");
            return value;
        }
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        // SHA-256은 JDK 필수 알고리즘이므로 예외 발생 시 내부 계약 위반으로 처리합니다.
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
