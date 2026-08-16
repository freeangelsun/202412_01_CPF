package com.cpf.data.cache.caffeine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import static org.assertj.core.api.Assertions.assertThat;

class CpfCaffeineCacheAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CpfCaffeineCacheAutoConfiguration.class);

    @Test void disabledByDefault() {
        runner.run(context -> assertThat(context).doesNotHaveBean(CacheManager.class));
    }

    @Test void enabledCreatesCacheManager() {
        runner.withPropertyValues("cpf.data.cache.caffeine.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(CacheManager.class));
    }
}
