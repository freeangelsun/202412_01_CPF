package com.cpf.common.template;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CmnTemplateRendererTest {
    private final CmnTemplateRenderer renderer = new CmnTemplateRenderer();

    @Test
    void rendersAllowedVariablesAndRepeatedTokens() {
        var definition = definition("안녕하세요 ${name}. 다시 ${name}.", Set.of("name"));
        assertEquals(
                "안녕하세요 CPF. 다시 CPF.",
                renderer.render(definition, Map.of("name", "CPF")));
    }

    @Test
    void rejectsMissingUnknownAndNullValues() {
        var definition = definition("안녕하세요 ${name}", Set.of("name"));
        assertThrows(IllegalArgumentException.class, () -> renderer.render(definition, Map.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(definition, Map.of("name", "CPF", "extra", "x")));
        assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(definition, java.util.Collections.singletonMap("name", null)));
    }

    @Test
    void rejectsMalformedAndUnterminatedTokens() {
        assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(definition("${invalid token}", Set.of()), Map.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(definition("${name", Set.of("name")), Map.of("name", "CPF")));
        assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(definition("${}", Set.of()), Map.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(definition("${1name}", Set.of("1name")), Map.of("1name", "CPF")));
    }


    @Test
    void escapesStandardChannelsAndRequiresExactlyOneCustomEscaper() {
        var html = new CmnTemplateDefinition(
                "WELCOME", 1, "EMAIL_HTML", "<p>${name}</p>", Set.of("name"), true);
        assertEquals(
                "<p>&lt;script&gt;&amp;&quot;&#39;</p>",
                renderer.render(html, Map.of("name", "<script>&\"'")));

        var json = new CmnTemplateDefinition(
                "WELCOME", 1, "JSON", "{\"name\":\"${name}\"}", Set.of("name"), true);
        assertEquals(
                "{\"name\":\"a\\\"b\\\\c\\n\"}",
                renderer.render(json, Map.of("name", "a\"b\\c\n")));

        var custom = new CmnTemplateDefinition(
                "WELCOME", 1, "KAKAO", "${name}", Set.of("name"), true);
        assertThrows(IllegalArgumentException.class, () -> renderer.render(custom, Map.of("name", "CPF")));

        CmnTemplateValueEscaper escaper = new CmnTemplateValueEscaper() {
            @Override public boolean supports(String channel) { return "KAKAO".equals(channel); }
            @Override public String escape(String channel, String variableName, String value) {
                return "[" + value + "]";
            }
        };
        assertEquals(
                "[CPF]",
                new CmnTemplateRenderer(List.of(escaper)).render(custom, Map.of("name", "CPF")));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CmnTemplateRenderer(List.of(escaper, escaper))
                        .render(custom, Map.of("name", "CPF")));
    }

    @Test
    void permitsLiteralDollarTextThatIsNotATokenPrefix() {
        var definition = definition("가격은 $100 입니다.", Set.of());
        assertEquals("가격은 $100 입니다.", renderer.render(definition, Map.of()));
    }

    private CmnTemplateDefinition definition(String body, Set<String> allowed) {
        return new CmnTemplateDefinition("WELCOME", 1, "SMS", body, allowed, true);
    }
}
