package cpf.bat.worker;

import java.time.Instant;

/** worker가 독점 실행할 수 있도록 DB에서 claim한 실행 lease입니다. */
public record BatWorkerLease(
        long executionId,
        String jobId,
        String jobParameters,
        String leaseToken,
        String workerId,
        Instant leaseUntil,
        int attemptNo,
        int takeoverCount) {
}
