package cpf.cmn.mqe.core;

/**
 * 硫붿떆吏 諛쒗뻾 寃곌낵?낅땲??
 *
 * @param success       ?깃났 ?щ?
 * @param broker        ?ъ슜 釉뚮줈而? * @param destination   諛쒗뻾 ⑹쟻吏
 * @param key           ?낅Т ?? * @param transactionId 硫붿떆吏???ㅻ┛ 湲濡쒕쾶 嫄곕옒ID
 * @param message       寃곌낵 硫붿떆吏
 */
public record CmnMessagePublishResult(
        boolean success,
        String broker,
        String destination,
        String key,
        String transactionId,
        String message) {
}

