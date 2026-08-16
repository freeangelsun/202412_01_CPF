package com.cpf.starter.api.config;

/** CPF 설정 소스의 Public 우선순위 계약입니다. */
public enum CpfConfigurationPrecedence {
    /** Framework가 제공하는 안전한 기본값입니다. */
    DEFAULT(0),
    /** application.yml, 환경변수 등 외부 Property 값입니다. */
    PROPERTY(100),
    /** Bean/Builder/Customizer/SPI를 통한 Programmatic override 값입니다. */
    PROGRAMMATIC(200),
    /** 권한·감사·검증을 거친 허용된 운영 Runtime 제어 값입니다. */
    RUNTIME(300);

    private final int priority;
    CpfConfigurationPrecedence(int priority) { this.priority = priority; }

    /** @return 설정 소스 적용 우선순위 */
    public int priority() { return priority; }
}
