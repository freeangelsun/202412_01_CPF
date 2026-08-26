package com.cpf.data.persistence.mybatis.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import static org.assertj.core.api.Assertions.assertThat;

/** Common Product Service must not recreate a separate CMN MyBatis runtime. */
@SuppressWarnings("deprecation")
class CmnMyBatisContextTest {
    @Test void legacyCmnMyBatisTypeIsNotAnActiveConfiguration() {
        assertThat(CmnMyBatisConfig.class.isAnnotationPresent(Configuration.class)).isFalse();
        assertThat(CmnMyBatisConfig.class.isAnnotationPresent(Deprecated.class)).isTrue();
        assertThat(CmnMyBatisConfig.class.getDeclaredMethods()).isEmpty();
    }
}
