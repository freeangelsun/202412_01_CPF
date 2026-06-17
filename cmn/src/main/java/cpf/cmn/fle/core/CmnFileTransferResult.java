package cpf.cmn.fle.core;

import java.util.List;

/**
 * ?뚯씪 ?꾩넚 寃곌낵?낅땲??
 *
 * @param success     ?깃났 ?щ?
 * @param executed    ?ㅼ젣 ?ㅽ뻾 ?щ?
 * @param protocol    ?꾨줈?좎퐳
 * @param command     ?ㅽ뻾 ?먮뒗 怨꾪쉷 낅졊
 * @param message     寃곌낵 硫붿떆吏
 * @param localPath   濡쒖뺄 寃쎈줈
 * @param remotePath  ?먭꺽 寃쎈줈
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

