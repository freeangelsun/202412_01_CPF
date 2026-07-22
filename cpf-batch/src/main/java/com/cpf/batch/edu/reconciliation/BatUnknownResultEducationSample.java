package com.cpf.batch.edu.reconciliation;

/**
 * 외부 호출 결과가 불명확할 때 대사 대상으로 분리하는 샘플입니다.
 */
public class BatUnknownResultEducationSample {

    public UnknownDecision decide(String responseStatus) {
        boolean unknown = responseStatus == null || "TIMEOUT".equals(responseStatus);
        return new UnknownDecision(unknown, unknown ? "RECONCILIATION_REQUIRED" : "NO_ACTION");
    }

    public record UnknownDecision(boolean unknown, String nextAction) {
    }
}
