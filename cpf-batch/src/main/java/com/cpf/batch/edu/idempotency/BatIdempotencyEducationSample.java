package cpf.bat.edu.idempotency;

import cpf.pfw.common.idempotency.CpfIdempotencyCommand;
import cpf.pfw.common.idempotency.CpfIdempotencyEngine;
import cpf.pfw.common.idempotency.CpfIdempotencyExecutionResult;
import cpf.pfw.common.idempotency.InMemoryCpfIdempotencyRepository;

import java.time.Duration;

/**
 * 배치 idempotency key 생성 샘플입니다.
 */
public class BatIdempotencyEducationSample {
    private final CpfIdempotencyEngine engine;

    /**
     * 교육 실행용 생성자이며 운영 배치는 JDBC port가 연결된 엔진을 주입합니다.
     */
    public BatIdempotencyEducationSample() {
        this(new CpfIdempotencyEngine(new InMemoryCpfIdempotencyRepository()));
    }

    public BatIdempotencyEducationSample(CpfIdempotencyEngine engine) {
        this.engine = engine;
    }

    public String key(String jobName, String businessDate, String parameterHash) {
        return jobName + ":" + businessDate + ":" + parameterHash;
    }

    public CpfIdempotencyExecutionResult runOnce(
            String jobName,
            String businessDate,
            String parameterHash) {
        String idempotencyKey = key(jobName, businessDate, parameterHash);
        return engine.execute(
                new CpfIdempotencyCommand(
                        "BAT_JOB_EDU",
                        idempotencyKey,
                        CpfIdempotencyEngine.sha256(jobName),
                        CpfIdempotencyEngine.sha256(businessDate + ':' + parameterHash),
                        "BAT-EDU-TRANSACTION",
                        "BAT-EDU-SEGMENT",
                        Duration.ofHours(1)),
                () -> "JOB_COMPLETED");
    }
}
