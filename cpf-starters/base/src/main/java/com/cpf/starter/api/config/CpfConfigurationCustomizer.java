package com.cpf.starter.api.config;

/**
 * CPF 설정을 Property만으로 고정하지 않고 애플리케이션 코드에서 안전하게 확장하기 위한 Public Customizer 계약입니다.
 * 기본값과 외부 Property가 적용된 뒤 호출되며, 허용된 Runtime 제어보다 먼저 적용됩니다.
 *
 * @param <T> 변경 가능한 설정 모델 타입
 */
@FunctionalInterface
public interface CpfConfigurationCustomizer<T> {
    /**
     * 설정 모델을 프로그램 방식으로 보정합니다.
     *
     * @param configuration 기본값과 Property가 적용된 설정 모델
     */
    void customize(T configuration);

    /**
     * 여러 Customizer가 있을 때의 적용 순서를 반환합니다. 작은 값이 먼저 적용됩니다.
     *
     * @return 적용 순서, 기본값은 0
     */
    default int order() { return 0; }
}
