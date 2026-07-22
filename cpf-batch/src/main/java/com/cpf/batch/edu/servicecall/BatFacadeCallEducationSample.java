package com.cpf.batch.edu.servicecall;

/**
 * 배치 내부에서 facade 경계를 먼저 두고 원격 전환 가능성을 열어두는 샘플입니다.
 */
public class BatFacadeCallEducationSample {

    public FacadeCall plan(String facadeName, String operation) {
        return new FacadeCall(facadeName, operation, "controller-direct-call-forbidden");
    }

    public record FacadeCall(String facadeName, String operation, String rule) {
    }
}
