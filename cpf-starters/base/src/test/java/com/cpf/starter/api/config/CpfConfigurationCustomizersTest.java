package com.cpf.starter.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** CpfConfigurationCustomizersTest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
class CpfConfigurationCustomizersTest {
    @Test
    void property다음ProgrammaticCustomizer를순서대로적용한다() {
        Model model = new Model("property");
        CpfConfigurationCustomizers.apply(model, List.of(new Ordered(20, "late"), new Ordered(10, "early")));
        assertEquals("property>early>late", model.value);
    }

    @Test
    void nullCustomizer는FailFast한다() {
        assertThrows(NullPointerException.class, () ->
                CpfConfigurationCustomizers.apply(new Model("x"), Arrays.asList((CpfConfigurationCustomizer<Model>) null)));
    }

    /** Model 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    private static final class Model {
        private String value;
        private Model(String value) { this.value = value; }
    }
    private record Ordered(int order, String suffix) implements CpfConfigurationCustomizer<Model> {
        @Override public void customize(Model configuration) { configuration.value += ">" + suffix; }
    }
}
