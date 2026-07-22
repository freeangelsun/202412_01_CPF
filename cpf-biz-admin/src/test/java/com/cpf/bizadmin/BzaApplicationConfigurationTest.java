package com.cpf.bizadmin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

/** BZA 패키지 전환 후 CPF·CMN·BZA 컴포넌트 스캔 범위를 고정하는 회귀 테스트입니다. */
class BzaApplicationConfigurationTest {

    @Test
    void scansCanonicalCpfPackages() {
        SpringBootApplication annotation = BzaApplication.class.getAnnotation(SpringBootApplication.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.scanBasePackages())
                .containsExactly("com.cpf.core", "com.cpf.common", "com.cpf.bizadmin");
    }
}
