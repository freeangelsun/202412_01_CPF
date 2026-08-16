package com.cpf.foundation.time.spi;

import java.time.LocalDate;

/**
 * CPF 업무 기준일을 제공하는 교체 가능한 정책 계약입니다.
 *
 * <p>금융권 고객사 또는 플랫폼 운영팀이 시스템 영업일, 채널 영업일, DB 관리 영업일,
 * 외부 Calendar 또는 테스트 Clock 정책으로 교체할 때 구현합니다. CPF 기본 구현은 Starter가
 * 주입한 Clock 기준의 현재 일자를 사용합니다.</p>
 *
 * <p>구현체는 Thread-safe 해야 하며 호출 시점에 유효한 업무일자를 반환해야 합니다.
 * 금지사항: 임의 fallback, 검증되지 않은 외부 입력 승계, 민감정보 포함을 허용하지 않습니다.
 * 일자를 계산할 수 없으면 임의의 날짜나 transactionId 파생값으로 대체하지 말고 실패를 명시적으로
 * 전파해야 합니다. HTTP Header, 사용자 입력, Secret을 검증 없이 업무일자로 사용해서는 안 됩니다.</p>
 */
@FunctionalInterface
public interface CpfBusinessDateProvider {
    LocalDate currentBusinessDate();
}
