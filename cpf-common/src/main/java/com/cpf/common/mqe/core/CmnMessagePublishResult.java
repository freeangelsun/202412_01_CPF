package cpf.cmn.mqe.core;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
public record CmnMessagePublishResult(
        boolean success,
        String broker,
        String destination,
        String key,
        String transactionId,
        String message) {
}

