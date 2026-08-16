package com.cpf.starter.api.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Public {@link CpfConfigurationCustomizer}를 결정적인 순서로 실제 실행하는 Runtime API입니다.
 * Property binding 이후 Programmatic override 단계를 제공하며 Internal package 접근을 요구하지 않습니다.
 */
public final class CpfConfigurationCustomizers {
    private CpfConfigurationCustomizers() { }

    /**
     * 등록된 Customizer를 order 오름차순으로 적용합니다.
     *
     * @param configuration 기본값과 Property가 반영된 설정 모델
     * @param customizers 애플리케이션의 Public Customizer 모음
     * @param <T> 설정 모델 타입
     * @return 같은 설정 모델 인스턴스
     * @throws NullPointerException 설정 모델 또는 Customizer가 null인 경우
     */
    public static <T> T apply(T configuration, Collection<? extends CpfConfigurationCustomizer<T>> customizers) {
        Objects.requireNonNull(configuration, "configuration");
        if (customizers == null || customizers.isEmpty()) return configuration;
        List<CpfConfigurationCustomizer<T>> ordered = new ArrayList<>();
        for (CpfConfigurationCustomizer<T> customizer : customizers) {
            ordered.add(Objects.requireNonNull(customizer, "customizer"));
        }
        ordered.sort(Comparator.comparingInt(CpfConfigurationCustomizer::order));
        ordered.forEach(customizer -> customizer.customize(configuration));
        return configuration;
    }
}
