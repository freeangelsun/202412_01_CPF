package cpf.cmn.fle.core;

import java.util.List;

/**
 * SSH ?먭꺽 낅졊 寃곌낵?낅땲??
 *
 * @param success   ?깃났 ?щ?
 * @param executed  ?ㅼ젣 ?ㅽ뻾 ?щ?
 * @param exitCode  醫낅즺 肄붾뱶
 * @param command   ?ㅽ뻾 ?먮뒗 怨꾪쉷 낅졊
 * @param output    ?쒖? 異쒕젰
 * @param message   寃곌낵 硫붿떆吏
 */
public record CmnRemoteCommandResult(
        boolean success,
        boolean executed,
        int exitCode,
        List<String> command,
        String output,
        String message) {
}

