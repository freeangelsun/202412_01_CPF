package com.cpf.foundation.id.spi;

/**
 * CPF 실행 단위와 Segment 식별자를 생성하는 교체 가능한 정책 계약입니다.
 *
 * <p>Batch, Async, Message, Integration 등 실행 단위의 식별 규칙을 고객사 또는 설치환경 정책에
 * 맞게 교체할 때 플랫폼/Provider 구현이 제공합니다. CPF 기본 구현은 충돌 가능성이 낮은 독립 ID를 생성합니다.</p>
 *
 * <p>구현체는 Thread-safe 해야 하며 동시 호출에서도 식별자 충돌을 만들지 않아야 합니다.
 * 금지사항: 검증되지 않은 외부 식별자 승계, 민감정보 포함, 충돌 ID 재사용을 허용하지 않습니다.
 * 이미 검증되지 않은 외부 Header 값을 그대로 생성 결과로 반환하거나 Secret/개인정보를 ID에 포함해서는 안 됩니다.
 * 생성 실패 시 빈 문자열이나 재사용 ID로 fallback하지 말고 명시적으로 실패해야 합니다.</p>
 */
public interface CpfExecutionIdGenerator {
    String newExecutionId();
    String newSegmentId();
}
