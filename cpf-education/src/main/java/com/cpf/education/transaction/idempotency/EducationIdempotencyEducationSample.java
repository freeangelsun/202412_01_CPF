package com.cpf.education.transaction.idempotency;
import com.cpf.messaging.reliability.api.CpfIdempotencyCommand;
import com.cpf.messaging.reliability.api.CpfIdempotencyEngine;
import com.cpf.messaging.reliability.api.CpfIdempotencyExecutionResult;
import com.cpf.messaging.reliability.api.InMemoryCpfIdempotencyRepository;

import java.time.Duration;

/**
 * 온라인 요청 중복 처리를 보여주는 멱등 샘플입니다.
 */
public class EducationIdempotencyEducationSample {
    private final CpfIdempotencyEngine engine;

    /**
     * 외부 DB 없이 학습할 때 사용하는 생성자입니다. 운영 코드에서는 JDBC port를 주입합니다.
     */
    public EducationIdempotencyEducationSample() {
        this(new CpfIdempotencyEngine(new InMemoryCpfIdempotencyRepository()));
    }

    public EducationIdempotencyEducationSample(CpfIdempotencyEngine engine) {
        this.engine = engine;
    }

    /** handle 작업을 CPF 표준 계약에 따라 수행한다. */
    public String handle(String idempotencyKey) {
        String payload = "EDU Education-PAYLOAD";
        CpfIdempotencyExecutionResult result = engine.execute(
                new CpfIdempotencyCommand(
                        "EDU_HTTP_EDU",
                        idempotencyKey,
                        CpfIdempotencyEngine.sha256("POST:/api/education/idempotency"),
                        CpfIdempotencyEngine.sha256(payload),
                        "EDU Education-TRANSACTION",
                        "EDU Education-SEGMENT",
                        Duration.ofMinutes(5)),
                () -> "PROCESSED");
        return result.replayed() ? "REPLAYED" : result.response();
    }
}
