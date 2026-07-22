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
 */
public record CmnRemoteCommandResult(
        boolean success,
        boolean executed,
        int exitCode,
        List<String> command,
        String output,
        String message) {
}

