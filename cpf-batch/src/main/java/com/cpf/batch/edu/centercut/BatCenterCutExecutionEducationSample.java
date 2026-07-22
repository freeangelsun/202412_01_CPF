package com.cpf.batch.edu.centercut;

/**
 * center-cut 실행 식별자를 생성하는 샘플입니다.
 */
public class BatCenterCutExecutionEducationSample {

    public String centerCutExecutionId(String jobId, String businessDate) {
        return "CC-" + jobId + "-" + businessDate;
    }
}
