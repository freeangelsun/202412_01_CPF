package cpf.cmn.fle.core;

/**
 * CMN ?뚯씪/?먭꺽 ?곌퀎 泥섎━ ?대젰?낅땲??
 *
 * @param exchangeId  ?뚯씪 ?곌퀎 ?대젰 ID
 * @param actionType  泥섎━ ?좏삎
 * @param protocol    ?곌퀎 ?꾨줈?좎퐳
 * @param direction   ?꾩넚 諛⑺뼢
 * @param executed    ?ㅼ젣 ?ㅽ뻾 ?щ?
 * @param success     ?깃났 ?щ?
 * @param host        ?먭꺽 ?몄뒪?? * @param sourcePath  ?먮낯 寃쎈줈
 * @param targetPath  ???寃쎈줈
 * @param requestUser ?붿껌?? * @param message     泥섎━ 硫붿떆吏
 * @param createdAt   ?대젰 ?앹꽦 ?쒓컖
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

