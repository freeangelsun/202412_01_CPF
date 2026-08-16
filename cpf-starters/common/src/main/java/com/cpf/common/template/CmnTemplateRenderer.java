package com.cpf.common.template;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Fail-closed token renderer with channel-aware output escaping. */
@Component
public final class CmnTemplateRenderer {
    private static final Pattern TOKEN = Pattern.compile("\\$\\{([A-Za-z][A-Za-z0-9_.-]{0,99})}");
    private static final String TOKEN_PREFIX = "${";
    private static final Set<String> HTML_CHANNELS = Set.of("EMAIL", "EMAIL_HTML", "HTML", "DOCUMENT", "DOCUMENT_HTML");
    private static final Set<String> JSON_CHANNELS = Set.of("JSON", "WEBHOOK", "WEBHOOK_JSON");
    private static final Set<String> TEXT_CHANNELS = Set.of("SMS", "PUSH", "TEXT", "EMAIL_TEXT", "DOCUMENT_TEXT");

    private final List<CmnTemplateValueEscaper> extensions;

    public CmnTemplateRenderer() {
        this(ServiceLoader.load(CmnTemplateValueEscaper.class));
    }

    public CmnTemplateRenderer(Iterable<CmnTemplateValueEscaper> extensions) {
        List<CmnTemplateValueEscaper> loaded = new ArrayList<>();
        if (extensions != null) extensions.forEach(value -> loaded.add(Objects.requireNonNull(value, "escaper")));
        this.extensions = List.copyOf(loaded);
    }

    public String render(CmnTemplateDefinition definition, Map<String, ?> variables) {
        Objects.requireNonNull(definition, "definition");
        String body = Objects.requireNonNull(definition.body(), "template body");
        Map<String, ?> values = variables == null ? Map.of() : variables;
        validateTokenSyntax(body);

        Matcher matcher = TOKEN.matcher(body);
        StringBuffer output = new StringBuffer(body.length());
        Set<String> used = new LinkedHashSet<>();
        while (matcher.find()) {
            String name = matcher.group(1);
            used.add(name);
            if (!definition.allowedVariables().contains(name))
                throw new IllegalArgumentException("Template variable is not allowed: " + name);
            if (!values.containsKey(name))
                throw new IllegalArgumentException("Template variable is missing: " + name);
            Object value = values.get(name);
            if (value == null)
                throw new IllegalArgumentException("Template variable value is null: " + name);
            String escaped = escape(definition.channel(), name, String.valueOf(value));
            matcher.appendReplacement(output, Matcher.quoteReplacement(escaped));
        }
        matcher.appendTail(output);

        Set<String> unknown = new LinkedHashSet<>(values.keySet());
        unknown.removeAll(definition.allowedVariables());
        if (!unknown.isEmpty()) throw new IllegalArgumentException("Unknown template variables: " + unknown);
        return output.toString();
    }

    private String escape(String channel, String name, String value) {
        String normalized = channel.toUpperCase(Locale.ROOT);
        if (HTML_CHANNELS.contains(normalized)) return html(value);
        if (JSON_CHANNELS.contains(normalized)) return json(value);
        if (TEXT_CHANNELS.contains(normalized)) return value;
        List<CmnTemplateValueEscaper> matching = extensions.stream().filter(e -> e.supports(channel)).toList();
        if (matching.size() != 1)
            throw new IllegalArgumentException("Template channel requires exactly one escaper: " + channel);
        String escaped = matching.get(0).escape(channel, name, value);
        if (escaped == null) throw new IllegalStateException("Template escaper returned null: " + channel);
        return escaped;
    }

    private static String html(String value) {
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            switch (value.charAt(i)) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(value.charAt(i));
            }
        }
        return out.toString();
    }

    private static String json(String value) {
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int)c));
                    else out.append(c);
                }
            }
        }
        return out.toString();
    }

    private void validateTokenSyntax(String body) {
        int cursor = 0;
        while (true) {
            int tokenStart = body.indexOf(TOKEN_PREFIX, cursor);
            if (tokenStart < 0) return;
            Matcher token = TOKEN.matcher(body);
            token.region(tokenStart, body.length());
            if (!token.lookingAt()) {
                int tokenEnd = body.indexOf('}', tokenStart + TOKEN_PREFIX.length());
                String fragment = tokenEnd < 0 ? body.substring(tokenStart) : body.substring(tokenStart, tokenEnd + 1);
                throw new IllegalArgumentException("Malformed template token: " + fragment);
            }
            cursor = token.end();
        }
    }
}
