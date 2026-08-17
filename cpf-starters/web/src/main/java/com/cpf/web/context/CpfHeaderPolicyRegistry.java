package com.cpf.web.context;

import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.web.api.CpfHttpHeaders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Validates dynamic header policy while preserving read access to unregistered headers. */
public final class CpfHeaderPolicyRegistry {
    private final CpfHeaderPolicyProperties properties;
    private final Map<String, CpfHeaderPolicyProperties.Rule> rules;

    public CpfHeaderPolicyRegistry(CpfHeaderPolicyProperties properties) {
        this.properties = properties == null ? new CpfHeaderPolicyProperties() : properties;
        LinkedHashMap<String, CpfHeaderPolicyProperties.Rule> byName = new LinkedHashMap<>();
        this.properties.getPolicies().forEach((name, rule) -> {
            if (name == null || name.isBlank() || rule == null) return;
            if (CpfHttpHeaderCatalog.isProtected(name)) {
                throw new IllegalStateException("Protected CPF header cannot be overridden by dynamic policy: " + name);
            }
            String key = name.toLowerCase(Locale.ROOT);
            if (byName.putIfAbsent(key, rule) != null) throw new IllegalStateException("Duplicate header policy: " + name);
            if (rule.getPattern() != null && !rule.getPattern().isBlank()) {
                try { Pattern.compile(rule.getPattern()); }
                catch (PatternSyntaxException ex) { throw new IllegalStateException("Invalid header policy pattern: " + name, ex); }
            }
        });
        rules = Map.copyOf(byName);
    }

    public void validate(CpfHttpHeaders headers) {
        CpfHttpHeaders safe = headers == null ? CpfHttpHeaders.empty() : headers;
        int totalCount = safe.names().stream().mapToInt(name -> safe.getAll(name).size()).sum();
        if (totalCount > positive(properties.getMaxCount(), 100)) {
            throw invalid("*", "Header 개수가 허용 범위를 초과했습니다.");
        }
        int totalBytes = safe.names().stream().mapToInt(name ->
                safe.getAll(name).stream().mapToInt(value -> name.length() + value.length()).sum()).sum();
        if (totalBytes > positive(properties.getMaxBytes(), 32768)) {
            throw invalid("*", "Header 전체 크기가 허용 범위를 초과했습니다.");
        }
        rules.forEach((name, rule) -> validateRule(safe, name, rule));
    }

    public boolean internalPropagationAllowed(String name) {
        CpfHeaderPolicyProperties.Rule rule = rule(name);
        return rule != null && rule.isInternalPropagationAllowed();
    }

    public boolean externalOutboundAllowed(String name) {
        CpfHeaderPolicyProperties.Rule rule = rule(name);
        return rule != null && rule.isExternalOutboundAllowed();
    }

    public boolean sensitive(String name) {
        CpfHeaderPolicyProperties.Rule rule = rule(name);
        return rule != null && (rule.isSensitive() || rule.isMasked());
    }

    public CpfHeaderLogPolicy logPolicy(String name) {
        CpfHttpHeaderSpec standard = CpfHttpHeaderCatalog.find(name);
        if (standard != null) return standard.logPolicy();
        CpfHeaderPolicyProperties.Rule rule = rule(name);
        if (rule == null) return CpfHeaderLogPolicy.MASKED;
        if (!rule.isLoggable()) return CpfHeaderLogPolicy.NEVER;
        return rule.isSensitive() || rule.isMasked() ? CpfHeaderLogPolicy.MASKED : CpfHeaderLogPolicy.IDENTIFIER;
    }

    private void validateRule(CpfHttpHeaders headers, String normalizedName, CpfHeaderPolicyProperties.Rule rule) {
        String actualName = headers.names().stream().filter(n -> n.equalsIgnoreCase(normalizedName)).findFirst().orElse(normalizedName);
        List<String> values = headers.getAll(actualName);
        if (rule.isRequired() && values.isEmpty()) throw missing(actualName);
        if (!values.isEmpty() && !rule.isInboundAllowed()) throw invalid(actualName, "수신이 허용되지 않은 Header입니다.");
        int maxCount = positive(rule.getMaxCount(), 1);
        if ((!rule.isDuplicateAllowed() && values.size() > 1) || values.size() > maxCount) {
            throw invalid(actualName, "중복 Header 개수가 허용 범위를 초과했습니다.");
        }
        for (String value : values) {
            if (value.length() > positive(rule.getMaxLength(), 4096)) throw invalid(actualName, "Header 길이가 허용 범위를 초과했습니다.");
            if (rule.getPattern() != null && !rule.getPattern().isBlank() && !Pattern.matches(rule.getPattern(), value)) {
                throw invalid(actualName, "Header 형식이 정책과 일치하지 않습니다.");
            }
            validateType(actualName, value, rule.getType());
        }
    }

    private void validateType(String name, String value, String type) {
        if (type == null || type.isBlank() || "string".equalsIgnoreCase(type)) return;
        try {
            switch (type.toLowerCase(Locale.ROOT)) {
                case "uuid" -> java.util.UUID.fromString(value);
                case "integer", "int" -> Integer.parseInt(value);
                case "long" -> Long.parseLong(value);
                case "boolean" -> { if (!("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))) throw new IllegalArgumentException(); }
                default -> throw new IllegalStateException("Unsupported header policy type: " + type);
            }
        } catch (IllegalArgumentException ex) { throw invalid(name, "Header 타입이 정책과 일치하지 않습니다."); }
    }

    private CpfHeaderPolicyProperties.Rule rule(String name) {
        return name == null ? null : rules.get(name.toLowerCase(Locale.ROOT));
    }
    private int positive(int value, int fallback) { return value > 0 ? value : fallback; }
    private CpfHeaderValidationException missing(String name) {
        return new CpfHeaderValidationException(CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER, name,
                "필수 Header가 없습니다: " + name, 400, "HEADER_REQUIRED");
    }
    private CpfHeaderValidationException invalid(String name, String message) {
        return new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA, name, message, 400, "HEADER_INVALID");
    }
}
