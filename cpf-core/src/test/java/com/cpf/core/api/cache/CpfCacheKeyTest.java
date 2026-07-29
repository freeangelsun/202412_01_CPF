package com.cpf.core.api.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfCacheKeyTest {
    @Test
    void rejectsRedisPatternAndDelimiterCharactersInNamespaceAndTenant() {
        assertThatThrownBy(() -> new CpfCacheKey("account*", "1", "GLOBAL"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CpfCacheKey("account", "1", "TENANT:OTHER"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
