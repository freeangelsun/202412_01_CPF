package com.cpf.messaging.reliability.saga;

/** 재시도/보상에 필요한 최소 Step 결과 Snapshot입니다. */
public record CpfSagaStepResult(String resultCode, String resultSnapshot) {
    public static CpfSagaStepResult success(Object snapshot) {
        return new CpfSagaStepResult("SUCCESS", snapshot == null ? null : String.valueOf(snapshot));
    }
}
