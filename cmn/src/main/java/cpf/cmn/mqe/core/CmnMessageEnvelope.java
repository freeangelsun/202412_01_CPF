package cpf.cmn.mqe.core;

import java.util.Map;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
public record CmnMessageEnvelope(
        String broker,
        String destination,
        String key,
        Object payload,
        Map<String, String> headers,
        String createdAt) {
}

