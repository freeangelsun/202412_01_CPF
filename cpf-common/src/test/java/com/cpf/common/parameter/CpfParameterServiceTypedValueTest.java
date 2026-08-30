package com.cpf.common.parameter;

import com.cpf.common.parameter.api.CpfParameter;
import com.cpf.common.parameter.api.CpfParameterService;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfParameterServiceTypedValueTest {
    private final CpfParameterService service = new CpfParameterService() {
        private final Map<String, String> values = Map.of(
                "MAX_RETRY", "3",
                "INVALID_INTEGER", "three",
                "ENABLED", "true",
                "TIMEOUT", "PT5S");
        @Override public Optional<CpfParameter> find(String key) {
            return Optional.ofNullable(values.get(key)).map(value ->
                    new CpfParameter(1L, key, "STRING", value, key, false, true));
        }
        @Override public String requiredValue(String key) { return find(key).orElseThrow().value(); }
    };

    @Test void convertsSupportedTypesWithoutBusinessCasting() {
        assertThat(service.requiredValue("MAX_RETRY", Integer.class)).isEqualTo(3);
        assertThat(service.requiredValue("ENABLED", Boolean.class)).isTrue();
        assertThat(service.requiredValue("TIMEOUT", Duration.class)).isEqualTo(Duration.ofSeconds(5));
    }

    @Test void failsClearlyForUnsupportedType() {
        assertThatThrownBy(() -> service.requiredValue("MAX_RETRY", Object.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported CPF Common parameter target type");
    }

    @Test void distinguishesMalformedSupportedValueFromUnsupportedTargetType() {
        assertThatThrownBy(() -> service.requiredValue("INVALID_INTEGER", Integer.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF Common parameter type conversion failed: key=INVALID_INTEGER, type=Integer")
                .hasCauseInstanceOf(NumberFormatException.class);
    }
}
