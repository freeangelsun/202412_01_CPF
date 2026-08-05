package com.cpf.common.template;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CmnTemplateDefinitionTest {
    @Test
    void normalizesAndCopiesTheVersionedContract() {
        Set<String> variables = new java.util.LinkedHashSet<>(Set.of("customer.name"));
        CmnTemplateDefinition definition = new CmnTemplateDefinition(
                " WELCOME ", 3, " SMS ", "Hello ${customer.name}", variables, true);
        variables.clear();

        assertThat(definition.templateCode()).isEqualTo("WELCOME");
        assertThat(definition.channel()).isEqualTo("SMS");
        assertThat(definition.allowedVariables()).containsExactly("customer.name");
    }

    @Test
    void rejectsUnversionedMalformedOrEmptyDefinitions() {
        assertThatThrownBy(() -> new CmnTemplateDefinition(
                "WELCOME", 0, "SMS", "Hello", Set.of(), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version");
        assertThatThrownBy(() -> new CmnTemplateDefinition(
                "bad code", 1, "SMS", "Hello", Set.of(), true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CmnTemplateDefinition(
                "WELCOME", 1, "SMS", " ", Set.of(), true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CmnTemplateDefinition(
                "WELCOME", 1, "SMS", "Hello", Set.of("bad variable"), true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
