package com.cpf.foundation.id.spi;

/**
 * CPF 거래 추적 식별자를 생성하는 교체 가능한 정책 계약입니다.
 *
 * <p>고객사 고유 ID 규칙, Legacy 연계 ID 정책, 중앙 ID 서비스 또는 테스트용 결정적 Generator를
 * 적용할 때 플랫폼/Provider가 구현합니다. CPF 기본 구현은 Starter가 제공하며 Context Runtime 자체와
 * 결합하지 않습니다.</p>
 *
 * <p>금지사항: 검증되지 않은 외부 식별자 승계, 민감정보 포함, 실패 시 기존 ID 재사용을 허용하지 않습니다.
 * 구현체는 Thread-safe 해야 하고 중복되지 않는 유효 식별자를 반환해야 합니다. 외부 입력을 검증 없이
 * 승계하거나 사용자/계좌/Token 등 민감정보를 ID에 포함하면 안 됩니다. 생성 실패 시 임의 재사용이나
 * 빈 값으로 계속 처리하지 말고 실패를 호출자에게 전달해야 합니다.</p>
 */
@FunctionalInterface
public interface CpfTransactionIdGenerator {
    String newTransactionId();
}
