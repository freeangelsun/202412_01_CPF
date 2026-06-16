package cpf.cmn.mqe.core;

import java.util.Map;

/**
 * 釉뚮줈而?醫낅쪟? 臾닿??섍쾶 硫붿떆吏瑜?諛쒗뻾?섍린 ?꾪븳 怨듯넻 ?명꽣?섏씠?ㅼ엯?덈떎.
 */
public interface CmnMessagePublisher {

    /**
     * 湲곕낯 紐⑹쟻吏濡?硫붿떆吏瑜?諛쒗뻾?⑸땲??
     *
     * @param key     ?낅Т ??     * @param payload ?낅Т Payload
     * @return 諛쒗뻾 寃곌낵
     */
    CmnMessagePublishResult publish(String key, Object payload);

    /**
     * 吏??紐⑹쟻吏濡?硫붿떆吏瑜?諛쒗뻾?⑸땲??
     *
     * @param destination Kafka topic ?먮뒗 Rabbit routing key
     * @param key         ?낅Т ??     * @param payload     ?낅Т Payload
     * @param headers     異붽? 硫붿떆吏 ?ㅻ뜑
     * @return 諛쒗뻾 寃곌낵
     */
    CmnMessagePublishResult publish(String destination, String key, Object payload, Map<String, String> headers);
}

