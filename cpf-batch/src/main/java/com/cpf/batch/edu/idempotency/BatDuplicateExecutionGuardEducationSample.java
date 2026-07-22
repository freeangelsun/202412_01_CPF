package com.cpf.batch.edu.idempotency;

import java.util.HashSet;
import java.util.Set;

/**
 * 동일 job/parameter 중복 실행을 막는 guard 샘플입니다.
 */
public class BatDuplicateExecutionGuardEducationSample {
    private final Set<String> runningKeys = new HashSet<>();

    public boolean acquire(String jobName, String businessDate) {
        return runningKeys.add(jobName + ":" + businessDate);
    }

    public void release(String jobName, String businessDate) {
        runningKeys.remove(jobName + ":" + businessDate);
    }
}
