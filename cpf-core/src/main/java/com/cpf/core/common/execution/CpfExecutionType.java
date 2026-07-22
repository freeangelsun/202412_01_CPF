package com.cpf.core.common.execution;

/**
 * CPF 표준 실행 유형입니다.
 *
 * <p>온라인 공개 거래, CPF 내부 공유 API, 배치·비동기 실행을 하나의 식별 규격으로
 * 연결하되 각 유형의 노출 정책은 분리합니다.</p>
 */
public enum CpfExecutionType {
    ONLINE('O'),
    SHARED('S'),
    BATCH('B');

    private final char prefix;

    CpfExecutionType(char prefix) {
        this.prefix = prefix;
    }

    public char prefix() {
        return prefix;
    }

    public static CpfExecutionType fromPrefix(char prefix) {
        return switch (prefix) {
            case 'O' -> ONLINE;
            case 'S' -> SHARED;
            case 'B' -> BATCH;
            default -> throw new IllegalArgumentException("표준 실행 ID 유형은 O, S, B 중 하나여야 합니다.");
        };
    }
}
