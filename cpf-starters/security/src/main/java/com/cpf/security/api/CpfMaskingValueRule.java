package com.cpf.security.api;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 운영자가 ADM에서 선택하는 마스킹 값 규칙입니다.
 *
 * <p>CPF는 마스킹 대상을 코드에 고정하지 않는다(Harness §28.1). 어떤 값 유형을 가릴지는
 * 운영자가 정하며, DB 로그와 File 로그에 같은 선택이 적용된다.</p>
 *
 * <p>고정하면 안 되는 이유는 실제 사고로 확인됐다. 장문 숫자 규칙이 항상 켜져 있어 CPF가 발급한
 * 거래ID·traceId가 계좌번호로 오인 마스킹됐고, File/DB/ADM 통합 로그를 잇는 상관관계 키 자체가
 * 사라졌다. 무엇을 가릴지는 그 로그를 운영하는 사람이 결정해야 한다.</p>
 *
 * <p>CPF가 발급한 추적 식별자(transactionId/traceId/segmentId)는 사용자 민감정보가 아니므로
 * 어떤 선택에서도 마스킹 대상이 아니다(Harness §28.2).</p>
 */
public enum CpfMaskingValueRule {

    /** PEM 개인키 블록을 가립니다. */
    PRIVATE_KEY("개인키(PEM) 블록"),

    /** JWT 형태의 토큰 문자열을 가립니다. */
    JWT("JWT 형식 토큰"),

    /** 이메일 주소의 local part를 가립니다. */
    EMAIL("이메일 주소"),

    /** 주민등록번호 형식을 가립니다. */
    KOREAN_RESIDENT_REGISTRATION_NUMBER("주민등록번호"),

    /** 국내 전화번호 형식을 가립니다. */
    KOREAN_PHONE_NUMBER("국내 전화번호"),

    /** 독립 토큰으로 나타난 10~19자리 숫자열(계좌·카드번호 후보)을 가립니다. */
    LONG_NUMERIC_IDENTIFIER("장문 숫자 식별자(계좌·카드번호)"),

    /** Authorization 헤더의 Bearer 토큰 값을 가립니다. */
    BEARER_TOKEN("Authorization Bearer 토큰");

    private final String displayName;

    CpfMaskingValueRule(String displayName) {
        this.displayName = displayName;
    }

    /** ADM 화면에 보여줄 한글 표시명입니다. */
    public String displayName() {
        return displayName;
    }

    /** 기본 선택입니다. 운영자가 아무것도 고르지 않은 초기 상태는 모든 규칙을 켠 fail-closed입니다. */
    public static Set<CpfMaskingValueRule> defaults() {
        return Set.copyOf(new LinkedHashSet<>(Arrays.asList(values())));
    }

    /** 저장소/명령 문자열을 규칙으로 해석합니다. 알 수 없는 이름은 계약 위반으로 거부합니다. */
    public static CpfMaskingValueRule of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("masking value rule is blank");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        for (CpfMaskingValueRule rule : values()) {
            if (rule.name().equals(normalized)) {
                return rule;
            }
        }
        throw new IllegalArgumentException("unknown masking value rule: " + value);
    }

    /** CSV 문자열을 규칙 집합으로 해석합니다. 비어 있으면 기본 선택을 돌려줍니다. */
    public static Set<CpfMaskingValueRule> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return defaults();
        }
        LinkedHashSet<CpfMaskingValueRule> rules = new LinkedHashSet<>();
        for (String token : csv.split(",")) {
            if (!token.isBlank()) {
                rules.add(of(token));
            }
        }
        return Set.copyOf(rules);
    }

    /** 규칙 집합을 저장 가능한 CSV로 정규화합니다(선언 순서 고정). */
    public static String toCsv(Set<CpfMaskingValueRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (CpfMaskingValueRule rule : values()) {
            if (rules.contains(rule)) {
                if (builder.length() > 0) {
                    builder.append(',');
                }
                builder.append(rule.name());
            }
        }
        return builder.toString();
    }
}
