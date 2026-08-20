package com.cpf.admin.opr.security;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * ADM API 권한의 Method/Path 패턴과 명시적 deny 우선순위를 한 곳에서 평가합니다.
 * Frontend operation projection과 Backend enforcement가 동일한 패턴 의미를 사용하도록 하는 내부 정책입니다.
 */
public final class AdmApiPermissionPolicy {
    private AdmApiPermissionPolicy() {
    }

    public record Rule(String httpMethod, String apiPath, String allowYn) {
        public Rule {
            httpMethod = normalize(httpMethod);
            apiPath = apiPath == null ? "" : apiPath.trim();
            allowYn = normalize(allowYn);
        }

        public boolean allowed() {
            return "Y".equals(allowYn);
        }
    }

    /**
     * 가장 구체적인 Method/Path rule을 평가하며 같은 specificity의 deny가 allow보다 우선합니다.
     * 일치하는 canonical API permission이 없으면 empty를 반환합니다.
     */
    public static Optional<Boolean> evaluate(List<Rule> rules, String method, String path) {
        if (rules == null || rules.isEmpty() || method == null || path == null) {
            return Optional.empty();
        }
        String normalizedMethod = normalize(method);
        int highestSpecificity = -1;
        boolean allowAtHighest = false;
        boolean denyAtHighest = false;
        for (Rule rule : rules) {
            if (rule == null || !(normalizedMethod.equals(rule.httpMethod()) || "ANY".equals(rule.httpMethod()))) {
                continue;
            }
            if (!matchesApiPattern(rule.apiPath(), path)) {
                continue;
            }
            int specificity = rule.apiPath().replace("*", "").length()
                    + (normalizedMethod.equals(rule.httpMethod()) ? 10_000 : 0);
            if (specificity > highestSpecificity) {
                highestSpecificity = specificity;
                allowAtHighest = rule.allowed();
                denyAtHighest = !rule.allowed();
            } else if (specificity == highestSpecificity) {
                allowAtHighest |= rule.allowed();
                denyAtHighest |= !rule.allowed();
            }
        }
        return highestSpecificity < 0 ? Optional.empty() : Optional.of(allowAtHighest && !denyAtHighest);
    }

    public static boolean matchesApiPattern(String pattern, String path) {
        if (pattern == null || pattern.isBlank() || path == null) {
            return false;
        }
        String normalizedPattern = pattern.trim();
        if (normalizedPattern.equals(path)) {
            return true;
        }
        if (normalizedPattern.endsWith("/**")) {
            String prefix = normalizedPattern.substring(0, normalizedPattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
        if (!normalizedPattern.contains("*")) {
            return false;
        }
        String[] parts = normalizedPattern.split("\\*", -1);
        int index = 0;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            int found = path.indexOf(part, index);
            if (found < 0 || (i == 0 && found != 0)) {
                return false;
            }
            index = found + part.length();
        }
        String last = parts[parts.length - 1];
        return last.isEmpty() || path.endsWith(last);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
