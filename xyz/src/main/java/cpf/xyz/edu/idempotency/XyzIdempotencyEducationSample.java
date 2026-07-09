package cpf.xyz.edu.idempotency;

import java.util.HashSet;
import java.util.Set;

/**
 * 온라인 요청 중복 처리를 보여주는 멱등 샘플입니다.
 */
public class XyzIdempotencyEducationSample {
    private final Set<String> handled = new HashSet<>();

    public String handle(String idempotencyKey) {
        return handled.add(idempotencyKey) ? "PROCESSED" : "DUPLICATE";
    }
}
