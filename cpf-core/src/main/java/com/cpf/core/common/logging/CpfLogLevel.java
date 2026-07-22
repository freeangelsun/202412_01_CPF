package com.cpf.core.common.logging;

/**
 * CPF 동적 거래 로그에서 사용할 수 있는 런타임 로그 레벨입니다.
 *
 * <p>ADM 동적 로그 레벨 조치나 운영 진단 요청이 들어오면 거래 ID, 업무 거래 ID,
 * 모듈 조건에 맞는 레벨이 선택되어 해당 거래의 추가 진단 로그에 적용됩니다.</p>
 */
public enum CpfLogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF
}
