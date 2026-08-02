package com.cpf.starter.security.resource;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
class CpfResourceServerPropertiesTest {
    @Test void failsClosedWithoutKeySource() {
        var p = new CpfResourceServerProperties(); p.setEnabled(true);
        assertThatThrownBy(p::validate).isInstanceOf(IllegalStateException.class);
    }
}
