package com.cpf.backoffice.online;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import static org.assertj.core.api.Assertions.assertThat;

/** Generated Domain과 동일하게 별도 scanBasePackages 없이 Root package discovery를 사용하는지 검증합니다. */
class BackofficeOnlineApplicationConfigurationTest {
    @Test
    void usesRootPackageComponentDiscovery() {
        SpringBootApplication annotation = BackofficeOnlineApplication.class.getAnnotation(SpringBootApplication.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.scanBasePackages()).isEmpty();
    }
}
