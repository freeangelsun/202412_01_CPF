package com.cpf.web.context;

/**
 * CPF HTTP Header의 전파·신뢰·변경·로그·필수 여부 계약입니다.
 * requiredInternal=true는 신뢰된 내부 서비스 경계에서 값 누락을 허용하지 않는다는 의미입니다.
 */
public record CpfHttpHeaderSpec(
        String name,
        CpfHeaderPropagationScope propagation,
        CpfHeaderTrustLevel trust,
        CpfHeaderMutationPolicy mutation,
        CpfHeaderDirection direction,
        CpfHeaderCompatibility compatibility,
        CpfHeaderLogPolicy logPolicy,
        boolean requiredInternal) {
}
