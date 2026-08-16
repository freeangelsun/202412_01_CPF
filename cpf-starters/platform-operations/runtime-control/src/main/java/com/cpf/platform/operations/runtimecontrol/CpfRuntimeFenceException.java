package com.cpf.platform.operations.runtimecontrol;

/**
 * 오래된 leader 또는 agent의 Runtime 변경 적용이 fencing token에 의해 거부됐음을 나타냅니다.
 *
 * <p>Runtime Control Plane 외부 Consumer는 {@code cpf-core.common} 구현 예외가 아니라
 * 이 Public API 예외만 의존해야 합니다.</p>
 */
public class CpfRuntimeFenceException extends RuntimeException {

    public CpfRuntimeFenceException(String message) {
        super(message);
    }
}
