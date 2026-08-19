package com.cpf.data.persistence.jdbc.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import static org.assertj.core.api.Assertions.assertThat;

/** Legacy CMN datasource owner must never reactivate outside cpf-starter-common. */
class CmnDataSourceContextTest {
    @Test void legacyCmnDatasourceTypeIsNotAnActiveConfiguration() {
        assertThat(CmnDataSourceConfig.class.isAnnotationPresent(Configuration.class)).isFalse();
        assertThat(CmnDataSourceConfig.class.isAnnotationPresent(Deprecated.class)).isTrue();
    }
}
