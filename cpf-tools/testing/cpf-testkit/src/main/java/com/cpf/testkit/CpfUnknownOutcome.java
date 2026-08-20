package com.cpf.testkit;

/**
 * 테스트 시나리오에서 결과를 성공/실패로 단정할 수 없는 CPF {@code UNKNOWN} 상태를 명시적으로 표현합니다.
 * <p>UNKNOWN을 일반 기술 실패로 축약하지 않고 Recovery/Reconcile 경로를 검증할 때 사용합니다.
 */
public final class CpfUnknownOutcome extends RuntimeException {
    public CpfUnknownOutcome(String message) { super(message); }
}
