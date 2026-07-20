package cpf.xyz.idempotency;

import cpf.pfw.common.idempotency.CpfIdempotencyCommand;
import cpf.pfw.common.idempotency.CpfIdempotencyEngine;
import cpf.pfw.common.idempotency.CpfIdempotencyExecutionResult;
import cpf.pfw.common.idempotency.InMemoryCpfIdempotencyRepository;

import java.time.Duration;

/**
 * 온라인 요청 중복 처리를 보여주는 멱등 샘플입니다.
 */
public class XyzIdempotencyEducationSample {
    private final CpfIdempotencyEngine engine;

    /**
     * 외부 DB 없이 학습할 때 사용하는 생성자입니다. 운영 코드에서는 JDBC port를 주입합니다.
     */
    public XyzIdempotencyEducationSample() {
        this(new CpfIdempotencyEngine(new InMemoryCpfIdempotencyRepository()));
    }

    public XyzIdempotencyEducationSample(CpfIdempotencyEngine engine) {
        this.engine = engine;
    }

    public String handle(String idempotencyKey) {
        String payload = "XYZ Reference-PAYLOAD";
        CpfIdempotencyExecutionResult result = engine.execute(
                new CpfIdempotencyCommand(
                        "XYZ_HTTP_EDU",
                        idempotencyKey,
                        CpfIdempotencyEngine.sha256("POST:/api/xyz/reference/idempotency"),
                        CpfIdempotencyEngine.sha256(payload),
                        "XYZ Reference-TRANSACTION",
                        "XYZ Reference-SEGMENT",
                        Duration.ofMinutes(5)),
                () -> "PROCESSED");
        return result.replayed() ? "REPLAYED" : result.response();
    }
}
