package cpf.cmn.fle.core;

import java.util.List;

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
 */
public record CmnFileTransferResult(
        boolean success,
        boolean executed,
        CmnFileProtocol protocol,
        List<String> command,
        String message,
        String localPath,
        String remotePath) {
}

