package com.cpf.batch.api;

/**
 * BAT Owner 호출 결과가 timeout/connection loss 등으로 최종 성공 여부를 확정할 수 없을 때 사용합니다.
 *
 * <p>Consumer는 이 예외를 일반 실패로 재시도하지 말고 reconciliation/운영 재확인 대상으로 보존해야 합니다.</p>
 */
public class CpfBatchOwnerUnknownResultException extends RuntimeException {
    private final String failureCode;

    public CpfBatchOwnerUnknownResultException(String failureCode, String message) {
        super(message);
        this.failureCode = failureCode;
    }

    public String failureCode() {
        return failureCode;
    }
}
