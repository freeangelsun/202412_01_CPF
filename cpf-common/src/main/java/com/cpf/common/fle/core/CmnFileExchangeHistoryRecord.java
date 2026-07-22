package cpf.cmn.fle.core;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
public record CmnFileExchangeHistoryRecord(
        String exchangeId,
        String actionType,
        String protocol,
        String direction,
        boolean executed,
        boolean success,
        String host,
        String sourcePath,
        String targetPath,
        String requestUser,
        String message,
        String createdAt) {
}

