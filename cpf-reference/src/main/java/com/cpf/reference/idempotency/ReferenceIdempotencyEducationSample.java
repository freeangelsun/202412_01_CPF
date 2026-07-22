package com.cpf.reference.idempotency;

import com.cpf.core.common.idempotency.CpfIdempotencyCommand;
import com.cpf.core.common.idempotency.CpfIdempotencyEngine;
import com.cpf.core.common.idempotency.CpfIdempotencyExecutionResult;
import com.cpf.core.common.idempotency.InMemoryCpfIdempotencyRepository;

import java.time.Duration;

/**
 * 온라인 요청 중복 처리를 보여주는 멱등 샘플입니다.
 */
public class ReferenceIdempotencyEducationSample {
    private final CpfIdempotencyEngine engine;

    /**
     * 외부 DB 없이 학습할 때 사용하는 생성자입니다. 운영 코드에서는 JDBC port를 주입합니다.
     */
    public ReferenceIdempotencyEducationSample() {
        this(new CpfIdempotencyEngine(new InMemoryCpfIdempotencyRepository()));
    }

    public ReferenceIdempotencyEducationSample(CpfIdempotencyEngine engine) {
        this.engine = engine;
    }

    public String handle(String idempotencyKey) {
        String payload = "REF Reference-PAYLOAD";
        CpfIdempotencyExecutionResult result = engine.execute(
                new CpfIdempotencyCommand(
                        "REF_HTTP_EDU",
                        idempotencyKey,
                        CpfIdempotencyEngine.sha256("POST:/api/reference/idempotency"),
                        CpfIdempotencyEngine.sha256(payload),
                        "REF Reference-TRANSACTION",
                        "REF Reference-SEGMENT",
                        Duration.ofMinutes(5)),
                () -> "PROCESSED");
        return result.replayed() ? "REPLAYED" : result.response();
    }
}
