package com.cpf.batch.edu.centercut;

/**
 * center-cut 성공/실패 건수 요약 샘플입니다.
 */
public class BatCenterCutResultSummaryEducationSample {

    public Summary summarize(int requested, int success) {
        return new Summary(requested, success, requested - success);
    }

    public record Summary(int requested, int success, int failed) {
    }
}
