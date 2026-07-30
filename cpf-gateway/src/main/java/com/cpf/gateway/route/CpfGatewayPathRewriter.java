package com.cpf.gateway.route;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Ingress Path Pattern과 Target Path Template을 분리하여 실제 요청 Path를 안전하게 재작성합니다.
 *
 * <p>Wildcard 또는 변수 Capture는 Target Template의 동일 토큰에만 주입하며, encoded traversal,
 * encoded separator, backslash, control character, dot segment는 Owner 호출 전에 차단합니다.</p>
 */
public final class CpfGatewayPathRewriter {
    private CpfGatewayPathRewriter() {
    }

    public static boolean matches(String ingressPattern, String requestPath) {
        try {
            capture(normalizePattern(ingressPattern), normalizeRequestPath(requestPath));
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static String rewrite(String ingressPattern, String targetTemplate, String requestPath) {
        String pattern = normalizePattern(ingressPattern);
        String inbound = normalizeRequestPath(requestPath);
        Capture capture = capture(pattern, inbound);
        String target = normalizeTarget(targetTemplate);

        if (target.equals(pattern)) return inbound;
        for (Map.Entry<String, String> entry : capture.variables().entrySet()) {
            target = target.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        if (target.contains("{*}")) target = target.replace("{*}", capture.wildcard());
        if (target.endsWith("/**")) {
            target = target.substring(0, target.length() - 3) + capture.wildcardWithSlash();
        } else if (target.endsWith("/*")) {
            target = target.substring(0, target.length() - 1) + capture.wildcard();
        }
        if (target.contains("{") || target.contains("}") || target.contains("*")) {
            throw new IllegalArgumentException("Gateway Target Path Template에 해석되지 않은 토큰이 있습니다.");
        }
        return normalizeRequestPath(target);
    }

    public static String normalizeRequestPath(String value) {
        String path = value == null || value.isBlank() ? "/" : value.trim();
        if (!path.startsWith("/")) throw new IllegalArgumentException("Gateway request path는 '/'로 시작해야 합니다.");
        String lower = path.toLowerCase(Locale.ROOT);
        if (lower.matches(".*%(2e|2f|5c|00|0d|0a).*")) {
            throw new IllegalArgumentException("Gateway encoded traversal/separator는 허용하지 않습니다.");
        }
        if (path.indexOf('\\') >= 0 || path.chars().anyMatch(ch -> ch < 0x20 || ch == 0x7f)) {
            throw new IllegalArgumentException("Gateway path에 backslash/control character를 허용하지 않습니다.");
        }
        for (String segment : path.split("/", -1)) {
            if (".".equals(segment) || "..".equals(segment)) {
                throw new IllegalArgumentException("Gateway dot segment를 허용하지 않습니다.");
            }
        }
        return path;
    }

    private static Capture capture(String pattern, String path) {
        if ("*".equals(pattern) || "/**".equals(pattern)) return new Capture(Map.of(), stripLeadingSlash(path));
        String[] p = split(pattern);
        String[] a = split(path);
        Map<String, String> variables = new LinkedHashMap<>();
        String wildcard = "";
        int i = 0;
        for (; i < p.length; i++) {
            String token = p[i];
            if ("**".equals(token) || "{*}".equals(token)) {
                wildcard = join(a, i);
                i = p.length;
                break;
            }
            if (i >= a.length) throw new IllegalArgumentException("Gateway path가 route pattern보다 짧습니다.");
            if ("*".equals(token)) {
                wildcard = a[i];
            } else if (token.startsWith("{") && token.endsWith("}") && token.length() > 2) {
                variables.put(token.substring(1, token.length() - 1), a[i]);
            } else if (!token.equals(a[i])) {
                throw new IllegalArgumentException("Gateway path가 route pattern과 일치하지 않습니다.");
            }
        }
        if (i < p.length || a.length != p.length && !pattern.endsWith("/**") && !pattern.endsWith("/{*}")) {
            throw new IllegalArgumentException("Gateway path가 route pattern과 일치하지 않습니다.");
        }
        return new Capture(Map.copyOf(variables), wildcard);
    }

    private static String normalizePattern(String value) {
        String pattern = value == null || value.isBlank() ? "/**" : value.trim();
        if ("*".equals(pattern)) return pattern;
        if (!pattern.startsWith("/")) throw new IllegalArgumentException("Gateway path pattern은 '/'로 시작해야 합니다.");
        return pattern;
    }

    private static String normalizeTarget(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Gateway target path가 필요합니다.");
        String target = value.trim();
        if (!target.startsWith("/")) throw new IllegalArgumentException("Gateway target path는 '/'로 시작해야 합니다.");
        return target;
    }

    private static String[] split(String path) {
        String value = stripLeadingSlash(path);
        return value.isEmpty() ? new String[0] : value.split("/", -1);
    }

    private static String stripLeadingSlash(String value) {
        return value.startsWith("/") ? value.substring(1) : value;
    }

    private static String join(String[] values, int from) {
        if (from >= values.length) return "";
        return String.join("/", java.util.Arrays.copyOfRange(values, from, values.length));
    }

    private record Capture(Map<String, String> variables, String wildcard) {
        String wildcardWithSlash() { return wildcard == null || wildcard.isBlank() ? "" : "/" + wildcard; }
    }
}
